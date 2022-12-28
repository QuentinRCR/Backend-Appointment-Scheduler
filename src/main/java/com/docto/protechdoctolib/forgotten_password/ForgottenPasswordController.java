package com.docto.protechdoctolib.forgotten_password;

import com.docto.protechdoctolib.registration.token.ConfirmationTokenRepository;
import com.docto.protechdoctolib.registration.token.ConfirmationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="api/forgotten_password")
public class ForgottenPasswordController {

    @Autowired
    private  BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private ForgottenPasswordService forgottenPasswordService;
    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;

    @Autowired
    ConfirmationTokenService confirmationTokenService;

    /**
     * Envoie un mail de réinitialisation de mot de passe à l'email fourni en paramètre
     * @param email
     * @return
     */
    @PostMapping
    public String forgottenPassword(@RequestBody String email) {
        return forgottenPasswordService.reinitializePassword(email.replace("%40","@").substring(0,email.length()-3)); //to correct encoding errors
    }

    /**
     * Confirme que le lien de modification de mot de passe a été créé
     * @param token
     * @return
     */
     @GetMapping(path="confirm")
    public String confirm(@RequestParam("token") String token){
        return forgottenPasswordService.confirmToken(token);
    }

    /**
     * Change le mot de passe de la personne faisant la requête pour le mot de passe fournie
     * @param passwordReinitializationRequest
     */
    @PostMapping("passwordReinitialisation")
    public void passwordReinitialization(@RequestBody PasswordReinitializationRequest passwordReinitializationRequest){
        (confirmationTokenRepository.findByToken(passwordReinitializationRequest.getToken())).get()
                .getUser().setPassword(passwordEncoder.encode(passwordReinitializationRequest.getPassword()));
        confirmationTokenService.setConfirmedAt(passwordReinitializationRequest.getToken());

    }
}
