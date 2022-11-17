package com.docto.protechdoctolib.user;

import javax.persistence.*;

public class UserDTO {
    private Long Id;

    private String lastName;

    private String firstName;

    private String email;

    private String phoneNumber;

    private String skypeAccount;

    private UserRole user_role;

    private String campus;

    public UserDTO() {
    }

    public UserDTO(User user) {
        Id = user.getId();
        this.lastName = user.getNom();
        this.firstName = user.getPrenom();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhonenumber();
        this.user_role = user.getUser_role();
        this.skypeAccount = user.getSkypeAccount();
        this.campus = user.getCampus();
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSkypeAccount() {
        return skypeAccount;
    }

    public void setSkypeAccount(String skypeAccount) {
        this.skypeAccount = skypeAccount;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public UserRole getUser_role() {
        return user_role;
    }
}
