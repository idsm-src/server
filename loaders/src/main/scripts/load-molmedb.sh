#!/bin/bash

set -ueo pipefail

source datasource.properties

dump="$base/molmedb/molmedb.sql"

if [ ! -f "$dump" ]; then
    echo "    error: no input file: molmedb/molmedb.sql"
    echo "the load has failed, its data have been rolled back"
    exit 1
fi

# the version of the dump, which is recorded beside it; load.sql reports a missing file and an unknown version
if [ -f "$base/molmedb/version.txt.gz" ]; then
    version=$(zcat "$base/molmedb/version.txt.gz" | head -n 1)
    missing=""
else
    version=""
    missing="molmedb/version.txt.gz"
fi

# the other files of the directory, which load.sql reports as unknown, as their data would be left out
unknown=$(cd "$base/molmedb" && find . -type f ! -path ./molmedb.sql ! -path ./version.txt.gz | sed 's|^\./|molmedb/|' | sort)

# the adaptation of the dump, a plain pg_dump of the MolMeDB database, to a load by an ordinary user: its schema
# public becomes molmedb_tmp, the extension citext is not installed and its type as well as varchar(N) become varchar,
# the owners are left out and so are the foreign keys, as they may reference tables that the dump does not contain;
# the data of the COPY statements are left as they are
adaptation='
/^COPY .* FROM stdin;$/,/^\\\.$/{
    /^COPY /s/\bpublic\./molmedb_tmp./g
    b
}
/^CREATE EXTENSION IF NOT EXISTS citext /d
/^COMMENT ON EXTENSION citext /d
/^ALTER .* OWNER TO /d
/^ALTER TABLE ONLY [^;]*$/{N;/\n    ADD CONSTRAINT .* FOREIGN KEY /d}
s/\bpublic\.citext\b/character varying/g
s/\bcharacter varying([0-9]*)/character varying/g
s/\bpublic\./molmedb_tmp./g
'

# one transaction loads the dump into the schema molmedb_tmp, synchronizes molmedb with it and drops the schema, so a
# failure, including an error of the data that load.sql reports, leaves the database unchanged; reset all undoes the
# session settings of the dump
if ! {
    echo "begin;"
    echo "create schema molmedb_tmp;"
    sed -e "$adaptation" "$dump"
    echo "reset all;"
    cat src/main/sql/molmedb/load.sql
    echo "drop schema molmedb_tmp cascade;"
    echo "commit;"
} | psql -v ON_ERROR_STOP=on --echo-errors -v version="$version" -v missing_files="$missing" \
        -v unknown_files="$unknown" --host="$host" --port="$port" --dbname="$dbname" --username="$user"
then
    echo "the load has failed, its data have been rolled back"
    exit 1
fi
