package cz.iocb.chemweb.shared.services.check;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



public class CheckResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    public List<CheckerWarning> warnings = new ArrayList<CheckerWarning>();
}
