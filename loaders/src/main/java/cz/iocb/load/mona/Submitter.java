package cz.iocb.load.mona;



/**
 * The submitter of a spectrum in the JSON export of MoNA.
 */
public class Submitter
{
    /**
     * The e-mail address of the submitter.
     */
    String emailAddress;

    /**
     * The first name of the submitter.
     */
    String firstName;

    /**
     * The last name of the submitter.
     */
    String lastName;

    /**
     * The institution of the submitter.
     */
    String institution;


    /**
     * Creates an empty submitter.
     */
    public Submitter()
    {
    }


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


    /**
     * Returns the value of a field; a missing value equals the empty one, as the database stores both as null.
     *
     * @param field the value of the field, or null
     * @return the value of the field, or the empty string if it is missing
     */
    private static String value(String field)
    {
        return field == null ? "" : field;
    }
}
