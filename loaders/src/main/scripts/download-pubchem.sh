#!/bin/bash

err()
{
    echo "$@"
    exit 1
}

set -ue -o pipefail

if [[ $BASH_SOURCE = */* ]]; then
  base="${BASH_SOURCE%/*}/.."
else
  err "cannot detect source path"
fi

if [[ $# -gt 1 ]]; then
  err "two many arguments"
fi

output=${1:-data/pubchem}

if [[ -e "$output" ]]; then
    err "output directory '$output' already exist"
fi

mkdir -p "$output"


wget -P "$output" -r -l 3 -nH --cut-dirs=1 ftp://ftp.ncbi.nlm.nih.gov/pubchem/RDF
wget -P "$output" -r -nH --cut-dirs=1 ftp://ftp.ncbi.nlm.nih.gov/pubchem/RDF/compound/general
wget -P "$output" -r -nH --cut-dirs=1 ftp://ftp.ncbi.nlm.nih.gov/pubchem/RDF/descriptor
wget -P "$output" -r -nH --cut-dirs=1 ftp://ftp.ncbi.nlm.nih.gov/pubchem/Bioassay/XML
wget -P "$output" -r -nH --cut-dirs=1 ftp://ftp.ncbi.nlm.nih.gov/pubchem/Compound/Extras/CID-Title.gz
