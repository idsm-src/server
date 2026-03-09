package cz.iocb.chemweb.server.sparql.config.molmedb;

import static cz.iocb.chemweb.server.sparql.config.molmedb.MolmedbConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInteger;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import static java.util.Arrays.asList;
import java.sql.Statement;
import java.util.List;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.ConstantColumn;
import cz.iocb.sparql.engine.database.ExpressionColumn;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.SimpleUserIriClass;
import cz.iocb.sparql.engine.parser.model.IRI;
import cz.iocb.sparql.engine.parser.model.triple.Node;



public class Substance
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("molmedb:substance", "integer", new Table(schema, "substance_bases"),
                new TableColumn("id"), new TableColumn("identifier"), "https://identifiers.org/molmedb/", "MM[0-9.]+"));

        config.addIriClass(new MapUserIriClass("molmedb:obsoleted_substance", "varchar",
                new Table(schema, "obsoleted_substances"), new TableColumn("identifier"), new TableColumn("identifier"),
                "https://identifiers.org/molmedb/", "MM[0-9.]+"));

        String prefix = "https://rdf.molmedb.upol.cz/substance/";

        config.addIriClass(new MapUserIriClass("molmedb:molecular_weight", "integer",
                new Table(schema, "substance_bases"), new TableColumn("id"), new TableColumn("identifier"), prefix,
                "MM[0-9.]+", "_Molecular_Weight"));
        config.addIriClass(new MapUserIriClass("molmedb:logp", "integer", new Table(schema, "substance_bases"),
                new TableColumn("id"), new TableColumn("identifier"), prefix, "MM[0-9.]+", "_LogP"));
        config.addIriClass(new MapUserIriClass("molmedb:charge", "integer", new Table(schema, "substance_bases"),
                new TableColumn("id"), new TableColumn("identifier"), prefix, "MM[0-9.]+", "_charge"));
        config.addIriClass(new MapUserIriClass("molmedb:ph_min", "integer", new Table(schema, "substance_bases"),
                new TableColumn("id"), new TableColumn("identifier"), prefix, "MM[0-9.]+", "_pHmin"));
        config.addIriClass(new MapUserIriClass("molmedb:ph_max", "integer", new Table(schema, "substance_bases"),
                new TableColumn("id"), new TableColumn("identifier"), prefix, "MM[0-9.]+", "_pHmax"));
        config.addIriClass(new MapUserIriClass("molmedb:inchi", "integer", new Table(schema, "substance_bases"),
                new TableColumn("id"), new TableColumn("identifier"), prefix, "MM[0-9.]+", "_InChI"));
        config.addIriClass(new MapUserIriClass("molmedb:smiles", "integer", new Table(schema, "substance_bases"),
                new TableColumn("id"), new TableColumn("identifier"), prefix, "MM[0-9.]+", "_SMILES"));
        config.addIriClass(new MapUserIriClass("molmedb:inchikey", "integer", new Table(schema, "substance_bases"),
                new TableColumn("id"), new TableColumn("identifier"), prefix, "MM[0-9.]+", "_InChIKey"));

        config.addIriClass(new MapUserIriClass("molmedb:mmdbid", "integer", new Table(schema, "substance_bases"),
                new TableColumn("id"), new TableColumn("identifier"), prefix, "MM[0-9.]+", "_MolMeDB"));

        config.addIriClass(new SubstanceIdentifierIriClass("molmedb:pubchem", "_PubChemCID", "[0-9]+"));
        config.addIriClass(new SubstanceIdentifierIriClass("molmedb:chembl", "_", "CHEMBL[0-9]+"));
        config.addIriClass(new SubstanceChebiIdentifierIriClass("molmedb:chebi"));
        config.addIriClass(new SubstanceIdentifierIriClass("molmedb:drugbank", "_", "DB(MET)?[0-9]+"));
        config.addIriClass(new SubstanceIdentifierIriClass("molmedb:pdb", "_PDB", "[A-Z]{3}"));


        config.addIriClass(new SimpleUserIriClass("pdb:chem_comp", "varchar")
        {
            @Override
            public int getCheckCost()
            {
                return 0;
            }

            @Override
            public boolean match(Statement statement, IRI iri)
            {
                return iri.getValue().matches("http://rdf\\.wwpdb\\.org/cc/([A-Z0-9]{3})/chem_comp/\\1");
            }

            @Override
            protected Column generateFunction(Column parameter)
            {
                String par = parameter.toString();
                String code = String.format("'http://rdf.wwpdb.org/cc/' || %s || '/chem_comp/' || %s", par, par);

                return new ExpressionColumn("(" + code + ")::varchar");
            }

            @Override
            protected Column generateInverseFunction(Column parameter, boolean check)
            {
                StringBuilder builder = new StringBuilder();

                if(check)
                {
                    builder.append("CASE WHEN sparql.regex_string(");
                    builder.append(parameter);
                    builder.append(", '^(");
                    builder.append("https://rdf\\.wwpdb\\.org/cc/([A-Z0-9]{3})/chem_comp/\\1");
                    builder.append(")$', '') THEN ");
                }

                builder.append(String.format("substring(%s, 40, 3)::", parameter));

                if(check)
                    builder.append(" END");

                return new ExpressionColumn(builder.toString());
            }

            @Override
            public List<Column> toColumns(Statement statement, Node node)
            {
                IRI iri = (IRI) node;
                assert match(statement, iri);

                String value = iri.getValue();
                String id = value.substring(value.length() - 3);

                return asList(new ConstantColumn(id, "varchar"));
            }

            @Override
            public List<Column> toOrderColumns(List<Column> columns)
            {
                return columns;
            }

            @Override
            public String getPrefix(List<Column> columns)
            {
                return "http://rdf.wwpdb.org/cc/";
            }
        });
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("<https://rdf.molmedb.upol.cz>");


        /*
         * substance central node
         */

        // triples map #1
        {
            Table table = new Table(schema, "substance_bases");
            NodeMapping subject = config.createIriMapping("molmedb:substance", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0000076"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:molecular_weight", "id"),
                    config.createIsNotNullCondition("molecular_weight"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:logp", "id"), config.createIsNotNullCondition("logp"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:charge", "id"), config.createIsNotNullCondition("charge"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:ph_min", "id"), config.createIsNotNullCondition("ph_start"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:ph_max", "id"), config.createIsNotNullCondition("ph_end"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:inchi", "id"), config.createIsNotNullCondition("inchi"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:smiles", "id"),
                    config.createIsNotNullCondition("canonical_smiles"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:inchikey", "id"), config.createIsNotNullCondition("inchikey"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:mmdbid", "id"));
        }

        // triples map #2
        {
            Table table = new Table(schema, "substance_bases");
            NodeMapping subject = config.createIriMapping("molmedb:substance", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:CHEMINF_000457"),
                    config.createIriMapping("molmedb:substance", "parent_id"));
        }

        // triples map #3
        {
            Table table = new Table(schema, "substance_bases");
            NodeMapping subject = config.createIriMapping("molmedb:substance", "id");
            Conditions cnd = config.createIsNullCondition("parent_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:CHEBI_25367"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:CHEMINF_000457"),
                    config.createIriMapping("molmedb:substance", "id"), cnd);
        }


        /*
         * identifiers and crossreferences
         */

        // triples map #4
        {
            Table table = new Table(schema, "substance_identifiers");
            NodeMapping subject = config.createIriMapping("molmedb:substance", "substance_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "value"),
                    config.createAreEqualCondition("type", "'1'::smallint"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:pubchem", "substance_id", "value"),
                    config.createAreEqualCondition("type", "'4'::smallint"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:drugbank", "substance_id", "value"),
                    config.createAreEqualCondition("type", "'5'::smallint"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:chebi", "substance_id", "value"),
                    config.createAreEqualCondition("type", "'6'::smallint"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:pdb", "substance_id", "value"),
                    config.createAreEqualCondition("type", "'7'::smallint"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:chembl", "substance_id", "value"),
                    config.createAreEqualCondition("type", "'8'::smallint"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("pdb:chem_comp", "value"),
                    config.createAreEqualCondition("type", "'7'::smallint"));
        }

        {
            Table table = new Table(schema, "substance_links");
            NodeMapping subject = config.createIriMapping("molmedb:substance", "substance_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("pubchem:compound", "value"),
                    config.createAreEqualCondition("type", "'4'::smallint"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "value"),
                    config.createAreEqualCondition("type", "'6'::smallint"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("chembl:compound", "value"),
                    config.createAreEqualCondition("type", "'8'::smallint"));
        }


        /*
         * mapping of obsolete substances
         */

        // triples map #5
        {
            Table table = new Table(schema, "obsoleted_substances");
            NodeMapping subject = config.createIriMapping("molmedb:obsoleted_substance", "identifier");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0000076"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:IAO_0100001"),
                    config.createIriMapping("molmedb:substance", "substance_id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:IAO_0000231"),
                    config.createIriMapping("obo:IAO_0000227"));
        }


        /*
         * molecular descriptors
         */

        // triples map #6
        {
            Table table = new Table(schema, "substance_bases");
            NodeMapping subject = config.createIriMapping("molmedb:smiles", "id");
            Conditions cnd = config.createIsNotNullCondition("canonical_smiles");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000018"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "canonical_smiles"));
        }

        // triples map #7
        {
            Table table = new Table(schema, "substance_bases");
            Conditions cnd = config.createIsNotNullCondition("inchikey");
            NodeMapping subject = config.createIriMapping("molmedb:inchikey", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000059"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "inchikey"));
        }

        // triples map #8
        {
            Table table = new Table(schema, "substance_bases");
            Conditions cnd = config.createIsNotNullCondition("molecular_weight");
            NodeMapping subject = config.createIriMapping("molmedb:molecular_weight", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000216"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdFloat, "molecular_weight"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"),
                    config.createIriMapping("obo:UO_0000221"), cnd); // dalton
        }

        // triples map #9
        {
            Table table = new Table(schema, "substance_bases");
            Conditions cnd = config.createIsNotNullCondition("logp");
            NodeMapping subject = config.createIriMapping("molmedb:logp", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000251"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdFloat, "logp"));
        }

        // triples map #10
        {
            Table table = new Table(schema, "substance_bases");
            NodeMapping subject = config.createIriMapping("molmedb:mmdbid", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000571"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "identifier"));
        }

        // triples map #11
        {
            Table table = new Table(schema, "substance_bases");
            NodeMapping subject = config.createIriMapping("molmedb:charge", "id");
            Conditions cnd = config.createIsNotNullCondition("charge");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000268"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdInteger, "charge"));
        }

        // triples map #12
        {
            Table table = new Table(schema, "substance_bases");
            NodeMapping subject = config.createIriMapping("molmedb:ph_min", "id");
            Conditions cnd = config.createIsNotNullCondition("ph_start");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:ProtonationMinPH"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdFloat, "ph_start"));
        }

        // triples map #13
        {
            Table table = new Table(schema, "substance_bases");
            NodeMapping subject = config.createIriMapping("molmedb:ph_max", "id");
            Conditions cnd = config.createIsNotNullCondition("ph_end");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:ProtonationMaxPH"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdFloat, "ph_end"));
        }

        // triples map #14
        {
            Table table = new Table(schema, "substance_bases");
            NodeMapping subject = config.createIriMapping("molmedb:inchi", "id");
            Conditions cnd = config.createIsNotNullCondition("inchi");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000113"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "inchi"));
        }


        /*
         * database crossidentifiers
         */

        // triples map #15
        {
            Table table = new Table(schema, "substance_identifiers");
            Conditions cnd = config.createAreEqualCondition("type", "'4'::smallint");
            NodeMapping subject = config.createIriMapping("molmedb:pubchem", "substance_id", "value");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000140"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "value"), cnd);
        }

        // triples map #16
        {
            Table table = new Table(schema, "substance_identifiers");
            Conditions cnd = config.createAreEqualCondition("type", "'5'::smallint");
            NodeMapping subject = config.createIriMapping("molmedb:drugbank", "substance_id", "value");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000406"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "value"), cnd);
        }

        // triples map #17
        {
            Table table = new Table(schema, "substance_identifiers");
            Conditions cnd = config.createAreEqualCondition("type", "'6'::smallint");
            NodeMapping subject = config.createIriMapping("molmedb:chebi", "substance_id", "value");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000407"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "value"), cnd);
        }

        // triples map #18
        {
            Table table = new Table(schema, "substance_identifiers");
            Conditions cnd = config.createAreEqualCondition("type", "'7'::smallint");
            NodeMapping subject = config.createIriMapping("molmedb:pdb", "substance_id", "value");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000572"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "value"), cnd);
        }

        // triples map #19
        {
            Table table = new Table(schema, "substance_identifiers");
            Conditions cnd = config.createAreEqualCondition("type", "'8'::smallint");
            NodeMapping subject = config.createIriMapping("molmedb:chembl", "substance_id", "value");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000412"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "value"), cnd);
        }
    }
}
