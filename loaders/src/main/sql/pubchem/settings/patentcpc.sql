create index patentcpcs__type on pubchem.patentcpcs(type);
create index patentcpcs__level on pubchem.patentcpcs(level);
create index patentcpcs__symbol on pubchem.patentcpcs(symbol);
create index patentcpcs__title on pubchem.patentcpcs(title);
create index patentcpcs__concordant_ipc on pubchem.patentcpcs(concordant_ipc);
grant select on pubchem.patentcpcs to sparql;

--------------------------------------------------------------------------------

create index patentcpc_broaders__patentcpc on pubchem.patentcpc_broaders(patentcpc);
create index patentcpc_broaders__broader on pubchem.patentcpc_broaders(broader);
grant select on pubchem.patentcpc_broaders to sparql;

--------------------------------------------------------------------------------

create index patentcpc_modified_dates__patentcpc on pubchem.patentcpc_modified_dates(patentcpc);
create index patentcpc_modified_dates__date on pubchem.patentcpc_modified_dates(date);
grant select on pubchem.patentcpc_modified_dates to sparql;
