package com.docto.protechdoctolib.registration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/registration")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    /** Prend une requête d'inscription
     * @param request
     * @return
     */
    @PostMapping
    public String register(@RequestBody RegistrationRequest request) {
        return registrationService.register(request);
    }

    /** Prend un token
     * @param token
     * @return le token et sa date de confirmation
     */
    @GetMapping(path="confirm")
    public String confirm(@RequestParam("token") String token){
        return registrationService.confirmToken(token);
    }

}
