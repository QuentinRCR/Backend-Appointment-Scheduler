package com.docto.protechdoctolib.forgotten_password;

import com.docto.protechdoctolib.email.EmailService;
import com.docto.protechdoctolib.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ForgottenPasswordService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public ForgottenPasswordService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public String reinitializePassword(String email){
        if(userRepository.findByEmail(email).isPresent() && (userRepository.findByEmail((email))).get().isEnabled()){

            String token = UUID.randomUUID().toString();


            String link="http à faire" + token;

            emailService.sendEmail(email, "Lien de réinitialisation de votre mot de passe",
                    buildReinitializationEmail((userRepository.findByEmail((email))).get().getNom(), link));

            return token;


        }
        else{
            throw new IllegalStateException("Cet utilisateur n'existe pas");
        }

    }

    private String buildReinitializationEmail(String nom, String link){

        return "blabla";

    }
}
