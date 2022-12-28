package com.docto.protechdoctolib.rendez_vous;

import com.docto.protechdoctolib.email.EmailService;
import com.docto.protechdoctolib.user.User;
import com.docto.protechdoctolib.user.UserRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Gère l'envoie des rappels de rendez-vous
 */
@Configuration
@EnableScheduling
public class Rappel_RDV {
    private Rendez_vousDAO rendez_vousDAO;
    private UserRepository userRepository;

    private EmailService emailService;
    public Rappel_RDV(Rendez_vousDAO rendez_vousDAO, UserRepository userRepository, EmailService emailService) {
        this.rendez_vousDAO = rendez_vousDAO;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Envoie un mail de rappel de rendez-vous tous les jours à 12h pour les rendez-vous de la journée d'après
     */
    @Scheduled(cron = "00 00 12 ? * * ")// Tous les jours à 12h
    public void rappel(){
        //get the appointements after the current date
        List<Rendez_vous> list_RDV = rendez_vousDAO.findRendez_vousAfterDate(LocalDateTime.now());

        //for each appointements found, send a remainder of the appointment is in the next day
        for (int i=0; i< list_RDV.size(); i++){
            if((list_RDV.get(i).getDateDebut().isAfter(LocalDateTime.now().plus(Duration.ofHours(12)))) && (list_RDV.get(i).getDateDebut().isBefore(LocalDateTime.now().plus(Duration.ofHours(36))))){
                User user = userRepository.findById(list_RDV.get(i).getIdUser()).get();
                emailService.sendEmail((user.getEmail()),
                        "Rappel de Rendez-vous avec la psychologue de l'école", buildEmailRappelRdv(user.getNom(),"linkSite",list_RDV.get(i).getDateDebut(), list_RDV.get(i).getMoyenCommunication()));
            }
        };
    }

    /**
     * Construit le mail de rappel de rendez-vous
     * @param name Prénom de l'élève
     * @param link lien zoom (non implémenté dans le mail)
     * @param date Date tu rendez-vous
     * @param comm Moyen de communication utilisé
     * @return le mail de rappel
     */
    public String buildEmailRappelRdv(String name, String link, LocalDateTime date, String comm) {
        //SimpleDateFormat dateFormat = new SimpleDateFormat("EEEEEEEE dd MMMMMMMMM yyyy", Locale.FRANCE);
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
                "                        <h1>"+ name +", votre rendez-vous est demain!</h1>\n" +
                "                        <h2>Bonjour, nous vous rappelons que votre rendez-vous avec la psychologue prend place demain. Voici les détails concernant ce dernier:</h2>\n" +
                "                        <div class=\"card_container\">\n" +
                "                          <div class=\"left_panel\"></div>\n" +
                "                          <div class=\"détails\">\n" +
                "                            <div class=\"détails_élément\">Date: "+date.toLocalDate()+"</div>\n" +
                "                            <div class=\"détails_élément\">Heure: "+date.toLocalTime()+"</div>\n" +
                "                            <div class=\"détails_élément\">Moyen de communication:"+comm+"</div>\n" +
                "                          </div>\n" +
                "                        </div>\n" +
                "                        <h2>Vous êtes donc attendu le "+date.toLocalDate()+" à "+date.toLocalTime()+" sur "+comm+".</h2>\n" +
                "                        <h2>Pour accéder au site de réservation, cliquez sur le lien ci-dessous:</h2>\n" +
                "                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"btn btn-primary\">\n" +
                "                          <tbody>\n" +
                "                            <tr>\n" +
                "                              <td align=\"left\">\n" +
                "                                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "                                  <tbody>\n" +
                "                                    <tr>\n" +
                "                                      <td> <a href=\""+link+"\" target=\"_blank\">Accès au site</a> </td>\n" +
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
                "                    <span class=\"apple-link\">Cécile Fraisse | Psychologue à l'école des Mines de Saint-Etienne</span>\n" +
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
