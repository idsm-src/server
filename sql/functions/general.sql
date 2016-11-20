create function gz_decompress(in value varchar) returns varchar
{
    declare str_out any;
    str_out := string_output();
    gz_uncompress(value, str_out);
    result (string_output_string (str_out));
};

grant execute on gz_decompress to "SPARQL";
