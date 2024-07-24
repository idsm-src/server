package cz.iocb.chemweb.client.ui.main;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import cz.iocb.chemweb.shared.services.query.DataGridNode;
import cz.iocb.chemweb.shared.utils.Encode;



public class LinkCell extends AbstractCell<DataGridNode>
{
    public LinkCell()
    {
        super("click", "keypress");
    }


    @Override
    public void render(Context context, DataGridNode value, SafeHtmlBuilder sb)
    {
        if(value == null)
        {
            return;
        }


        String htmlCode = value.html;

        if(value.ref != null)
            htmlCode = "<div class=\"BOX-selection BOX-NODE_" + Encode.base32m(value.ref) + "\">" + htmlCode + "</div>";

        sb.append(SafeHtmlUtils.fromTrustedString(htmlCode));
    }
}
