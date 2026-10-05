package cz.iocb.chemweb.server.sparql.config.molmedb;

import static cz.iocb.chemweb.server.sparql.config.molmedb.MolmedbConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInteger;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import static java.util.Arrays.asList;
import java.util.List;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.ExpressionColumn;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.database.ValueColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.SimpleUserIriClass;
import cz.iocb.sparql.engine.rdf.Iri;
import cz.iocb.sparql.engine.request.Request;



public class Substance
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("molmedb:substance", INT4, new DatabaseTable(schema, "substance_bases"),
                new TableColumn("id", INT4), new TableColumn("identifier", VARCHAR), "https://identifiers.org/molmedb/",
                "MM[0-9.]+"));

        config.addIriClass(new MapUserIriClass("molmedb:obsoleted_substance", VARCHAR,
                new DatabaseTable(schema, "obsoleted_substances"), new TableColumn("identifier", VARCHAR),
                new TableColumn("identifier", VARCHAR), "https://identifiers.org/molmedb/", "MM[0-9.]+"));

        String prefix = "https://rdf.molmedb.upol.cz/substance/";

        config.addIriClass(new MapUserIriClass("molmedb:molecular_weight", INT4,
                new DatabaseTable(schema, "substance_bases"), new TableColumn("id", INT4),
                new TableColumn("identifier", VARCHAR), prefix, "MM[0-9.]+", "_Molecular_Weight"));
        config.addIriClass(new MapUserIriClass("molmedb:logp", INT4, new DatabaseTable(schema, "substance_bases"),
                new TableColumn("id", INT4), new TableColumn("identifier", VARCHAR), prefix, "MM[0-9.]+", "_LogP"));
        config.addIriClass(new MapUserIriClass("molmedb:charge", INT4, new DatabaseTable(schema, "substance_bases"),
                new TableColumn("id", INT4), new TableColumn("identifier", VARCHAR), prefix, "MM[0-9.]+", "_charge"));
        config.addIriClass(new MapUserIriClass("molmedb:ph_min", INT4, new DatabaseTable(schema, "substance_bases"),
                new TableColumn("id", INT4), new TableColumn("identifier", VARCHAR), prefix, "MM[0-9.]+", "_pHmin"));
        config.addIriClass(new MapUserIriClass("molmedb:ph_max", INT4, new DatabaseTable(schema, "substance_bases"),
                new TableColumn("id", INT4), new TableColumn("identifier", VARCHAR), prefix, "MM[0-9.]+", "_pHmax"));
        config.addIriClass(new MapUserIriClass("molmedb:inchi", INT4, new DatabaseTable(schema, "substance_bases"),
                new TableColumn("id", INT4), new TableColumn("identifier", VARCHAR), prefix, "MM[0-9.]+", "_InChI"));
        config.addIriClass(new MapUserIriClass("molmedb:smiles", INT4, new DatabaseTable(schema, "substance_bases"),
                new TableColumn("id", INT4), new TableColumn("identifier", VARCHAR), prefix, "MM[0-9.]+", "_SMILES"));
        config.addIriClass(new MapUserIriClass("molmedb:inchikey", INT4, new DatabaseTable(schema, "substance_bases"),
                new TableColumn("id", INT4), new TableColumn("identifier", VARCHAR), prefix, "MM[0-9.]+", "_InChIKey"));

        config.addIriClass(new MapUserIriClass("molmedb:mmdbid", INT4, new DatabaseTable(schema, "substance_bases"),
                new TableColumn("id", INT4), new TableColumn("identifier", VARCHAR), prefix, "MM[0-9.]+", "_MolMeDB"));

        config.addIriClass(new SubstanceIdentifierIriClass("molmedb:pubchem", "_PubChemCID", "[0-9]+"));
        config.addIriClass(new SubstanceIdentifierIriClass("molmedb:chembl", "_", "CHEMBL[0-9]+"));
        config.addIriClass(new SubstanceChebiIdentifierIriClass("molmedb:chebi"));
        config.addIriClass(new SubstanceIdentifierIriClass("molmedb:drugbank", "_", "DB(MET)?[0-9]+"));
        config.addIriClass(new SubstanceIdentifierIriClass("molmedb:pdb", "_PDB", "[A-Z]{3}"));


        config.addIriClass(new SimpleUserIriClass("pdb:chem_comp", VARCHAR)
        {
            @Override
            public int getCheckCost()
            {
                return 0;
            }

            @Override
            public boolean match(Request request, Iri iri)
            {
                return iri.getValue().matches("http://rdf\\.wwpdb\\.org/cc/([A-Z0-9]{3})/chem_comp/\\1");
            }

            @Override
            protected Column generateFunction(Column parameter)
            {
                String par = parameter.toString();
                String code = String.format("'http://rdf.wwpdb.org/cc/' || %s || '/chem_comp/' || %s", par, par);

                return new ExpressionColumn("(" + code + ")::varchar", VARCHAR);
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
                    builder.append("http://rdf\\.wwpdb\\.org/cc/([A-Z0-9]{3})/chem_comp/\\1");
                    builder.append(")$', '') THEN ");
                }

                builder.append(String.format("substring(%s, 39, 3)::varchar", parameter));

                if(check)
                    builder.append(" END");

                return new ExpressionColumn(builder.toString(), VARCHAR);
            }

            @Override
            public List<Column> toColumns(Request request, Iri iri)
            {
                assert match(request, iri);

                String value = iri.getValue();
                String id = value.substring(value.length() - 3);

                return asList(new ValueColumn(id, VARCHAR));
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
            DatabaseTable table = new DatabaseTable(schema, "substance_bases");
            TermMapping subject = config.createIriMapping("molmedb:substance", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0000076"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:molecular_weight", "id"),
                    config.createIsNotNullCondition(table, "molecular_weight"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:logp", "id"), config.createIsNotNullCondition(table, "logp"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:charge", "id"), config.createIsNotNullCondition(table, "charge"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:ph_min", "id"),
                    config.createIsNotNullCondition(table, "ph_start"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:ph_max", "id"), config.createIsNotNullCondition(table, "ph_end"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:inchi", "id"), config.createIsNotNullCondition(table, "inchi"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:smiles", "id"),
                    config.createIsNotNullCondition(table, "canonical_smiles"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:inchikey", "id"),
                    config.createIsNotNullCondition(table, "inchikey"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:mmdbid", "id"));
        }

        // triples map #2
        {
            DatabaseTable table = new DatabaseTable(schema, "substance_bases");
            TermMapping subject = config.createIriMapping("molmedb:substance", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:CHEMINF_000457"),
                    config.createIriMapping("molmedb:substance", "parent_id"));
        }

        // triples map #3
        {
            DatabaseTable table = new DatabaseTable(schema, "substance_bases");
            TermMapping subject = config.createIriMapping("molmedb:substance", "id");
            Conditions cnd = config.createIsNullCondition(table, "parent_id");

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
            DatabaseTable table = new DatabaseTable(schema, "substance_identifiers");
            TermMapping subject = config.createIriMapping("molmedb:substance", "substance_id");

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
            DatabaseTable table = new DatabaseTable(schema, "substance_links");
            TermMapping subject = config.createIriMapping("molmedb:substance", "substance_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("pubchem:compound", "value"),
                    config.createAreEqualCondition("type", "'4'::smallint"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("ontology:chebi", "value"),
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
            DatabaseTable table = new DatabaseTable(schema, "obsoleted_substances");
            TermMapping subject = config.createIriMapping("molmedb:obsoleted_substance", "identifier");

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
            DatabaseTable table = new DatabaseTable(schema, "substance_bases");
            TermMapping subject = config.createIriMapping("molmedb:smiles", "id");
            Conditions cnd = config.createIsNotNullCondition(table, "canonical_smiles");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000018"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "canonical_smiles"));
        }

        // triples map #7
        {
            DatabaseTable table = new DatabaseTable(schema, "substance_bases");
            Conditions cnd = config.createIsNotNullCondition(table, "inchikey");
            TermMapping subject = config.createIriMapping("molmedb:inchikey", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000059"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "inchikey"));
        }

        // triples map #8
        {
            DatabaseTable table = new DatabaseTable(schema, "substance_bases");
            Conditions cnd = config.createIsNotNullCondition(table, "molecular_weight");
            TermMapping subject = config.createIriMapping("molmedb:molecular_weight", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000216"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdFloat, "molecular_weight"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"),
                    config.createIriMapping("obo:UO_0000221"), cnd); // dalton
        }

        // triples map #9
        {
            DatabaseTable table = new DatabaseTable(schema, "substance_bases");
            Conditions cnd = config.createIsNotNullCondition(table, "logp");
            TermMapping subject = config.createIriMapping("molmedb:logp", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000251"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdFloat, "logp"));
        }

        // triples map #10
        {
            DatabaseTable table = new DatabaseTable(schema, "substance_bases");
            TermMapping subject = config.createIriMapping("molmedb:mmdbid", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000571"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "identifier"));
        }

        // triples map #11
        {
            DatabaseTable table = new DatabaseTable(schema, "substance_bases");
            TermMapping subject = config.createIriMapping("molmedb:charge", "id");
            Conditions cnd = config.createIsNotNullCondition(table, "charge");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000268"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdInteger, "charge"));
        }

        // triples map #12
        {
            DatabaseTable table = new DatabaseTable(schema, "substance_bases");
            TermMapping subject = config.createIriMapping("molmedb:ph_min", "id");
            Conditions cnd = config.createIsNotNullCondition(table, "ph_start");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:ProtonationMinPH"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdFloat, "ph_start"));
        }

        // triples map #13
        {
            DatabaseTable table = new DatabaseTable(schema, "substance_bases");
            TermMapping subject = config.createIriMapping("molmedb:ph_max", "id");
            Conditions cnd = config.createIsNotNullCondition(table, "ph_end");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:ProtonationMaxPH"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdFloat, "ph_end"));
        }

        // triples map #14
        {
            DatabaseTable table = new DatabaseTable(schema, "substance_bases");
            TermMapping subject = config.createIriMapping("molmedb:inchi", "id");
            Conditions cnd = config.createIsNotNullCondition(table, "inchi");

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
            DatabaseTable table = new DatabaseTable(schema, "substance_identifiers");
            Conditions cnd = config.createAreEqualCondition("type", "'4'::smallint");
            TermMapping subject = config.createIriMapping("molmedb:pubchem", "substance_id", "value");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000140"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "value"), cnd);
        }

        // triples map #16
        {
            DatabaseTable table = new DatabaseTable(schema, "substance_identifiers");
            Conditions cnd = config.createAreEqualCondition("type", "'5'::smallint");
            TermMapping subject = config.createIriMapping("molmedb:drugbank", "substance_id", "value");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000406"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "value"), cnd);
        }

        // triples map #17
        {
            DatabaseTable table = new DatabaseTable(schema, "substance_identifiers");
            Conditions cnd = config.createAreEqualCondition("type", "'6'::smallint");
            TermMapping subject = config.createIriMapping("molmedb:chebi", "substance_id", "value");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000407"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "value"), cnd);
        }

        // triples map #18
        {
            DatabaseTable table = new DatabaseTable(schema, "substance_identifiers");
            Conditions cnd = config.createAreEqualCondition("type", "'7'::smallint");
            TermMapping subject = config.createIriMapping("molmedb:pdb", "substance_id", "value");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000572"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "value"), cnd);
        }

        // triples map #19
        {
            DatabaseTable table = new DatabaseTable(schema, "substance_identifiers");
            Conditions cnd = config.createAreEqualCondition("type", "'8'::smallint");
            TermMapping subject = config.createIriMapping("molmedb:chembl", "substance_id", "value");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000412"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "value"), cnd);
        }
    }
}
