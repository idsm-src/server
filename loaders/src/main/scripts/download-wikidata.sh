#!/bin/bash

set -ueo pipefail

source datasource.properties

date=$(date '+%Y-%m-%d')
version="$date"

if [ -e "$base/wikidata-$version" ]; then
    suffix=1
    while [ -e "$base/wikidata-$version.$suffix" ]; do
        suffix=$((suffix + 1))
    done
    version="$version.$suffix"
fi

output="$base/wikidata-$version"
mkdir "$output"

echo "$date" | gzip > "$output/version.txt.gz"

endpoint="https://query.wikidata.org/sparql"
agent="idsm-loader (https://idsm.elixir-czech.cz/)"


query()
{
    local encoded
    encoded=$(printf '%s' "$1" | sed 's/ /%20/g; s/?/%3F/g; s/</%3C/g; s/>/%3E/g; s/{/%7B/g; s/}/%7D/g; s/(/%28/g; s/)/%29/g')

    wget -q --user-agent="$agent" --header='Accept: text/tab-separated-values' --tries=5 --waitretry=60 --retry-on-http-error=429,500,502,503,504 -O "$2" "$endpoint?query=$encoded"
}


# the query service cuts a result at its time limit without an error, so the number of rows is checked
download()
{
    local file="$1"
    local pattern="?entity <http://www.wikidata.org/prop/direct/$2> ?$3"
    local attempt count rows

    for attempt in 1 2 3; do
        if query "SELECT (COUNT(*) AS ?count) WHERE { $pattern }" "$output/count.tsv" && query "SELECT ?entity ?$3 WHERE { $pattern }" "$output/$file"; then
            count=$(tail -n 1 "$output/count.tsv")
            rows=$(($(wc -l < "$output/$file") - 1))

            if [ "$(tail -c 1 "$output/$file" | wc -l)" -eq 1 ] && [ "$rows" -ge "$((count - count / 100))" ] && [ "$rows" -le "$((count + count / 100))" ]; then
                rm "$output/count.tsv"
                return
            fi

            echo "incomplete $file: $rows of $count rows" >&2
        else
            echo "cannot download $file" >&2
        fi

        if [ "$attempt" -lt 3 ]; then
            sleep 60
        fi
    done

    rm -f "$output/count.tsv"
    exit 1
}


download isomeric_smiles.tsv P2017 smiles
download canonical_smiles.tsv P233 smiles
download inchis.tsv P234 inchi

test -L "$base/wikidata" && rm "$base/wikidata"
ln -s "wikidata-$version" "$base/wikidata"
