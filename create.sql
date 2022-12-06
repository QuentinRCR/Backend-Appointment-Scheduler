create sequence hibernate_sequence start with 1 increment by 1;
create table confirmation_token (id bigint not null, confirmed_at timestamp, created_at timestamp not null, expires_at timestamp not null, token varchar(255) not null, user_id bigint not null, primary key (id));
create table creneaux (id bigint not null, date_debut date, date_fin date, primary key (id));
create table creneaux_jours (creneaux_id bigint not null, jours varchar(255));
create table heures_debut_fin (id_plage bigint not null, id_creneaux bigint, temps_debut time, temps_fin time, primary key (id_plage));
create table rendez_vous (id bigint not null, date_debut timestamp, duree bigint, id_creneau bigint, id_user bigint, moyen_communication varchar(255), zoom_link varchar(255), primary key (id));
create table ruser (id bigint not null, campus varchar(255), email varchar(255) not null, enabled boolean, locked boolean, nom varchar(255) not null, password varchar(255) not null, phonenumber varchar(255), prenom varchar(255) not null, skype_account varchar(255), user_role varchar(255), primary key (id));
alter table confirmation_token add constraint FKj6rn2x6ifxqcafh3lby7jxc0u foreign key (user_id) references ruser;
alter table creneaux_jours add constraint FKpv75dr2ofiir86qxsd6u07w3k foreign key (creneaux_id) references creneaux;
create sequence hibernate_sequence start with 1 increment by 1;
create table confirmation_token (id bigint not null, confirmed_at timestamp, created_at timestamp not null, expires_at timestamp not null, token varchar(255) not null, user_id bigint not null, primary key (id));
create table creneaux (id bigint not null, date_debut date, date_fin date, primary key (id));
create table creneaux_jours (creneaux_id bigint not null, jours varchar(255));
create table heures_debut_fin (id_plage bigint not null, id_creneaux bigint, temps_debut time, temps_fin time, primary key (id_plage));
create table rendez_vous (id bigint not null, date_debut timestamp, duree bigint, id_creneau bigint, id_user bigint, moyen_communication varchar(255), zoom_link varchar(255), primary key (id));
create table ruser (id bigint not null, campus varchar(255), email varchar(255) not null, enabled boolean, locked boolean, nom varchar(255) not null, password varchar(255) not null, phonenumber varchar(255), prenom varchar(255) not null, skype_account varchar(255), user_role varchar(255), primary key (id));
alter table confirmation_token add constraint FKj6rn2x6ifxqcafh3lby7jxc0u foreign key (user_id) references ruser;
alter table creneaux_jours add constraint FKpv75dr2ofiir86qxsd6u07w3k foreign key (creneaux_id) references creneaux;
create sequence hibernate_sequence start with 1 increment by 1;
create table confirmation_token (id bigint not null, confirmed_at timestamp, created_at timestamp not null, expires_at timestamp not null, token varchar(255) not null, user_id bigint not null, primary key (id));
create table creneaux (id bigint not null, date_debut date, date_fin date, primary key (id));
create table creneaux_jours (creneaux_id bigint not null, jours varchar(255));
create table heures_debut_fin (id_plage bigint not null, id_creneaux bigint, temps_debut time, temps_fin time, primary key (id_plage));
create table rendez_vous (id bigint not null, date_debut timestamp, duree bigint, id_creneau bigint, id_user bigint, moyen_communication varchar(255), zoom_link varchar(255), primary key (id));
create table ruser (id bigint not null, campus varchar(255), email varchar(255) not null, enabled boolean, locked boolean, nom varchar(255) not null, password varchar(255) not null, phonenumber varchar(255), prenom varchar(255) not null, skype_account varchar(255), user_role varchar(255), primary key (id));
alter table confirmation_token add constraint FKj6rn2x6ifxqcafh3lby7jxc0u foreign key (user_id) references ruser;
alter table creneaux_jours add constraint FKpv75dr2ofiir86qxsd6u07w3k foreign key (creneaux_id) references creneaux;
create sequence hibernate_sequence start with 1 increment by 1;
create table confirmation_token (id bigint not null, confirmed_at timestamp, created_at timestamp not null, expires_at timestamp not null, token varchar(255) not null, user_id bigint not null, primary key (id));
create table creneaux (id bigint not null, date_debut date, date_fin date, primary key (id));
create table creneaux_jours (creneaux_id bigint not null, jours varchar(255));
create table heures_debut_fin (id_plage bigint not null, id_creneaux bigint, temps_debut time, temps_fin time, primary key (id_plage));
create table rendez_vous (id bigint not null, date_debut timestamp, duree bigint, id_creneau bigint, id_user bigint, moyen_communication varchar(255), zoom_link varchar(255), primary key (id));
create table ruser (id bigint not null, campus varchar(255), email varchar(255) not null, enabled boolean, locked boolean, nom varchar(255) not null, password varchar(255) not null, phonenumber varchar(255), prenom varchar(255) not null, skype_account varchar(255), user_role varchar(255), primary key (id));
alter table confirmation_token add constraint FKj6rn2x6ifxqcafh3lby7jxc0u foreign key (user_id) references ruser;
alter table creneaux_jours add constraint FKpv75dr2ofiir86qxsd6u07w3k foreign key (creneaux_id) references creneaux;
create sequence hibernate_sequence start with 1 increment by 1;
create table confirmation_token (id bigint not null, confirmed_at timestamp, created_at timestamp not null, expires_at timestamp not null, token varchar(255) not null, user_id bigint not null, primary key (id));
create table creneaux (id bigint not null, date_debut date, date_fin date, primary key (id));
create table creneaux_jours (creneaux_id bigint not null, jours varchar(255));
create table heures_debut_fin (id_plage bigint not null, id_creneaux bigint, temps_debut time, temps_fin time, primary key (id_plage));
create table rendez_vous (id bigint not null, date_debut timestamp, duree bigint, id_creneau bigint, id_user bigint, moyen_communication varchar(255), zoom_link varchar(255), primary key (id));
create table ruser (id bigint not null, campus varchar(255), email varchar(255) not null, enabled boolean, locked boolean, nom varchar(255) not null, password varchar(255) not null, phonenumber varchar(255), prenom varchar(255) not null, skype_account varchar(255), user_role varchar(255), primary key (id));
alter table confirmation_token add constraint FKj6rn2x6ifxqcafh3lby7jxc0u foreign key (user_id) references ruser;
alter table creneaux_jours add constraint FKpv75dr2ofiir86qxsd6u07w3k foreign key (creneaux_id) references creneaux;
