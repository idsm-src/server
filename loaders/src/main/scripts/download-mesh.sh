#!/bin/bash

set -ueo pipefail

source datasource.properties

server=https://nlmpubs.nlm.nih.gov/projects/mesh/rdf

# the dump of the current production year, the latest year directory, whose versioned data mirror the current MeSH;
# the root mesh.nt.gz is no longer updated
year=$(wget -q -O - "$server/" | sed -n 's|.*href="\(20[0-9][0-9]\)/".*|\1|p' | sort -n | tail -n 1)

if [ -z "$year" ]; then
    echo "the current production year of MeSH cannot be determined" >&2
    exit 1
fi

tmp=$(mktemp -d)

wget --progress=bar:force -P "$tmp" "$server/$year/mesh$year.nt.gz"

# the date (in UTC, as the server states it) of the last update of the dump, which wget gives to the downloaded file
date=$(date -u -r "$tmp/mesh$year.nt.gz" +"%Y-%m-%d")
version="$date"

if [ -e "$base/mesh-$version" ]; then
    suffix=1
    while [ -e "$base/mesh-$version.$suffix" ]; do
        suffix=$((suffix + 1))
    done
    version="$version.$suffix"
fi

output="$base/mesh-$version"
mkdir "$output"

mv "$tmp/mesh$year.nt.gz" "$output/mesh.nt.gz"
echo "$date" | gzip > "$output/version.txt.gz"

test -L "$base/mesh" && rm "$base/mesh"
ln -s "mesh-$version" "$base/mesh"

rmdir "$tmp"
