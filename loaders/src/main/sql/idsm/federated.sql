--============================================================================--
-- examples from git@github.com:iocbbioinf/federated-sparql-examples.git
-- commit 1550ffaba1a10bcf89b62584e4935a95c7dc0a97 (2026-10-02)
--============================================================================--


--------------------------------------------------------------------------------
-- examples/iocb/001.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (1,
'Which molecules are hydrophobic, can cross the blood-brain barrier, and have been experimentally shown to interact with the human 5-HT2C receptor?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>
PREFIX up: <http://purl.uniprot.org/core/>

SELECT ?chemblId ?name (MIN(?nm) AS ?bestNM) (SAMPLE(?alogp) AS ?logP)
  (SAMPLE(?tpsa) AS ?TPSA) (SAMPLE(?mw) AS ?MW) (SAMPLE(?phase) AS ?maxPhase)
WHERE {
  # (1) TARGET: resolve the human 5-HT2C receptor at UniProt (not by name string).
  #     Wrapped in a subselect so IDSM materialises the remote result before joining.
  {
    SELECT ?protein
    WHERE {
      SERVICE <https://sparql.uniprot.org/sparql> {
        ?protein a up:Protein ;
          up:mnemonic "5HT2C_HUMAN" ;
          up:organism <http://purl.uniprot.org/taxonomy/9606> .
      }
    }
  }

  GRAPH <http://rdf.ebi.ac.uk/dataset/chembl> {
    # single-protein human target only (excludes 5-HT2 family/selectivity groups)
    ?target cco:hasTargetComponent/cco:targetCmptXref ?protein ;
      cco:targetType "SINGLE PROTEIN" ;
      cco:organismName "Homo sapiens" .

    # (2) EXPERIMENTALLY SHOWN TO INTERACT: exact-value binding/functional assay
    ?assay cco:hasTarget ?target ;
      cco:assayType ?atype ;
      dcterms:description ?assayLabel .
    FILTER (?atype IN ("Binding", "Functional"))

    ?act cco:hasAssay ?assay ;
      cco:hasMolecule ?mol ;
      cco:standardType ?stype ;
      cco:standardRelation "=" ;
      cco:standardUnits "nM" ;
      cco:standardValue ?nm .
    FILTER (?stype IN ("Ki", "Kd", "IC50", "EC50"))
    FILTER (?nm > 0.0 && ?nm <= 1000.0)

    # human receptor system; exclude non-human tissue preparations
    FILTER (REGEX(?assayLabel, "human|HEK293|CHO|tsA201|GripTite", "i"))
    FILTER (!REGEX(?assayLabel, "porcine|bovine|rat |mouse|murine|canine|monkey|calf", "i"))

    ?mol cco:chemblId ?chemblId ;
      rdfs:label ?name .

    # (3) HYDROPHOBIC: AlogP >= 3   |   (4) BBB descriptor profile (CNS-MPO-consistent)
    ?mol sio:SIO_000008 [ a sio:CHEMINF_000251 ; sio:SIO_000300 ?alogp ], # AlogP
      [ a sio:CHEMINF_000307 ; sio:SIO_000300 ?tpsa ], # TPSA
      [ a sio:CHEMINF_000216 ; sio:SIO_000300 ?mw ], # MW
      [ a sio:CHEMINF_000244 ; sio:SIO_000300 ?hbd ], # HB donors
      [ a sio:CHEMINF_000254 ; sio:SIO_000300 ?rtb ] . # rotatable bonds
    FILTER (?alogp >= 3.0 && ?alogp <= 5.0)
    FILTER (?tpsa <= 90.0 && ?mw <= 450.0 && ?hbd <= 3.0 && ?rtb <= 8.0)

    # (5) BBB EXPERIMENTAL EVIDENCE: reached the clinic as a CNS-active agent
    ?mol cco:highestDevelopmentPhase ?phase .
    FILTER (?phase >= 1)
  }
}
GROUP BY ?chemblId ?name
ORDER BY ?bestNM');

insert into idsm.federated_query_targets values (1, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/iocb/002.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (2,
'Which hydrophobic, blood-brain-barrier-permeant ligands of the human 5-HT2C receptor are there, together with their Wikidata concept and PubChem compound identifiers?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>
PREFIX up: <http://purl.uniprot.org/core/>
PREFIX wdt: <http://www.wikidata.org/prop/direct/>

SELECT ?chemblId ?name ?bestNM ?logP ?wdItem ?wdLabel ?pubchemCIDs
WHERE {
  {
    SELECT ?mol ?chemblId ?name (MIN(?nm) AS ?bestNM) (SAMPLE(?alogp) AS ?logP)
    WHERE {
      {
        SELECT ?protein
        WHERE {
          SERVICE <https://sparql.uniprot.org/sparql> {
            ?protein a up:Protein ;
              up:mnemonic "5HT2C_HUMAN" ;
              up:organism <http://purl.uniprot.org/taxonomy/9606> .
          }
        }
      }
      GRAPH <http://rdf.ebi.ac.uk/dataset/chembl> {
        ?target cco:hasTargetComponent/cco:targetCmptXref ?protein ;
          cco:targetType "SINGLE PROTEIN" ;
          cco:organismName "Homo sapiens" .
        ?assay cco:hasTarget ?target ;
          cco:assayType ?atype ;
          dcterms:description ?ad .
        FILTER (?atype IN ("Binding", "Functional"))
        ?act cco:hasAssay ?assay ;
          cco:hasMolecule ?mol ;
          cco:standardType ?stype ;
          cco:standardRelation "=" ;
          cco:standardUnits "nM" ;
          cco:standardValue ?nm .
        FILTER (?stype IN ("Ki", "Kd", "IC50", "EC50"))
        FILTER (?nm > 0.0 && ?nm <= 1000.0)
        FILTER (REGEX(?ad, "human|HEK293|CHO|tsA201|GripTite", "i"))
        FILTER (!REGEX(?ad, "porcine|bovine|rat |mouse|murine|canine|monkey|calf", "i"))
        ?mol cco:chemblId ?chemblId ;
          rdfs:label ?name .
        ?mol sio:SIO_000008 [ a sio:CHEMINF_000251 ; sio:SIO_000300 ?alogp ],
          [ a sio:CHEMINF_000307 ; sio:SIO_000300 ?tpsa ],
          [ a sio:CHEMINF_000216 ; sio:SIO_000300 ?mw ],
          [ a sio:CHEMINF_000244 ; sio:SIO_000300 ?hbd ],
          [ a sio:CHEMINF_000254 ; sio:SIO_000300 ?rtb ] .
        FILTER (?alogp >= 3.0 && ?alogp <= 5.0)
        FILTER (?tpsa <= 90.0 && ?mw <= 450.0 && ?hbd <= 3.0 && ?rtb <= 8.0)
        ?mol cco:highestDevelopmentPhase ?phase .
        FILTER (?phase >= 1)
      }
    }
    GROUP BY ?mol ?chemblId ?name
  }
  OPTIONAL {
    {
      SELECT ?chemblId ?wdItem ?wdLabel
        (GROUP_CONCAT(DISTINCT ?c; SEPARATOR=", ") AS ?pubchemCIDs)
      WHERE {
        SERVICE <https://query.wikidata.org/sparql> {
          ?wdItem wdt:P592 ?chemblId ;
            rdfs:label ?wdLabel .
          OPTIONAL { ?wdItem wdt:P662 ?c }
          FILTER (LANG(?wdLabel) = "en")
        }
      }
      GROUP BY ?chemblId ?wdItem ?wdLabel
    }
  }
}
ORDER BY ?bestNM');

insert into idsm.federated_query_targets values (2, 'https://query.wikidata.org/sparql');
insert into idsm.federated_query_targets values (2, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/iocb/003.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (3,
'Which human lipid-recognising GPCRs have experimentally determined structures, and what are their Wikidata concepts? (candidate set for receptors where a ligand can egress laterally into the membrane through the transmembrane helices)',
'https://sparql.uniprot.org/sparql',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX up: <http://purl.uniprot.org/core/>
PREFIX wdt: <http://www.wikidata.org/prop/direct/>

SELECT ?ac ?mnemonic ?name ?pdbid ?method ?wdItem ?wdLabel
WHERE {
  # ---- Leg 1 (UniProt): lipid-recognising human GPCRs that have structures.
  #      This is the receptor class in which membrane-lateral ligand
  #      entry/egress pathways are reported.
  ?protein a up:Protein ;
    up:organism <http://purl.uniprot.org/taxonomy/9606> ;
    up:reviewed true ;
    up:classifiedWith <http://purl.uniprot.org/keywords/297> ; # GPCR
    up:mnemonic ?mnemonic ;
    up:recommendedName/up:fullName ?name ;
    rdfs:seeAlso ?pdb .
  ?pdb up:database <http://purl.uniprot.org/database/PDB> ;
    up:method ?method .
  FILTER (REGEX(?name,
    "leukotriene|sphingosine|lysophosphatid|cannabinoid|prostagland|oxysterol|free fatty acid|platelet-activating",
    "i"))
  BIND (STRAFTER(STR(?protein), "uniprot/") AS ?ac)
  BIND (STRAFTER(STR(?pdb), "pdb/") AS ?pdbid)

  # ---- Leg 2 (Wikidata): cross-KG identity for the receptor.
  OPTIONAL {
    SERVICE <https://query.wikidata.org/sparql> {
      ?wdItem wdt:P352 ?ac ;
        rdfs:label ?wdLabel .
      FILTER (LANG(?wdLabel) = "en")
    }
  }
}
ORDER BY ?mnemonic ?pdbid');

insert into idsm.federated_query_targets values (3, 'https://query.wikidata.org/sparql');


--------------------------------------------------------------------------------
-- examples/iocb/004.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (4,
'Which experimentally determined structures exist for the GPCRs in which lateral, membrane-facing ligand entry or egress through the transmembrane helices has been reported (CysLT1R, GPR183, S1P1, CB1, LPA1), and which PubChem compounds are bound in them?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX cheminf: <http://semanticscience.org/resource/>
PREFIX pdbo: <http://rdf.wwpdb.org/schema/pdbx-v50.owl#>
PREFIX up: <http://purl.uniprot.org/core/>

SELECT ?ac ?mnemonic ?name ?pdbid ?method ?resolution ?compound
WHERE {
  # GPCR structures and their metadata from UniProt
  SERVICE <https://sparql.uniprot.org/sparql> {
    VALUES ?protein {
      <http://purl.uniprot.org/uniprot/Q9Y271> # CysLT1R  - lateral access TM4/TM5 (crystallographic)
      <http://purl.uniprot.org/uniprot/P32249> # GPR183   - lateral entry TM4/TM5 (MD + mutagenesis)
      <http://purl.uniprot.org/uniprot/P21453> # S1P1     - TM1/TM7 gate
      <http://purl.uniprot.org/uniprot/P21554> # CB1      - TM1/TM7 gate
      <http://purl.uniprot.org/uniprot/Q92633> # LPA1     - TM1/TM7 gate
    }
    ?protein up:mnemonic ?mnemonic ;
      rdfs:seeAlso ?pdb .
    OPTIONAL { ?protein up:recommendedName/up:fullName ?name }
    ?pdb up:database <http://purl.uniprot.org/database/PDB> ;
      up:method ?method .
    OPTIONAL { ?pdb up:resolution ?resolution }
    BIND (STRAFTER(STR(?protein), "uniprot/") AS ?ac)
    BIND (STRAFTER(STR(?pdb), "pdb/") AS ?pdbid)
  }

  # ligands deposited in the same PDB entries, as PubChem compounds
  OPTIONAL {
    GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/substance> {
      ?substance pdbo:link_to_pdb ?pdb ;
        cheminf:CHEMINF_000477 ?compound .
    }
  }
}
ORDER BY ?mnemonic ?pdbid ?compound');

insert into idsm.federated_query_targets values (4, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/iocb/005.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (5,
'What are the koff and kon values for agomelatine at the human 5-HT2C receptor?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>
PREFIX up: <http://purl.uniprot.org/core/>

SELECT ?molName ?chemblId ?stdType ?relation ?value ?units ?assayDesc ?doc
WHERE {
  # Target leg: human 5-HT2C resolved at UniProt, joined into ChEMBL.
  {
    SELECT ?protein
    WHERE {
      SERVICE <https://sparql.uniprot.org/sparql> {
        ?protein a up:Protein ;
          up:mnemonic "5HT2C_HUMAN" ;
          up:organism <http://purl.uniprot.org/taxonomy/9606> .
      }
    }
  }

  GRAPH <http://rdf.ebi.ac.uk/dataset/chembl> {
    ?target cco:hasTargetComponent/cco:targetCmptXref ?protein ;
      cco:targetType "SINGLE PROTEIN" .

    # Ligand leg: agomelatine, matched on preferred label (not a guessed ID).
    ?mol a cco:SmallMolecule ;
      cco:chemblId ?chemblId ;
      rdfs:label ?molName .
    FILTER (UCASE(?molName) = "AGOMELATINE")

    ?act cco:hasMolecule ?mol ;
      cco:hasAssay ?assay ;
      cco:standardType ?stdType .
    ?assay cco:hasTarget ?target .
    OPTIONAL { ?assay dcterms:description ?assayDesc }
    OPTIONAL { ?act cco:standardValue ?value }
    OPTIONAL { ?act cco:standardUnits ?units }
    OPTIONAL { ?act cco:standardRelation ?relation }
    OPTIONAL { ?act cco:hasDocument ?doc }

    # Binding-kinetics measurements. koff/kon are the requested quantities;
    # Kd/Ki/pKb are retained because koff/kon are (verified) absent here, and
    # equilibrium constants are the only kinetics-adjacent data that exist.
    FILTER (?stdType IN ("koff", "kon", "Kd", "KD", "Ki", "pKb", "IC50", "EC50", "AC50"))
  }
}
ORDER BY ?stdType ?value');

insert into idsm.federated_query_targets values (5, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/iocb/006.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (6,
'Which molecules that target human GPCRs have entered phase 1 or phase 2 clinical testing recently, and which UniProtKB protein does each target correspond to?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>
PREFIX up: <http://purl.uniprot.org/core/>

SELECT DISTINCT ?chemblId ?molName ?phase ?targetName ?gpcrFamily ?latestYear ?uniprotMnemonic
WHERE {
  # UniProt leg: confirm the ChEMBL target is a reviewed human protein and
  # return its UniProtKB mnemonic. The inner SELECT makes UniProt evaluate the
  # pattern remotely and hand back a materialised table, which the driver then
  # joins locally on ?xref. Nesting this inside OPTIONAL{{SELECT ...}} makes
  # IDSM throw UnsupportedOperationException, so it sits at top level.
  SERVICE <https://sparql.uniprot.org/sparql> {
    SELECT ?xref ?uniprotMnemonic
    WHERE {
      ?xref up:reviewed true ;
        up:mnemonic ?uniprotMnemonic ;
        up:organism <http://purl.uniprot.org/taxonomy/9606> .
    }
  }
  {
    SELECT ?mol ?chemblId ?molName ?phase ?targetName ?gpcrFamily ?xref
      (MAX(?year) AS ?latestYear)
    WHERE {
      GRAPH <http://rdf.ebi.ac.uk/dataset/chembl> {
        # --- GPCR target: ChEMBL''s five L2 GPCR families, plus any class
        #     beneath them (skos:broader* walks the hierarchy upward).
        VALUES ?gpcrClass {
          <http://rdf.ebi.ac.uk/resource/chembl/protclass/CHEMBL_PC_1020> # Family A
          <http://rdf.ebi.ac.uk/resource/chembl/protclass/CHEMBL_PC_1021> # Family B
          <http://rdf.ebi.ac.uk/resource/chembl/protclass/CHEMBL_PC_1022> # Family C
          <http://rdf.ebi.ac.uk/resource/chembl/protclass/CHEMBL_PC_619> # Frizzled
          <http://rdf.ebi.ac.uk/resource/chembl/protclass/CHEMBL_PC_620> # Taste
        }
        ?gpcrClass rdfs:label ?gpcrFamily .

        ?target cco:hasTargetComponent ?tc ;
          cco:targetType "SINGLE PROTEIN" ;
          cco:organismName "Homo sapiens" ;
          dcterms:title ?targetName .
        ?tc cco:hasProteinClassification ?pcls ;
          cco:targetCmptXref ?xref .
        FILTER (STRSTARTS(STR(?xref), "http://purl.uniprot.org/uniprot/"))
        ?pcls (skos:broader)* ?gpcrClass .

        # --- Molecule in clinical phase 1 or 2, with activity on that target
        ?assay cco:hasTarget ?target .
        ?act cco:hasAssay ?assay ;
          cco:hasMolecule ?mol ;
          cco:hasDocument ?doc .
        ?mol cco:chemblId ?chemblId ;
          rdfs:label ?molName ;
          cco:highestDevelopmentPhase ?phase .
        FILTER (?phase = 1 || ?phase = 2)

        # --- Recency. ChEMBL RDF has NO trial/approval date on molecules
        #     (verified: cco:firstApproval does not exist). The only usable
        #     time signal is the supporting document''s year, so "last 5 years"
        #     is approximated by most-recent supporting publication.
        ?doc dcterms:date ?year .
      }
    }
    GROUP BY ?mol ?chemblId ?molName ?phase ?targetName ?gpcrFamily ?xref
  }
  FILTER (?latestYear >= 2021)
}
ORDER BY DESC(?latestYear) ?molName
LIMIT 200');

insert into idsm.federated_query_targets values (6, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/iocb/007.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (7,
'Which deuterated drug analogues are currently in clinical development or testing, what non-deuterated parent compound is each a minimal variant of, and what is the corresponding Wikidata concept?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>
PREFIX wdt: <http://www.wikidata.org/prop/direct/>

SELECT DISTINCT ?chemblId ?molName ?phase ?parentName ?wdItem ?wdLabel
WHERE {
  GRAPH <http://rdf.ebi.ac.uk/dataset/chembl> {
    ?mol cco:chemblId ?chemblId ;
      rdfs:label ?molName ;
      cco:highestDevelopmentPhase ?phase .

    # In medical development / testing (any clinical phase, or approved).
    FILTER (?phase >= 1)

    # DEUTERATED: an explicit [2H] isotope label in the canonical SMILES.
    # This is a genuine STRUCTURAL test, not a name search — it catches
    # deuterated analogues whose names do not start with "deu-".
    ?mol sio:SIO_000008 [ a sio:CHEMINF_000018 ; sio:SIO_000300 ?smiles ] .
    FILTER (CONTAINS(?smiles, "[2H]"))

    # The "small change only" relationship: ChEMBL links a salt/isotopologue
    # to its parent structure. Where present, this names the compound the
    # deuterated analogue is a minimal variant OF.
    OPTIONAL {
      ?mol cco:hasParentMolecule ?parent .
      ?parent rdfs:label ?parentName .
      OPTIONAL { ?parent cco:highestDevelopmentPhase ?parentPhase }
    }
  }

  # Cross-KG identity for the deuterated drug: Wikidata concept + label,
  # bridged on the ChEMBL id (P592). Wikidata is queried in its own
  # subselect so the remote result is materialised once.
  OPTIONAL {
    {
      SELECT ?chemblId ?wdItem ?wdLabel
      WHERE {
        SERVICE <https://query.wikidata.org/sparql> {
          ?wdItem wdt:P592 ?chemblId ;
            rdfs:label ?wdLabel .
          FILTER (LANG(?wdLabel) = "en")
        }
      }
    }
  }
}
ORDER BY DESC(?phase) ?molName');

insert into idsm.federated_query_targets values (7, 'https://query.wikidata.org/sparql');


--------------------------------------------------------------------------------
-- examples/iocb/008.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (8,
'Which chemical analogues of alkaloids are currently approved or undergoing clinical testing, to which alkaloid family does each belong, and which human proteins do they act on?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX oboInOwl: <http://www.geneontology.org/formats/oboInOwl#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX chemrof: <https://w3id.org/chemrof/>
PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>
PREFIX taxon: <http://purl.uniprot.org/taxonomy/>
PREFIX up: <http://purl.uniprot.org/core/>

SELECT DISTINCT ?chemblId ?molName ?phase ?chebiId ?chebiLabel ?alkaloidSubclass ?parentName
  ?mechanism ?protein ?mnemonic ?proteinName
WHERE {
  {
    SELECT DISTINCT ?chebiCls ?chebiId ?chebiLabel ?mol ?chemblId ?molName ?phase
      ?mechanism ?protein
    WHERE {
      # --- Leg 1 (ChEBI): the alkaloid class subtree.
      #     CHEBI_22315 = alkaloid (verified via OLS; and note Wikidata''s Q70702
      #     P279* walk was REJECTED for this query - it pulls in indomethacin,
      #     ciprofloxacin, biotin and miconazole, which are not alkaloids.
      #     ChEBI''s curated hierarchy is clean.)
      GRAPH <http://rdf.ebi.ac.uk/dataset/chebi> {
        ?chebiCls rdfs:subClassOf* obo:CHEBI_22315 ;
          rdfs:label ?chebiLabel ;
          chemrof:inchi_key_string ?ik ;
          oboInOwl:id ?chebiId .
      }

      # --- Leg 2 (ChEMBL): same compound by InChIKey, in clinical development.
      #     InChIKey is a STRUCTURAL identity join - no name matching, and it
      #     survives the two datasets naming a compound differently.
      GRAPH <http://rdf.ebi.ac.uk/dataset/chembl> {
        ?mol sio:SIO_000008 [ a sio:CHEMINF_000059 ; sio:SIO_000300 ?ik ] ;
          cco:chemblId ?chemblId ;
          rdfs:label ?molName ;
          cco:highestDevelopmentPhase ?phase .
        FILTER (?phase >= 1)
      }

      # --- Leg 3 (ChEMBL): curated mechanism of action and its target proteins.
      GRAPH <http://rdf.ebi.ac.uk/dataset/chembl> {
        ?mech cco:hasMolecule ?mol ;
          cco:mechanismActionType ?mechanism ;
          cco:hasTarget/cco:hasTargetComponent/cco:targetCmptXref ?protein .
        FILTER (STRSTARTS(STR(?protein), "http://purl.uniprot.org/uniprot/"))
      }
    }
  }

  # --- Leg 4 (UniProt): keep the human targets and name them.
  SERVICE <https://sparql.uniprot.org/sparql> {
    ?protein up:organism taxon:9606 ;
      up:mnemonic ?mnemonic ;
      up:recommendedName/up:fullName ?proteinName .
  }

  # the immediate alkaloid family (indole/vinca/aporphine/... ) when present
  OPTIONAL {
    GRAPH <http://rdf.ebi.ac.uk/dataset/chebi> {
      ?chebiCls rdfs:subClassOf ?sub .
      ?sub rdfs:label ?alkaloidSubclass .
      FILTER (CONTAINS(LCASE(?alkaloidSubclass), "alkaloid"))
    }
  }

  # "analog of" - ChEMBL''s parent-structure link, when the clinical entity
  # is a salt/derivative of the parent alkaloid
  OPTIONAL {
    GRAPH <http://rdf.ebi.ac.uk/dataset/chembl> {
      ?mol cco:hasParentMolecule ?parent .
      ?parent rdfs:label ?parentName .
    }
  }
}
ORDER BY DESC(?phase) ?molName ?mnemonic');

insert into idsm.federated_query_targets values (8, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/iocb/009.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (9,
'How do methylnicotine (6-methylnicotine) and nicotine compare in binding kinetics, binding affinity, and in-vivo pharmacokinetic readouts?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>

SELECT ?compound ?chemblId ?stdType ?relation ?value ?units ?targetName ?assayType ?assayDesc
WHERE {
  GRAPH <http://rdf.ebi.ac.uk/dataset/chembl> {
    # Both compounds selected by InChIKey — a STRUCTURAL identity, because
    # 6-methylnicotine has NO pref_name in ChEMBL (it is CHEMBL5787731) and so
    # cannot be found by name. Keys from PubChem CID 89594 / CID 25768196.
    VALUES (?ik ?compound) {
      ("SNICXCGAKADSCV-JTQLQIEISA-N" "nicotine")
      ("SWNIAVIKMKSDBJ-NSHDSACASA-N" "6-methylnicotine")
    }
    ?mol sio:SIO_000008 [ a sio:CHEMINF_000059 ; sio:SIO_000300 ?ik ] ;
      cco:chemblId ?chemblId .

    ?act cco:hasMolecule ?mol ;
      cco:hasAssay ?assay ;
      cco:standardType ?stdType .
    OPTIONAL { ?act cco:standardValue ?value }
    OPTIONAL { ?act cco:standardUnits ?units }
    OPTIONAL { ?act cco:standardRelation ?relation }

    ?assay cco:assayType ?assayType .
    OPTIONAL { ?assay dcterms:description ?assayDesc }
    OPTIONAL { ?assay cco:hasTarget/dcterms:title ?targetName }

    # kinetics (koff/kon), affinity (Ki/Kd/IC50/EC50), and in-vivo /
    # ADMET readouts — the three parts of the question that ChEMBL records.
    FILTER (?stdType IN ("koff", "kon", "Kd", "Ki", "IC50", "EC50", "pKi", "pKb",
      "Vdss", "Fu", "CL", "T1/2", "Cmax", "AUC", "F"))
  }
}
ORDER BY ?compound ?stdType');


--------------------------------------------------------------------------------
-- examples/iocb_inf/010.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (10,
'What are UniProt and Rhea, and what kinds of data can I find in them?',
'https://sparql.uniprot.org/sparql',
'PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX pav: <http://purl.org/pav/>
PREFIX sd: <http://www.w3.org/ns/sparql-service-description#>
PREFIX void: <http://rdfs.org/ns/void#>

# WHAT ARE UNIPROT AND RHEA, AND WHAT IS IN THEM?
#
# Nothing here is hard-coded prose: every row is read out of the VoID
# description that each endpoint serves at its own /.well-known/void graph.
# VoID answers "what kinds of data" structurally - each named graph is a
# void:Dataset whose void:classes / void:triples / void:distinctSubjects say
# how big it is and how many kinds of thing it holds.

SELECT ?database ?release ?dataCollection ?kindsOfThing ?triples ?subjects ?license
WHERE {
  {
    # (A) UNIPROT describes itself, graph by graph. The UniProt VoID also
    #     carries a description of the Rhea graph it mirrors locally, so the
    #     inventory of "what can I find here" spans both resources.
    GRAPH <https://sparql.uniprot.org/.well-known/void> {
      ?service sd:endpoint <https://sparql.uniprot.org/sparql> ;
        dcterms:title ?database .
      OPTIONAL { ?service sd:defaultDataset/pav:version ?release }

      # Each available named graph = one data collection in the endpoint.
      ?graphDesc sd:name ?dataCollection ;
        sd:graph ?stats .
      ?stats void:classes ?kindsOfThing .
      OPTIONAL { ?stats void:triples ?triples }
      OPTIONAL { ?stats void:distinctSubjects ?subjects }
      OPTIONAL { ?dataCollection dcterms:license ?license }
    }
  }
  UNION
  {
    # (B) RHEA describes itself, over federation, in exactly the same VoID
    #     shape - so the two resources are directly comparable. Rhea serves
    #     no dcterms:title, so the name is supplied here; every number is
    #     still read from Rhea''s own VoID graph.
    SERVICE <https://sparql.rhea-db.org/sparql> {
      GRAPH <https://sparql.rhea-db.org/.well-known/void> {
        ?graphDesc sd:name ?dataCollection ;
          sd:graph ?stats .
        ?stats void:classes ?kindsOfThing .
        OPTIONAL { ?stats void:triples ?triples }
        OPTIONAL { ?stats void:distinctSubjects ?subjects }
      }
    }
    BIND ("Rhea" AS ?database)
  }
}
ORDER BY DESC(?triples) ?database ?dataCollection');

insert into idsm.federated_query_targets values (10, 'https://sparql.rhea-db.org/sparql');


--------------------------------------------------------------------------------
-- examples/iocb_inf/011.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (11,
'How can I programmatically access the following databases: PDB, UniProt, Rhea, PubChem, and ChEMBL?',
'https://sparql.uniprot.org/sparql',
'PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX sd: <http://www.w3.org/ns/sparql-service-description#>
PREFIX up: <http://purl.uniprot.org/core/>

# HOW DO I ACCESS THESE DATABASES PROGRAMMATICALLY?
#
# Two machine-readable routes, both discovered rather than assumed:
#
#   SPARQL protocol  - each endpoint publishes a sd:Service description next
#                      to its VoID, naming its own sd:endpoint URL, the query
#                      language it speaks and the federation features it
#                      supports. That is the contract a client codes against.
#   REST / resolvable - UniProt maintains a registry of the databases it
#                      cross-references (the <.../database> graph), and each
#                      entry carries an up:urlTemplate whose %u / %s
#                      placeholder is substituted with an accession. This is
#                      how PDB, ChEMBL and PubChem records are fetched.

SELECT ?database ?accessRoute ?endpointOrTemplate
  (GROUP_CONCAT(DISTINCT STR(?queryLanguage); SEPARATOR=", ") AS ?queryLanguages)
  (GROUP_CONCAT(DISTINCT STR(?federationFeature); SEPARATOR=", ") AS ?features)
  (COUNT(DISTINCT ?graphName) AS ?nNamedGraphs)
WHERE {
  {
    # (A) SPARQL access to UNIPROT, self-described in its own VoID graph.
    GRAPH <https://sparql.uniprot.org/.well-known/void> {
      ?service a sd:Service ;
        sd:endpoint ?endpointOrTemplate ;
        sd:supportedLanguage ?queryLanguage .
      OPTIONAL { ?service sd:feature ?federationFeature }
      OPTIONAL { ?service dcterms:title ?title }
    }
    BIND (COALESCE(?title, "UniProt") AS ?database)
    BIND ("SPARQL 1.1 protocol" AS ?accessRoute)
  }
  UNION
  {
    # (B) SPARQL access to RHEA, read from Rhea''s own service description
    #     over federation - the same discovery pattern, a different server.
    SERVICE <https://sparql.rhea-db.org/sparql> {
      GRAPH <https://sparql.rhea-db.org/.well-known/void> {
        ?service a sd:Service ;
          sd:endpoint ?endpointOrTemplate ;
          sd:supportedLanguage ?queryLanguage .
        OPTIONAL { ?service sd:feature ?federationFeature }
      }
    }
    BIND ("Rhea" AS ?database)
    BIND ("SPARQL 1.1 protocol (federated SERVICE)" AS ?accessRoute)
  }
  UNION
  {
    # (C) REST / resolvable-URL access to PDB, ChEMBL, PubChem and friends,
    #     taken from UniProt''s machine-readable cross-reference registry.
    #     up:urlTemplate is the programmatic access pattern for one record.
    GRAPH <http://sparql.uniprot.org/database> {
      ?db a up:Database ;
        up:abbreviation ?database ;
        up:urlTemplate ?endpointOrTemplate .
    }
    FILTER (?database IN ("PDB", "RCSB-PDB", "PDBe", "PDBsum",
      "ChEMBL", "PubChem", "DrugBank", "BindingDB", "Rhea"))
    BIND ("REST / resolvable URL - substitute the accession for %u or %s" AS ?accessRoute)
  }
  UNION
  {
    # (D) PUBCHEM and CHEMBL are not in UniProt''s URL-template registry, but
    #     both are served as local named graphs by IDSM - so their access
    #     route is IDSM''s own SPARQL endpoint. Discovered from IDSM''s VoID:
    #     the graph names below are read, not assumed.
    # NOTE: STRSTARTS over these graph names crashes IDSM
    #       (NullPointerException), so the graphs are selected with CONTAINS.
    SERVICE <https://idsm.elixir-czech.cz/sparql/endpoint/idsm> {
      SELECT ?endpointOrTemplate ?queryLanguage ?federationFeature ?graphName
      WHERE {
        GRAPH <https://idsm.elixir-czech.cz/.well-known/void> {
          ?service a sd:Service ;
            sd:endpoint ?endpointOrTemplate ;
            sd:supportedLanguage ?queryLanguage ;
            sd:availableGraphs ?graphCollection ;
            sd:feature ?federationFeature .
          ?graphCollection sd:namedGraph ?ng .
          ?ng sd:name ?graphName .
        }
        FILTER (CONTAINS(STR(?graphName), "pubchem") || CONTAINS(STR(?graphName), "chembl"))
      }
    }
    BIND (IF(CONTAINS(STR(?graphName), "pubchem"), "PubChem", "ChEMBL") AS ?database)
    BIND ("SPARQL 1.1 protocol (local named graph at IDSM)" AS ?accessRoute)
  }
}
GROUP BY ?database ?accessRoute ?endpointOrTemplate
ORDER BY ?accessRoute ?database ?endpointOrTemplate');

insert into idsm.federated_query_targets values (11, 'https://idsm.elixir-czech.cz/sparql/endpoint/idsm');
insert into idsm.federated_query_targets values (11, 'https://sparql.rhea-db.org/sparql');


--------------------------------------------------------------------------------
-- examples/iocb_inf/012.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (12,
'Where can I find documentation for their data formats? Which data formats are used by PDB, UniProt, Rhea, PubChem, and ChEMBL?',
'https://sparql.uniprot.org/sparql',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX sd: <http://www.w3.org/ns/sparql-service-description#>
PREFIX voidext: <http://ldf.fi/void-ext#>
PREFIX up: <http://purl.uniprot.org/core/>

# WHICH DATA FORMATS, AND WHERE IS THE DOCUMENTATION?
#
# Three complementary kinds of answer, all machine-readable:
#
#  (1) SERIALISATION FORMATS - sd:resultFormat lists, per endpoint, exactly
#      which media types it can return. Each value is a URI in the W3C
#      formats registry (http://www.w3.org/ns/formats/...), so the format
#      URI *is* the pointer to that format''s own specification.
#  (2) LITERAL DATATYPES - VoID-ext datatype partitions say which XSD types
#      actually occur in the data, i.e. the format of the values themselves.
#  (3) RECORD DOCUMENTATION - UniProt''s database registry gives, for each
#      cross-referenced resource (PDB, ChEMBL, ...), a homepage and the DOI
#      of the publication that defines its data model.

SELECT DISTINCT ?database ?answerKind ?formatOrDatatype ?nPartitions ?documentation
WHERE {
  {
    # (1a) UNIPROT: the formats its SPARQL endpoint serves.
    GRAPH <https://sparql.uniprot.org/.well-known/void> {
      ?service a sd:Service ;
        sd:endpoint ?ep ;
        sd:resultFormat ?formatOrDatatype .
    }
    BIND ("UniProt" AS ?database)
    BIND ("Serialisation format (sd:resultFormat)" AS ?answerKind)
    # The format URI dereferences to its own W3C specification page.
    BIND (?formatOrDatatype AS ?documentation)
  }
  UNION
  {
    # (1b) RHEA: same question, answered by Rhea''s own service description.
    SERVICE <https://sparql.rhea-db.org/sparql> {
      GRAPH <https://sparql.rhea-db.org/.well-known/void> {
        ?service a sd:Service ;
          sd:resultFormat ?formatOrDatatype .
      }
    }
    BIND ("Rhea" AS ?database)
    BIND ("Serialisation format (sd:resultFormat)" AS ?answerKind)
    BIND (?formatOrDatatype AS ?documentation)
  }
  UNION
  {
    # (1c) PUBCHEM + CHEMBL: served by IDSM, so IDSM''s service description
    #      states the formats in which their RDF can be retrieved.
    SERVICE <https://idsm.elixir-czech.cz/sparql/endpoint/idsm> {
      GRAPH <https://idsm.elixir-czech.cz/.well-known/void> {
        ?service a sd:Service ;
          sd:resultFormat ?formatOrDatatype .
      }
    }
    BIND ("PubChem + ChEMBL (via IDSM)" AS ?database)
    BIND ("Serialisation format (sd:resultFormat)" AS ?answerKind)
    BIND (?formatOrDatatype AS ?documentation)
  }
  UNION
  {
    # (2) The datatypes really used inside UniProt''s own data - the format
    #     of the values, declared by VoID-ext datatype partitions. Reported
    #     once per datatype (a subselect collapses the many partitions that
    #     each datatype appears in).
    {
      SELECT ?formatOrDatatype (COUNT(*) AS ?nPartitions)
      WHERE {
        GRAPH <https://sparql.uniprot.org/.well-known/void> {
          ?part voidext:datatypePartition/voidext:datatype ?formatOrDatatype .
        }
      }
      GROUP BY ?formatOrDatatype
    }
    BIND ("UniProt" AS ?database)
    BIND ("Literal datatype in the data (voidext:datatype)" AS ?answerKind)
    BIND (<https://www.w3.org/TR/xmlschema11-2/> AS ?documentation)
  }
  UNION
  {
    # (3) PDB, CHEMBL and the other cross-referenced resources: the registry
    #     entry carries the homepage and the DOI of the defining paper.
    GRAPH <http://sparql.uniprot.org/database> {
      ?db a up:Database ;
        up:abbreviation ?database .
      OPTIONAL { ?db rdfs:seeAlso ?homepage }
      OPTIONAL { ?db up:citation/dcterms:identifier ?doi }
    }
    FILTER (?database IN ("PDB", "RCSB-PDB", "PDBe", "PDBsum", "ChEMBL", "DrugBank", "BindingDB"))
    BIND ("Record/format documentation" AS ?answerKind)
    BIND (COALESCE(?doi, "see homepage") AS ?formatOrDatatype)
    BIND (?homepage AS ?documentation)
  }
}
ORDER BY ?answerKind ?database ?formatOrDatatype');

insert into idsm.federated_query_targets values (12, 'https://idsm.elixir-czech.cz/sparql/endpoint/idsm');
insert into idsm.federated_query_targets values (12, 'https://sparql.rhea-db.org/sparql');


--------------------------------------------------------------------------------
-- examples/iocb_inf/013.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (13,
'How is UniProt interconnected with other databases such as PDB, Rhea, PubChem, and ChEMBL? What is the central entity in these mappings, and what is the cardinality (one-to-one, one-to-many, many-to-many) of each mapping?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX void: <http://rdfs.org/ns/void#>
PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>
PREFIX up: <http://purl.uniprot.org/core/>

# HOW ARE THESE DATABASES INTERCONNECTED, AND WITH WHAT CARDINALITY?
#
# THE CENTRAL ENTITY is the UniProtKB accession (the protein). Every mapping
# below is anchored on it: a ChEMBL target component carries the accession as
# its cco:UniprotRef, and the same target component is what links onward to
# PDB structures; PubChem even mints its protein URIs from the accession.
# That makes the accession the hub through which all five resources join.
#
# WHY ANSWER THIS FROM VoID: a VoID linkset records, for ONE predicate
# joining ONE pair of classes, the number of triples and the number of
# DISTINCT subjects and objects those triples use. Those three published
# numbers determine the cardinality outright:
#
#     triples / distinctSubjects = objects per subject  (fan-out)
#     triples / distinctObjects  = subjects per object  (fan-in)
#
#     fan-out ~1, fan-in ~1  -> one-to-one
#     fan-out >1, fan-in ~1  -> one-to-many
#     fan-out ~1, fan-in >1  -> many-to-one
#     both >1                -> many-to-many
#
# No protein, molecule or structure is ever read: the whole answer comes
# from dataset-level metadata, which is why it is fast and exact.
#
# CAVEAT, and the reason part (B) reports no ratio: UniProt''s VoID declares
# void:entities per class partition as a GRAPH-WIDE figure, and publishes no
# distinctSubjects/distinctObjects for these property partitions - so its
# linkage can be shown to EXIST and be counted, but its cardinality cannot
# honestly be computed from what UniProt publishes. Only IDSM''s linksets
# carry the distinct counts the derivation needs.

SELECT DISTINCT ?linkFrom ?linkPredicate ?linkTo ?cardinality
  ?triples ?distinctSubjects ?distinctObjects
  ?objectsPerSubject ?subjectsPerObject
WHERE {
  {
    # (A) LOCAL AT IDSM - ChEMBL''s cross-reference linksets. ChEMBL is the
    #     resource that points outward to UniProt, PDB, PubChem and ChEBI at
    #     once, so these linksets answer most of the question, WITH ratios.
    #     A DISTINCT subselect collapses the several graph-scoped linkset
    #     copies IDSM publishes for each identical class pair.
    {
      SELECT DISTINCT ?linkFrom ?linkPredicate ?linkTo
        ?triples ?distinctSubjects ?distinctObjects
      WHERE {
        GRAPH <https://idsm.elixir-czech.cz/.well-known/void> {
          ?linkset void:linkPredicate ?linkPredicate ;
            void:subjectsTarget ?subjectPart ;
            void:objectsTarget ?objectPart ;
            void:triples ?triples ;
            void:distinctSubjects ?distinctSubjects ;
            void:distinctObjects ?distinctObjects .
          ?subjectPart void:class ?linkFrom .
          ?objectPart void:class ?linkTo .
          FILTER (?linkPredicate IN (cco:targetCmptXref, cco:moleculeXref))
        }
        # Only the databases this question asks about.
        FILTER (?linkTo IN (cco:UniprotRef, cco:ProteinDataBankRef, cco:PubchemRef, cco:ChebiRef))
      }
    }
    # THE CARDINALITY RULE, applied to the published counts.
    BIND (ROUND(1000.0 * ?triples / ?distinctSubjects) / 1000 AS ?objectsPerSubject)
    BIND (ROUND(1000.0 * ?triples / ?distinctObjects) / 1000 AS ?subjectsPerObject)
    BIND (IF(?objectsPerSubject < 1.05 && ?subjectsPerObject < 1.05, "one-to-one",
      IF(?objectsPerSubject >= 1.05 && ?subjectsPerObject < 1.05, "one-to-many",
        IF(?objectsPerSubject < 1.05 && ?subjectsPerObject >= 1.05, "many-to-one",
          "many-to-many")))
      AS ?cardinality)
  }
  UNION
  {
    # (B) FEDERATED AT UNIPROT - the two links UniProt itself curates:
    #     accession -> PDB structure, and accession -> Rhea reaction.
    #     Their SIZE is published (void:triples on the property partition
    #     of the owning class), so the link is proven and quantified; the
    #     ratio is left unbound for the reason given in the header.
    #     NOTE: IDSM refuses a SELECT DISTINCT inside SERVICE ("the SERVICE
    #     pattern cannot be evaluated"), so this is written as plain triple
    #     patterns and de-duplicated by the outer DISTINCT.
    SERVICE <https://sparql.uniprot.org/sparql> {
      GRAPH <https://sparql.uniprot.org/.well-known/void> {
        ?classPart void:class ?linkFrom ;
          void:propertyPartition ?propPart .
        ?propPart void:property ?linkPredicate ;
          void:triples ?triples .
      }
      FILTER (?linkFrom IN (up:Structure_Resource, up:Catalytic_Activity))
      FILTER (?linkPredicate IN (up:database, up:catalyzedReaction))
    }
    BIND (IF(?linkPredicate = up:catalyzedReaction,
      <http://rdf.rhea-db.org/Reaction>,
      <http://purl.uniprot.org/database/PDB>) AS ?linkTo)
    BIND ("not derivable - UniProt publishes no distinct subject/object counts" AS ?cardinality)
  }
}
ORDER BY ?cardinality ?linkTo ?linkFrom');

insert into idsm.federated_query_targets values (13, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/pubchem/014.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (14,
'How can I retrieve all Rhea reactions that involve L-glutamate(1-) (CID 5460299)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rh: <http://rdf.rhea-db.org/>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>

SELECT DISTINCT ?rhea ?equation
WHERE {
  GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/compound> {
    compound:CID5460299 a ?chebi .
  }

  SERVICE <https://sparql.rhea-db.org/sparql> {
    ?rhea rdfs:subClassOf rh:Reaction .
    ?rhea rh:equation ?equation .
    ?rhea rh:side/rh:contains/rh:compound ?compound .

    ?compound (rh:chebi|(rh:reactivePart/rh:chebi)|(rh:underlyingChebi/rh:chebi)) ?chebi .
  }
}');

insert into idsm.federated_query_targets values (14, 'https://sparql.rhea-db.org/sparql');


--------------------------------------------------------------------------------
-- examples/pubchem/015.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (15,
'How can I retrieve all compounds involved in the given Rhea reaction (RHEA:10020)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rh: <http://rdf.rhea-db.org/>

SELECT DISTINCT ?compound
WHERE {
  SERVICE <https://sparql.rhea-db.org/sparql> {
    VALUES ?rhea { rh:10020 }

    ?rhea rdfs:subClassOf rh:Reaction .
    ?rhea rh:equation ?equation .
    ?rhea rh:side/rh:contains/rh:compound ?cmpd .

    ?cmpd (rh:chebi|(rh:reactivePart/rh:chebi)|(rh:underlyingChebi/rh:chebi)) ?chebi .
  }

  GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/compound> {
    ?compound a ?chebi .
  }
}');

insert into idsm.federated_query_targets values (15, 'https://sparql.rhea-db.org/sparql');


--------------------------------------------------------------------------------
-- examples/pubchem/016.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (16,
'How can I retrieve the WURCS sequence from Glycosmos for the glycan structure (SID 252275760) in PubChem?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX glycan: <http://purl.jp/bio/12/glyco/glycan#>
PREFIX substance: <http://rdf.ncbi.nlm.nih.gov/pubchem/substance/>

SELECT DISTINCT ?gtcid ?wurcs
WHERE {
  GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/substance> {
    substance:SID252275760 rdfs:seeAlso ?glycan .
  }

  SERVICE <https://ts.glycosmos.org/sparql> {
    ?glycan dcterms:identifier ?gtcid ;
      glycan:has_glycosequence ?gs .
    ?gs glycan:in_carbohydrate_format glycan:carbohydrate_format_wurcs ;
      glycan:has_sequence ?wurcs .
  }
}');

insert into idsm.federated_query_targets values (16, 'https://ts.glycosmos.org/sparql');


--------------------------------------------------------------------------------
-- examples/pubchem/017.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (17,
'Which PubChem pathways include the genes associated with Keshan disease (DOID:0050083), as identified by Glycosmos?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX bp3: <http://www.biopax.org/release/biopax-level3.owl#>
PREFIX glycan: <http://purl.jp/bio/12/glyco/glycan#>

SELECT DISTINCT ?pathway ?title
WHERE {
  SERVICE <https://ts.glycosmos.org/sparql> {
    VALUES ?disease { <http://glycosmos.org/disease/DOID:0050083> }

    ?disease a glycan:Disease ;
      sio:SIO_000255 [ sio:SIO_000001 ?glycogene ] .
  }

  GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/gene> {
    ?gene a sio:SIO_010035 ;
      rdfs:seeAlso ?glycogene .
  }

  GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/pathway> {
    ?pathway a bp3:Pathway ;
      obo:RO_0000057 ?gene ;
      dcterms:title ?title .
  }
}');

insert into idsm.federated_query_targets values (17, 'https://ts.glycosmos.org/sparql');


--------------------------------------------------------------------------------
-- examples/pubchem/018.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (18,
'Which PDB structures with a resolution better than 2 Å that include Aspirin (CID 2244) and have associated bioactivity data in PubChem?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX cheminf: <http://semanticscience.org/resource/>
PREFIX bao: <http://www.bioassayontology.org/bao#>
PREFIX pdbo: <http://rdf.wwpdb.org/schema/pdbx-v50.owl#>
PREFIX up: <http://purl.uniprot.org/core/>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>

SELECT ?structure ?resolution
WHERE {
  GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/substance> {
    ?substance cheminf:CHEMINF_000477 compound:CID2244 ;
      pdbo:link_to_pdb ?structure .
  }

  SERVICE <https://sparql.uniprot.org/sparql> {
    ?structure a up:Structure_Resource ;
      up:database <http://purl.uniprot.org/database/PDB> ;
      up:resolution ?resolution .
    FILTER (?resolution < 2)

    ?uniprot a up:Protein ;
      rdfs:seeAlso ?structure .
  }

  GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/protein> {
    ?protein a sio:SIO_010043 ;
      rdfs:seeAlso ?uniprot .
  }

  FILTER EXISTS {
    GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup> {
      ?measuregroup a bao:BAO_0000040 ;
        obo:RO_0000057 ?protein .
    }
  }
}');

insert into idsm.federated_query_targets values (18, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/pubchem/019.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (19,
'How can I retrieve all compounds involved in PDB structures with a resolution better than 2 Å for the protein Basic phospholipase A2 VRV-PL-VIIIa (UniProt ID: P59071)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX cheminf: <http://semanticscience.org/resource/>
PREFIX pdbo: <http://rdf.wwpdb.org/schema/pdbx-v50.owl#>
PREFIX up: <http://purl.uniprot.org/core/>

SELECT ?compound
WHERE {
  SERVICE <https://sparql.uniprot.org/sparql> {
    VALUES ?uniprot { <http://purl.uniprot.org/uniprot/P59071> }

    ?uniprot a up:Protein ;
      rdfs:seeAlso ?pdb .

    ?pdb a up:Structure_Resource ;
      up:database <http://purl.uniprot.org/database/PDB> ;
      up:resolution ?resolution .
    FILTER (?resolution < 2)
  }

  GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/substance> {
    ?substance cheminf:CHEMINF_000477 ?compound ;
      pdbo:link_to_pdb ?pdb .
  }
}');

insert into idsm.federated_query_targets values (19, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/pubchem/020.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (20,
'How to retrieve the labels of Aspirin (CID 2244) in English and Spanish from Wikidata?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>

SELECT ?wdent ?label
WHERE {
  GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/compound> {
    compound:CID2244 rdfs:seeAlso ?wdent .
  }

  SERVICE <https://query.wikidata.org/sparql> {
    ?wdent rdfs:label ?label .
    FILTER (LANG(?label) = "en" || LANG(?label) = "es")
  }
}');

insert into idsm.federated_query_targets values (20, 'https://query.wikidata.org/sparql');


--------------------------------------------------------------------------------
-- examples/pubchem/021.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (21,
'How to retrieve the preferred label from PubChem for the Wikidata entry (Q18216)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX wd: <http://www.wikidata.org/entity/>
PREFIX wdt: <http://www.wikidata.org/prop/direct/>

SELECT ?label
WHERE {
  SERVICE <https://query.wikidata.org/sparql> {
    VALUES ?wdent { wd:Q18216 }

    ?wdent wdt:P662 ?cid .
  }

  GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/compound> {
    ?compound dcterms:identifier ?cid ;
      skos:prefLabel ?label .
  }
}');

insert into idsm.federated_query_targets values (21, 'https://query.wikidata.org/sparql');


--------------------------------------------------------------------------------
-- examples/pubchem/022.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (22,
'How can I find the WikiPathways that include the compound dihydroflavine-adenine dinucleotide (CID 446013)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX bp3: <http://www.biopax.org/release/biopax-level3.owl#>
PREFIX wp: <http://vocabularies.wikipathways.org/wp#>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>

SELECT DISTINCT ?wpid
WHERE {
  GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/pathway> {
    ?pathway a bp3:Pathway ;
      rdfs:seeAlso ?wp ;
      obo:RO_0000057 compound:CID446013 .
    FILTER (CONTAINS(STR(?wp), "wikipathways"))
  }

  BIND (IRI(REPLACE(STR(?wp), "http://identifiers.org/wikipathways:",
    "https://identifiers.org/wikipathways/")) AS ?wikipathways)

  SERVICE <https://sparql.wikipathways.org/sparql> {
    ?wikipathways a wp:Pathway ;
      dcterms:identifier ?wpid .
  }
}');

insert into idsm.federated_query_targets values (22, 'https://sparql.wikipathways.org/sparql');


--------------------------------------------------------------------------------
-- examples/pubchem/023.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (23,
'How can I retrieve the CID of compounds involved in the pathway Electron Transport Chain: OXPHOS system in mitochondria (Wikipathways:WP111)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX bp3: <http://www.biopax.org/release/biopax-level3.owl#>
PREFIX wp: <http://vocabularies.wikipathways.org/wp#>

SELECT DISTINCT (STRAFTER(STR(?cmpd), "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID") AS ?cid)
WHERE {
  SERVICE <https://sparql.wikipathways.org/sparql> {
    ?wikipathways a wp:Pathway ;
      dcterms:identifier "WP111" .
  }

  SERVICE <https://sparql.api.identifiers.org/sparql> {
    GRAPH <id:active> {
      ?wikipathways owl:sameAs ?wp .
    }
  }

  GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/pathway> {
    ?pathway a bp3:Pathway ;
      rdfs:seeAlso ?wp ;
      obo:RO_0000057 ?cmpd .
    FILTER (STRSTARTS(STR(?cmpd), "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/"))
  }
}');

insert into idsm.federated_query_targets values (23, 'https://sparql.api.identifiers.org/sparql');
insert into idsm.federated_query_targets values (23, 'https://sparql.wikipathways.org/sparql');


--------------------------------------------------------------------------------
-- examples/rhea/024.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (24,
'Retrieve the Rhea biochemical reactions that involve cholesterol or cholesterol derivatives',
'https://sparql.rhea-db.org/sparql',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rh: <http://rdf.rhea-db.org/>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>
PREFIX up: <http://purl.uniprot.org/core/>
PREFIX endpoint: <https://idsm.elixir-czech.cz/sparql/endpoint/>

SELECT DISTINCT ?CHEBI ?CHEBI_UNIPROT_NAME ?RHEA_REACTION ?RHEA_REACTION_EQUATION
WHERE {
  SERVICE endpoint:chebi {
    ?CHEBI sachem:substructureSearch [
      sachem:query "C1C2(C3(CCC4(C(C3(CC=C2CC(C1)O))(CCC4(C(C)CCCC(C)C)))C))C"
    ] .
  }

  ?RHEA_REACTION rdfs:subClassOf rh:Reaction .
  ?RHEA_REACTION rh:status rh:Approved .
  ?RHEA_REACTION rh:equation ?RHEA_REACTION_EQUATION .
  ?RHEA_REACTION rh:side/rh:contains/rh:compound/rh:chebi ?CHEBI .
  ?CHEBI up:name ?CHEBI_UNIPROT_NAME .
}');

insert into idsm.federated_query_targets values (24, 'https://idsm.elixir-czech.cz/sparql/endpoint/chebi');


--------------------------------------------------------------------------------
-- examples/rhea/025.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (25,
'Retrieve the number of UniProtKB/Swiss-Prot human enzymes that metabolize cholesterol or cholesterol derivatives',
'https://sparql.rhea-db.org/sparql',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rh: <http://rdf.rhea-db.org/>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>
PREFIX taxon: <http://purl.uniprot.org/taxonomy/>
PREFIX up: <http://purl.uniprot.org/core/>
PREFIX endpoint: <https://idsm.elixir-czech.cz/sparql/endpoint/>

SELECT (COUNT(DISTINCT ?PROTEIN) AS ?HUMAN_PROTEIN_COUNT)
  (COUNT(DISTINCT ?RHEA_REACTION) AS ?RHEA_REACTION_COUNT)
WHERE {
  # Rhea reactions involving cholesterol or its derivatives, computed first
  # so that only their IRIs are sent on to UniProt
  {
    SELECT DISTINCT ?RHEA_REACTION
    WHERE {
      # endpoint:chebi service
      SERVICE endpoint:chebi {
        ?CHEBI sachem:substructureSearch [
          sachem:query "C1C2(C3(CCC4(C(C3(CC=C2CC(C1)O))(CCC4(C(C)CCCC(C)C)))C))C"
        ] .
      }

      ?RHEA_REACTION rdfs:subClassOf rh:Reaction .
      ?RHEA_REACTION rh:status rh:Approved .
      ?RHEA_REACTION rh:side/rh:contains/rh:compound/rh:chebi ?CHEBI .
    }
  }

  # UniProt service
  SERVICE <https://sparql.uniprot.org/sparql> {
    # Rhea reactions catalyzed by UniProt proteins
    ?PROTEIN up:annotation/up:catalyticActivity/up:catalyzedReaction ?RHEA_REACTION .

    # UniProtKB/Swiss-Prot entries
    ?PROTEIN up:reviewed true .
    # Human entries
    ?PROTEIN up:organism taxon:9606 .
  }
}');

insert into idsm.federated_query_targets values (25, 'https://idsm.elixir-czech.cz/sparql/endpoint/chebi');
insert into idsm.federated_query_targets values (25, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/rhea/026.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (26,
'Retrieve the list of UniProtKB/Swiss-Prot human proteins that catalyze Rhea reactions involving cholesterol or cholesterol derivatives',
'https://sparql.rhea-db.org/sparql',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rh: <http://rdf.rhea-db.org/>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>
PREFIX taxon: <http://purl.uniprot.org/taxonomy/>
PREFIX up: <http://purl.uniprot.org/core/>
PREFIX endpoint: <https://idsm.elixir-czech.cz/sparql/endpoint/>

SELECT DISTINCT ?CHEBI ?CHEBI_UNIPROT_NAME ?RHEA_REACTION ?PROTEIN ?PROTEIN_FULL_NAME
WHERE {
  # Rhea reactions involving cholesterol or its derivatives, computed first
  # so that only their IRIs are sent on to UniProt
  {
    SELECT DISTINCT ?CHEBI ?CHEBI_UNIPROT_NAME ?RHEA_REACTION
    WHERE {
      # endpoint:chebi service
      SERVICE endpoint:chebi {
        ?CHEBI sachem:substructureSearch [
          sachem:query "C1C2(C3(CCC4(C(C3(CC=C2CC(C1)O))(CCC4(C(C)CCCC(C)C)))C))C"
        ] .
      }

      ?RHEA_REACTION rdfs:subClassOf rh:Reaction .
      ?RHEA_REACTION rh:status rh:Approved .
      ?RHEA_REACTION rh:side/rh:contains/rh:compound/rh:chebi ?CHEBI .
      ?CHEBI up:name ?CHEBI_UNIPROT_NAME .
    }
  }

  # UniProt service
  SERVICE <https://sparql.uniprot.org/sparql> {
    # Rhea reactions catalyzed by UniProt proteins
    ?PROTEIN up:annotation/up:catalyticActivity/up:catalyzedReaction ?RHEA_REACTION .

    # UniProtKB/Swiss-Prot entries
    ?PROTEIN up:reviewed true .
    # Human entries
    ?PROTEIN up:organism taxon:9606 .
    # Protein name
    ?PROTEIN up:recommendedName/up:fullName ?PROTEIN_FULL_NAME .
  }
}');

insert into idsm.federated_query_targets values (26, 'https://idsm.elixir-czech.cz/sparql/endpoint/chebi');
insert into idsm.federated_query_targets values (26, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/rhea/027.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (27,
'Retrieve the number of UniProtKB/Swiss-Prot human enzymes that metabolize cholesterol or cholesterol derivatives and that are involved in diseases',
'https://sparql.rhea-db.org/sparql',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rh: <http://rdf.rhea-db.org/>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>
PREFIX taxon: <http://purl.uniprot.org/taxonomy/>
PREFIX up: <http://purl.uniprot.org/core/>
PREFIX endpoint: <https://idsm.elixir-czech.cz/sparql/endpoint/>

SELECT (COUNT(DISTINCT ?PROTEIN) AS ?HUMAN_PROTEIN_COUNT)
  (COUNT(DISTINCT ?DISEASE) AS ?DISEASE_COUNT)
WHERE {
  # Rhea reactions involving cholesterol or its derivatives, computed first
  # so that only their IRIs are sent on to UniProt
  {
    SELECT DISTINCT ?RHEA_REACTION
    WHERE {
      # endpoint:chebi service
      SERVICE endpoint:chebi {
        ?CHEBI sachem:substructureSearch [
          sachem:query "C1C2(C3(CCC4(C(C3(CC=C2CC(C1)O))(CCC4(C(C)CCCC(C)C)))C))C"
        ] .
      }

      ?RHEA_REACTION rdfs:subClassOf rh:Reaction .
      ?RHEA_REACTION rh:status rh:Approved .
      ?RHEA_REACTION rh:side/rh:contains/rh:compound/rh:chebi ?CHEBI .
    }
  }

  # UniProt service
  SERVICE <https://sparql.uniprot.org/sparql> {
    # Rhea reactions catalyzed by UniProt proteins
    ?PROTEIN up:annotation/up:catalyticActivity/up:catalyzedReaction ?RHEA_REACTION .

    # UniProtKB/Swiss-Prot entries
    ?PROTEIN up:reviewed true .
    # Human entries
    ?PROTEIN up:organism taxon:9606 .
    # disease
    ?PROTEIN up:annotation/up:disease ?DISEASE .
  }
}');

insert into idsm.federated_query_targets values (27, 'https://idsm.elixir-czech.cz/sparql/endpoint/chebi');
insert into idsm.federated_query_targets values (27, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/rhea/028.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (28,
'Retrieve the list of diseases involving human enzymes that metabolize cholesterol or cholesterol derivatives and the number of proteins involved',
'https://sparql.rhea-db.org/sparql',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX rh: <http://rdf.rhea-db.org/>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>
PREFIX taxon: <http://purl.uniprot.org/taxonomy/>
PREFIX up: <http://purl.uniprot.org/core/>
PREFIX endpoint: <https://idsm.elixir-czech.cz/sparql/endpoint/>

SELECT ?DISEASE ?DISEASE_NAME (COUNT(DISTINCT ?PROTEIN) AS ?HUMAN_PROTEIN_COUNT)
WHERE {
  # Rhea reactions involving cholesterol or its derivatives, computed first
  # so that only their IRIs are sent on to UniProt
  {
    SELECT DISTINCT ?RHEA_REACTION
    WHERE {
      SERVICE endpoint:chebi {
        ?CHEBI sachem:substructureSearch [
          sachem:query "C1C2(C3(CCC4(C(C3(CC=C2CC(C1)O))(CCC4(C(C)CCCC(C)C)))C))C"
        ] .
      }

      ?RHEA_REACTION rdfs:subClassOf rh:Reaction .
      ?RHEA_REACTION rh:status rh:Approved .
      ?RHEA_REACTION rh:side/rh:contains/rh:compound/rh:chebi ?CHEBI .
    }
  }

  # UniProt endpoint service
  SERVICE <https://sparql.uniprot.org/sparql> {
    # Rhea reactions catalyzed by UniProt proteins
    ?PROTEIN up:annotation/up:catalyticActivity/up:catalyzedReaction ?RHEA_REACTION .

    # UniProtKB/Swiss-Prot entries
    ?PROTEIN up:reviewed true .
    # Human entries
    ?PROTEIN up:organism taxon:9606 .
    # disease
    ?PROTEIN up:annotation/up:disease ?DISEASE .
    ?DISEASE skos:prefLabel ?DISEASE_NAME .
  }
}
GROUP BY ?DISEASE ?DISEASE_NAME
ORDER BY DESC(COUNT(DISTINCT ?PROTEIN))');

insert into idsm.federated_query_targets values (28, 'https://idsm.elixir-czech.cz/sparql/endpoint/chebi');
insert into idsm.federated_query_targets values (28, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/rhea/029.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (29,
'Retrieve ChEMBL drugs that interact with UniProt enzymes catalyzing Rhea reactions involving members of the ChEBI class ChEBI:15889 (sterol) as participants.',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX CHEBI: <http://purl.obolibrary.org/obo/CHEBI_>
PREFIX chebihash: <http://purl.obolibrary.org/obo/chebi#>
PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>
PREFIX rh: <http://rdf.rhea-db.org/>
PREFIX taxon: <http://purl.uniprot.org/taxonomy/>
PREFIX up: <http://purl.uniprot.org/core/>

SELECT DISTINCT ?protein ?proteinFullName ?activityType
  ?standardActivityValue ?standardActivityUnit ?chemblMolecule ?chemlbMoleculePrefLabel
WHERE {
  SERVICE <https://sparql.uniprot.org/sparql> {
    SERVICE <https://sparql.rhea-db.org/sparql> {
      # retrieve members of the ChEBI class ChEBI:15889 (sterol)
      {
        ?chebi (rdfs:subClassOf)+ CHEBI:15889 .
      }
      UNION
      {
        _:bn (rdfs:subClassOf)+ CHEBI:15889 .
        _:bn rdfs:subClassOf [
          a owl:Restriction ;
          owl:onProperty chebihash:has_major_microspecies_at_pH_7_3 ;
          owl:someValuesFrom ?chebi
        ] .
      }

      # retrieve the Rhea reactions involving these ChEBI as participants
      ?reaction rdfs:subClassOf rh:Reaction ;
        rh:status rh:Approved ;
        rh:side/rh:contains/rh:compound/rh:chebi ?chebi .
    }

    # retrieve the human (taxid:9606) enzymes catalyzing these Rhea reactions
    ?protein up:annotation/up:catalyticActivity/up:catalyzedReaction ?reaction ;
      up:organism taxon:9606 ;
      up:recommendedName/up:fullName ?proteinFullName .
  }

  # retrieve the drugs in clinical phase 4 that interact with the enzymes
  ?activity a cco:Activity ;
    cco:hasAssay/cco:hasTarget/cco:hasTargetComponent/cco:targetCmptXref ?protein ;
    cco:hasMolecule ?chemblMolecule ;
    cco:standardType ?activityType ;
    cco:standardValue ?standardActivityValue ;
    cco:standardUnits ?standardActivityUnit .

  ?chemblMolecule cco:highestDevelopmentPhase ?phase ;
    skos:prefLabel ?chemlbMoleculePrefLabel .
  FILTER (?phase = 4)
}');

insert into idsm.federated_query_targets values (29, 'https://sparql.rhea-db.org/sparql');
insert into idsm.federated_query_targets values (29, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/scicomp/030.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (30,
'Give me all oxidoreductase inhibitors active <100 nM in human and mouse.',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>
PREFIX ec: <http://purl.uniprot.org/enzyme/>
PREFIX taxon: <http://purl.uniprot.org/taxonomy/>
PREFIX up: <http://purl.uniprot.org/core/>

SELECT DISTINCT ?molecule ?humanProtein ?humanType ?humanValue ?mouseProtein ?mouseType ?mouseValue
WHERE {
  SERVICE <https://sparql.uniprot.org/sparql> {
    SELECT DISTINCT ?humanProtein ?mouseProtein
    WHERE {
      ?humanProtein up:organism taxon:9606 ;
        (up:enzyme|up:domain/up:enzyme|up:component/up:enzyme) ?enzyme .
      ?mouseProtein up:organism taxon:10090 ;
        (up:enzyme|up:domain/up:enzyme|up:component/up:enzyme) ?enzyme .
      ?enzyme rdfs:subClassOf+ ec:1.-.-.- .
    }
  }

  GRAPH <http://rdf.ebi.ac.uk/dataset/chembl> {
    ?humanActivity a cco:Activity ;
      cco:hasMolecule ?molecule ;
      cco:hasAssay ?humanAssay ;
      cco:standardType ?humanType ;
      cco:standardValue ?humanValue ;
      cco:standardUnits "nM" .

    ?humanAssay cco:hasTarget/cco:hasTargetComponent/cco:targetCmptXref ?humanProtein .

    ?mouseActivity a cco:Activity ;
      cco:hasMolecule ?molecule ;
      cco:hasAssay ?mouseAssay ;
      cco:standardType ?mouseType ;
      cco:standardValue ?mouseValue ;
      cco:standardUnits "nM" .

    ?mouseAssay cco:hasTarget/cco:hasTargetComponent/cco:targetCmptXref ?mouseProtein .

    FILTER (xsd:decimal(?humanValue) < 100)
    FILTER (xsd:decimal(?mouseValue) < 100)

    # Operational approximation of "inhibitor"
    FILTER (?humanType IN ("IC50", "Ki"))
    FILTER (?mouseType IN ("IC50", "Ki"))
  }
}
LIMIT 500');

insert into idsm.federated_query_targets values (30, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/scicomp/031.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (31,
'What compounds are known to modulate human PRKCA (protein kinase C alpha, UniProt P17252) directly?',
'https://sparql.uniprot.org/sparql',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>
PREFIX uniprot: <http://purl.uniprot.org/uniprot/>
PREFIX up: <http://purl.uniprot.org/core/>

SELECT DISTINCT ?compound ?compoundLabel ?activityType ?value ?unit ?assay
WHERE {
  VALUES ?protein { uniprot:P17252 }

  ?protein a up:Protein .

  SERVICE <https://idsm.elixir-czech.cz/sparql/endpoint/idsm> {
    ?activity a cco:Activity ;
      cco:hasMolecule ?compound ;
      cco:hasAssay ?assay ;
      cco:standardType ?activityType ;
      cco:standardValue ?value ;
      cco:standardUnits ?unit .

    ?assay cco:hasTarget ?target .

    ?target cco:hasTargetComponent/cco:targetCmptXref ?protein .

    OPTIONAL {
      ?compound rdfs:label ?compoundLabel .
    }
  }
}
ORDER BY ?value');

insert into idsm.federated_query_targets values (31, 'https://idsm.elixir-czech.cz/sparql/endpoint/idsm');


--------------------------------------------------------------------------------
-- examples/scicomp/032.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (32,
'For a given compound (here loratadine, ChEMBL998), give its interaction profile with targets.',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>
PREFIX chembl_molecule: <http://rdf.ebi.ac.uk/resource/chembl/molecule/>
PREFIX up: <http://purl.uniprot.org/core/>

SELECT DISTINCT ?protein ?proteinName ?activityType ?value ?unit ?assayType
WHERE {
  {
    SELECT DISTINCT ?protein ?activityType ?value ?unit ?assayType
    WHERE {
      ?activity a cco:Activity ;
        cco:hasMolecule chembl_molecule:CHEMBL998 ;
        cco:hasAssay ?assay ;
        cco:standardType ?activityType ;
        cco:standardValue ?value ;
        cco:standardUnits ?unit .

      ?assay cco:assayType ?assayType ;
        cco:hasTarget ?target .

      ?target cco:hasTargetComponent/cco:targetCmptXref ?protein .

      FILTER (STRSTARTS(STR(?protein), "http://purl.uniprot.org/uniprot/"))
    }
  }

  SERVICE <https://sparql.uniprot.org/sparql> {
    SELECT ?protein ?proteinName
    WHERE {
      ?protein a up:Protein ;
        up:reviewed true ;
        up:recommendedName/up:fullName ?proteinName .
    }
  }
}
ORDER BY ?value');

insert into idsm.federated_query_targets values (32, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/scicomp/033.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (33,
'For a given compound (here imatinib), summarize similar compounds and their activities.',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>
PREFIX up: <http://purl.uniprot.org/core/>

SELECT DISTINCT ?compound ?similarity ?protein ?proteinName ?activityType ?value ?unit
WHERE {
  # The chemistry + bioactivity side is computed first and capped, so only a
  # small, bounded set of ?protein values is sent to UniProt. Materialising the
  # whole UniProt name table instead is prohibitively slow here.
  {
    SELECT DISTINCT ?compound ?similarity ?protein ?activityType ?value ?unit
    WHERE {
      # Sachem similarity search over ChEMBL structures. The query structure is
      # imatinib (PubChem CID 5291, ChEMBL941) - replace the SMILES to profile a
      # different compound.
      # NOTE: results are limited with sachem:topn only. sachem:cutoff is accepted
      # by the endpoint but returns zero rows at any threshold (verified for
      # 0.5 and 0.8 even though the top scores here are 0.95-1.0), so ranking is
      # done with ORDER BY DESC(?similarity) instead.
      SERVICE <https://idsm.elixir-czech.cz/sparql/endpoint/chembl> {
        [
          sachem:compound ?compound ;
          sachem:score ?similarity
        ]
          sachem:similaritySearch [
            sachem:query "CC1=C(C=C(C=C1)NC(=O)C2=CC=C(C=C2)CN3CCN(CC3)C)NC4=NC=CC(=N4)C5=CN=CC=C5" ;
            sachem:topn 20
          ] .
      }

      # Reported activities of those similar compounds, and the target component
      # each assay maps to.
      GRAPH <http://rdf.ebi.ac.uk/dataset/chembl> {
        ?activity a cco:Activity ;
          cco:hasMolecule ?compound ;
          cco:hasAssay ?assay ;
          cco:standardType ?activityType ;
          cco:standardValue ?value ;
          cco:standardUnits ?unit .

        ?assay cco:hasTarget/cco:hasTargetComponent/cco:targetCmptXref ?protein .

        FILTER (STRSTARTS(STR(?protein), "http://purl.uniprot.org/uniprot/"))
      }
    }
    LIMIT 200
  }

  # UniProt leg: recommended full name for each of those target proteins.
  SERVICE <https://sparql.uniprot.org/sparql> {
    ?protein up:recommendedName/up:fullName ?proteinName .
  }
}
ORDER BY DESC(?similarity) ?value
LIMIT 200');

insert into idsm.federated_query_targets values (33, 'https://idsm.elixir-czech.cz/sparql/endpoint/chembl');
insert into idsm.federated_query_targets values (33, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/scicomp/034.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (34,
'Which compounds are known activators of targets related to Parkinson''s or Alzheimer''s disease?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>
PREFIX wd: <http://www.wikidata.org/entity/>
PREFIX wdt: <http://www.wikidata.org/prop/direct/>

SELECT DISTINCT ?disease ?gene ?protein ?compound ?compoundLabel ?action
WHERE {
  SERVICE <https://query.wikidata.org/sparql> {
    VALUES ?disease {
      wd:Q11081 # Alzheimer''s disease
      wd:Q11085 # Parkinson''s disease
    }

    {
      ?gene wdt:P2293 ?disease .
    }
    UNION
    {
      ?disease wdt:P2293 ?gene .
    }
    UNION
    {
      ?gene wdt:P1918 ?disease .
    }

    ?gene wdt:P688 ?proteinItem .
    ?proteinItem wdt:P352 ?uniprotAccession .

    BIND (IRI(CONCAT("http://purl.uniprot.org/uniprot/", ?uniprotAccession)) AS ?protein)
  }

  SERVICE <https://idsm.elixir-czech.cz/sparql/endpoint/idsm> {
    ?compound cco:hasMechanism ?mechanism .

    ?mechanism cco:hasTarget ?target ;
      cco:mechanismActionType ?action .

    ?target cco:hasTargetComponent/cco:targetCmptXref ?protein .

    # "Activator" = a positive-direction mechanism. An enumerated list is used
    # rather than CONTAINS(...,"AGONIST"): substring matching also admits
    # ANTAGONIST, INVERSE AGONIST and ALLOSTERIC ANTAGONIST, which are the
    # opposite of activation (all three were returned by the substring form).
    FILTER (?action IN (
      "AGONIST",
      "PARTIAL AGONIST",
      "ACTIVATOR",
      "POSITIVE ALLOSTERIC MODULATOR",
      "POSITIVE MODULATOR",
      "OPENER",
      "RELEASING AGENT",
      "STABILISER"
    ))

    OPTIONAL {
      ?compound rdfs:label ?compoundLabel .
    }
  }
}');

insert into idsm.federated_query_targets values (34, 'https://query.wikidata.org/sparql');


--------------------------------------------------------------------------------
-- examples/scicomp/035.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (35,
'Which compounds annotated in an active Melochia umbellata extract have reported activity against Trypanosoma cruzi, and in which taxa are they reported?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>
PREFIX wdt: <http://www.wikidata.org/prop/direct/>
PREFIX vocab: <http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#>

SELECT DISTINCT ?inchikey ?pubchemCompound ?wdCompound ?taxon ?taxonLabel
WHERE {
  # Wikidata: in which taxa is each compound reported to occur (P703).
  # The remote pattern is bounded by an explicit VALUES list of the three
  # compounds'' Wikidata items (resolved from the same InChIKeys via P235).
  # An unrestricted "?wdCompound wdt:P703 ?taxon" spans ~2.9 million
  # statements: too large to materialise, and Wikidata then rejects the
  # SERVICE pattern. Keep this list in step with the InChIKeys below.
  SERVICE <https://query.wikidata.org/sparql> {
    SELECT ?wdCompound ?taxon ?taxonLabel
    WHERE {
      VALUES ?wdCompound {
        <http://www.wikidata.org/entity/Q425300> # benznidazole
        <http://www.wikidata.org/entity/Q411582> # nifurtimox
        <http://www.wikidata.org/entity/Q409478> # quercetin
      }
      ?wdCompound wdt:P703 ?taxon .
      OPTIONAL {
        ?taxon rdfs:label ?taxonLabel .
        FILTER (LANG(?taxonLabel) = "en")
      }
    }
  }

  # Input: structures annotated in the active extract, given as InChIKeys.
  # Substitute the InChIKeys of your own annotated features here.
  VALUES ?inchikey {
    "CULUWZNBISUWAS-UHFFFAOYSA-N" # benznidazole (CID 31593, ChEMBL110)
    "ARFHIAQFJWUCFH-IZZDOVSWSA-N" # nifurtimox   (CID 6842999)
    "REFJWTPEDVJJIY-UHFFFAOYSA-N" # quercetin    (CID 5280343)
  }

  # PubChem: InChIKey -> compound -> Wikidata concept.
  # The InChIKey node carries the string on sio:SIO_000300 and points at its
  # compound with sio:SIO_000011; the Wikidata link is a plain rdfs:seeAlso.
  GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey> {
    ?ik a vocab:InChIKey ;
      sio:SIO_000300 ?inchikey ;
      sio:SIO_000011 ?pubchemCompound .
  }
  GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/compound> {
    ?pubchemCompound rdfs:seeAlso ?wdCompound .
    FILTER (STRSTARTS(STR(?wdCompound), "http://www.wikidata.org/entity/"))
  }

  # ChEMBL: the same structure as a ChEMBL molecule, with a reported activity
  # measured against a Trypanosoma cruzi target.
  # NOTE the direction: the link is asserted FROM the ChEMBL molecule TO the
  # PubChem compound (?mol skos:exactMatch ?compound). PubChem substances carry
  # no skos:exactMatch to ChEMBL, so the reverse pattern returns nothing.
  GRAPH <http://rdf.ebi.ac.uk/dataset/chembl> {
    ?chemblCompound skos:exactMatch ?pubchemCompound .

    ?activity a cco:Activity ;
      cco:hasMolecule ?chemblCompound ;
      cco:hasAssay ?assay .

    ?assay cco:hasTarget ?target .
    ?target cco:organismName "Trypanosoma cruzi" .
  }
}');

insert into idsm.federated_query_targets values (35, 'https://query.wikidata.org/sparql');


--------------------------------------------------------------------------------
-- examples/students/036.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (36,
'Which proteins in UniProtKB/Swiss-Prot have magnesium(2+) as a cofactor?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX oboInOwl: <http://www.geneontology.org/formats/oboInOwl#>
PREFIX chemrof: <https://w3id.org/chemrof/>
PREFIX up: <http://purl.uniprot.org/core/>

SELECT ?chebi ?protein
WHERE {
  ?chebi oboInOwl:hasRelatedSynonym ?o .
  ?chebi chemrof:smiles_string ?smiles
  FILTER (LCASE(STR(?o)) = "magnesium(2+)")

  SERVICE <https://sparql.uniprot.org/sparql> {
    SELECT ?protein
    WHERE {
      ?protein up:annotation ?ann ;
        up:reviewed true .
      ?ann a up:Cofactor_Annotation ;
        up:cofactor ?chebi .
    }
    LIMIT 100
  }
}');

insert into idsm.federated_query_targets values (36, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/students/037.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (37,
'Which Rhea reactions involve any germacrene compound?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX oboInOwl: <http://www.geneontology.org/formats/oboInOwl#>
PREFIX chemrof: <https://w3id.org/chemrof/>
PREFIX rh: <http://rdf.rhea-db.org/>

SELECT DISTINCT ?rhea ?chebi
WHERE {
  ?chebi oboInOwl:hasRelatedSynonym ?o .
  ?chebi chemrof:smiles_string ?smiles
  FILTER (CONTAINS(LCASE(STR(?o)), "germacrene"))

  SERVICE <https://sparql.rhea-db.org/sparql> {
    ?rhea rdfs:subClassOf rh:Reaction .
    ?rhea rh:side/rh:contains/rh:compound ?compound .

    ?compound (rh:chebi|(rh:reactivePart/rh:chebi)|(rh:underlyingChebi/rh:chebi)) ?chebi .
  }
}');

insert into idsm.federated_query_targets values (37, 'https://sparql.rhea-db.org/sparql');


--------------------------------------------------------------------------------
-- examples/students/038.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (38,
'Which diterpenoids in ChEBI also have a DrugBank record?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX chemrof: <https://w3id.org/chemrof/>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>
PREFIX endpoint: <https://idsm.elixir-czech.cz/sparql/endpoint/>

SELECT ?compound ?smiles ?drug
WHERE {
  {
    SELECT ?compound ?smiles
    WHERE {
      ?compound rdfs:subClassOf* obo:CHEBI_23849 ;
        chemrof:smiles_string ?smiles .
    }
    LIMIT 25
  }

  SERVICE endpoint:drugbank {
    ?drug sachem:substructureSearch [
      sachem:query ?smiles ;
      sachem:searchMode sachem:exactSearch
    ] .
  }
}');

insert into idsm.federated_query_targets values (38, 'https://idsm.elixir-czech.cz/sparql/endpoint/drugbank');


--------------------------------------------------------------------------------
-- examples/students/039.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (39,
'Which chemical compounds with a recorded discovery date are the oldest, among those that have a measured mass spectrum?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>
PREFIX wdt: <http://www.wikidata.org/prop/direct/>

SELECT ?smiles (MIN(?year) AS ?firstYear) (SAMPLE(?name) AS ?aName)
  (SAMPLE(?spectrum) AS ?aSpectrum)
WHERE {
  SERVICE <https://query.wikidata.org/sparql> {
    SELECT ?name ?year ?ikey ?smiles
    WHERE {
      ?c wdt:P575 ?date .
      BIND (YEAR(?date) AS ?year)
      ?c wdt:P235 ?ikey .
      ?c wdt:P233 ?smiles .
      ?c rdfs:label ?name .
      FILTER (LANG(?name) = "en")
    }
  }

  ?compound sachem:substructureSearch [
    sachem:query ?smiles ;
    sachem:searchMode sachem:exactSearch
  ] .

  ?experiment sio:SIO_000230 ?compound .
  ?experiment sio:SIO_000229 ?spectrum .
  ?spectrum sio:SIO_000300 ?msvalue .
}
GROUP BY ?smiles
ORDER BY ?firstYear
LIMIT 10');

insert into idsm.federated_query_targets values (39, 'https://query.wikidata.org/sparql');


--------------------------------------------------------------------------------
-- examples/students/040.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (40,
'Which taxa produce sesterterpenes?',
'https://sparql.uniprot.org/sparql',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>
PREFIX wdt: <http://www.wikidata.org/prop/direct/>
PREFIX endpoint: <https://idsm.elixir-czech.cz/sparql/endpoint/>

SELECT DISTINCT ?wd ?taxonName ?smiles ?compoundLabel
WHERE {
  {
    SELECT ?wd ?smiles
    WHERE {
      SERVICE endpoint:wikidata {
        VALUES ?smiles {
          "C=C(CC/C=C(C)CC/C=C(C)CCC=C(C)C)[C@@H]1CC=C(C)CC1"
          "C=C(CC/C=C(C)CC/C=C(C)CCC=C(C)C)[C@H]1CC=C(C)CC1"
          "C=CC(=C)CC/C=C(C)CC/C=C(C)CC/C=C(C)CCC=C(C)C"
          "CC(C)=CC/C=C(C)[C@H]1C/C=C(C)CC/C=C(C)CC/C=C(C)CC1"
          "CC(C)=CCC/C(C)=C/CC/C(C)=C/CC[C@]1(C)[C@H]2CC=C(C)[C@@H]1C2"
          "CC(C)=CCCC1(C)CCC2(C)C/C=C(C)CC/C=C(C)C/C=C/2C1"
          "CC1CCC2[C@]3(C)CCC4C(C)(C)CCC[C@]4(C)C3CC[C@]2(C)C1C"
          "[H]C1C[C@@]2([H])[C@H](C(=C)C)CC[C@]2(C)C/C=C(/C)CC/C=C(/C)CC/C=C1C"
          "[H][C@@]12/C=C/C(=C)CCC/C(C)=C/CC/C(C)=C/C[C@@]1(C)CC[C@@H]2C(C)C"
          "[H][C@@]12/C=CC(C)=C/CC/C(C)=CCCC(=C)CC[C@@]1(C)CC[C@@H]2C(C)C"
          "[H][C@@]12C/C=C(/C)[C@@]3([H])CCC(=C)C3CC/C(C)=CC[C@@]1(C)CC[C@@H]2C(C)C"
          "[H][C@@]12CC/C(C)=C/CC/C(C)=C/CC/C(C)=C/C[C@@]1(C)CC[C@@H]2C(=C)C"
          "[H][C@@]12CC/C(C)=C/CC/C(C)=C/CC/C(C)=C/C[C@@]1(C)CC[C@H]2C(=C)C"
          "[H][C@@]12CC/C(C)=C/CC/C(C)=C/C[C@@]1(C)CC[C@]1([H])[C@@H](C(=C)C)CC[C@@]21C"
          "[H][C@@]12CC/C(C)=C/CCC(=C)[C@@]1([H])C[C@@]2(C)CC/C=C(C)CCC=C(C)C"
          "[H][C@@]12CC/C(C)=CCC/C(C)=CCCC(=C)/C=C[C@@]1(C)CC[C@@H]2C(C)C"
          "[H][C@@]12CC/C(C)=CCC/C(C)=CCCC(=C)/C=C[C@@]1(C)CC[C@H]2C(C)C"
          "[H][C@@]12CC/C(C)=CC[C@@]3(C)CC[C@H](C(C)C)[C@]3([H])C/C=C(/C)[C@]1([H])CCC2=C"
          "[H][C@@]12CC3=C(C)[C@@]45CC[C@@H](C)[C@]4([H])CC[C@@]5(C)[C@@]3([H])C[C@@]1(C)CC[C@]2([H])C(C)C"
          "[H][C@@]12CCC(=C)[C@]1(C)[C@@]1([H])C[C@@]3(C)CC[C@]4(C)CC[C@H](C(C)C)[C@@]4([H])[C@]3([H])[C@@]1([H])C2"
          "[H][C@@]12CC[C@@H](C)/C1=C/C/C(C)=C/C[C@]1(C)CC[C@@H](C(C)C)[C@@]1([H])C/C=C/2C"
          "[H][C@@]12CC[C@H](C)/C1=C/C/C(C)=CC[C@@]1(C)CC[C@H](C(C)C)[C@]1([H])C/C=C2C"
          "[H][C@@]12CC[C@H](C)[C@@]13CC[C@H](C)C3=C[C@@]1(C)CC[C@](C)(CCC=C(C)C)[C@@]21[H]"
          "[H][C@@]12CC[C@H](C)[C@@]13CC[C@H](C)[C@@]1([H])C[C@@]4(C)CC[C@H](C(C)C)[C@]4([H])C[C@]1(O)[C@@]23C"
          "[H][C@@]12CC[C@H](C)[C@@]13CC[C@H](C)[C@@]1([H])C[C@@]4(C)CC[C@H](C(C)C)[C@]4([H])C[C@]1(O)[C@@]23CO"
          "[H][C@@]12CC[C@H](C)[C@@]3([H])CC[C@H](C)[C@@]3([H])C[C@@]1(C)CC[C@]2([H])[C@@H](C)CCCC(C)C"
          "[H][C@@]12CC[C@H]([C@H](C)CCC(O)C(C)(C)O)[C@@]1(C)CCC/C2=CC=C1C[C@H](O)CCC1=C"
          "[H][C@@]12CC[C@]([H])([C@H](C)CCCC(C)(O)CO)[C@@]1(C)CC/C(=C/C=C1/C[C@@H](O)CCC1=C)C2"
          "[H][C@@]12C[C@@]3(C)C(=CC14C(C)C21CC[C@H](C)[C@]1([H])CC[C@@H]4C)CC[C@@H]3C(C)C"
          "[H][C@@]12C[C@@]3(C)C(=CC[C@@H]3C(C)C)CC13C(C)C21CC[C@H](C)[C@]1([H])CC[C@@H]3C"
          "[H][C@@]12C[C@@]3([H])[C@@H](C(C)C)CC[C@]3(C)C/C1=C(C)CC[C@]1([H])/C(=C2C)CC[C@@H]1C"
          "[H][C@@]12C[C@]3([H])/C(C)=C/CC/C(C)=C/CCC(=C)[C@@]3([H])C[C@@]1(C)CC[C@@H]2C(C)C"
          "[H][C@@]12C[C@]3([H])/C(C)=C4/CC[C@@H](C)[C@]4([H])C/C=C(/C)[C@@]3([H])C[C@@]1(C)CC[C@@H]2C(C)C"
          "[H][C@@]12[C@@H](C(C)C)CC[C@@]1(C)CC[C@]1(C)C=C3[C@]([H])(C[C@]4(C)CCC[C@]34C)[C@@]21[H]"
          "[H][C@]12/C=C(/C)C3=CC[C@@H](C)[C@@]3([H])CC[C@]1(C)CC[C@@]1(C)CC[C@H](C(C)C)[C@@]12[H]"
          "[H][C@]12C/C=C(C)/C=C/C/C(C)=C/CC/C(C)=C/C[C@]1(C)CC[C@H]2C(C)C"
          "[H][C@]12CC/C(C)=C3/C[C@@]4(C)CC[C@H](C(C)C)[C@]4([H])C/C3=C(C)[C@]1([H])CC[C@]2([H])C"
          "[H][C@]12CC/C(C)=CC[C@@]3(C)CC[C@H](C(C)C)[C@]3([H])/C=CC(C)=C/1CC[C@H]2C"
          "[H][C@]12CC/C(C)=CC[C@@]3(C)CC[C@H](C(C)C)[C@]3([H])C/C=C(/C)C1=CC[C@H]2C"
          "[H][C@]12CCC(C)=C1CC/C(C)=CC[C@@]1(C)CC[C@H](C(C)C)[C@]1([H])C/C=C2C"
          "[H][C@]12CC[C@@H](C)[C@]13CCC(=C)[C@]1([H])C[C@]4(C)CC[C@@H](C(C)C)[C@@]4([H])C[C@@]1([H])[C@]23C"
          "[H][C@]12CC[C@@H](C)[C@]13CC[C@@H](C)C1=C(C[C@]4([H])[C@H](C(C)C)CC[C@@]4(C)C1)[C@]23C"
          "[H][C@]12CC[C@@H](C)[C@]13CC[C@@H](C)[C@]1([H])C[C@]4(C)CC[C@@H](C(C)C)[C@@]4([H])C[C@]21C3=C"
          "[H][C@]12CC[C@@]1(C)[C@]1([H])CC[C@H](C)[C@]1([H])[C@@]2([H])C(=C)CC/C=C(C)CCC=C(C)C"
          "[H][C@]12C[C@@]1([H])[C@]1(C)[C@@]3([H])C[C@@]4(C)CC[C@]5(C)CC[C@H](C(C)C)[C@@]5([H])[C@]4([H])[C@@]3([H])C[C@]21C"
          "[H][C@]12C[C@@]3(C)C(=CC[C@@H]3C)[C@@]1([H])C[C@@]1(C)CC[C@]3(C)CC[C@H](C(C)C)[C@@]3([H])[C@@]12[H]"
          "[H][C@]12C[C@@]3(C)CCC(C(C)C)=C3CC1[C@]1(CC[C@@H]2C)C(C)=CCC[C@@H]1C"
          "[H][C@]12C[C@@]3(C)CC[C@H](C(C)C)C3=CC1[C@]1(CC[C@@H]2C)C(C)=CCC[C@@H]1C"
          "[H][C@]12C[C@@]3(C)CC[C@H](C(C)C)[C@]3([H])C=C1[C@]1(CC[C@@H]2C)C(C)=CCC[C@@H]1C"
          "[H][C@]12C[C@@]3(C)CC[C@]4(C)CC[C@H](C(C)C)[C@@]4([H])[C@]3([H])[C@@]1([H])C[C@]1(C)C(C)=CC[C@]21[H]"
          "[H][C@]12C[C@@]3([H])C(=CCC3(C)C)[C@@]1([H])C[C@@]1(C)CC[C@]3(C)CC[C@H](C(C)C)[C@@]3([H])[C@@]12[H]"
          "[H][C@]12C[C@](C)(C/C=C3[C@](C)(CC[C@]4(C)CC[C@H](C(=C)C)[C@@]34[H])C1)CC[C@@H]2C"
          "[H][C@]12C[C@]3(C)CC=C[C@]3(C)[C@@]1([H])C[C@@]1(C)CC[C@]3(C)CC[C@H](C(C)C)[C@@]3([H])[C@@]12[H]"
          "[H][C@]12C[C@]3(C)CCC[C@@]1(C[C@@]1(C)CC[C@]4(C)CC[C@H](C(C)C)[C@@]4([H])[C@@]12[H])C3=C"
          "[H][C@]12C[C@]3(C)CC[C@@H](C3=C)[C@@]1([H])C[C@@]1(C)CC[C@]3(C)CC[C@H](C(C)C)[C@@]3([H])[C@@]12[H]"
          "[H][C@]12C[C@]3([H])[C@H](C(C)C)CC[C@@]3(C)CC1=C(C)[C@]1(C)CC[C@@]3([H])[C@H](C)CC[C@@]213"
          "[H][C@]12[C@@H](C)CC[C@@]1([H])C1(C)CC[C@]([H])(C(=C)CC/C=C(C)CCC=C(C)C)[C@@]12[H]"
        }
        ?wd sachem:substructureSearch [
          sachem:query ?smiles ;
          sachem:searchMode sachem:exactSearch
        ] .
      }
    }
  }
  SERVICE <https://query.wikidata.org/sparql> {
    ?wd wdt:P703 ?taxon .
    ?taxon rdfs:label ?taxonName .
    FILTER (LANG(?taxonName) = "en")
    OPTIONAL { ?wd rdfs:label ?compoundLabel . FILTER (LANG(?compoundLabel) = "en") }
  }
}');

insert into idsm.federated_query_targets values (40, 'https://idsm.elixir-czech.cz/sparql/endpoint/wikidata');
insert into idsm.federated_query_targets values (40, 'https://query.wikidata.org/sparql');


--------------------------------------------------------------------------------
-- examples/students/041.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (41,
'Which sesterterpenes have no DrugBank record?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX chemrof: <https://w3id.org/chemrof/>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>
PREFIX endpoint: <https://idsm.elixir-czech.cz/sparql/endpoint/>

SELECT ?compound ?smiles
WHERE {
  ?compound rdfs:subClassOf* obo:CHEBI_35192 ;
    chemrof:smiles_string ?smiles .

  MINUS {
    SELECT ?smiles
    WHERE {
      ?compounds_drugs rdfs:subClassOf* obo:CHEBI_35192 ;
        chemrof:smiles_string ?smiles .
      SERVICE endpoint:drugbank {
        ?drug sachem:substructureSearch [
          sachem:query ?smiles ;
          sachem:searchMode sachem:exactSearch
        ] .
      }
    }
  }
}');

insert into idsm.federated_query_targets values (41, 'https://idsm.elixir-czech.cz/sparql/endpoint/drugbank');


--------------------------------------------------------------------------------
-- examples/students/042.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (42,
'How do sesterterpenes in ChEBI/IDSM map to Wikidata items, with their English labels?',
'https://sparql.uniprot.org/sparql',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX chemrof: <https://w3id.org/chemrof/>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>

SELECT DISTINCT ?compound ?wdid ?label
WHERE {
  {
    SELECT ?compound ?wdid
    WHERE {
      SERVICE <https://idsm.elixir-czech.cz/sparql/endpoint/idsm> {
        ?compound rdfs:subClassOf obo:CHEBI_35192 ;
          chemrof:smiles_string ?smiles .
        ?match sachem:substructureSearch [
          sachem:query ?smiles ;
          sachem:searchMode sachem:exactSearch
        ] .
        GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/compound> {
          ?match rdfs:seeAlso ?wdid .
        }
        FILTER (STRSTARTS(STR(?wdid), "http://www.wikidata.org/entity/"))
      }
    }
  }
  SERVICE <https://query.wikidata.org/sparql> {
    ?wdid rdfs:label ?label .
    FILTER (LANG(?label) = "en")
  }
}');

insert into idsm.federated_query_targets values (42, 'https://idsm.elixir-czech.cz/sparql/endpoint/idsm');
insert into idsm.federated_query_targets values (42, 'https://query.wikidata.org/sparql');


--------------------------------------------------------------------------------
-- examples/students/043.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (43,
'Which molecules are structurally similar to aspirin, and what are their Wikidata labels?',
'https://sparql.uniprot.org/sparql',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>

SELECT ?compound ?score ?label
WHERE {
  {
    SELECT ?compound ?score ?wdid
    WHERE {
      SERVICE <https://idsm.elixir-czech.cz/sparql/endpoint/idsm> {
        [ sachem:compound ?compound ; sachem:score ?score ]
          sachem:similaritySearch [
            sachem:query "CC(=O)OC1=CC=CC=C1C(=O)O" ;
            sachem:cutoff "0.85"^^xsd:double
          ] .
        GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/compound> {
          ?compound rdfs:seeAlso ?wdid .
        }
        FILTER (STRSTARTS(STR(?wdid), "http://www.wikidata.org/entity/"))
      }
    }
  }

  SERVICE <https://query.wikidata.org/sparql> {
    ?wdid rdfs:label ?label .
    FILTER (LANG(?label) = "en")
  }
}');

insert into idsm.federated_query_targets values (43, 'https://idsm.elixir-czech.cz/sparql/endpoint/idsm');
insert into idsm.federated_query_targets values (43, 'https://query.wikidata.org/sparql');


--------------------------------------------------------------------------------
-- examples/students/044.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (44,
'In how many Rhea reactions do given terpene compounds appear?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX rh: <http://rdf.rhea-db.org/>

SELECT ?chebi (SAMPLE(?label) AS ?name) (COUNT(DISTINCT ?rhea) AS ?reactionCount)
WHERE {
  VALUES ?chebi { obo:CHEBI_175763 obo:CHEBI_58756 }

  ?chebi rdfs:label ?label .

  SERVICE <https://sparql.rhea-db.org/sparql> {
    ?rhea rdfs:subClassOf rh:Reaction ;
      rh:side/rh:contains/rh:compound ?compound .
    ?compound (rh:chebi|rh:reactivePart/rh:chebi|rh:underlyingChebi/rh:chebi) ?chebi .
  }
}
GROUP BY ?chebi
ORDER BY DESC(?reactionCount)');

insert into idsm.federated_query_targets values (44, 'https://sparql.rhea-db.org/sparql');


--------------------------------------------------------------------------------
-- examples/students/045.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (45,
'What names does a compound (farnesyl triphosphate) have across Wikidata and ChEBI/IDSM?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX oboInOwl: <http://www.geneontology.org/formats/oboInOwl#>
PREFIX chemrof: <https://w3id.org/chemrof/>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>

SELECT ?source ?value
WHERE {
  {
    obo:CHEBI_17961 chemrof:smiles_string ?smiles .
    ?match sachem:substructureSearch [
      sachem:query ?smiles ;
      sachem:searchMode sachem:exactSearch
    ] .
    ?match rdfs:seeAlso ?wdid .
    SERVICE <https://query.wikidata.org/sparql> {
      ?wdid rdfs:label ?value .
      FILTER (LANG(?value) = "en")
    }
    BIND ("wikidata" AS ?source)
  }
  UNION
  {
    obo:CHEBI_17961 chemrof:smiles_string ?smiles .
    ?match sachem:substructureSearch [
      sachem:query ?smiles ;
      sachem:searchMode sachem:exactSearch
    ] .
    ?match (rdfs:label|oboInOwl:hasRelatedSynonym) ?value .
    BIND ("idsm" AS ?source)
  }
}');

insert into idsm.federated_query_targets values (45, 'https://query.wikidata.org/sparql');


--------------------------------------------------------------------------------
-- examples/students/046.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (46,
'Which biological pathways contain compounds at least 0.7 similar to germacrene, and how many such compounds does each contain?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX chemrof: <https://w3id.org/chemrof/>
PREFIX bp: <http://www.biopax.org/release/biopax-level3.owl#>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT ?wpid ?title (MAX(?score) AS ?bestScore) (COUNT(DISTINCT ?compound) AS ?nCompounds)
WHERE {
  obo:CHEBI_36515 chemrof:smiles_string ?smiles .
  [ sachem:compound ?compound ; sachem:score ?score ]
    sachem:similaritySearch [
      sachem:query ?smiles ;
      sachem:cutoff "0.7"^^xsd:double
    ] .

  GRAPH pubchem:pathway {
    ?pathway a bp:Pathway .
    ?pathway rdfs:seeAlso ?wp .
    ?pathway obo:RO_0000057 ?compound .
    FILTER (CONTAINS(STR(?wp), "wikipathways"))
  }
  BIND (IRI(REPLACE(STR(?wp), "http://identifiers.org/wikipathways:",
    "https://identifiers.org/wikipathways/")) AS ?wpIri)

  SERVICE <https://sparql.wikipathways.org/sparql> {
    ?pw dc:identifier ?wpIri ;
      dcterms:identifier ?wpid ;
      dc:title ?title .
  }
}
GROUP BY ?wpid ?title
ORDER BY DESC(?bestScore)');

insert into idsm.federated_query_targets values (46, 'https://sparql.wikipathways.org/sparql');


--------------------------------------------------------------------------------
-- examples/students/047.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (47,
'Which chosen terpene substrates take part in at least three Rhea reactions, and in which reactions?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX rh: <http://rdf.rhea-db.org/>

SELECT ?chebi (SAMPLE(?name) AS ?label)
  (COUNT(DISTINCT ?rhea) AS ?reactionCount)
  (GROUP_CONCAT(DISTINCT ?rheaId; SEPARATOR=", ") AS ?reactions)
WHERE {
  VALUES ?chebi {
    obo:CHEBI_16584
    obo:CHEBI_128769
    obo:CHEBI_17211
    obo:CHEBI_15831
    obo:CHEBI_16057
    obo:CHEBI_57623
    obo:CHEBI_175763
    obo:CHEBI_58756
    obo:CHEBI_23375
  }
  ?chebi rdfs:label ?name .

  SERVICE <https://sparql.rhea-db.org/sparql> {
    ?rhea rdfs:subClassOf rh:Reaction ;
      rh:side/rh:contains/rh:compound ?compound .
    ?compound (rh:chebi|rh:reactivePart/rh:chebi|rh:underlyingChebi/rh:chebi) ?chebi .
    BIND (STRAFTER(STR(?rhea), "http://rdf.rhea-db.org/") AS ?rheaId)
  }
}
GROUP BY ?chebi
HAVING (COUNT(DISTINCT ?rhea) >= 3)
ORDER BY DESC(?reactionCount)');

insert into idsm.federated_query_targets values (47, 'https://sparql.rhea-db.org/sparql');


--------------------------------------------------------------------------------
-- examples/students/048.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (48,
'Which PDB structures of beta-tubulin - the target of paclitaxel - are bound to paclitaxel?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX cheminf: <http://semanticscience.org/resource/>
PREFIX chemrof: <https://w3id.org/chemrof/>
PREFIX pdbo: <http://rdf.wwpdb.org/schema/pdbx-v50.owl#>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>
PREFIX up: <http://purl.uniprot.org/core/>
PREFIX endpoint: <https://idsm.elixir-czech.cz/sparql/endpoint/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT ?protein ?structure ?resolution ?method
WHERE {
  SERVICE <https://sparql.uniprot.org/sparql> {
    VALUES ?protein {
      <http://purl.uniprot.org/uniprot/P02554>
      <http://purl.uniprot.org/uniprot/P07437>
    }
    ?protein a up:Protein ;
      rdfs:seeAlso ?structure .
    ?structure a up:Structure_Resource ;
      up:database <http://purl.uniprot.org/database/PDB> ;
      up:resolution ?resolution ;
      up:method ?method .
  }

  obo:CHEBI_45863 chemrof:smiles_string ?smiles .
  SERVICE endpoint:pubchem {
    ?compound sachem:substructureSearch [
      sachem:query ?smiles ;
      sachem:searchMode sachem:exactSearch
    ] .
  }
  GRAPH pubchem:substance {
    ?substance cheminf:CHEMINF_000477 ?compound ;
      pdbo:link_to_pdb ?structure .
  }
}
ORDER BY ?resolution
LIMIT 50');

insert into idsm.federated_query_targets values (48, 'https://idsm.elixir-czech.cz/sparql/endpoint/pubchem');
insert into idsm.federated_query_targets values (48, 'https://sparql.uniprot.org/sparql');


--------------------------------------------------------------------------------
-- examples/students/049.ttl
--------------------------------------------------------------------------------

insert into idsm.federated_queries values (49,
'How are molecules similar to aspirin distributed across molecular-mass bands?',
'https://sparql.uniprot.org/sparql',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>
PREFIX wdt: <http://www.wikidata.org/prop/direct/>

SELECT ?massBand
  (COUNT(DISTINCT ?compound) AS ?nCompounds)
  (ROUND(AVG(?cScore) * 1000) / 1000 AS ?avgSimilarity)
  (MIN(?cMass) AS ?minMass)
  (MAX(?cMass) AS ?maxMass)
WHERE {
  {
    SELECT ?compound (SAMPLE(?score) AS ?cScore) (AVG(?mass) AS ?cMass)
    WHERE {
      SERVICE <https://idsm.elixir-czech.cz/sparql/endpoint/idsm> {
        [ sachem:compound ?compound ; sachem:score ?score ]
          sachem:similaritySearch [
            sachem:query "CC(=O)OC1=CC=CC=C1C(=O)O" ;
            sachem:cutoff "0.85"^^xsd:double
          ] .
        GRAPH <http://rdf.ncbi.nlm.nih.gov/pubchem/compound> {
          ?compound rdfs:seeAlso ?wdid .
        }
        FILTER (STRSTARTS(STR(?wdid), "http://www.wikidata.org/entity/"))
      }
      SERVICE <https://query.wikidata.org/sparql> {
        ?wdid wdt:P2067 ?mass .
      }
    }
    GROUP BY ?compound
  }
  BIND (xsd:integer(FLOOR(?cMass / 25)) * 25 AS ?bandLow)
  BIND (CONCAT(STR(?bandLow), "-", STR(?bandLow + 25), " Da") AS ?massBand)
}
GROUP BY ?massBand ?bandLow
ORDER BY ?bandLow');

insert into idsm.federated_query_targets values (49, 'https://idsm.elixir-czech.cz/sparql/endpoint/idsm');
insert into idsm.federated_query_targets values (49, 'https://query.wikidata.org/sparql');
