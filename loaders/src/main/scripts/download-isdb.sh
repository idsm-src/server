#!/bin/bash

set -ueo pipefail

source datasource.properties

version=4.1

if [ -e "$base/isdb-$version" ]; then
    suffix=1
    while [ -e "$base/isdb-$version.$suffix" ]; do
        suffix=$((suffix + 1))
    done
    version="$version.$suffix"
fi

output="$base/isdb-$version"
mkdir "$output"

wget --progress=bar:force -P "$output" https://zenodo.org/records/8287341/files/isdb_neg.mgf
wget --progress=bar:force -P "$output" https://zenodo.org/records/8287341/files/isdb_pos.mgf

test -L "$base/isdb" && rm "$base/isdb"
ln -s "isdb-$version" "$base/isdb"
