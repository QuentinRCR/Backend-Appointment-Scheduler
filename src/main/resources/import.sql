INSERT INTO CRENEAUX(id,date_Debut, date_Fin) VALUES(2000,{ts '2022-10-10 00:00:00.00'}, {ts '2022-10-22 23:59:59.99'});
INSERT INTO CRENEAUX(id,date_Debut, date_Fin) VALUES(1000,{ts '2022-11-22 00:00:00.00'}, {ts '2022-11-30 23:59:59.99'});

INSERT INTO HEURES_DEBUT_FIN (id,ID_CRENEAUX ,TEMPS_DEBUT , TEMPS_FIN ) VALUES(1001,1000,{ts '1900-01-01 08:00:00.00'}, {ts '1900-01-01 12:00:00.00'});
INSERT INTO HEURES_DEBUT_FIN (id,ID_CRENEAUX ,TEMPS_DEBUT , TEMPS_FIN ) VALUES(1002,1000,{ts '1900-01-01 14:00:00.00'}, {ts '1900-01-01 18:00:00.00'});
INSERT INTO HEURES_DEBUT_FIN (id,ID_CRENEAUX ,TEMPS_DEBUT , TEMPS_FIN ) VALUES(1003,2000,{ts '1900-01-01 09:00:00.00'}, {ts '1900-01-01 10:00:00.00'});
INSERT INTO HEURES_DEBUT_FIN (id,ID_CRENEAUX ,TEMPS_DEBUT , TEMPS_FIN ) VALUES(1004,2000,{ts '1900-01-01 14:00:00.00'}, {ts '1900-01-01 20:00:00.00'});

INSERT INTO CRENEAUX_JOURS(CRENEAUX_ID ,JOURS ) VALUES(2000,'MONDAY');
INSERT INTO CRENEAUX_JOURS(CRENEAUX_ID ,JOURS ) VALUES(2000,'TUESDAY');
INSERT INTO CRENEAUX_JOURS(CRENEAUX_ID ,JOURS ) VALUES(1000,'FRIDAY');