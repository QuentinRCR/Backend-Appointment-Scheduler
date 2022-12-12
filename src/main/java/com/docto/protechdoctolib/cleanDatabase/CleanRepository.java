package com.docto.protechdoctolib.cleanDatabase;

import com.docto.protechdoctolib.creneaux.Creneaux;
import com.docto.protechdoctolib.registration.token.ConfirmationToken;
import com.docto.protechdoctolib.user.User;
import org.hibernate.sql.Select;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CleanRepository extends JpaRepository<ConfirmationToken, Long> {

    @Query("select u from ConfirmationToken u where u.createdAt<=:datee")  // (2)
    List<ConfirmationToken> findCreneauxAfterDate(@Param("datee") LocalDateTime datee);



}
