#!/bin/bash

set -ueo pipefail

source datasource.properties

for I in "$@"; do
    if [ -d "$I" ]; then
        cat "$I"/*.sql
    else
        cat "$I"
    fi
done | psql -v ON_ERROR_STOP=on --echo-errors --host="$host" --port="$port" --dbname="$dbname" --username="$user"
