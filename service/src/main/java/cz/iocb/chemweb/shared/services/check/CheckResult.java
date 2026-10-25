package cz.iocb.chemweb.shared.services.check;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



/**
 * Result of the check of a query: the messages of the checker.
 */
public class CheckResult implements Serializable
{
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Messages of the checker.
     */
    public List<CheckerWarning> warnings = new ArrayList<>();

    /**
     * Creates an empty result.
     */
    public CheckResult()
    {
    }
}
