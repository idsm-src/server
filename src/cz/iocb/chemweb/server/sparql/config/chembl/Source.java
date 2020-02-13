package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.mapping.ConstantIriMapping;
import cz.iocb.chemweb.server.sparql.mapping.NodeMapping;
import cz.iocb.chemweb.server.sparql.mapping.classes.IntegerUserIriClass;
import cz.iocb.chemweb.server.sparql.mapping.classes.UserIriClass;



class Source
{
    static void addIriClasses(ChemblConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("source", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/source/CHEMBL_SRC_"));
    }


    static void addQuadMapping(ChemblConfiguration config)
    {
        UserIriClass source = config.getIriClass("source");
        ConstantIriMapping graph = config.createIriMapping("<http://rdf.ebi.ac.uk/dataset/chembl>");

        String table = "source";
        NodeMapping subject = config.createIriMapping(source, "src_id");

        config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("cco:Source"));

        config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("cco:chemblId"),
                config.createLiteralMapping(xsdString, "('CHEMBL_SRC_' || src_id)"));

        config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("rdfs:label"),
                config.createLiteralMapping(xsdString, "src_short_name"));

        config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("dcterms:description"),
                config.createLiteralMapping(xsdString, "src_description"));
    }
}
