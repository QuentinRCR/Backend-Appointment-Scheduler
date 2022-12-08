package com.docto.protechdoctolib.forgotten_password;

public class ForgottenPasswordRequest {

    String password;
    String token;

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public ForgottenPasswordRequest(String password, String token) {
        this.password = password;
        this.token=token;
    }
}
