sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map-orchem:similaritySearch
}.;

sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map-orchem:similarCompoundSearch
}.;

sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map-orchem:substructureSearch
}.;

sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map-orchem:smartsSearch
}.;

--------------------------------------------------------------------------------

DB.DBA.unimport_jar(null, 'cz.iocb.orchem.OrChemService');
DB.DBA.unimport_jar(null, 'cz.iocb.orchem.bean.OrChemCompound');

drop procedure DB.dba.orchem_similaritySearch_store;
drop procedure DB.dba.orchem_similarCompoundSearch_store;
drop procedure DB.dba.orchem_substructureSearch_store;
drop procedure DB.dba.orchem_smartsSearch_store;

--------------------------------------------------------------------------------

drop table orchem_smartssearch_results;
drop table orchem_smartssearch_parameters;
drop table orchem_subsearch_results;
drop table orchem_subsearch_parameters;
drop table orchem_simsearch_results;
drop table orchem_simsearch_parameters;
