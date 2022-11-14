package com.docto.protechdoctolib.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void shouldEnableUser(){
        User user = userRepository.getReferenceById(-9L);
        userRepository.enableUser("utilisateur@gmail.com");
        User user1=userRepository.getReferenceById(-9L);
        Assertions.assertThat(user1.isEnabled()).isEqualTo(true);

    }
}
