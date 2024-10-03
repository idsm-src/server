package cz.iocb.chemweb.server.sparql.config.matchms;



public class Spectrum
{
    final private float[] value;

    Spectrum(float[] value)
    {
        this.value = value;
    }

    public static Spectrum valueOf(String literal)
    {
        String spaces = "(( |\\f|\\n|\\r|\\t|\\v)*)";
        String decimal = "(" + spaces + "(([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([eE][+-]?[0-9]+)?)" + spaces + ")";
        String p1 = "(" + spaces + "\\[" + decimal + "," + decimal + "\\]" + spaces + ")";
        String v1 = "(" + spaces + "\\[" + "((" + p1 + ",)*" + p1 + ")?|" + spaces + "\\]" + spaces + ")";
        String p2 = "(" + spaces + "\\{" + decimal + "," + decimal + "\\}" + spaces + ")";
        String v2 = "(" + spaces + "\\{" + "((" + p2 + ",)*" + p2 + ")?|" + spaces + "\\}" + spaces + ")";
        String p3 = decimal + ":" + decimal;
        String v3 = "(((" + p3 + "( |\\f|\\n|\\r|\\t|\\v|,|;)" + ")*" + p3 + ")?|" + spaces + ")";
        String patern = v1 + "|" + v2 + "|" + v3;

        if(!literal.matches(patern))
            return null;

        String[] parts = literal.replaceAll("([{},;: ]|\\[|\\])+", " ").trim().split(" ");

        if(parts.length == 1)
            return new Spectrum(new float[0]);

        float[] value = new float[parts.length];

        for(int i = 0; i < parts.length; i++)
        {
            value[i] = Float.valueOf(parts[i].trim());

            if(!Float.isFinite(value[i]))
                return null;
        }

        return new Spectrum(value);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < value.length / 2; i++)
        {
            if(i > 0)
                builder.append(" ");

            builder.append(value[2 * i + 0]);
            builder.append(":");
            builder.append(value[2 * i + 1]);
        }

        return builder.toString();
    }
}
