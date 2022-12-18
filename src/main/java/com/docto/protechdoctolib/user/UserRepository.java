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

@Qualifier("users")
@Repository
@Transactional(readOnly=true)
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    /** Active le compte de l'utilisateur quande celui ci va faire une requête GET à l'API
     * avec le token de confirmation qu'il reçoit lorsqu'il poste une reqête d'inscription
     * Cette reqûete GET se fera par la suite en cliquant sur un lien de confirmation d'email
     * mais le système d'envoi de mail n'est pas envore configuré
     */

    @Transactional
    @Modifying
    @Query("UPDATE User u " +
            "SET u.enabled = TRUE WHERE u.email = ?1")
    int enableUser(String email);

    @Query("select u from User u where  u.user_role=:ROLE")
    List<User> findByRole(@Param("ROLE") UserRole ROLE);

    @Query("select u from User u order by lower(u.nom) asc")
    List<User> findAll();

}
