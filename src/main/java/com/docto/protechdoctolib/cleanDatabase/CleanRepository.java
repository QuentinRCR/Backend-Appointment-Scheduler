package com.docto.protechdoctolib.cleanDatabase;

import com.docto.protechdoctolib.registration.token.ConfirmationToken;
import com.docto.protechdoctolib.user.User;
import org.hibernate.sql.Select;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Qualifier("Cleaning")
@Repository
public interface CleanRepository extends JpaRepository<ConfirmationToken, Long> {

    @Query("select u.user from ConfirmationToken u ")
    List<ConfirmationToken> cleanDatabase();

}
