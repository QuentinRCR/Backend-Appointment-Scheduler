package com.docto.protechdoctolib.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Entity
@Table(name="RUser")
public class User implements UserDetails {
    @Id
    @GeneratedValue
    Long Id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private String phonenumber;

    @Column
    private String skypeAccount;

    @Column
    private String campus;

    @Enumerated(EnumType.STRING)
    private UserRole user_role;
    /**
     * La fonctionnalité locked n'est pas utilisé dans le projet ici mais permettrait de bloquer un compte
     * après des tentatives de mdp échouées répétées par exemple
     */
    private Boolean locked= false;
    /**
     * Tant que enabled=false l'utilisateur ne peut pas utiliser son compte
     * Le but est d'activer le compte de l'utilisateur une fois son email validé
     * Le système d'envoi d'email n'est pas encore configuré mais cette fonctionnalité fonctionne
     * via un système de tokens déjà opérationnel
     */
    private Boolean enabled= false;

    public Long getId() {
        return Id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getSkypeAccount() {
        return skypeAccount;
    }

    public void setSkypeAccount(String skypeAccount) {
        this.skypeAccount = skypeAccount;
    }

    public UserRole getUser_role() {
        return user_role;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    /** Collecte les différents rôles existants et demande l'authentification pour ces rôles
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority= new SimpleGrantedAuthority(user_role.name());
        return Collections.singletonList(authority);
    }
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public User(){

    }

    public User( String nom, String prenom, String email, String password, String phonenumber, UserRole user_role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.phonenumber = phonenumber;
        this.user_role = user_role;
    }
}
