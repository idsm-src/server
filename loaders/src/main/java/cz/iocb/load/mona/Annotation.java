package cz.iocb.load.mona;



/**
 * An annotation of a peak of a spectrum in the JSON export of MoNA.
 */
public class Annotation
{
    /**
     * The category of the annotation.
     */
    String category;

    /**
     * Whether the annotation is computed.
     */
    boolean computed;

    /**
     * Whether the annotation is hidden.
     */
    boolean hidden;

    /**
     * The text of the annotation.
     */
    String name;

    /**
     * The m/z of the annotated peak.
     */
    float value;


    /**
     * Creates an empty annotation.
     */
    public Annotation()
    {
    }


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
