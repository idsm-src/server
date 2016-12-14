log_enable(2);

--============================================================================--

create table descriptor_compound_hydrogen_bond_acceptor_counts
(
    compound                        integer not null,
    hydrogen_bond_acceptor_count    smallint not null,
    primary key(compound)
);


insert into descriptor_compound_hydrogen_bond_acceptor_counts(compound, hydrogen_bond_acceptor_count)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    cast(hydrogen_bond_acceptor_count as smallint)
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000388;
           sio:has-value ?hydrogen_bond_acceptor_count
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_tautomer_counts
(
    compound          integer not null,
    tautomer_count    smallint not null,
    primary key(compound)
);


insert into descriptor_compound_tautomer_counts(compound, tautomer_count)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    cast(tautomer_count as smallint)
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000391;
           sio:has-value ?tautomer_count
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_defined_atom_stereo_counts
(
    compound                     integer not null,
    defined_atom_stereo_count    smallint not null,
    primary key(compound)
);


insert into descriptor_compound_defined_atom_stereo_counts(compound, defined_atom_stereo_count)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    cast(defined_atom_stereo_count as smallint)
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000370;
           sio:has-value ?defined_atom_stereo_count
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_defined_bond_stereo_counts
(
    compound                     integer not null,
    defined_bond_stereo_count    smallint not null,
    primary key(compound)
);


insert into descriptor_compound_defined_bond_stereo_counts(compound, defined_bond_stereo_count)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    cast(defined_bond_stereo_count as smallint)
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000371;
           sio:has-value ?defined_bond_stereo_count
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_undefined_bond_stereo_counts
(
    compound                       integer not null,
    undefined_bond_stereo_count    smallint not null,
    primary key(compound)
);


insert into descriptor_compound_undefined_bond_stereo_counts(compound, undefined_bond_stereo_count)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    cast(undefined_bond_stereo_count as smallint)
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000375;
           sio:has-value ?undefined_bond_stereo_count
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_isotope_atom_counts
(
    compound              integer not null,
    isotope_atom_count    smallint not null,
    primary key(compound)
);


insert into descriptor_compound_isotope_atom_counts(compound, isotope_atom_count)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    cast(isotope_atom_count as smallint)
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000372;
           sio:has-value ?isotope_atom_count
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_covalent_unit_counts
(
    compound               integer not null,
    covalent_unit_count    smallint not null,
    primary key(compound)
);


insert into descriptor_compound_covalent_unit_counts(compound, covalent_unit_count)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    cast(covalent_unit_count as smallint)
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000369;
           sio:has-value ?covalent_unit_count
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_hydrogen_bond_donor_counts
(
    compound                     integer not null,
    hydrogen_bond_donor_count    smallint not null,
    primary key(compound)
);


insert into descriptor_compound_hydrogen_bond_donor_counts(compound, hydrogen_bond_donor_count)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    cast(hydrogen_bond_donor_count as smallint)
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000387;
           sio:has-value ?hydrogen_bond_donor_count
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_non_hydrogen_atom_counts
(
    compound                   integer not null,
    non_hydrogen_atom_count    smallint not null,
    primary key(compound)
);


insert into descriptor_compound_non_hydrogen_atom_counts(compound, non_hydrogen_atom_count)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    cast(non_hydrogen_atom_count as smallint)
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000373;
           sio:has-value ?non_hydrogen_atom_count
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_rotatable_bond_counts
(
    compound                integer not null,
    rotatable_bond_count    smallint not null,
    primary key(compound)
);


insert into descriptor_compound_rotatable_bond_counts(compound, rotatable_bond_count)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    cast(rotatable_bond_count as smallint)
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000389;
           sio:has-value ?rotatable_bond_count
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_undefined_atom_stereo_counts
(
    compound                       integer not null,
    undefined_atom_stereo_count    smallint not null,
    primary key(compound)
);


insert into descriptor_compound_undefined_atom_stereo_counts(compound, undefined_atom_stereo_count)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    cast(undefined_atom_stereo_count as smallint)
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000374;
           sio:has-value ?undefined_atom_stereo_count
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_total_formal_charges
(
    compound               integer not null,
    total_formal_charge    smallint not null,
    primary key(compound)
);


insert into descriptor_compound_total_formal_charges(compound, total_formal_charge)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    cast(total_formal_charge as smallint)
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000336;
           sio:has-value ?total_formal_charge
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_structure_complexities
(
    compound                integer not null,
    structure_complexity    float not null,
    primary key(compound)
);


insert into descriptor_compound_structure_complexities(compound, structure_complexity)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    structure_complexity
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000390;
           sio:has-value ?structure_complexity
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_mono_isotopic_weights
(
    compound                integer not null,
    mono_isotopic_weight    float not null,
    primary key(compound)
);


insert into descriptor_compound_mono_isotopic_weights(compound, mono_isotopic_weight)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    mono_isotopic_weight
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000337;
           sio:has-value ?mono_isotopic_weight
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_xlogp3_aas
(
    compound     integer not null,
    xlogp3_aa    float not null,
    primary key(compound)
);


insert into descriptor_compound_xlogp3_aas(compound, xlogp3_aa)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    xlogp3_aa
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000395;
           sio:has-value ?xlogp3_aa
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_exact_masses
(
    compound      integer not null,
    exact_mass    float not null,
    primary key(compound)
);


insert into descriptor_compound_exact_masses(compound, exact_mass)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    exact_mass
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000338;
           sio:has-value ?exact_mass
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_molecular_weights
(
    compound            integer not null,
    molecular_weight    float not null,
    primary key(compound)
);


insert into descriptor_compound_molecular_weights(compound, molecular_weight)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    molecular_weight
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000334;
           sio:has-value ?molecular_weight
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_tpsas
(
    compound    integer not null,
    tpsa        float not null,
    primary key(compound)
);


insert into descriptor_compound_tpsas(compound, tpsa)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    tpsa
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000392;
           sio:has-value ?tpsa
    }
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_ids
(
    compound    integer not null,
    primary key(compound)
);


insert replacing descriptor_compound_ids(compound)
select compound from (
    select compound from descriptor_compound_hydrogen_bond_acceptor_counts union all
    select compound from descriptor_compound_tautomer_counts union all
    select compound from descriptor_compound_defined_atom_stereo_counts union all
    select compound from descriptor_compound_defined_bond_stereo_counts union all
    select compound from descriptor_compound_undefined_bond_stereo_counts union all
    select compound from descriptor_compound_isotope_atom_counts union all
    select compound from descriptor_compound_covalent_unit_counts union all
    select compound from descriptor_compound_hydrogen_bond_donor_counts union all
    select compound from descriptor_compound_non_hydrogen_atom_counts union all
    select compound from descriptor_compound_rotatable_bond_counts union all
    select compound from descriptor_compound_undefined_atom_stereo_counts union all
    select compound from descriptor_compound_total_formal_charges union all
    select compound from descriptor_compound_structure_complexities union all
    select compound from descriptor_compound_mono_isotopic_weights union all
    select compound from descriptor_compound_xlogp3_aas union all
    select compound from descriptor_compound_exact_masses union all
    select compound from descriptor_compound_molecular_weights union all
    select compound from descriptor_compound_tpsas
) as tbl;

--------------------------------------------------------------------------------

create table descriptor_compound_bases
(
    compound                        integer not null,
    hydrogen_bond_acceptor_count    smallint,
    tautomer_count                  smallint,
    defined_atom_stereo_count       smallint,
    defined_bond_stereo_count       smallint,
    undefined_bond_stereo_count     smallint,
    isotope_atom_count              smallint,
    covalent_unit_count             smallint,
    hydrogen_bond_donor_count       smallint,
    non_hydrogen_atom_count         smallint,
    rotatable_bond_count            smallint,
    undefined_atom_stereo_count     smallint,
    total_formal_charge             smallint,
    structure_complexity            float,
    mono_isotopic_weight            float,
    xlogp3_aa                       float,
    exact_mass                      float,
    molecular_weight                float,
    tpsa                            float,
    primary key(compound)
);


insert into descriptor_compound_bases (
    compound,
    hydrogen_bond_acceptor_count,
    tautomer_count,
    defined_atom_stereo_count,
    defined_bond_stereo_count,
    undefined_bond_stereo_count,
    isotope_atom_count,
    covalent_unit_count,
    hydrogen_bond_donor_count,
    non_hydrogen_atom_count,
    rotatable_bond_count,
    undefined_atom_stereo_count,
    total_formal_charge,
    structure_complexity,
    mono_isotopic_weight,
    xlogp3_aa,
    exact_mass,
    molecular_weight,
    tpsa)
select
    descriptor_compound_ids.compound,
    descriptor_compound_hydrogen_bond_acceptor_counts.hydrogen_bond_acceptor_count,
    descriptor_compound_tautomer_counts.tautomer_count,
    descriptor_compound_defined_atom_stereo_counts.defined_atom_stereo_count,
    descriptor_compound_defined_bond_stereo_counts.defined_bond_stereo_count,
    descriptor_compound_undefined_bond_stereo_counts.undefined_bond_stereo_count,
    descriptor_compound_isotope_atom_counts.isotope_atom_count,
    descriptor_compound_covalent_unit_counts.covalent_unit_count,
    descriptor_compound_hydrogen_bond_donor_counts.hydrogen_bond_donor_count,
    descriptor_compound_non_hydrogen_atom_counts.non_hydrogen_atom_count,
    descriptor_compound_rotatable_bond_counts.rotatable_bond_count,
    descriptor_compound_undefined_atom_stereo_counts.undefined_atom_stereo_count,
    descriptor_compound_total_formal_charges.total_formal_charge,
    descriptor_compound_structure_complexities.structure_complexity,
    descriptor_compound_mono_isotopic_weights.mono_isotopic_weight,
    descriptor_compound_xlogp3_aas.xlogp3_aa,
    descriptor_compound_exact_masses.exact_mass,
    descriptor_compound_molecular_weights.molecular_weight,
    descriptor_compound_tpsas.tpsa
from
    descriptor_compound_ids
    left join descriptor_compound_hydrogen_bond_acceptor_counts on descriptor_compound_hydrogen_bond_acceptor_counts.compound=descriptor_compound_ids.compound
    left join descriptor_compound_tautomer_counts on descriptor_compound_tautomer_counts.compound=descriptor_compound_ids.compound
    left join descriptor_compound_defined_atom_stereo_counts on descriptor_compound_defined_atom_stereo_counts.compound=descriptor_compound_ids.compound
    left join descriptor_compound_defined_bond_stereo_counts on descriptor_compound_defined_bond_stereo_counts.compound=descriptor_compound_ids.compound
    left join descriptor_compound_undefined_bond_stereo_counts on descriptor_compound_undefined_bond_stereo_counts.compound=descriptor_compound_ids.compound
    left join descriptor_compound_isotope_atom_counts on descriptor_compound_isotope_atom_counts.compound=descriptor_compound_ids.compound
    left join descriptor_compound_covalent_unit_counts on descriptor_compound_covalent_unit_counts.compound=descriptor_compound_ids.compound
    left join descriptor_compound_hydrogen_bond_donor_counts on descriptor_compound_hydrogen_bond_donor_counts.compound=descriptor_compound_ids.compound
    left join descriptor_compound_non_hydrogen_atom_counts on descriptor_compound_non_hydrogen_atom_counts.compound=descriptor_compound_ids.compound
    left join descriptor_compound_rotatable_bond_counts on descriptor_compound_rotatable_bond_counts.compound=descriptor_compound_ids.compound
    left join descriptor_compound_undefined_atom_stereo_counts on descriptor_compound_undefined_atom_stereo_counts.compound=descriptor_compound_ids.compound
    left join descriptor_compound_total_formal_charges on descriptor_compound_total_formal_charges.compound=descriptor_compound_ids.compound
    left join descriptor_compound_structure_complexities on descriptor_compound_structure_complexities.compound=descriptor_compound_ids.compound
    left join descriptor_compound_mono_isotopic_weights on descriptor_compound_mono_isotopic_weights.compound=descriptor_compound_ids.compound
    left join descriptor_compound_xlogp3_aas on descriptor_compound_xlogp3_aas.compound=descriptor_compound_ids.compound
    left join descriptor_compound_exact_masses on descriptor_compound_exact_masses.compound=descriptor_compound_ids.compound
    left join descriptor_compound_molecular_weights on descriptor_compound_molecular_weights.compound=descriptor_compound_ids.compound
    left join descriptor_compound_tpsas on descriptor_compound_tpsas.compound=descriptor_compound_ids.compound;


drop table descriptor_compound_ids;
drop table descriptor_compound_hydrogen_bond_acceptor_counts;
drop table descriptor_compound_tautomer_counts;
drop table descriptor_compound_defined_atom_stereo_counts;
drop table descriptor_compound_defined_bond_stereo_counts;
drop table descriptor_compound_undefined_bond_stereo_counts;
drop table descriptor_compound_isotope_atom_counts;
drop table descriptor_compound_covalent_unit_counts;
drop table descriptor_compound_hydrogen_bond_donor_counts;
drop table descriptor_compound_non_hydrogen_atom_counts;
drop table descriptor_compound_rotatable_bond_counts;
drop table descriptor_compound_undefined_atom_stereo_counts;
drop table descriptor_compound_total_formal_charges;
drop table descriptor_compound_structure_complexities;
drop table descriptor_compound_mono_isotopic_weights;
drop table descriptor_compound_xlogp3_aas;
drop table descriptor_compound_exact_masses;
drop table descriptor_compound_molecular_weights;
drop table descriptor_compound_tpsas;


create bitmap index descriptor_compound_bases__hydrogen_bond_acceptor_count on descriptor_compound_bases(hydrogen_bond_acceptor_count);
create bitmap index descriptor_compound_bases__tautomer_count on descriptor_compound_bases(tautomer_count);
create bitmap index descriptor_compound_bases__defined_atom_stereo_count on descriptor_compound_bases(defined_atom_stereo_count);
create bitmap index descriptor_compound_bases__defined_bond_stereo_count on descriptor_compound_bases(defined_bond_stereo_count);
create bitmap index descriptor_compound_bases__undefined_bond_stereo_count on descriptor_compound_bases(undefined_bond_stereo_count);
create bitmap index descriptor_compound_bases__isotope_atom_count on descriptor_compound_bases(isotope_atom_count);
create bitmap index descriptor_compound_bases__covalent_unit_count on descriptor_compound_bases(covalent_unit_count);
create bitmap index descriptor_compound_bases__hydrogen_bond_donor_count on descriptor_compound_bases(hydrogen_bond_donor_count);
create bitmap index descriptor_compound_bases__non_hydrogen_atom_count on descriptor_compound_bases(non_hydrogen_atom_count);
create bitmap index descriptor_compound_bases__rotatable_bond_count on descriptor_compound_bases(rotatable_bond_count);
create bitmap index descriptor_compound_bases__undefined_atom_stereo_count on descriptor_compound_bases(undefined_atom_stereo_count);
create bitmap index descriptor_compound_bases__total_formal_charge on descriptor_compound_bases(total_formal_charge);
create index descriptor_compound_bases__structure_complexity on descriptor_compound_bases(structure_complexity);
create index descriptor_compound_bases__mono_isotopic_weight on descriptor_compound_bases(mono_isotopic_weight);
-- create index descriptor_compound_bases__xlogp3_aa on descriptor_compound_bases(xlogp3_aa); -- crash
create index descriptor_compound_bases__exact_mass on descriptor_compound_bases(exact_mass);
create index descriptor_compound_bases__molecular_weight on descriptor_compound_bases(molecular_weight);
create index descriptor_compound_bases__tpsa on descriptor_compound_bases(tpsa);
grant select on descriptor_compound_bases to "SPARQL";

--============================================================================--

create table descriptor_compound_molecular_formulas
(
    compound             integer not null,
    molecular_formula    varchar not null,
    primary key(compound)
);


insert into descriptor_compound_molecular_formulas(compound, molecular_formula)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    molecular_formula
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000335;
           sio:has-value ?molecular_formula
    }
) as tbl;


create index descriptor_compound_molecular_formulas__molecular_formula on descriptor_compound_molecular_formulas(molecular_formula);
grant select on descriptor_compound_molecular_formulas to "SPARQL";

--============================================================================--

create table descriptor_compound_isomeric_smileses
(
    compound           integer not null,
    isomeric_smiles    varchar not null,
    primary key(compound)
);


insert into descriptor_compound_isomeric_smileses(compound, isomeric_smiles)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    isomeric_smiles
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000379;
           sio:has-value ?isomeric_smiles
    }
) as tbl;


-- create index descriptor_compound_isomeric_smileses__isomeric_smiles on descriptor_compound_isomeric_smileses(isomeric_smiles); -- too long key
grant select on descriptor_compound_isomeric_smileses to "SPARQL";

--============================================================================--

create table descriptor_compound_canonical_smileses
(
    compound            integer not null,
    canonical_smiles    varchar not null,
    primary key(compound)
);


insert into descriptor_compound_canonical_smileses(compound, canonical_smiles)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    canonical_smiles
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000376;
           sio:has-value ?canonical_smiles
    }
) as tbl;


-- create index descriptor_compound_canonical_smileses__canonical_smiles on descriptor_compound_canonical_smileses(canonical_smiles);
grant select on descriptor_compound_canonical_smileses to "SPARQL";

--============================================================================--

create table descriptor_compound_iupac_inchis
(
    compound       integer not null,
    iupac_inchi    varchar not null,
    primary key(compound)
);


insert into descriptor_compound_iupac_inchis(compound, iupac_inchi)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    iupac_inchi
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000396;
           sio:has-value ?iupac_inchi.

        filter(strlen(?iupac_inchi) < 2048)
    }
) as tbl;


-- create index descriptor_compound_iupac_inchis__iupac_inchi on descriptor_compound_iupac_inchis(iupac_inchi);
grant select on descriptor_compound_iupac_inchis to "SPARQL";

--------------------------------------------------------------------------------

create table descriptor_compound_iupac_inchis_long
(
    compound       integer not null,
    iupac_inchi    long varchar not null,
    primary key(compound)
);


insert into descriptor_compound_iupac_inchis_long(compound, iupac_inchi)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    iupac_inchi
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000396;
           sio:has-value ?iupac_inchi.

        filter(strlen(?iupac_inchi) >= 2048)
    }
) as tbl;


grant select on descriptor_compound_iupac_inchis_long to "SPARQL";

--============================================================================--

create table descriptor_compound_preferred_iupac_names
(
    compound                integer not null,
    preferred_iupac_name    nvarchar not null,
    primary key(compound)
);


insert into descriptor_compound_preferred_iupac_names(compound, preferred_iupac_name)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    cast(preferred_iupac_name as nvarchar)
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000382;
           sio:has-value ?preferred_iupac_name

        filter(strlen(?preferred_iupac_name) < 2048)
    }
) as tbl;


-- create index descriptor_compound_preferred_iupac_names__iupac_name on descriptor_compound_preferred_iupac_names(preferred_iupac_name);
grant select on descriptor_compound_preferred_iupac_names to "SPARQL";

--------------------------------------------------------------------------------

create table descriptor_compound_preferred_iupac_names_long
(
    compound                integer not null,
    preferred_iupac_name    long nvarchar not null,
    primary key(compound)
);


insert into descriptor_compound_preferred_iupac_names_long(compound, preferred_iupac_name)
select
    sprintf_inverse(S, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID%d_%U', 0)[0] as compound,
    cast(preferred_iupac_name as nvarchar)
from
(
    sparql select * from descriptor:compound where
    {
        ?S rdf:type sio:CHEMINF_000382;
           sio:has-value ?preferred_iupac_name

        filter(strlen(?preferred_iupac_name) >= 2048)
    }
) as tbl;


grant select on descriptor_compound_preferred_iupac_names_long to "SPARQL";
