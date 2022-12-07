package com.docto.protechdoctolib.forgotten_password;

public class PasswordReinitializationRequest {

    String email;

    public String getEmail() {
        return email;
    }

    public PasswordReinitializationRequest(String email) {
        this.email = email;
    }
}
