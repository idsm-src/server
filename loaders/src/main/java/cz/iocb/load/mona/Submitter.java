package cz.iocb.load.mona;



public class Submitter
{
    String emailAddress;
    String firstName;
    String lastName;
    String institution;


    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
            return true;

        if(obj == null || obj.getClass() != this.getClass())
            return false;

        Submitter other = (Submitter) obj;

        return value(emailAddress).equals(value(other.emailAddress)) && value(firstName).equals(value(other.firstName))
                && value(lastName).equals(value(other.lastName)) && value(institution).equals(value(other.institution));
    }


    @Override
    public int hashCode()
    {
        return value(lastName).hashCode();
    }


    /*
     * Returns the value of a field; a missing value equals the empty one, as the database stores both as null.
     */
    private static String value(String field)
    {
        return field == null ? "" : field;
    }
}
