package cz.iocb.load.mona;

import java.util.HashSet;
import java.util.Set;



public class ClassyFire
{
    int id;
    Set<Integer> chebi = new HashSet<Integer>();
    Set<String> mesh = new HashSet<String>();

    public ClassyFire(int id)
    {
        this.id = id;
    }
}
