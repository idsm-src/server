#!/bin/bash

set -ueo pipefail

source datasource.properties

tmp=$(mktemp -d)

wget --progress=bar:force -P "$tmp" https://nlmpubs.nlm.nih.gov/projects/mesh/rdf/mesh.nt.gz

version=$(zcat "$tmp/mesh.nt.gz" | sed -r -n '/^\# <http:\/\/id\.nlm\.nih\.gov\/mesh> exported at/s/.* exported at ([^ ]+).*/\1/p')

if [ -e "$base/mesh-$version" ]; then
    suffix=1
    while [ -e "$base/mesh-$version.$suffix" ]; do
        suffix=$((suffix + 1))
    done
    version="$version.$suffix"
fi

output="$base/mesh-$version"
mkdir "$output"

mv "$tmp/mesh.nt.gz" "$output"

test -L "$base/mesh" && rm "$base/mesh"
ln -s "mesh-$version" "$base/mesh"

rmdir "$tmp"
