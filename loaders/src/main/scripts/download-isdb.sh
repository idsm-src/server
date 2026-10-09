#!/bin/bash

set -ueo pipefail

source datasource.properties

record=14887271
release=5.0.0
version="$release"

if [ -e "$base/isdb-$version" ]; then
    suffix=1
    while [ -e "$base/isdb-$version.$suffix" ]; do
        suffix=$((suffix + 1))
    done
    version="$version.$suffix"
fi

output="$base/isdb-$version"
mkdir "$output"

echo "$release" | gzip > "$output/version.txt.gz"

# the metadata of the record list the checksums of its files
wget -q -O "$output/record.json" "https://zenodo.org/api/records/$record"

for file in isdb_wikidata_neg_energySum.mgf isdb_wikidata_pos_energySum.mgf; do
    wget --progress=bar:force -O "$output/$file" "https://zenodo.org/api/records/$record/files/$file/content"

    test "$(md5sum "$output/$file" | cut -d' ' -f1)" = "$(sed 's/"key"/\n"key"/g' "$output/record.json" | sed -n "/^\"key\": *\"$file\"/s/.*\"checksum\": *\"md5:\([0-9a-f]*\)\".*/\1/p")"
done

test -L "$base/isdb" && rm "$base/isdb"
ln -s "isdb-$version" "$base/isdb"
