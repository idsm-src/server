package cz.iocb.chemweb.server.sparql.config.matchms;

import cz.iocb.sparql.engine.database.UserType;
import cz.iocb.sparql.engine.mapping.datatypes.UserDatatype;
import cz.iocb.sparql.engine.rdf.Iri;



/**
 * Datatype {@code ms:spectrum} of mass spectra, stored as {@code pgms.spectrum}; the canonical form is the one the type
 * prints, the peaks sorted by m/z as {@code mz:intensity} pairs separated by spaces.
 */
public class SpectrumDatatype extends UserDatatype
{
    /**
     * Creates the datatype.
     */
    public SpectrumDatatype()
    {
        super(new Iri(Matchms.ms + "spectrum"), "spectrum", new UserType("pgms.spectrum"));
    }


    @Override
    public boolean isValidForm(String value)
    {
        return Spectrum.valueOf(value) != null;
    }


    @Override
    public String getCanonicalLexicalForm(String value)
    {
        Spectrum spectrum = Spectrum.valueOf(value);

        return spectrum != null ? spectrum.toString() : null;
    }
}
