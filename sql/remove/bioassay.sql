drop function bioassay(id in integer);
drop function bioassay_inverse(iri in varchar);
drop function bioassay_data(bioassay in integer, type in integer);
drop function bioassay_data_inv1(iri in varchar);
drop function bioassay_data_inv2(iri in varchar);

--------------------------------------------------------------------------------

drop table bioassay_measuregroups;
drop table bioassay_data;
drop table bioassay_bases;
