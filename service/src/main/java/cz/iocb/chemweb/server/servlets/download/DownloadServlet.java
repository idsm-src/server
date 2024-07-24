package cz.iocb.chemweb.server.servlets.download;

import java.rmi.RemoteException;
import java.util.Base64;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class DownloadServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, java.io.IOException
    {
        try
        {
            processRequest(req, res);
        }
        catch(Exception e)
        {
            throw new ServletException(e);
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, java.io.IOException
    {
        try
        {
            processRequest(req, res);
        }
        catch(Exception e)
        {
            throw new ServletException(e);
        }
    }


    protected void processRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException, NumberFormatException, RemoteException
    {
        ServletOutputStream out = res.getOutputStream();

        String call = req.getParameter("call");
        String encoding = req.getParameter("encoding");

        if(call.equals("saveFile"))
        {
            String imagedata = req.getParameter("data");
            String filename = req.getParameter("filename");
            String contentType = req.getParameter("mimetype");


            byte[] rawdata = null;

            if(encoding.equals("base64"))
                rawdata = Base64.getDecoder().decode(imagedata.replaceAll("(\\r|\\n)", ""));
            else
                rawdata = imagedata.getBytes();

            res.setContentType(contentType);

            res.setHeader("Content-Description", "File Transfer");

            res.setHeader("Content-Type", "application/octet-stream");
            res.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
            res.setHeader("Content-Transfer-Encoding", "binary");

            res.setHeader("X-Frame-Options", "SAMEORIGIN");

            res.setHeader("Expires", "0");
            res.setHeader("Cache-Control", "must-revalidate");
            res.setHeader("Pragma", "public");

            /*
            header('Last-Modified: '.date('r'));
            header('Accept-Ranges: bytes');
            header('Content-Length: '.strlen($output));
             */

            out.write(rawdata);
        }


        out.close();
    }



    @Override
    public String getServletInfo()
    {
        return this.getClass().getName();
    }


    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
    }


    @Override
    public void destroy()
    {

    }
}
