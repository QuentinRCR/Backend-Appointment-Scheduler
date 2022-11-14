INSERT INTO RUSER(id, nom, prenom, email, password, phonenumber, user_role, locked, enabled) VALUES(-10, 'nom', 'prénom', 'user@gmail.com', '$2a$10$W53fx0JwQ8Seyfyg6ienc.18pFWzkok3CuK45mxNGBHe.etcZ6TfS', 00, 'USER', false, true);
-- the not encripted password is: spring

INSERT INTO RUSER(id, nom, prenom, email, password, phonenumber, user_role, locked) VALUES(-9, 'nom', 'prénom', 'utilisateur@gmail.com', 'password', 00, 'USER', false);
-- this 2nd user can not be used to log in. He is there for the test only

INSERT INTO CONFIRMATION_TOKEN(id, token, created_at, expires_at,user_id) VALUES(-5,'abc123','2100-11-15 8:35:20','2100-11-15 8:50:20',-10);

