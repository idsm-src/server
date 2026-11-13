#!/bin/bash

set -ueo pipefail

source datasource.properties

repository=iocbbioinf/federated-sparql-examples
branch=main

# the archive of the commit, not of the branch, so that the version names exactly the downloaded examples
commit=$(wget -q -O - --header="Accept: application/vnd.github.sha" "https://api.github.com/repos/$repository/commits/$branch" || true)

if [[ ! "$commit" =~ ^[0-9a-f]{40}$ ]]; then
    echo "the latest commit of the federated SPARQL examples cannot be determined" >&2
    exit 1
fi

version=$(date '+%Y-%m-%d')

if [ -e "$base/idsm-$version" ]; then
    suffix=1
    while [ -e "$base/idsm-$version.$suffix" ]; do
        suffix=$((suffix + 1))
    done
    version="$version.$suffix"
fi

output="$base/idsm-$version"
mkdir "$output"

echo "$commit" | gzip > "$output/version.txt.gz"

wget --progress=bar:force -O - "https://github.com/$repository/archive/$commit.tar.gz" | tar -xz -C "$output" --strip-components=1 "${repository#*/}-$commit/examples"

test -L "$base/idsm" && rm "$base/idsm"
ln -s "idsm-$version" "$base/idsm"
