-- run as dba

rdf_obj_ft_rule_del(null, null, 'ALL');

user_create('rdf', 'password');
user_set_option('rdf', 'PRIMARY_GROUP', 'dba');
user_set_qualifier ('rdf', 'DB');
