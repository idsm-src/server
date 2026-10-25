package cz.iocb.load.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;



/**
 * Collects the problems of the loaded data. An error is a part of the data that the loader cannot load, such as an
 * unknown predicate, class or file: the load goes on, so that all the errors are reported at once, but the data
 * would be incomplete, so they are rolled back at the end. A warning is a problem that the loader has dealt with,
 * such as a repaired IRI, and does not prevent the commit. The first occurrences of each kind are printed at once,
 * the further ones are only counted, and the summary gives the numbers of all the kinds.
 */
public final class Problems
{
    /**
     * Number of the first occurrences of each kind that are printed.
     */
    private static final int maxPrinted = 10;

    /**
     * Numbers of the reported errors by their kinds, in the order of their first occurrence.
     */
    private static final LinkedHashMap<String, Integer> errors = new LinkedHashMap<>();

    /**
     * Numbers of the reported warnings by their kinds, in the order of their first occurrence.
     */
    private static final LinkedHashMap<String, Integer> warnings = new LinkedHashMap<>();


    /**
     * Not instantiable.
     */
    private Problems()
    {
    }


    /**
     * Reports a part of the data that cannot be loaded; the detail, which may be null, identifies the occurrence.
     *
     * @param kind the kind of the error
     * @param detail the occurrence of the error, or null
     */
    public static synchronized void error(String kind, String detail)
    {
        report("error", errors, kind, detail);
    }


    /**
     * Reports a problem of the data that the loader has dealt with.
     *
     * @param kind the kind of the warning
     * @param detail the occurrence of the warning, or null
     */
    public static synchronized void warning(String kind, String detail)
    {
        report("warning", warnings, kind, detail);
    }


    /**
     * Counts an occurrence of a problem and prints it if it is among the first occurrences of its kind.
     *
     * @param severity the severity printed with the problem
     * @param counts the numbers of the problems of the severity by their kinds
     * @param kind the kind of the problem
     * @param detail the occurrence of the problem, or null
     */
    private static void report(String severity, Map<String, Integer> counts, String kind, String detail)
    {
        int count = counts.merge(kind, 1, Integer::sum);

        if(count <= maxPrinted)
            System.out.println("    " + severity + ": " + kind + (detail == null ? "" : ": " + detail));
        else if(count == maxPrinted + 1)
            System.out.println("    " + severity + ": " + kind + ": further occurrences are only counted");
    }


    /**
     * Tests whether an error has been reported, i.e. whether the loaded data are incomplete.
     *
     * @return true if an error has been reported
     */
    public static synchronized boolean hasErrors()
    {
        return !errors.isEmpty();
    }


    /**
     * Tests whether a problem, an error or a warning, has been reported.
     *
     * @return true if an error or a warning has been reported
     */
    public static synchronized boolean hasProblems()
    {
        return !errors.isEmpty() || !warnings.isEmpty();
    }


    /**
     * Prints the numbers of the errors and the warnings of each kind.
     */
    public static synchronized void printSummary()
    {
        print("errors", errors);
        print("warnings", warnings);
        System.out.println();
    }


    /**
     * Prints the numbers of the problems of one severity by their kinds.
     *
     * @param name the name of the problems of the severity
     * @param counts the numbers of the problems by their kinds
     */
    private static void print(String name, Map<String, Integer> counts)
    {
        System.out.println(counts.isEmpty() ? "no " + name : name + ":");

        for(Entry<String, Integer> entry : counts.entrySet())
            System.out.println("    " + entry.getKey() + ": " + entry.getValue());
    }
}
