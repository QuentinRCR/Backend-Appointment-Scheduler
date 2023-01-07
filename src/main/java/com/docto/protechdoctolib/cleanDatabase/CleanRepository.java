package com.docto.protechdoctolib.cleanDatabase;

import com.docto.protechdoctolib.registration.token.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CleanRepository extends JpaRepository<ConfirmationToken, Long> {

    // Cette fonction sert à récupérer les tokens qui ont été crées avant une certaine date passée en paramètre.
    // Ell est utilisée pour déterminer les comptes obsolètes à supprimer

    @Query("select u from ConfirmationToken u where u.createdAt<=:datee")  // (2)
    List<ConfirmationToken> findTokensToDelete(@Param("datee") LocalDateTime datee);



}
