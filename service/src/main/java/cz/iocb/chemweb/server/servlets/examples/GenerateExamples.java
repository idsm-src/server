package cz.iocb.chemweb.server.servlets.examples;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class GenerateExamples extends HttpServlet
{
    private static final long serialVersionUID = 1L;



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        processRequest(req, res);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        processRequest(req, res);
    }


    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        ServletOutputStream out = res.getOutputStream();

        //out.print("var examples = [{\"description\": \"ABC\",\"code\": \"codeABC\"},{\"description\": \"DEF\",\"code\": \"codeDEF\"},{\"description\": \"GHI\",\"code\": \"codeGHI\"}];");

        out.print("var examples = [");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try
        {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            Document dom = db.parse(req.getServletContext().getResourceAsStream("/examples.xml"));


            Element docEle = dom.getDocumentElement();


            NodeList nl = docEle.getElementsByTagName("example");
            boolean first = true;

            if(nl != null && nl.getLength() > 0)
            {
                for(int i = 0; i < nl.getLength(); i++)
                {

                    Element el = (Element) nl.item(i);

                    String description = getTextValue(el, "description");
                    String code = getTextValue(el, "code");

                    if(first)
                        first = false;
                    else
                        out.print(",");

                    out.print("{\"description\": \"");
                    out.print(description);
                    out.print("\",\"code\": \"");
                    out.print(code);
                    out.print("\"}");
                }
            }


        }
        catch(ParserConfigurationException | SAXException | IOException e)
        {
            throw new ServletException(e);
        }

        out.print("];");
        out.close();
    }


    private String getTextValue(Element ele, String tagName)
    {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);

        if(nl != null && nl.getLength() > 0)
        {
            Element el = (Element) nl.item(0);

            textVal = el.getFirstChild() == null ? "" : el.getFirstChild().getNodeValue();
        }

        return textVal.trim().replace("\n", "\\n").replace("\"", "\\\"");
    }
}
