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

        if(emailAddress == null && other.emailAddress != null
                || emailAddress != null && !emailAddress.equals(other.emailAddress))
            return false;

        if(firstName == null && other.firstName != null || firstName != null && !firstName.equals(other.firstName))
            return false;

        if(lastName == null && other.lastName != null || lastName != null && !lastName.equals(other.lastName))
            return false;

        if(institution == null && other.institution != null
                || institution != null && !institution.equals(other.institution))
            return false;

        return true;
    }


    @Override
    public int hashCode()
    {
        return lastName.hashCode();
    }
}
