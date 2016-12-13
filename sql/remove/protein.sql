sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:protein
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:protein .;

--------------------------------------------------------------------------------

drop function iri_protein;
drop function iri_protein_INVERSE;

--------------------------------------------------------------------------------

drop table protein_complexes
drop table protein_types
drop table protein_locations
drop table protein_functions
drop table protein_participates_biosystems
drop table protein_participates_goes
drop table protein_continuantparts
drop table protein_conserveddomains
drop table protein_closematches
drop table protein_genes
drop table protein_similarproteins
drop table protein_pdblinks
drop table protein_references
drop table protein_bases
