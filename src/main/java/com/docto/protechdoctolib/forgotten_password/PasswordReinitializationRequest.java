package com.docto.protechdoctolib.forgotten_password;

public class PasswordReinitializationRequest {

    String password;

    public String getPassword() {
        return password;
    }

    public PasswordReinitializationRequest(String password) {
        this.password = password;
    }
}
