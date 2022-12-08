package com.docto.protechdoctolib.forgotten_password;

import com.docto.protechdoctolib.registration.RegistrationRequest;
import com.docto.protechdoctolib.registration.token.ConfirmationToken;
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

    @PostMapping
    public String forgottenPassword(@RequestBody String email) {
        return forgottenPasswordService.reinitializePassword(email);
    }

     @GetMapping(path="confirm")
    public String confirm(@RequestParam("token") String token){
        return forgottenPasswordService.confirmToken(token);
    }

    @PostMapping("passwordReinitialisation")
    public void passwordReinitialization(@RequestBody ForgottenPasswordRequest forgottenPasswordRequest){
        (confirmationTokenRepository.findByToken(forgottenPasswordRequest.getToken())).get()
                .getUser().setPassword(passwordEncoder.encode(forgottenPasswordRequest.getPassword()));
        confirmationTokenService.setConfirmedAt(forgottenPasswordRequest.getToken());

    }
}
