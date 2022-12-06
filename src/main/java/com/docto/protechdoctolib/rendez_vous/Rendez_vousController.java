package com.docto.protechdoctolib.rendez_vous;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.docto.protechdoctolib.creneaux.*;
import com.docto.protechdoctolib.email.EmailService;
import com.docto.protechdoctolib.user.User;
import com.docto.protechdoctolib.user.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@CrossOrigin
@RestController // (1)
@RequestMapping("/api/rendez_vous") // (2)
@Transactional // (3)
public class Rendez_vousController {

    private final CreneauxDAO creneauxDAO;
    private final Rendez_vousDAO rendez_vousDAO;

    private final EmailService emailService;

    private final UserRepository userRepository;
    private static final Logger logger = LogManager.getLogger(CreneauxController.class);

    public Rendez_vousController(CreneauxDAO creneauxDAO, Rendez_vousDAO rendez_vousDAO, EmailService emailService, UserRepository userRepository) {
        this.creneauxDAO = creneauxDAO;
        this.rendez_vousDAO = rendez_vousDAO;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    /**
     * Donne la liste de tous les rdvs
     *
     * @return une liste de tous les rdvs
     */
    @GetMapping
    public List<Rendez_vousDTO> findAll() {
        return rendez_vousDAO.findAll().stream().map(Rendez_vousDTO::new).collect(Collectors.toList());
    }

    /**
     * Donne tous les rendez-vous si la personne est admin et donne seulement les rendez-vous de la personne si cette personne n'est pas admin
     * @param request
     * @return
     */
    @GetMapping("/auth")
    public List<Rendez_vousDTO> findAllByAuth(HttpServletRequest request){
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        String acces_token = authorizationHeader.substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes()); //TODO check video around 17min-1:38h should crypt the token
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(acces_token);
        String auth = decodedJWT.getClaim("roles").asArray(String.class)[0];
        if (auth.equals("ADMIN")){ //if the right rights, send all the appointements
            return rendez_vousDAO.findAll().stream().map(Rendez_vousDTO::new).collect(Collectors.toList());
        }
        else{
            Long id = Long.parseLong(decodedJWT.getKeyId());
            return rendez_vousDAO.findAllByIdUser(id).stream().map(Rendez_vousDTO::new).collect(Collectors.toList());
        }
    }

    /**
     * Renvoi le rdv ayant pour id le paramètre
     *
     * @param id
     * @return rdv
     */
    @GetMapping(path = "/{id}")
    public Rendez_vousDTO findById(@PathVariable Long id) {
        Rendez_vousDTO rendez_vousId= rendez_vousDAO.findById(id).map(Rendez_vousDTO::new).orElse(null);
        if (rendez_vousId==null){
            throw new ResponseStatusException( //If not found, throw 404 error
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        }
        else{
            return rendez_vousId;
        }
    }

    /**
     * Renvoi tous les rendez-vous d'un client
     * @param idUser
     * @return Renvoi la liste des rendez-vous d'un client
     */
    @GetMapping(path = "/user/{idUser}")
    public List<Rendez_vousDTO> findAllByClientId(@PathVariable Long idUser) {
        return rendez_vousDAO.findAllByIdUser(idUser).stream().map(Rendez_vousDTO::new).collect(Collectors.toList());
    }


    /**
     * Supprime le créneau ayant pour id le paramètre
     * @param id
     */
    @DeleteMapping(path = "/{id}")
    public void deleteParId(@PathVariable Long id) {
        try{
            rendez_vousDAO.deleteById(id);
        }
        catch (EmptyResultDataAccessException e){
            throw new ResponseStatusException( //if not found, throw 404 error
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        }
    }

    /**
     * Prend un dto de rdv en paramètre, vérifie que ce rendez-vous rentre dans un créneau, crée ce rdv dans la db si son id est null et le modifie si son id existe déjà. L'id du créneau est mis à jour automatiquement
     *
     * @param dto
     * @return le dto du rdv crée
     */
    @PostMapping("/create_or_modify") // (8)
    public Rendez_vousDTO create_or_modify(@RequestBody Rendez_vousDTO dto,HttpServletRequest request) {
        CreneauxDTO creneauMatch = isWithinASlot(dto.getDateDebut(),dto.getDuree()); //Get the slot in which the appointment fit.
        if (creneauMatch == null){ //If there is no corresponding slot, throw 404 error
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "slot not found"
            );
        }

        // Diferent case depending on the role of the personne
        String acces_token = request.getHeader(AUTHORIZATION).substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(acces_token);
        String auth = decodedJWT.getClaim("roles").asArray(String.class)[0];
        if (auth.equals("USER")){ //if the person is a user, for the id to be his
            dto.setIdUser(Long.parseLong(decodedJWT.getKeyId()));
        }
        //if it is not the case, it means that the ADMIN try to add an appointment for a user so we keen the id

        Long creneauId = creneauMatch.getId(); //If a slot is found, assign the value of the corresponding slot
        Rendez_vous rendez_vous = null;
        // On creation id is not defined
        if (dto.getId() == null) {
            rendez_vous = rendez_vousDAO.save(new Rendez_vous(dto.getId(), creneauId ,dto.getIdUser(), dto.getDateDebut(), dto.getDuree(), dto.getMoyenCommunication(),dto.getZoomLink())); //Create new appointment
            //envoi mail de confirmation prise de rdv
            User user= userRepository.findById(dto.getIdUser()).get();
            emailService.sendEmail(
                    user.getEmail(),
                    "confirmation prise de rendez-vous",
                    buildEmailConfirmationRdv(user.getPrenom(), "link", dto.getDateDebut(),dto.getMoyenCommunication()));


        } else {
            rendez_vous = rendez_vousDAO.getReferenceById(dto.getId());  //Modify existing appointment
            rendez_vous.setDateDebut(dto.getDateDebut());
            rendez_vous.setIdCreneau(creneauId);
            rendez_vous.setIdUser(dto.getIdUser());
            rendez_vous.setDuree(dto.getDuree()); /*use a duration using format "PT60S" or "PT2M"...*/
            rendez_vous.setMoyenCommunication(dto.getMoyenCommunication());
            rendez_vous.setZoomLink(dto.getZoomLink());


        }
        return new Rendez_vousDTO(rendez_vous);
    }


    /**
     * Cherche si un créneau se terminant dans le futur permet de contenir ce rendez-vous
     * @param dateDebutRDV
     * @param duree
     * @return L'id du créneau correspondant et null s'il n'y a pas de créneau correspondant
     */
    public CreneauxDTO isWithinASlot(LocalDateTime dateDebutRDV, Duration duree){
        return isWithinASlot(dateDebutRDV,duree, LocalDate.now());
    }

    /**
     * Cherche si un créneau se terminant après dateDebutRecherche permet de contenir ce rendez-vous
     * @param dateDebutRDV
     * @param duree
     * @param dateDebutRecherche
     * @return L'id du créneau correspondant et null s'il n'y a pas de créneau correspondant
     */
    public CreneauxDTO isWithinASlot(LocalDateTime dateDebutRDV, Duration duree, LocalDate dateDebutRecherche){
        logger.info( "un créneau pour un rendez-vous le "+dateDebutRDV.toString()+" d'une durée de "+duree.toString()+ "après la date de "+dateDebutRecherche.toString()+" a été cherché");

        LocalDateTime dateFinRDV= dateDebutRDV.plus(duree); //Create a time of the end of the appointment

        CreneauxDTO bonCreneau=null;
        for (Creneaux creneau : creneauxDAO.findCreneauxAfterDate(dateDebutRecherche)){ //Get all slots ending after the given date
            if (
                    (creneau.getJours().contains(dateDebutRDV.getDayOfWeek())) && //check that the day match one of the registered days
                            ((dateDebutRDV.toLocalDate().isAfter(creneau.getDateDebut())) || (dateDebutRDV.toLocalDate().equals(creneau.getDateDebut()))) && //Check that the stating date in within the slot
                            ((dateFinRDV.toLocalDate().isBefore(creneau.getDateFin())) || (dateDebutRDV.toLocalDate().equals(creneau.getDateDebut()))) //Check that the ending date in within the slot
            ){
                for (HeuresDebutFin plage:creneau.getHeuresDebutFin()){
                    if (
                            ((dateDebutRDV.toLocalTime().isAfter(plage.getTempsDebut())) || (dateDebutRDV.toLocalTime().equals(plage.getTempsDebut()))) && //Check that the stating time is within a time-slot
                                    ((dateFinRDV.toLocalTime().isBefore(plage.getTempsFin())) || (dateFinRDV.toLocalTime().equals(plage.getTempsFin()))) //Check that the ending time is within a time-slot
                    ){
                        bonCreneau=new CreneauxDTO(creneau); //if one is found, assign it
                    }
                }

            }
        }
        if(bonCreneau != null && logger.isDebugEnabled()){
            logger.info("Le créneau "+bonCreneau.getId().toString()+" correspond");
        } else if (logger.isDebugEnabled()) {
            logger.info("Aucun créneau ne correspond");
        }
        return bonCreneau;
    }

    public String buildEmailConfirmationRdv(String name, String link, LocalDateTime date, String comm) {
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
                "                        <h1>"+ name +", votre rendez-vous est fixé!</h1>\n" +
                "                        <h2>Bonjour, votre demande a bien été traitée. Voici les détails concernant votre rendez-vous:</h2>\n" +
                "                        <div class=\"card_container\">\n" +
                "                          <div class=\"left_panel\"></div>\n" +
                "                          <div class=\"détails\">\n" +
                "                            <div class=\"détails_élément\">Date: "+date.toLocalDate()+"</div>\n" +
                "                            <div class=\"détails_élément\">Heure: "+date.toLocalTime()+"</div>\n" +
                "                            <div class=\"détails_élément\">Moyen de communication:"+comm+"</div>\n" +
                "                          </div>\n" +
                "                        </div>\n" +
                "                        <h2>Vous êtes donc attendu le "+date.getDayOfMonth()/date.getMonthValue()+" à "+date.toLocalTime()+" sur "+comm+"</h2>\n" +
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
