package cz.iocb.chemweb.server.servlets.sources;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;



@SuppressWarnings("serial")
public class CompoundSvgImageServlet extends CompoundImageServlet
{
    @Override
    public String getServletInfo()
    {
        return "Compound SVG Image Servlet";
    }


    @Override
    protected void generateResponse(HttpServletRequest req, HttpServletResponse res)
            throws IOException, SQLException, CDKException
    {
        String id = req.getParameter("id");

        if(id == null)
            throw new IllegalArgumentException("an invalid value of the 'id' argument");

        IAtomContainer molecule = getMolecule(id);


        res.setContentType("image/svg+xml");

        String filename = req.getParameter("filename");

        if(filename != null)
            res.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");


        try(ServletOutputStream out = res.getOutputStream())
        {
            String svg = new DepictionGenerator().withAtomColors().depict(molecule).toSvgStr();
            out.write(svg.getBytes());
        }
    }
}
