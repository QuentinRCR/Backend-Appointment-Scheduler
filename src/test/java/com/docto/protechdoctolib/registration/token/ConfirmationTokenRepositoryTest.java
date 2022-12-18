package com.docto.protechdoctolib.registration.token;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class ConfirmationTokenRepositoryTest {

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;

    @Test
    public void shouldConfirmedAt(){
        ConfirmationToken confirmationToken= confirmationTokenRepository.getReferenceById(-5L);
        confirmationTokenRepository.updateConfirmedAt("abc123",LocalDateTime.of(2015,12,20,8,35,20));
        ConfirmationToken confirmationToken1=confirmationTokenRepository.getReferenceById((-5L));
        Assertions.assertThat(confirmationToken1.getCreatedAt()).isEqualTo(LocalDateTime.of(2015,12,20,8,35,20));



    }

}
