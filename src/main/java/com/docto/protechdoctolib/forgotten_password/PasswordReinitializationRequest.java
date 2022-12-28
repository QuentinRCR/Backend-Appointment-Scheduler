package com.docto.protechdoctolib.forgotten_password;

/**
 * Model pour la requête de réinitialisation du mot de passe
 */
public class PasswordReinitializationRequest {

    String password;
    String token;

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public PasswordReinitializationRequest(String password, String token) {
        this.password = password;
        this.token=token;
    }
}
