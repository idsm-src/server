#!/bin/bash

set -ueo pipefail

source datasource.properties

line=$(wget -q -O - https://mona.fiehnlab.ucdavis.edu/rest/downloads/predefined | sed 's|\("[^"]*Export"\)|\n\1|g'  | grep "MoNA-export-All_Spectra-json.zip")
version=$(date -d @$(($(echo "$line" | sed 's|.*"date":\([0-9]*\),.*|\1|')/1000)) +"%Y-%m-%d")
id=$(echo "$line" | sed 's|.*"id":"\([^"]*\)".*|\1|')

if [ -e "$base/mona-$version" ]; then
    suffix=1
    while [ -e "$base/mona-$version.$suffix" ]; do
        suffix=$((suffix + 1))
    done
    version="$version.$suffix"
fi

output="$base/mona-$version"
mkdir "$output"

wget --progress=bar:force -P "$output" http://classyfire.wishartlab.com/system/downloads/1_0/chemont/ChemOnt_2_1.obo.zip
wget --progress=bar:force -O "$output/MoNA-export-All_Spectra-json.zip" "https://mona.fiehnlab.ucdavis.edu/rest/downloads/retrieve/$id"

test -L "$base/mona" && rm "$base/mona"
ln -s "mona-$version" "$base/mona"
