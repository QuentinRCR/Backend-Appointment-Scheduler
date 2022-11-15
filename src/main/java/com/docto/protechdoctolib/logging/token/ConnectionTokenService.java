package com.docto.protechdoctolib.logging.token;


import com.docto.protechdoctolib.registration.token.ConfirmationToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConnectionTokenService {

    private final ConnectionTokenRepository connectionTokenRepository;

    public ConnectionTokenService(@Qualifier("ConnectionTokens") ConnectionTokenRepository connectionTokenRepository) {
        this.connectionTokenRepository = connectionTokenRepository;
    }

    public Optional<ConnectionToken> getToken(String token){
        return connectionTokenRepository.findByToken(token);
    }

}
