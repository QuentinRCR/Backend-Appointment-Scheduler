package com.docto.protechdoctolib.registration;

import com.docto.protechdoctolib.registration.token.ConfirmationToken;
import com.docto.protechdoctolib.registration.token.ConfirmationTokenService;
import com.docto.protechdoctolib.user.User;
import com.docto.protechdoctolib.user.UserRole;
import com.docto.protechdoctolib.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RegistrationService {

    private final EmailValidator emailValidator;
    private final UserService userService;
    private final ConfirmationTokenService confirmationTokenService;


    public RegistrationService(EmailValidator emailValidator, UserService userService, ConfirmationTokenService confirmationTokenService/*, EmailSender emailSender*/) {
        this.emailValidator = emailValidator;
        this.userService = userService;
        this.confirmationTokenService = confirmationTokenService;
    }

    /** Si l'email est valide selon les contraintes de email validator, la requête est exécutée et l'utilisateur est enregistré.
     * @param request
     * @return token de confirmation qui est généré.
     */
    public String register(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.test(request.getEmail());
        if (!isValidEmail){
            throw new IllegalStateException("email not valid");
        }
         String token = userService.signUpUser(
                new User(
                        request.getNom(),
                        request.getPrenom(),
                        request.getEmail(),
                        request.getPassword(),
                        request.getPhonenumber(),
                        UserRole.USER
                ));

        return token;
    }

    /** Si le token existe, que l'email n'est pas déjà confirmé et que le token n'a pas expiré,
     * le compte de l'utilisateur qui a généré ce token est activé
     * @param token
     * @return
     */
    @Transactional
    public String confirmToken(String token){
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() ->
                        new IllegalStateException("token not found"));

        if (confirmationToken.getConfirmedAt() != null){
            throw new IllegalStateException("email already confirmed");

        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())){
            throw new IllegalStateException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        userService.enableAppUser(confirmationToken.getUser().getUsername());
        return "confirmed";
    }
}
