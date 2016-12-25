ld_dir ('pubchem/RDF/compound/general' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound');
ld_dir ('pubchem/RDF/substance' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/substance');
ld_dir ('pubchem/RDF/descriptor/compound' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/compound');
ld_dir ('pubchem/RDF/descriptor/substance' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/substance');
ld_dir ('pubchem/RDF/synonym' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/synonym');
ld_dir ('pubchem/RDF/inchikey' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey');
ld_dir ('pubchem/RDF/measuregroup' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup');
ld_dir ('pubchem/RDF/endpoint' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint');
ld_dir ('pubchem/RDF/bioassay' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay');
ld_dir ('pubchem/RDF/protein' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein');
ld_dir ('pubchem/RDF/biosystem' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem');
ld_dir ('pubchem/RDF/conserveddomain' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain');
ld_dir ('pubchem/RDF/gene' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/gene');
ld_dir ('pubchem/RDF/reference' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference');
ld_dir ('pubchem/RDF/source' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/source');
ld_dir ('pubchem/RDF/concept' ,'*.ttl.gz', 'http://rdf.ncbi.nlm.nih.gov/pubchem/concept');
ld_dir ('pubchem/RDF' ,'void.ttl', 'http://rdf.ncbi.nlm.nih.gov/pubchem/void');
rdf_loader_run();
checkpoint;



ld_dir ('base/ontology' ,'*', 'http://bioinfo.iocb.cz/rdf/ontologies');
rdf_loader_run();
checkpoint;
