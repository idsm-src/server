package cz.iocb.chemweb.server.servlets.sources;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.imageio.ImageIO;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.vecmath.Point2d;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.geometry.cip.CIPTool;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IDoubleBondStereochemistry;
import org.openscience.cdk.interfaces.ISingleElectron;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.interfaces.ITetrahedralChirality;
import org.openscience.cdk.io.DefaultChemObjectReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.SymbolVisibility;
import org.openscience.cdk.renderer.color.CDK2DAtomColors;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.standard.StandardGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.stereo.ExtendedTetrahedral;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;



@SuppressWarnings("serial")
public class CompoundImageServlet extends HttpServlet
{
    private static final String V30_HEADER = "M  V30 BEGIN CTAB";

    private DataSource connectionPool;
    private String query;


    @Override
    public void init(ServletConfig config) throws ServletException
    {
        String resourceName = config.getInitParameter("resource");
        String schema = config.getInitParameter("schema");
        String table = config.getInitParameter("table");
        String id = config.getInitParameter("id");
        String structure = config.getInitParameter("structure");

        if(resourceName == null || resourceName.isEmpty())
            throw new ServletException("Resource name is not set");

        if(id == null)
            id = "id";

        if(structure == null)
            structure = "molfile";

        query = "select \"" + structure.replace("\"", "\"\"") + "\" from \"" + schema.replace("\"", "\"\"") + "\".\""
                + table.replace("\"", "\"\"") + "\" where \"" + id.replace("\"", "\"\"") + "\" = ?";

        try
        {
            Context context = (Context) (new InitialContext()).lookup("java:comp/env");
            connectionPool = (DataSource) context.lookup(resourceName);
        }
        catch(NamingException e)
        {
            throw new ServletException(e);
        }
    }


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


    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        try
        {
            int id;
            int size;
            Color background = null;


            try
            {
                id = Integer.parseInt(req.getParameter("id"));
            }
            catch(NullPointerException | NumberFormatException e)
            {
                throw new IllegalArgumentException("an invalid value of the 'id' argument");
            }


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


            IAtomContainer molecule = getMolecule(id);

            if(molecule == null)
                throw new NoSuchElementException("invalid structure id");


            BufferedImage bufferedImage = generateImage(molecule, size, size, background);


            res.setContentType("image/png");

            String filename = req.getParameter("filename");

            if(filename != null)
                res.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");

            try(ServletOutputStream out = res.getOutputStream())
            {
                ImageIO.write(bufferedImage, "png", out);
            }
        }
        catch(NoSuchElementException e)
        {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
        catch(IllegalArgumentException e)
        {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
        catch(SQLException e)
        {
            res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, e.getMessage());
        }
        catch(Throwable e)
        {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    @Override
    public String getServletInfo()
    {
        return "Compound Image Servlet";
    }


    public static BufferedImage generateImage(IAtomContainer molecule, int hsize, int vsize, Color background)
            throws IOException, CDKException
    {
        List<IGenerator<IAtomContainer>> generators;
        generators = new ArrayList<IGenerator<IAtomContainer>>();
        generators.add(new BasicSceneGenerator());
        generators.add(new StandardGenerator(new Font("Verdana", Font.PLAIN, 18)));

        AtomContainerRenderer renderer = new AtomContainerRenderer(generators, new AWTFontManager());
        RendererModel model = renderer.getRenderer2DModel();

        model.set(StandardGenerator.AtomColor.class, new CDK2DAtomColors());
        model.set(StandardGenerator.AnnotationColor.class, new Color(0x455FFF));
        model.set(BasicSceneGenerator.Scale.class, 0.5);
        model.set(StandardGenerator.Visibility.class, SymbolVisibility.iupacRecommendationsWithoutTerminalCarbon());
        model.set(StandardGenerator.Visibility.class, SymbolVisibility.iupacRecommendationsWithoutTerminalCarbon());

        Rectangle2D bounds = new Rectangle2D.Double(0, 0, hsize, vsize);
        BufferedImage bufferedImage = new BufferedImage(hsize, vsize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setBackground(background);
        graphics.setColor(background);
        graphics.fillRect(0, 0, hsize, hsize);

        renderer.paint(molecule, new AWTDrawVisitor(graphics), bounds, true);
        bufferedImage.flush();

        return bufferedImage;
    }


    private IAtomContainer getMolecule(int id) throws SQLException, CDKException, IOException
    {
        try(Connection connection = connectionPool.getConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement(query))
            {
                statement.setInt(1, id);
                ResultSet result = statement.executeQuery();

                if(result.next())
                    return crateAtomContainer(result.getString(1));
            }

            return null;
        }
    }


    private static IAtomContainer crateAtomContainer(String mol) throws CDKException, IOException
    {
        IAtomContainer molecule = null;

        if(!mol.contains("\n"))
        {
            SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());

            try
            {
                molecule = sp.parseSmiles(mol);
            }
            catch(InvalidSmilesException e)
            {
                sp.kekulise(false);
                molecule = sp.parseSmiles(mol);
            }

            StructureDiagramGenerator sdg = new StructureDiagramGenerator();
            sdg.setMolecule(molecule);
            sdg.generateCoordinates();
            molecule = sdg.getMolecule();
        }
        else
        {
            try(DefaultChemObjectReader reader = mol.contains(V30_HEADER) ? new MDLV3000Reader() : new MDLV2000Reader())
            {
                reader.setReader(new StringReader(mol));
                molecule = reader.read(new AtomContainer());
            }
        }


        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);


        CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(molecule.getBuilder());

        for(IAtom atom : molecule.atoms())
        {
            if(atom.getImplicitHydrogenCount() == null)
            {
                IAtomType type = matcher.findMatchingAtomType(molecule, atom);
                AtomTypeManipulator.configure(atom, type);
                CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
                adder.addImplicitHydrogens(molecule, atom);
            }
        }


        ElectronDonation model = ElectronDonation.cdkAllowingExocyclic();
        CycleFinder cycles = Cycles.cdkAromaticSet();
        Aromaticity aromaticity = new Aromaticity(model, cycles);
        aromaticity.apply(molecule);


        CIPTool.label(molecule);
        removeNonChiralHydrogens(molecule);


        for(IAtom a : molecule.atoms())
        {
            if(a.getPoint2d() == null)
            {
                for(IAtom atom : molecule.atoms())
                    atom.setPoint2d(new Point2d(atom.getPoint3d().x, atom.getPoint3d().y));

                break;
            }
        }


        for(IAtom atom : molecule.atoms())
            atom.setProperty(StandardGenerator.ANNOTATION_LABEL, getCipLabel(atom));
        for(IBond bond : molecule.bonds())
            bond.setProperty(StandardGenerator.ANNOTATION_LABEL, getCipLabel(bond));


        return molecule;
    }


    private static String getCipLabel(IChemObject chemObj)
    {
        String label = chemObj.getProperty(CDKConstants.CIP_DESCRIPTOR);

        if(label == null || label.equals("NONE"))
            return null;

        return StandardGenerator.ITALIC_DISPLAY_PREFIX + label;
    }


    private static void removeNonChiralHydrogens(IAtomContainer molecule)
    {
        List<IAtom> remove = new ArrayList<IAtom>();

        for(IAtom atom : molecule.atoms())
        {
            // is not hydrogen
            if(atom.getAtomicNumber() != 1)
                continue;

            // is hydrogen ion
            if(atom.getFormalCharge() != null && atom.getFormalCharge() != 0)
                continue;

            // is hydrogen isotope
            if(atom.getMassNumber() != null && atom.getMassNumber() != 1)
                continue;

            // is dihydrogen with implicit H
            if(atom.getImplicitHydrogenCount() != null && atom.getImplicitHydrogenCount() != 0)
                continue;

            List<IAtom> neighbors = molecule.getConnectedAtomsList(atom);

            // single hydrogen
            if(neighbors.size() == 0)
                continue;

            // is multivalent hydrogen
            if(neighbors.size() > 1)
                continue;

            // is dihydrogen
            if(neighbors.size() == 1 && neighbors.get(0).getAtomicNumber() <= 1)
                continue;

            remove.add(atom);
        }


        for(@SuppressWarnings("rawtypes")
        IStereoElement stereoElement : molecule.stereoElements())
        {
            if(stereoElement instanceof ITetrahedralChirality)
            {
                ITetrahedralChirality chirality = (ITetrahedralChirality) stereoElement;

                for(IAtom atom : chirality.getLigands())
                    if(atom.getAtomicNumber() == 1)
                        remove.remove(atom);
            }
            else if(stereoElement instanceof ExtendedTetrahedral)
            {
                ExtendedTetrahedral chirality = (ExtendedTetrahedral) stereoElement;

                for(IAtom atom : chirality.peripherals())
                    if(atom.getAtomicNumber() == 1)
                        remove.remove(atom);
            }
            else if(stereoElement instanceof IDoubleBondStereochemistry)
            {
                IDoubleBondStereochemistry bond = (IDoubleBondStereochemistry) stereoElement;

                for(IAtom endAtom : bond.getStereoBond().atoms())
                    for(IAtom atom : molecule.getConnectedAtomsList(endAtom))
                        remove.remove(atom);
            }
        }


        for(ISingleElectron electron : molecule.singleElectrons())
            remove.remove(electron.getAtom());


        for(IAtom removedH : remove)
            for(IAtom atom : molecule.getConnectedAtomsList(removedH))
                atom.setImplicitHydrogenCount(atom.getImplicitHydrogenCount() + 1);


        for(IAtom removedH : remove)
            molecule.removeAtom(removedH);
    }
}
