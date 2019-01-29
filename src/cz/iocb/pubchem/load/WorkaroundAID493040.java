package cz.iocb.pubchem.load;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import cz.iocb.pubchem.load.common.Loader;



public class WorkaroundAID493040 extends Loader
{
    private static HashMap<Integer, String> loadOutcomesAndReferences() throws SQLException, IOException
    {
        Integer reference = null;

        try(BufferedReader input = getReader("RDF/endpoint/pc_endpoint2reference.ttl.gz"))
        {
            for(String line = input.readLine(); line != null; line = input.readLine())
            {
                if(line.matches("endpoint:SID[0-9]+_AID493040\tcito:citesAsDataSource\t.*"))
                {
                    int id = Integer.parseInt(line.replaceFirst("^.*reference:PMID([0-9]+) \\.$", "$1"));

                    if(reference != null && reference != id)
                        throw new IOException("unexpected reference line");

                    reference = id;
                }
            }
        }


        HashMap<String, Integer> outcomes = new HashMap<String, Integer>();
        outcomes.put("Active", 0);
        outcomes.put("Inactive", 1);


        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(getStream("AID_493040_datatable_all.csv", false))))
        {
            String[] head = reader.readLine().split(",");

            LinkedHashMap<Integer, Integer> groupCols = new LinkedHashMap<Integer, Integer>();
            HashMap<Integer, String> groupNames = new HashMap<Integer, String>();

            int idx = 0;

            for(int column = 0; column < head.length; column++)
            {
                if(head[column].endsWith("_outcome"))
                {
                    int id = idx++;
                    groupCols.put(id, column);
                    groupNames.put(id, head[column].replace("_outcome", ""));
                }
            }


            String line = reader.readLine();

            while(!line.startsWith("1"))
                line = reader.readLine();


            try(Connection connection = Loader.getConnection())
            {
                try(PreparedStatement referenceStatement = connection.prepareStatement("insert into endpoint_references"
                        + "(substance, bioassay, measuregroup, reference) values (?,493040,?,?)"))
                {
                    try(PreparedStatement outcomeStatement = connection.prepareStatement("insert into endpoint_bases"
                            + "(substance, bioassay, measuregroup, outcome_id) values (?,493040,?,?)"))
                    {
                        do
                        {
                            String[] items = line.split(",");

                            outcomeStatement.setInt(1, Integer.parseInt(items[1]));
                            referenceStatement.setInt(1, Integer.parseInt(items[1]));

                            for(Entry<Integer, Integer> group : groupCols.entrySet())
                            {
                                if(group.getValue() < items.length && !items[group.getValue()].isEmpty())
                                {
                                    outcomeStatement.setInt(2, group.getKey());
                                    outcomeStatement.setInt(3, outcomes.get(items[group.getValue()]));
                                    outcomeStatement.addBatch();

                                    referenceStatement.setInt(2, group.getKey());
                                    referenceStatement.setInt(3, reference);
                                    referenceStatement.addBatch();
                                }
                            }
                        }
                        while((line = reader.readLine()) != null);

                        outcomeStatement.executeBatch();
                    }

                    referenceStatement.executeBatch();
                }
            }

            return groupNames;
        }
    }


    private static void loadSourcesAndTitles(HashMap<Integer, String> groupNames) throws IOException, SQLException
    {
        String source = null;
        String title = null;

        try(BufferedReader input = getReader("RDF/measuregroup/pc_measuregroup_source.ttl.gz"))
        {
            for(String line = input.readLine(); line != null; line = input.readLine())
            {
                if(line.startsWith("measuregroup:AID493040\t"))
                {
                    if(source != null)
                        throw new IOException("unexpected source line");

                    source = line.replaceFirst("^.*source:(.+) \\.$", "$1");
                }
            }
        }

        try(BufferedReader input = getReader("RDF/measuregroup/pc_measuregroup_title.ttl.gz"))
        {
            for(String line = input.readLine(); line != null; line = input.readLine())
            {
                if(line.startsWith("measuregroup:AID493040\t"))
                {
                    if(title != null)
                        throw new IOException("unexpected title line");

                    title = line.replaceFirst("^.*\"(.+)\"@en \\.$", "$1");
                }
            }
        }


        try(Connection connection = Loader.getConnection())
        {
            Integer sourceId = null;

            try(PreparedStatement statement = connection.prepareStatement("select id from source_bases where iri = ?"))
            {
                statement.setString(1, "http://rdf.ncbi.nlm.nih.gov/pubchem/source/" + source);

                try(ResultSet result = statement.executeQuery())
                {
                    if(result.next())
                        sourceId = result.getInt(1);
                }
            }


            try(PreparedStatement statement = connection.prepareStatement(
                    "insert into measuregroup_bases(bioassay, measuregroup, source, title) values (493040,?,?,?)"))
            {
                for(Entry<Integer, String> entry : groupNames.entrySet())
                {
                    statement.setInt(1, entry.getKey());
                    statement.setInt(2, sourceId);
                    statement.setString(3, title + "(" + entry.getValue() + ")");
                    statement.addBatch();
                }

                statement.executeBatch();
            }
        }
    }


    private static void loadGenes(HashMap<Integer, String> groupNames) throws SQLException, IOException
    {
        List<Integer> linkedGeneIds = new LinkedList<Integer>();

        try(BufferedReader input = getReader("RDF/measuregroup/pc_measuregroup2protein.ttl.gz"))
        {
            for(String line = input.readLine(); line != null; line = input.readLine())
                if(line.startsWith("measuregroup:AID493040\t"))
                    linkedGeneIds.add(Integer.parseInt((line.replaceFirst("^.*gene:GID([0-9]+) \\.$", "$1"))));
        }


        try(Connection connection = Loader.getConnection())
        {
            LinkedHashMap<Integer, List<String>> linkedGeneNames = new LinkedHashMap<Integer, List<String>>();

            try(PreparedStatement statement = connection.prepareStatement("select title from gene_bases where id = ? "
                    + "union all select alternative from gene_alternatives where gene = ?"))
            {
                for(int linkedGeneId : linkedGeneIds)
                {
                    statement.setInt(1, linkedGeneId);
                    statement.setInt(2, linkedGeneId);
                    ResultSet result = statement.executeQuery();

                    List<String> names = new LinkedList<String>();
                    linkedGeneNames.put(linkedGeneId, names);

                    while(result.next())
                        names.add(result.getString(1));
                }
            }


            try(PreparedStatement statement = connection.prepareStatement(
                    "insert into measuregroup_genes(bioassay, measuregroup, gene) values (493040,?,?)"))
            {
                for(Entry<Integer, String> groupEntry : groupNames.entrySet())
                {
                    String geneName = groupEntry.getValue();

                    if(geneName.equals("KIAA1811"))
                        geneName = "BRSK1";
                    else if(geneName.equals("SGK"))
                        geneName = "SGK2";

                    int geneId = -1;

                    for(Entry<Integer, List<String>> entry : linkedGeneNames.entrySet())
                    {
                        if(entry.getValue().contains(geneName))
                        {
                            if(geneId != -1)
                                System.out.println("conflict for gene " + geneName);

                            if(geneId == -1 || entry.getValue().indexOf(geneName) == 0)
                                geneId = entry.getKey();
                        }
                    }

                    if(geneId == -1)
                        System.out.println("cannot find gene " + geneName);

                    if(geneId != -1)
                    {
                        statement.setInt(1, groupEntry.getKey());
                        statement.setInt(2, geneId);
                        statement.addBatch();
                    }
                }

                statement.executeBatch();
            }
        }
    }


    public static void load() throws IOException, SQLException
    {
        HashMap<Integer, String> groupNames = loadOutcomesAndReferences();
        loadSourcesAndTitles(groupNames);
        loadGenes(groupNames);
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        load();
    }
}
