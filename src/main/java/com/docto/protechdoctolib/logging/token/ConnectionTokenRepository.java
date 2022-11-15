package com.docto.protechdoctolib.logging.token;

import com.docto.protechdoctolib.registration.token.ConfirmationToken;
import com.docto.protechdoctolib.user.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Optional;

@Qualifier("ConnectionTokens")
@Repository
public interface ConnectionTokenRepository extends JpaRepository <ConnectionToken, Long> {
    Optional<ConnectionToken> findByToken(String token);

}