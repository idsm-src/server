#!/bin/bash

set -ueo pipefail

source datasource.properties

server=https://go.drugbank.com

tag=$(wget -q -O - "$server/releases/latest" | sed -n 's|.*/releases/\([^/"]*\)/downloads/all-open-structures.*|\1|p' | tail -n 1)

if [ -z "$tag" ]; then
    echo "the latest release of DrugBank cannot be determined" >&2
    exit 1
fi

release=$(echo "$tag" | tr '-' '.')
version="$release"

if [ -e "$base/drugbank-$version" ]; then
    suffix=1
    while [ -e "$base/drugbank-$version.$suffix" ]; do
        suffix=$((suffix + 1))
    done
    version="$version.$suffix"
fi

output="$base/drugbank-$version"
mkdir "$output"

echo "$release" | gzip > "$output/version.txt.gz"

wget --progress=bar:force -O "$output/drugbank_all_open_structures.sdf.zip" "$server/releases/$tag/downloads/all-open-structures"

test -L "$base/drugbank" && rm "$base/drugbank"
ln -s "drugbank-$version" "$base/drugbank"
