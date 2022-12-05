package com.docto.protechdoctolib.registration;

import com.docto.protechdoctolib.email.EmailService;
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
    private final EmailService emailService;


    public RegistrationService(EmailValidator emailValidator, UserService userService, ConfirmationTokenService confirmationTokenService,/*, EmailSender emailSender*/EmailService emailSender, EmailService emailService) {
        this.emailValidator = emailValidator;
        this.userService = userService;
        this.confirmationTokenService = confirmationTokenService;
        this.emailService = emailService;
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
                        UserRole.USER,
                        request.getSkypeAccount(),
                        request.getCampus()
                ));
        String link = "http://localhost:8080/api/registration/confirm?token=" + token;
        emailService.sendEmail(
                request.getEmail(),
                "Lien d'activation de votre compte",
                buildEmail(request.getNom(), link));

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
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <title>Compte activé</title>\n" +
                "</head>\n" +
                "<style>\n" +
                "    p{\n" +
                "        align-items: center;\n" +
                "        padding: 15px 15px 15px 15px;\n" +
                "        background-color: rgba(0, 128, 0, 0.392);\n" +
                "        font-size: 18px;\n" +
                "        font-weight: bold;\n" +
                "        text-align: center;\n" +
                "    }\n" +
                "</style>\n" +
                "<body>\n" +
                "    <p>Votre compte a bien été activé. Vous pouvez maintenant vous connecter en utilisant cet email.</p>\n" +
                "</body>\n" +
                "</html>";
    }
    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirmez votre adresse mail</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Bonjour " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> nous vous remercions pour votre inscription. Cliquez sur le lien ci-dessous pour confirmer votre adresse mail et activer votre compte: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activez votre compte</a> </p></blockquote>\n Le lien est valable 15 minutes. <p>A bientôt!</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }

    public String buildEmailConfirmationRdv(String name, String link, LocalDateTime date, String comm) {
        return "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width\" />\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                "    <title>Simple Transactional Email</title>\n" +
                "    <style>\n" +
                "      \n" +
                "     \n" +
                "      body {\n" +
                "        background-color: #f6f6f6;\n" +
                "        font-family: sans-serif;\n" +
                "        -webkit-font-smoothing: antialiased;\n" +
                "        font-size: 14px;\n" +
                "        line-height: 1.4;\n" +
                "        margin: 0;\n" +
                "        padding: 0; \n" +
                "        -ms-text-size-adjust: 100%;\n" +
                "        -webkit-text-size-adjust: 100%; }\n" +
                "      table {\n" +
                "        margin: auto;\n" +
                "        border-collapse: separate;\n" +
                "        \n" +
                "        width: 100%; }\n" +
                "        table td {\n" +
                "          font-family: sans-serif;\n" +
                "          font-size: 14px;\n" +
                "          vertical-align: top;}\n" +
                "      \n" +
                "      .body {\n" +
                "        background-color: #f6f6f6;\n" +
                "        width: 100%; }\n" +
                "      /* Set a max-width, and make it display as block so it will automatically stretch to that width, but will also shrink down on a phone or something */\n" +
                "      \n" +
                "      .content {\n" +
                "        box-sizing: border-box;\n" +
                "        display: block;\n" +
                "        Margin: 0 auto;\n" +
                "        max-width: 580px;\n" +
                "        padding: 10px; }\n" +
                "      \n" +
                "      .main {\n" +
                "        background: #fff;\n" +
                "        border-radius: 3px;\n" +
                "        width: 100%; }\n" +
                "      .wrapper {\n" +
                "        box-sizing: border-box;\n" +
                "        padding: 20px; }\n" +
                "      .footer {\n" +
                "        clear: both;\n" +
                "        padding-top: 10px;\n" +
                "        text-align: center;\n" +
                "        width: 100%; }\n" +
                "        .footer td,\n" +
                "        .footer p,\n" +
                "        .footer span,\n" +
                "        .footer a {\n" +
                "          color: #999999;\n" +
                "          font-size: 12px;\n" +
                "          text-align: center; }\n" +
                "      /* -------------------------------------\n" +
                "          TYPOGRAPHY\n" +
                "      ------------------------------------- */\n" +
                "      h1,\n" +
                "      h2,\n" +
                "      h3,\n" +
                "      h4 {\n" +
                "        color: #000000;\n" +
                "        font-family: sans-serif;\n" +
                "        font-weight: 400;\n" +
                "        line-height: 1.4;\n" +
                "        margin: 0;\n" +
                "        Margin-bottom: 20px; }\n" +
                "      h1 {\n" +
                "        font-size: 35px;\n" +
                "        font-weight: 300;\n" +
                "        text-align: center;\n" +
                "         }\n" +
                "      p,\n" +
                "      ul,\n" +
                "      ol {\n" +
                "        font-family: sans-serif;\n" +
                "        font-size: 14px;\n" +
                "        font-weight: normal;\n" +
                "        margin: 0;\n" +
                "        Margin-bottom: 15px; }\n" +
                "        p li,\n" +
                "        ul li,\n" +
                "        ol li {\n" +
                "          list-style-position: inside;\n" +
                "          margin-left: 5px; }\n" +
                "      a {\n" +
                "        color: #3498db;\n" +
                "        text-decoration: underline; }\n" +
                "\n" +
                "\n" +
                "      .card_container{\n" +
                "        display: flex;\n" +
                "        margin-bottom: 20px;\n" +
                "        border: 4px solid #3498db;\n" +
                "        border-radius: 15px;\n" +
                "      }\n" +
                "      \n" +
                "      .left_panel{\n" +
                "        background-color: #3498db;\n" +
                "        width: 10%;\n" +
                "      }\n" +
                "        .détails{\n" +
                "        display: flex;\n" +
                "        flex-direction: column;\n" +
                "        Margin: 0 auto;\n" +
                "        \n" +
                "        align-content: center;\n" +
                "        /* makes it centered */\n" +
                "        max-width: 580px;\n" +
                "        padding: 10px;\n" +
                "        width: 90%;\n" +
                "    \n" +
                "      }\n" +
                "      .détails_élément{\n" +
                "        align-items: center;\n" +
                "    \n" +
                "        margin-left: 0px;\n" +
                "        font-size: 20px;\n" +
                "      }\n" +
                "      /* -------------------------------------\n" +
                "          BUTTONS\n" +
                "      ------------------------------------- */\n" +
                "      .btn {\n" +
                "        box-sizing: border-box;\n" +
                "        width: 100%; }\n" +
                "        .btn > tbody > tr > td {\n" +
                "          padding-bottom: 15px; }\n" +
                "        .btn table {\n" +
                "          width: auto; }\n" +
                "        .btn table td {\n" +
                "          background-color: #ffffff;\n" +
                "          border-radius: 5px;\n" +
                "          text-align: center; }\n" +
                "        .btn a {\n" +
                "          background-color: #ffffff;\n" +
                "          border: solid 1px #3498db;\n" +
                "          border-radius: 5px;\n" +
                "          box-sizing: border-box;\n" +
                "          color: #3498db;\n" +
                "          cursor: pointer;\n" +
                "          display: inline-block;\n" +
                "          font-size: 14px;\n" +
                "          font-weight: bold;\n" +
                "          margin: 0;\n" +
                "          padding: 12px 25px;\n" +
                "          text-decoration: none;\n" +
                "          text-transform: capitalize; }\n" +
                "      .btn-primary table td {\n" +
                "        background-color: #3498db; }\n" +
                "      .btn-primary a {\n" +
                "        background-color: #3498db;\n" +
                "        border-color: #3498db;\n" +
                "        color: #ffffff; }\n" +
                "      \n" +
                "    </style>\n" +
                "  </head>\n" +
                "  <body class=\"\">\n" +
                "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"body\">\n" +
                "      <tr>\n" +
                "        <td>&nbsp;</td>\n" +
                "        <td class=\"container\">\n" +
                "          <div class=\"content\">\n" +
                "            \n" +
                "            <table class=\"main\">\n" +
                "\n" +
                "              <!-- START MAIN CONTENT AREA -->\n" +
                "              <tr>\n" +
                "                <td class=\"wrapper\">\n" +
                "                  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "                    <tr>\n" +
                "                      <td>\n" +
                "                        <h1>" + name + ", votre rendez-vous est fixé!</h1>\n" +
                "                        <h2>Bonjour, votre demande a bien été traitée. Voici les détails concernant votre rendez-vous:</h2>\n" +
                "                        <div class=\"card_container\">\n" +
                "                          <div class=\"left_panel\"></div>\n" +
                "                          <div class=\"détails\">\n" +
                "                            <div class=\"détails_élément\">Date:" + date.getDayOfMonth() / date.getMonthValue() + "</div>\n" +
                "                            <div class=\"détails_élément\">Heure:" + date.toLocalTime() + "</div>\n" +
                "                            <div class=\"détails_élément\">Moyen de communication:" + comm + "</div>\n" +
                "                          </div>\n" +
                "                        </div>\n" +
                "                        <h2>Vous êtes donc attendu le " + date.getDayOfMonth() / date.getMonthValue() + " à " + date.toLocalTime() + " sur " + comm + "</h2>\n" +
                "                        <h2>Pour accéder au site de réservation, cliquez sur le lien ci-dessous:</h2>\n" +
                "                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"btn btn-primary\">\n" +
                "                          <tbody>\n" +
                "                            <tr>\n" +
                "                              <td align=\"left\">\n" +
                "                                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "                                  <tbody>\n" +
                "                                    <tr>\n" +
                "                                      <td> <a href=\"" + link + "\" target=\"_blank\">Accès au site</a> </td>\n" +
                "                                    </tr>\n" +
                "                                  </tbody>\n" +
                "                                </table>\n" +
                "                              </td>\n" +
                "                            </tr>\n" +
                "                          </tbody>\n" +
                "                        </table>\n" +
                "                        <p>Si vous avez reçu ce mail par erreur, effacez le.</p>\n" +
                "      \n" +
                "                      </td>\n" +
                "                    </tr>\n" +
                "                  </table>\n" +
                "                </td>\n" +
                "              </tr>\n" +
                "\n" +
                "            <!-- END MAIN CONTENT AREA -->\n" +
                "            </table>\n" +
                "\n" +
                "            <!-- START FOOTER -->\n" +
                "            <div class=\"footer\">\n" +
                "              <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "                <tr>\n" +
                "                  <td class=\"content-block\">\n" +
                "                    <span class=\"apple-link\">Cécile Fraisse | psychologue de l'école des Mines de Saint-Etienne</span>\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "                \n" +
                "              </table>\n" +
                "            </div>\n" +
                "            <!-- END FOOTER -->\n" +
                "            \n" +
                "          <!-- END CENTERED WHITE CONTAINER -->\n" +
                "          </div>\n" +
                "        </td>\n" +
                "        <td>&nbsp;</td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </body>\n" +
                "</html>";
    }

}

