package com.docto.protechdoctolib.user;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Permet de lier la base de donnée à la representation dans java de l'objet User
 */
@Qualifier("users")
@Repository
@Transactional(readOnly=true)
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Renvoie l'utilisateur correspondant à l'email envoyé
     * @param email
     * @return l'utilisateur correspondant à l'email envoyé
     */
    Optional<User> findByEmail(String email);


    /** Active le compte de l'utilisateur quand celui-ci va fait une requête GET à l'API
     * avec le token de confirmation qu'il reçoit lorsqu'il poste une requête d'inscription.
     */
    @Transactional
    @Modifying
    @Query("UPDATE User u " +
            "SET u.enabled = TRUE WHERE u.email = ?1")
    int enableUser(String email);

    /**
     * Renvoie tous les utilisateurs qui ont le role fournis en paramètre
     * @param ROLE
     * @return tous les utilisateurs qui ont le role fournis en paramètre
     */
    @Query("select u from User u where  u.user_role=:ROLE")
    List<User> findByRole(@Param("ROLE") UserRole ROLE);

    /**
     * Renvoie tous les utilisateurs par ordre alphabétique de nom de famille
     * @return tous les utilisateurs par ordre alphabétique de nom de famille
     */
    @Query("select u from User u order by lower(u.nom) asc")
    List<User> findAll();

}
