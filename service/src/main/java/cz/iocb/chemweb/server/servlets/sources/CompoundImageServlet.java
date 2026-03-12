package cz.iocb.chemweb.server.servlets.sources;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IDoubleBondStereochemistry;
import org.openscience.cdk.interfaces.ISingleElectron;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.interfaces.ITetrahedralChirality;
import org.openscience.cdk.io.DefaultChemObjectReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.stereo.ExtendedTetrahedral;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;



@SuppressWarnings("serial")
public abstract class CompoundImageServlet extends SourceServlet
{
    protected static final String V30_HEADER = "M  V30 BEGIN CTAB";

    protected static final ThreadLocal<Aromaticity> aromaticity = new ThreadLocal<Aromaticity>()
    {
        @Override
        protected Aromaticity initialValue()
        {
            return new Aromaticity(Aromaticity.Model.Daylight, Cycles.or(Cycles.all(), Cycles.cdkAromaticSet()));
        }
    };


    protected IAtomContainer getMolecule(String id) throws SQLException, CDKException, IOException
    {
        try(Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(true);

            try(PreparedStatement statement = connection.prepareStatement(access))
            {
                statement.setObject(1, id);
                ResultSet result = statement.executeQuery();

                if(result.next())
                    return crateAtomContainer(result.getString(1));
            }

            throw new NoSuchElementException("invalid structure id");
        }
    }


    protected static IAtomContainer crateAtomContainer(String mol) throws CDKException, IOException
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


        aromaticity.get().apply(molecule);

        removeNonChiralHydrogens(molecule);

        if(StreamSupport.stream(molecule.atoms().spliterator(), false).allMatch(a -> a.getPoint2d() == null))
            new StructureDiagramGenerator().generateCoordinates(molecule);


        return molecule;
    }


    protected static void removeNonChiralHydrogens(IAtomContainer molecule)
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
