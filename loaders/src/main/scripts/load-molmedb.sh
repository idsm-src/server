#!/bin/bash

set -ueo pipefail

source datasource.properties

dump="$base/molmedb/molmedb.sql"

if [ ! -f "$dump" ]; then
    echo "the dump $dump does not exist" >&2
    exit 1
fi

# one transaction loads the dump into the schema molmedb_tmp, synchronizes molmedb with it and drops the schema, so a
# failure leaves the database unchanged; the foreign keys of the dump are left out, as they may reference tables that
# the dump does not contain, and reset all undoes the session settings of the dump
{
    echo "begin;"
    echo "create schema molmedb_tmp;"
    sed '/^ALTER TABLE ONLY /{N;/\n    ADD CONSTRAINT .* FOREIGN KEY /d}' "$dump"
    echo "reset all;"
    cat src/main/sql/molmedb/load.sql
    echo "drop schema molmedb_tmp cascade;"
    echo "commit;"
} | psql -v ON_ERROR_STOP=on --echo-errors --host="$host" --port="$port" --dbname="$dbname" --username="$user"
