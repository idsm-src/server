-- bioassay
create text index on bioassay_bases(title);
create text index on bioassay_data(value) clustered with (bioassay, type);
