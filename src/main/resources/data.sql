INSERT INTO RUSER(id, nom, prenom, email, password, phonenumber, user_role, locked, enabled) VALUES(-10, 'NomUser', 'PrenomUser', 'user@gmail.com', '$2a$10$aa924oYBfEalhJtHaBJZued.h0qeyU7p3Bg7fukaRIXSEEnL.ZLSK', '06', 'USER', false, true); --psw user
-- the not encripted password is: string
INSERT INTO RUSER(id, nom, prenom, email, password, phonenumber, user_role, locked, enabled) VALUES(-12, 'REY', 'Quentin', 'quentin.rey@etu.emse.fr', '$2a$10$aa924oYBfEalhJtHaBJZued.h0qeyU7p3Bg7fukaRIXSEEnL.ZLSK', '0656586465', 'USER', false, true); --psw user
-- the not encripted password is: string
INSERT INTO RUSER(id, nom, prenom, email, password, phonenumber, user_role, locked, enabled) VALUES(-13, 'Villeneuve', 'Paul', 'paul.villeneuve@etu.emse.fr', '$2a$10$aa924oYBfEalhJtHaBJZued.h0qeyU7p3Bg7fukaRIXSEEnL.ZLSK', '0656586465', 'USER', false, true); --psw user
-- the not encripted password is: string

INSERT INTO RUSER(id, nom, prenom, email, password, phonenumber, user_role, locked, enabled) VALUES(-9, 'nom', 'prénom', 'utilisateur@gmail.com', 'password', '06', 'USER', false, false);
-- this 2nd user can not be used to log in. He is there for the test only

INSERT INTO RUSER(id, nom, prenom, email, password, phonenumber, user_role, locked, enabled) VALUES(-11, 'NomAdmin', 'PrénomAdmin', 'admin@gmail.com', '$2a$10$4hPNxbIkVY2U6mpUsA/kFek4wuSUF8VuzZ6hc6OKEpO80IdUuSqRG', '06', 'ADMIN', false, true); --psw: admin

INSERT INTO CONFIRMATION_TOKEN(id, token, created_at, expires_at,user_id) VALUES(-5,'abc123','2015-12-20 8:35:20','2015-12-20 8:50:20',-10);
INSERT INTO CONFIRMATION_TOKEN(id, token, created_at, expires_at,user_id) VALUES(-4,'abc1234','2016-10-20 8:35:20','2016-10-20 8:50:20',-12);

