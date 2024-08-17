#!/bin/bash

set -ueo pipefail

source datasource.properties

tmp=$(mktemp -d)

wget --progress=bar:force -P "$tmp" https://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi.owl

version=$(sed -r -n '/owl:versionIRI/s|.*/([^/]+)/chebi\.owl.*|\1|p' "$tmp/chebi.owl")

if [ -e "$base/chebi-$version" ]; then
    suffix=1
    while [ -e "$base/chebi-$version.$suffix" ]; do
        suffix=$((suffix + 1))
    done
    version="$version.$suffix"
fi

output="$base/chebi-$version"
mkdir "$output"

mv "$tmp/chebi.owl" "$output"

test -L "$base/chebi" && rm "$base/chebi"
ln -s "chebi-$version" "$base/chebi"

rmdir "$tmp"
