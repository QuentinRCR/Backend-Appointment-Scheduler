package com.docto.protechdoctolib.forgotten_password;

public class ForgottenPasswordRequest {

    String email;

    public String getEmail() {
        return email;
    }

    public ForgottenPasswordRequest(String email) {
        this.email = email;
    }
}
