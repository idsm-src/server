#!/bin/bash

set -ueo pipefail

source datasource.properties

version=$(date '+%Y-%m-%d')

if [ -e "$base/ontology-$version" ]; then
    suffix=1
    while [ -e "$base/ontology-$version.$suffix" ]; do
        suffix=$((suffix + 1))
    done
    version="$version.$suffix"
fi

output="$base/ontology-$version"
mkdir "$output"

data="${data:-data/ontology}"

sources=(

# PubChem Ontology
https://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary.owl

# ChEMBL Ontology
ftp://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBL-RDF/latest/cco.ttl.gz

# ChEBI Ontology
ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi.owl

# MeSH Ontology
ftp://ftp.nlm.nih.gov/online/mesh/rdf/vocabulary_1.0.0.ttl

# BioAssay Ontology (BAO)
http://www.bioassayontology.org/bao/bao_complete.owl

# Protein Ontology (PRO)
http://purl.obolibrary.org/obo/pr.owl

# Gene Ontology (GO)
http://purl.obolibrary.org/obo/go.owl

# Sequence Ontology (SO)
http://purl.obolibrary.org/obo/so.owl

# Cell Line Ontology (CLO)
http://purl.obolibrary.org/obo/clo.owl

# Cell Ontology (CL)
http://purl.obolibrary.org/obo/cl.owl

# The BRENDA Tissue Ontology (BTO)
http://purl.obolibrary.org/obo/bto.owl

# Human Disease Ontology (DO)
http://purl.obolibrary.org/obo/doid.owl

# Mondo Disease Ontology (MONDO)
http://purl.obolibrary.org/obo/mondo.owl

# Symptom Ontology (SYMP)
http://purl.obolibrary.org/obo/symp.owl

# Pathogen Transmission Ontology (TRANS)
http://purl.obolibrary.org/obo/trans.owl

# The Human Phenotype Ontology (HP)
http://purl.obolibrary.org/obo/hp.owl

# Phenotype And Trait Ontology (PATO)
http://purl.obolibrary.org/obo/pato.owl

# Units of Measurement Ontology (UO)
http://purl.obolibrary.org/obo/uo.owl

# Ontology for Biomedical Investigations (OBI)
http://purl.obolibrary.org/obo/obi.owl

# Information Artifact Ontology (IAO)
http://purl.obolibrary.org/obo/iao.owl

# Uber-anatomy Ontology (UBERON)
http://purl.obolibrary.org/obo/uberon.owl

# NCBI Taxonomy Database
http://purl.obolibrary.org/obo/ncbitaxon.owl

# National Center Institute Thesaurus (OBO Edition)
http://purl.obolibrary.org/obo/ncit.owl

# OBO Relations Ontology
http://purl.obolibrary.org/obo/ro.owl

# OBO in OWL Meta-Ontology
http://www.geneontology.org/formats/oboInOwl.owl

# Basic Formal Ontology (BFO)
http://purl.obolibrary.org/obo/bfo.owl

# Food Ontology (FOODON)
http://purl.obolibrary.org/obo/foodon.owl

# Evidence & Conclusion Ontology (ECO)
http://purl.obolibrary.org/obo/eco.owl

# Disease Drivers Ontology (DISDRIV)
http://purl.obolibrary.org/obo/disdriv.owl

# Genotype Ontology (GENO)
http://purl.obolibrary.org/obo/geno.owl

# Common Anatomy Reference Ontology (CARO)
http://purl.obolibrary.org/obo/caro.owl

# Environment Ontology (ENVO)
http://purl.obolibrary.org/obo/envo.owl

# Ontology for General Medical Science (OGMS)
http://purl.obolibrary.org/obo/ogms.owl

# Unified phenotype ontology (uPheno)
http://purl.obolibrary.org/obo/upheno/v2/upheno.owl

# OBO Metadata Ontology
http://purl.obolibrary.org/obo/omo.owl

# Biological Pathway Exchange (BioPAX)
http://www.biopax.org/release/biopax-level3.owl

# UniProt RDF schema ontology
https://ftp.uniprot.org/pub/databases/uniprot/current_release/rdf/core.owl

# PDBx ontology
http://rdf.wwpdb.org/schema/pdbx-v50.owl

# Quantities, Units, Dimensions and Types Ontology (QUDT)
http://qudt.org/2.1/schema/qudt.ttl
http://qudt.org/2.1/schema/datatype.ttl
http://qudt.org/2.1/vocab/constant.ttl
http://qudt.org/2.1/vocab/prefix.ttl
http://qudt.org/2.1/vocab/dimensionvector.ttl
http://qudt.org/2.1/vocab/quantitykind.ttl
http://qudt.org/2.1/vocab/soqk.ttl
http://qudt.org/2.1/vocab/sou.ttl
http://qudt.org/2.1/vocab/unit.ttl

# Open PHACTS Units extending QUDT
https://github.com/openphacts/jqudt/raw/master/src/main/resources/onto/ops.ttl

# Shapes Constraint Language (SHACL)
http://www.w3.org/ns/shacl.ttl

# Linked Models: Datatype Ontology (DTYPE)
http://www.linkedmodel.org/schema/dtype.ttl

# Linked Models: Vocabulary for Attaching Essential Metadata (VAEM)
http://www.linkedmodel.org/schema/vaem.ttl

# Information Ontology (CHEMINF)
http://semanticscience.org/ontology/cheminf.owl

# Semanticscience integrated ontology (SIO)
http://semanticscience.org/ontology/sio.owl

# Ontology of Bioscientific Data Analysis and Data Management (EDAM)
https://edamontology.org/EDAM.owl

# National Drug File-Reference Terminology (NDF-RT)
https://data.bioontology.org/ontologies/NDF-RT/submissions/1/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb

# National Center Institute Thesaurus (NCIt)
https://evs.nci.nih.gov/ftp1/NCI_Thesaurus/Thesaurus.OWL.zip

# Experimental Factor Ontology (EFO)
http://www.ebi.ac.uk/efo/efo.owl

# Funding, Research Administration and Projects Ontology (FRAPO)
https://sparontologies.github.io/frapo/current/frapo.owl

# Patent Ontology (EPO)
https://data.epo.org/linked-data/api/datasets/download?path=vocabularies\&fileName=patent.ttl
https://data.epo.org/linked-data/api/datasets/download?path=vocabularies\&fileName=cpcOnt.ttl
https://data.epo.org/linked-data/api/datasets/download?path=vocabularies\&fileName=ipcOnt.ttl
https://data.epo.org/linked-data/api/datasets/download?path=vocabularies\&fileName=st3Ont.ttl
https://data.epo.org/linked-data/api/datasets/download?path=vocabularies\&fileName=st3Ref.ttl

# W3C PROVenance Interchange
http://www.w3.org/ns/prov.ttl
http://www.w3.org/ns/prov-aq.ttl
http://www.w3.org/ns/prov-dc.ttl
http://www.w3.org/ns/prov-dictionary.ttl
http://www.w3.org/ns/prov-links.ttl
http://www.w3.org/ns/prov-o.ttl
http://www.w3.org/ns/prov-o-inverses.ttl

# Metadata Authority Description Schema in RDF (MADS/RDF)
http://www.loc.gov/standards/mads/rdf/v1.rdf

# Citation Typing Ontology (CiTO)
https://sparontologies.github.io/cito/current/cito.owl

# Ontology for vCard
http://www.w3.org/2006/vcard/ns.ttl

#Feature Annotation Location Description Ontology (FALDO)
http://biohackathon.org/resource/faldo.ttl

# FRBR-aligned Bibliographic Ontology (FaBiO)
http://purl.org/spar/fabio.ttl

# Essential FRBR in OWL2 DL Ontology (FRBR)
http://purl.org/spar/frbr.ttl

# Dublin Core Metadata Initiative Terms (DCMI)
https://www.dublincore.org/specifications/dublin-core/dcmi-terms/dublin_core_terms.rdf
https://www.dublincore.org/specifications/dublin-core/dcmi-terms/dublin_core_type.rdf
https://www.dublincore.org/specifications/dublin-core/dcmi-terms/dublin_core_abstract_model.rdf
https://www.dublincore.org/specifications/dublin-core/dcmi-terms/dublin_core_elements.rdf

# Bibliographic Ontology (BIBO)
https://www.dublincore.org/specifications/bibo/bibo/bibo.rdf.xml

# Simple Knowledge Organization System (SKOS)
http://www.w3.org/TR/skos-reference/skos.rdf

# Description of a Project Vocabulary (DOAP)
https://github.com/ewilderj/doap/raw/master/schema/doap.rdf

# FOAF Vocabulary
http://xmlns.com/foaf/0.1/index.rdf

# Provenance, Authoring and Versioning (PAV)
http://pav-ontology.github.io/pav/pav.rdf

# SemWeb Vocab Status Ontology
http://www.w3.org/2003/06/sw-vocab-status/ns.rdf

# Vocabulary of Interlinked Datasets (VoID)
http://vocab.deri.ie/void.ttl

# Situation Ontology
http://www.ontologydesignpatterns.org/cp/owl/situation.owl

# Mass Spectrometry Ontology (MS)
https://purl.obolibrary.org/obo/ms.owl

# OWL 2 Schema (OWL 2)
http://www.w3.org/2002/07/owl.ttl

# RDF Schema (RDFS)
http://www.w3.org/2000/01/rdf-schema.ttl

# RDF Vocabulary Terms
http://www.w3.org/1999/02/22-rdf-syntax-ns.ttl
)


declare -A rewrite=(
[http://www.bioassayontology.org/bao]=http://www.bioassayontology.org/bao/bao_complete.owl
[http://xmlns.com/foaf/0.1/]=https://w3id.org/spar/foaf.ttl
[http://qudt.org/2.1/schema/facade/qudt]=http://qudt.org/2.1/schema/qudt.ttl
[http://www.w3.org/2004/02/skos/core]=http://www.w3.org/TR/skos-reference/skos.rdf
[http://bioassayontology.org/bao/external/BAO_CHEBI_import.owl]=skip
[http://www.bioassayontology.org/bao/external/BAO_NCBITaxon_import.owl]=skip
[http://www.bioassayontology.org/bao/external/BAO_NCIT_import.owl]=skip
[http://www.bioassayontology.org/bao/external/BAO_CLO_import.owl]=skip
[http://www.bioassayontology.org/bao/external/BAO_UO_import.owl]=skip
[http://www.bioassayontology.org/bao/external/BAO_PATO_import.owl]=skip
[http://www.bioassayontology.org/bao/external/BAO_EFO_import.owl]=skip
[http://www.bioassayontology.org/bao/external/BAO_DOID_import.owl]=skip
[http://www.bioassayontology.org/bao/external/BAO_UBERON_import.owl]=skip
[http://www.bioassayontology.org/bao/external/BAO_GO_import.owl]=skip
[http://www.bioassayontology.org/bao/external/BAO_ERO_import.owl]=skip
[http://purl.obolibrary.org/obo/doid/imports/chebi_import.owl]=skip
[http://purl.obolibrary.org/obo/doid/imports/cl_import.owl]=skip
[http://purl.obolibrary.org/obo/doid/imports/disdriv_import.owl]=skip
[http://purl.obolibrary.org/obo/doid/imports/eco_import.owl]=skip
[http://purl.obolibrary.org/obo/doid/imports/foodon_import.owl]=skip
[http://purl.obolibrary.org/obo/doid/imports/geno_import.owl]=skip
[http://purl.obolibrary.org/obo/doid/imports/hp_import.owl]=skip
[http://purl.obolibrary.org/obo/doid/imports/ncbitaxon_import.owl]=skip
[http://purl.obolibrary.org/obo/doid/imports/ro_import.owl]=skip
[http://purl.obolibrary.org/obo/doid/imports/so_import.owl]=skip
[http://purl.obolibrary.org/obo/doid/imports/symp_import.owl]=skip
[http://purl.obolibrary.org/obo/doid/imports/trans_import.owl]=skip
[http://purl.obolibrary.org/obo/doid/imports/uberon_import.owl]=skip
[http://purl.org/dc/terms/]=skip
[http://purl.org/dc/elements/1.1/]=skip
[http://protege.stanford.edu/plugins/owl/protege]=skip
)


get_imports()
{
    if [[ "$1" == *.ttl ]]; then
        format=turtle
    else
        format=rdfxml
    fi

    rapper --ignore-errors --quiet --input $format --output ntriples "$1" | (grep -F ' <http://www.w3.org/2002/07/owl#imports> ' || true) | cut -d' ' -f 3 | sed 's/^.\(.*\).$/\1/'
}


get_name()
{
    local iri="$1"
    local filename="${iri##*/}"

    if [[ "$iri" = "https://data.bioontology.org/ontologies/NDF-RT/submissions/1/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb" ]]; then
        filename=NDF-RT.owl
    elif [[ "$filename" =~ \.(gz|zip)$ ]]; then
        filename="${filename%.*}"
    elif [[ "$filename" =~ fileName= ]]; then
        filename="${filename#*fileName=}"
    fi

    if [[ -e "$output/$filename" ]]; then
        local name=${filename%.*}
        local ext=${filename##*.}
        local num=1

        while [[ -e "$output/$name-$num.$ext" ]]; do
            num=$((num + 1))
        done

        filename="$name-$num.$ext"
    fi

    echo "$filename"
}


download()
{
    local iri="$1"
    local skip="$2"

    if [[ -n "${rewrite["$iri"]-}" ]]; then
        if [[ "${rewrite["$iri"]}" = skip ]]; then
            echo "$skip[$iri -> /dev/null]"
            return
        else
            iri="${rewrite["$iri"]}"
        fi
    fi

    if [[ "$iri" =~ /$ ]]; then
        iri="${iri%/}"
    fi

    if [[ "$iri" =~ \#$ ]]; then
        iri="${iri%#}"
    fi

    if [[ "$iri" =~ /[^/.?]+$ ]]; then
        iri="$iri.ttl"
    fi

    if [[ -n "${downloaded["$iri"]-}" ]]; then
        echo "$skip[$iri -> ${downloaded["$iri"]}]"
        return
    fi

    filename="$(get_name "$iri")"

    echo "$skip$iri -> $filename"

    case "$iri" in
        http://rdf.wwpdb.org/schema/pdbx-v50.owl)
            wget --no-check-certificate -q -O "$output/$filename" "$iri"
            ;;

        ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi.owl)
            wget -q -O - "$iri" | sed -n -e '1,/\/\/ Classes/'p -e '$a-->\n</rdf:RDF>' > "$output/$filename"
            ;;

        *.gz | *.zip)
            wget -q -O - "$iri" | zcat > "$output/$filename"
            ;;

        *)
            wget -q -O "$output/$filename" "$iri"
            ;;
    esac

    case "$iri" in
        http://www.loc.gov/standards/mads/rdf/v1.rdf)
            sed -i 's|<rdf:Description rdf:resource=|<rdf:Description rdf:about=|g' "$output/$filename"
            ;;

        http://purl.obolibrary.org/obo/caro.owl)
            sed -i 's|\([^h]\)ttp://www.geneontology.org|\1http://www.geneontology.org|g' "$output/$filename"
            ;;

        http://rdf.wwpdb.org/schema/pdbx-v50.owl)
            sed -i 's|rdf:resource="dcterms:|rdf:resource="http://purl.org/dc/terms/|g' "$output/$filename"
            ;;

        https://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary.owl)
            sed -i 's|xml:lang="FDA:[^"]*"|xml:lang="en"|' "$output/$filename"
            ;;

        https://edamontology.org/EDAM.owl)
            sed -i 's|rdf:resource="https:doi.org|rdf:resource="https://doi.org|' "$output/$filename"
            ;;
    esac

    downloaded["$iri"]="$filename"

    local imports
    imports=$(get_imports "$output/$filename")

    for i in $imports; do
        download "$i" "$skip  "
    done
}


# PDBx ontology 4.0
cp "$data/pdbx-v40.owl" "$output"

# Eagle-i Resource Ontology (ERO)
cp "$data/ero.owl" "$output"

# other ontologies
cp "$data/base.owl" "$output"
cp "$data/pubchem-missing.ttl" "$output"
cp "$data/xsd.owl" "$output"
cp "$data/ChemOnt_2_1.owl" "$output"


declare -A downloaded

for iri in ${sources[@]}; do
    download "$iri" ""
done


md5sum -c --quiet <<EOF
899afa83d66cafa8f1445c70de76eb53  $output/${downloaded[http://www.biopax.org/release/biopax-level3.owl]}
790ffdba2fe25bc3d596ad467c2dc938  $output/${downloaded[https://www.dublincore.org/specifications/dublin-core/dcmi-terms/dublin_core_abstract_model.rdf]}
eecf96b8bf61fa82d6825b948f7dedad  $output/${downloaded[https://www.dublincore.org/specifications/dublin-core/dcmi-terms/dublin_core_elements.rdf]}
60fe93118b817beeb2933d31fa439f43  $output/${downloaded[https://www.dublincore.org/specifications/dublin-core/dcmi-terms/dublin_core_terms.rdf]}
6ff992991c5ea9330532903ab2e37ea2  $output/${downloaded[https://www.dublincore.org/specifications/dublin-core/dcmi-terms/dublin_core_type.rdf]}
2d0905d3eab903326384b758dc39eb37  $output/${downloaded[http://purl.obolibrary.org/obo/disdriv.owl]}
680d65b279cb55d86f292ab6ecd31124  $output/${downloaded[https://github.com/ewilderj/doap/raw/master/schema/doap.rdf]}
f6575d8f9f50e5355506e1d87e9d11ef  $output/${downloaded[http://biohackathon.org/resource/faldo.ttl]}
3a5d4778240b986a35566dd0f0619ce8  $output/${downloaded[http://xmlns.com/foaf/0.1/index.rdf]}
549f93606e3c1ffb60164305065841d1  $output/${downloaded[https://github.com/openphacts/jqudt/raw/master/src/main/resources/onto/ops.ttl]}
a6340a448fc6b84f33badab46913ffc6  $output/${downloaded[http://www.w3.org/2002/07/owl.ttl]}
becfa1348a8949ba77585878dd9b66a5  $output/${downloaded[http://www.w3.org/2000/01/rdf-schema.ttl]}
60661c6fd863924eecce61e01db11da0  $output/${downloaded[http://www.w3.org/1999/02/22-rdf-syntax-ns.ttl]}
64e6512b0c23f91aeb3fc85d8b3d3744  $output/${downloaded[http://www.w3.org/ns/shacl.ttl]}
78d5f2106a7814fb7e2626134c40ce3c  $output/${downloaded[http://www.ontologydesignpatterns.org/schemas/cpannotationschema.owl]}
5422c841e9cecbabe99c016c4004238b  $output/${downloaded[http://www.ontologydesignpatterns.org/cp/owl/situation.owl]}
ea21388ea72fb98aaecec48b8f8a5765  $output/${downloaded[http://www.w3.org/TR/skos-reference/skos.rdf]}
4eda1eb9c5e33f0e31e5130b03112e6e  $output/${downloaded[http://www.w3.org/2003/06/sw-vocab-status/ns.rdf]}
6e54cb65a79d14956bc1e5a768d797ca  $output/${downloaded[http://purl.obolibrary.org/obo/upheno/v2/upheno.owl]}
3990536d17244347bc22256c4d524dc4  $output/${downloaded[http://www.w3.org/2006/vcard/ns.ttl]}
57d88c7d6a44220fd95189d0f995e51d  $output/${downloaded[http://vocab.deri.ie/void.ttl]}
8ab3a314969cd6ab4094a9bb4213ebdb  $output/${downloaded[https://data.bioontology.org/ontologies/NDF-RT/submissions/1/download?apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb]}
EOF


echo
echo "download completed"

test -L "$base/ontology" && rm "$base/ontology"
ln -s "ontology-$version" "$base/ontology"
