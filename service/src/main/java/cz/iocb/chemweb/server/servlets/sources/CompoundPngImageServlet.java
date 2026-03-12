package cz.iocb.chemweb.server.servlets.sources;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;



@SuppressWarnings("serial")
public class CompoundPngImageServlet extends CompoundImageServlet
{
    @Override
    public String getServletInfo()
    {
        return "Compound PNG Image Servlet";
    }


    @Override
    protected void generateResponse(HttpServletRequest req, HttpServletResponse res)
            throws IOException, SQLException, CDKException
    {
        int size;

        try
        {
            size = Integer.parseInt(req.getParameter("w"));
        }
        catch(NullPointerException | NumberFormatException e)
        {
            throw new IllegalArgumentException("an invalid value of the 'w' argument");
        }

        if(size < 40 || size > 1600)
            throw new IllegalArgumentException("an invalid value of the 'w' argument");


        Color background = null;

        try
        {
            String backgroundParameter = req.getParameter("background");

            if(backgroundParameter != null)
                background = new Color(Integer.parseInt(backgroundParameter, 16));
            else
                background = new Color(0, 0, 0, 0);
        }
        catch(NumberFormatException e)
        {
            throw new IllegalArgumentException("an invalid value of the 'background' argument");
        }


        String id = req.getParameter("id");

        if(id == null)
            throw new IllegalArgumentException("an invalid value of the 'id' argument");

        IAtomContainer molecule = getMolecule(id);


        res.setContentType("image/png");

        String filename = req.getParameter("filename");

        if(filename != null)
            res.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");


        try(ServletOutputStream out = res.getOutputStream())
        {
            new DepictionGenerator(new Font("Verdana", Font.PLAIN, 18)).withAtomColors().withBackgroundColor(background)
                    .withSize(size, size).withFillToFit().depict(molecule).writeTo(Depiction.PNG_FMT, out);
        }
    }
}
