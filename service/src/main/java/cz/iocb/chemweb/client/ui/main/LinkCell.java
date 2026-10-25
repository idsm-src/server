package cz.iocb.chemweb.client.ui.main;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import cz.iocb.chemweb.shared.services.query.DataGridNode;
import cz.iocb.chemweb.shared.utils.Encode;



/**
 * Cell of a data grid rendering a node as its HTML; a node of a resource is marked so that the resource can be
 * highlighted wherever it occurs. The cell receives the click and key press events, which its users handle to visit the
 * clicked resources.
 */
public class LinkCell extends AbstractCell<DataGridNode>
{
    /**
     * Creates the cell.
     */
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

        String htmlCode = "<div lang=\"en\" "
                + (value.ref != null ? "class=\"BOX-selection BOX-NODE_" + Encode.base32m(value.ref) + "\" " : "")
                + "style=\"-webkit-hyphens: auto; -moz-hyphens: auto; -ms-hyphens: auto; hyphens: auto; min-height: 20px\">"
                + value.html + "</div>";

        sb.append(SafeHtmlUtils.fromTrustedString(htmlCode));
    }
}
