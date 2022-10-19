package com.docto.protechdoctolib.registration;


public class RegistrationRequest {
    private final String nom;
    private final String prenom;
    private final String email;
    private final String password;

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public RegistrationRequest(String nom, String prenom, String email, String password) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
    }




}
