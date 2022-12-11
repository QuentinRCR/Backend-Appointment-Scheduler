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

    @PostMapping
    public String forgottenPassword(@RequestBody String email) {
        return forgottenPasswordService.reinitializePassword(email.replace("%40","@").substring(0,email.length()-3)); //to correct encoding errors
    }

     @GetMapping(path="confirm")
    public String confirm(@RequestParam("token") String token){
        return forgottenPasswordService.confirmToken(token);
    }

    @PostMapping("passwordReinitialisation")
    public void passwordReinitialization(@RequestBody PasswordReinitializationRequest passwordReinitializationRequest){
        (confirmationTokenRepository.findByToken(passwordReinitializationRequest.getToken())).get()
                .getUser().setPassword(passwordEncoder.encode(passwordReinitializationRequest.getPassword()));
        confirmationTokenService.setConfirmedAt(passwordReinitializationRequest.getToken());

    }
}
