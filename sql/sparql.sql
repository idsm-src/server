update DB.DBA.SYS_USERS set U_PASSWORD='neaztaktajneheslo' where U_NAME='SPARQL';
USER_SET_OPTION ('SPARQL', 'DISABLED', 0);
db..user_set_qualifier ('SPARQL', 'DB');


grant execute on DB.DBA.RL_I2ID TO "SPARQL";
grant execute on DB.DBA.RL_I2ID_NP TO "SPARQL";
