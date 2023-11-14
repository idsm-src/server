package cz.iocb.load.mona;



public class Annotation
{
    String category;
    boolean computed;
    boolean hidden;
    String name;
    float value;


    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
            return true;

        if(obj == null || obj.getClass() != this.getClass())
            return false;

        Annotation other = (Annotation) obj;

        if(!name.equals(other.name))
            return false;

        if(value != other.value)
            return false;

        return true;
    }


    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
