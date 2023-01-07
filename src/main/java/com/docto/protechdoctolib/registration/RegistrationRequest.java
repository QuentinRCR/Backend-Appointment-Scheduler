package com.docto.protechdoctolib.registration;

/** Données à saisir par l'utilisateur lors de l'inscription */
public class RegistrationRequest {
    private final String nom;
    private final String prenom;
    private final String email;
    private final String password;

    private final String phonenumber;

    private final String skypeAccount;

    private final String campus;

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

    public String getPhonenumber() { return phonenumber; }

    public String getSkypeAccount() { return skypeAccount; }

    public String getCampus() {
        return campus;
    }

    public RegistrationRequest(String nom, String prenom, String email, String password, String phonenumber, String skypeAccount, String campus) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.phonenumber = phonenumber;
        this.skypeAccount = skypeAccount;
        this.campus = campus;
    }




}
