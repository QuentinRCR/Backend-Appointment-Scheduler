package com.docto.protechdoctolib.rendez_vous;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.docto.protechdoctolib.creneaux.*;
import com.docto.protechdoctolib.email.EmailService;
import com.docto.protechdoctolib.user.User;
import com.docto.protechdoctolib.user.UserRepository;
import com.docto.protechdoctolib.user.UserRole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Met à disposition les apis pour gérer les Rendez-vous
 */
@CrossOrigin
@RestController 
@RequestMapping("/api/rendez_vous")
@Transactional
public class Rendez_vousController {

    @Autowired
    private Environment environment; //to be able to use global environment variables  

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
     * Test l'authorité de la personne. Si la personne est admin, ça lui renvoie les 100 derniers rendez-vous, si la personne est un utilisateur, ça lui renvoie aussi les 100 derniers rendez-vous, mais anonymisés (à par pour les siens).
     *
     * @return une liste des 100 derniers rendez-vous
     */
    @GetMapping("/user")
    public List<Rendez_vousDTO> findAll(HttpServletRequest request) {
        
        //Get the authorisation 
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        String acces_token = authorizationHeader.substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(acces_token);
        String auth = decodedJWT.getClaim("roles").asArray(String.class)[0]; //extract the role from the header

        //get all the appointments 
        List<Rendez_vous> rendez_vousList=rendez_vousDAO.findAll();
        List<Rendez_vousDTO> rendez_vousListCourte= rendez_vousList.subList(0,Integer.min(100,rendez_vousList.size())).stream().map(Rendez_vousDTO::new).collect(Collectors.toList()); //the subList is to avoid adding loading time
        
        //if it is a USER, anonymize the appointements by removing the client id, except if it is its own appointment
        if (auth.equals("USER")){
            rendez_vousListCourte.forEach(rdv -> {
                if(rdv.getIdUser()!=Long.parseLong(decodedJWT.getKeyId())){ //if it is its own appointment then let the id
                    rdv.setIdUser(null);
                }
            });
        }


        return  rendez_vousListCourte;
    }

    /**
     * Donne tous les rendez-vous si la personne est admin et donne seulement les rendez-vous de la personne si cette personne n'est pas admin
     * @param request
     * @return liste de rendez-vous
     */
    @GetMapping("/user/auth")
    public List<Rendez_vousDTO> findAllByAuth(HttpServletRequest request){
        //extract the role from the header
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        String acces_token = authorizationHeader.substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes()); //TODO check video around 17min-1:38h should crypt the token
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(acces_token);
        String auth = decodedJWT.getClaim("roles").asArray(String.class)[0]; //Get the authorisation

        //if the right rights, send all the appointements
        if (auth.equals("ADMIN")){
            List<Rendez_vous> rendez_vousList= rendez_vousDAO.findAll();
            return rendez_vousList.subList(0,Integer.min(100,rendez_vousList.size())).stream().map(Rendez_vousDTO::new).collect(Collectors.toList()); //the sublist is to avoid adding time to load old appointements
        }

        //if it is a user, send only own appointements
        else{
            Long id = Long.parseLong(decodedJWT.getKeyId());
            return rendez_vousDAO.findAllByIdUser(id).stream().map(Rendez_vousDTO::new).collect(Collectors.toList());
        }
    }

    /**
     * Renvoi le rdv ayant pour id le paramètre (non inutilisé dans le front-end)
     *
     * @param id
     * @return rdv
     */
    @GetMapping(path = "/admin/{id}")
    public Rendez_vousDTO findById(@PathVariable Long id) {
        Rendez_vousDTO rendez_vousId= rendez_vousDAO.findById(id).map(Rendez_vousDTO::new).orElse(null);

        //If not found, throw 404 error
        if (rendez_vousId==null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        }
        else{
            return rendez_vousId;
        }
    }

    /**
     * Renvoi tous les rendez-vous d'un client (non utilisé dans le front-end)
     * @param idUser
     * @return Renvoi la liste des rendez-vous d'un client
     */
    @GetMapping(path = "/admin/{idUser}")
    public List<Rendez_vousDTO> findAllByClientId(@PathVariable Long idUser) {
        return rendez_vousDAO.findAllByIdUser(idUser).stream().map(Rendez_vousDTO::new).collect(Collectors.toList());
    }


    /**
     * Supprime le rendez-vous dont l'identifient est fourni et envoie un email aux admins pour les informer que ce rendez-vous a été supprimé
     * @param id
     */
    @DeleteMapping(path = "/user/{id}")
    public void deleteParId(@PathVariable Long id) {
        try{
            //get infos on the appointment and the user before deleting it
            Rendez_vous deletedApp=rendez_vousDAO.findById(id).get();
            User user=userRepository.findById(deletedApp.getIdUser()).get();

            //delete the appointment
            rendez_vousDAO.deleteById(id);

            //Email all the admins
            List <User> Admins= userRepository.findByRole(UserRole.ADMIN);
            for (int i=0; i<Admins.size(); i++) {
                emailService.sendEmail(Admins.get(i).getEmail(),
                        "Annulation rendez-vous "+user.getNom()+" "+user.getPrenom()+".", buildEmailSuppressionPsy(
                                user.getNom(),
                                user.getPrenom(),
                                deletedApp.getDateDebut(),
                                deletedApp.getMoyenCommunication(),
                                environment.getProperty("frontend.url"))
                );
            }
        }

        //handle exceptions
        catch (EmptyResultDataAccessException e){
            throw new ResponseStatusException( //if not found, throw 404 error
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        }

    }

    /**
     * Modifie ou ajoute un rendez-vous à la base de donnée. Des testes suivant sont réalisé :
     * - Vérifie que le rendez-vous correspond à un créneau
     * - Vérifie que le rendez-vous est dans le future.
     * - Vérifie que le rendez-vous rentre en entière dans le créneau
     * - Vérifie qu'il n'y a pas déjà un rendez-vous pour cette heure
     * Si l'id du rendez-vous est précisé, ça modifier le rendez-vous dont l'id est spécifié. Si ce n'est pas le cas, ça crée un nouveau rendez-vous
     * L'id de l'utilisateur est automatiquement assigné si un User fait la request. L'idée fournit est utilisé si un admin fait la request.
     *
     * @param dto
     * @return le rendez-vous crée ou modifié
     */
    @PostMapping("/user/create_or_modify") // (8)
    public Rendez_vousDTO create_or_modify(@RequestBody Rendez_vousDTO dto,HttpServletRequest request) {

        //Get the slot in which the appointment fit.
        CreneauxDTO creneauMatch = isWithinASlot(dto.getDateDebut(),dto.getDuree());

        //If there is no corresponding slot, throw 404 error
        if (creneauMatch == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "no slot corresponding to this time"
            );
        }

        //If a slot is found, assign the value of the corresponding slot
        Long creneauId = creneauMatch.getId();



        // Get the role of the user in the header
        String acces_token = request.getHeader(AUTHORIZATION).substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(acces_token);
        String auth = decodedJWT.getClaim("roles").asArray(String.class)[0];

        //if the person is a user, the id used to book the appointment is his (regardless of what was previously set)
        if (auth.equals("USER")){
            dto.setIdUser(Long.parseLong(decodedJWT.getKeyId()));
        }
        //if it is not the case, it means that the ADMIN try to add an appointment for a user so we keep the id specified



        //handle different errors

        //test that the date and time of the appointment is in the future. If it is not the case, it means that the user mad a mistake.
        if( (dto.getDateDebut().isBefore(LocalDateTime.now()))){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "can't take an appointment in the past"
            );
        }

        //check that the appointment fits within the matching time slot
        if(
                (dto.getDateDebut().toLocalTime().isBefore(creneauMatch.getHeuresDebutFin().get(0).getTempsDebut())) ||
                        (dto.getDateDebut().toLocalTime().plus(Duration.ofMinutes(30)).isAfter(creneauMatch.getHeuresDebutFin().get(0).getTempsFin()))
        ){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "the appointment does not fit within the timeslot"
            );
        }

        //detect if there already are an appointment at this time
        List<Rendez_vous> rendez_vousAtTime= rendez_vousDAO.findByDateDebut(dto.getDateDebut());
        if(rendez_vousAtTime.size()>0 && !rendez_vousAtTime.get(0).getId().equals(dto.getId())){ //check no appointements are at the same time except if it is the same appointment trying to be changed
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "already an appointment at this time"
            );
        }



        Rendez_vous rendez_vous = null;

        // If the id is not defined, it means we are in creation mode
        if (dto.getId() == null) {

            //Create new appointment
            rendez_vous = rendez_vousDAO.save(new Rendez_vous(dto.getId(), creneauId ,dto.getIdUser(), dto.getDateDebut(), dto.getDuree(), dto.getMoyenCommunication(),dto.getZoomLink()));

            //send confirmation email to the user
            User user= userRepository.findById(dto.getIdUser()).get();
            emailService.sendEmail(
                    user.getEmail(),
                    "Confirmation prise de rendez-vous",
                    buildEmailConfirmationRdv(user.getPrenom(), environment.getProperty("frontend.url"), dto.getDateDebut(),dto.getMoyenCommunication()));

            //if the appointment is not made by an admin, email all admins to inform them of a new appointment
            if (auth.equals("USER")) {
                List<User> Admins = userRepository.findByRole(UserRole.ADMIN);
                for (int i = 0; i < Admins.size(); i++) {
                    emailService.sendEmail(             // Pour la psy
                            Admins.get(i).getEmail(),
                            "Nouveau rendez-vous pour "+user.getPrenom()+" "+user.getNom(),
                            buildEmailConfirmationRdvPsy(user.getNom(), user.getPrenom(), dto.getDateDebut(), dto.getMoyenCommunication(),"linkSite"));
                }
            }



        // If the id is defined, it means we are in modification mode
        } else {
            //Get and modify existing appointment
            rendez_vous = rendez_vousDAO.getReferenceById(dto.getId());
            LocalDateTime ancienDateRdv= rendez_vous.getDateDebut();
            rendez_vous.setDateDebut(dto.getDateDebut());
            rendez_vous.setIdCreneau(creneauId);
            rendez_vous.setIdUser(dto.getIdUser());
            rendez_vous.setDuree(dto.getDuree()); //use a duration using format "PT60S" or "PT2M"... (for 60 seconds and 2 minutes)
            rendez_vous.setMoyenCommunication(dto.getMoyenCommunication());
            rendez_vous.setZoomLink(dto.getZoomLink());

            //Informe the user that the appointment as been has been moved
            User user= userRepository.findById(dto.getIdUser()).get();
            emailService.sendEmail(
                    user.getEmail(),
                    "Modification de rendez-vous",
                    buildEmailModificationRdv(user.getPrenom(), environment.getProperty("frontend.url"), dto.getDateDebut(),dto.getMoyenCommunication()));

            //If the appointment is modified by a user, email the admin
            if (auth.equals("USER")) {
                List<User> Admins = userRepository.findByRole(UserRole.ADMIN);
                for (int i = 0; i < Admins.size(); i++) {
                    emailService.sendEmail( Admins.get(i).getEmail(),
                            "Un rendez-vous a été modifié",
                            buildEmailModificationRdvPsy(user.getNom(), user.getPrenom(),ancienDateRdv, dto.getDateDebut(), dto.getMoyenCommunication(), "linkSite"));
                }
            }

        }

        //return the appointment that was juste created
        return new Rendez_vousDTO(rendez_vous);
    }


    /**
     * Crée le document Excel contenant les rendez-vous entre la date de début et la date de fin puis rend disponible ce fichier au téléchargement
     * @param startDate date début
     * @param endDate date fin
     * @return
     * @throws IOException
     */
    @GetMapping("/admin/downloadFile/{startDate}/{endDate}")
    public ResponseEntity<?> downloadFile(@PathVariable("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @PathVariable("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,HttpServletRequest request) throws IOException {
            //export appointements to excel
            String absolutePath= Export_excel.exportAppointements(rendez_vousDAO,userRepository,startDate,endDate);

            // transform the path to a ressource
            Path path = Paths.get(absolutePath);
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

            //send the document
            String contentType = "application/octet-stream";
            //set the name for the file
            String headerValue = "attachment; filename=RecapRDVDu"+startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))+"Au"+startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))+".xlsx";
            return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                            .body(resource);
    }



    /**
     * Cherche si un créneau correspond à la date à la durée indiquée correspond. Ne cherche que les créneaux se situant dans le futur.
     * @param dateDebutRDV
     * @param duree
     * @return L'id du créneau correspondant et null s'il n'y a pas de créneau correspondant
     */
    public CreneauxDTO isWithinASlot(LocalDateTime dateDebutRDV, Duration duree){
        return isWithinASlot(dateDebutRDV,duree, LocalDate.now());
    }

    /**
     * Cherche si un créneau correspond à la date à la durée indiquée correspond. Ne cherche que les créneaux se situant après la date indiquée.
     * @param dateDebutRDV
     * @param duree
     * @param dateDebutRecherche
     * @return L'id du créneau correspondant et null s'il n'y a pas de créneau correspondant
     */
    public CreneauxDTO isWithinASlot(LocalDateTime dateDebutRDV, Duration duree, LocalDate dateDebutRecherche){
        logger.info( "un créneau pour un rendez-vous le "+dateDebutRDV.toString()+" d'une durée de "+duree.toString()+ "après la date de "+dateDebutRecherche.toString()+" a été cherché");

        //Create a time of the end of the appointment
        LocalDateTime dateFinRDV= dateDebutRDV.plus(duree);

        CreneauxDTO bonCreneau=null;
        for (Creneaux creneau : creneauxDAO.findCreneauxAfterDate(dateDebutRecherche)){ //Get all slots ending after the given date
            if (
                //check that the day match one of the registered days
                (creneau.getJours().contains(dateDebutRDV.getDayOfWeek())) &&
                //Check that the stating date in within the slot
                ((dateDebutRDV.toLocalDate().isAfter(creneau.getDateDebut())) || (dateDebutRDV.toLocalDate().equals(creneau.getDateDebut()))) &&
                //Check that the ending date in within the slot
                ((dateFinRDV.toLocalDate().isBefore(creneau.getDateFin())) || (dateDebutRDV.toLocalDate().equals(creneau.getDateFin())))
            ){

                //check that the appointment time is withing the timeslots of the slot.
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

    /**
     * Crée un mail personalisé pour confirmer la prise de rendez-vous auprès d'un client
     * @param name
     * @param link
     * @param date
     * @param comm
     * @return Le mail sous forme de String
     */
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

    /**
     * Crée un mail personalisé pour confirmer la modification d'un rendez-vous auprès d'un client
     * @param name
     * @param link
     * @param date
     * @param comm
     * @return Le mail sous forme de String
     */
    public String buildEmailModificationRdv(String name, String link, LocalDateTime date, String comm) {
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
                "                        <h1>"+ name +", votre rendez-vous a bien été modifié!</h1>\n" +
                "                        <h2>Bonjour, votre demande a bien été traitée. Voici les détails concernant votre nouveau rendez-vous:</h2>\n" +
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

    /**
     * Crée un mail personalisé pour confirmer la suppression de rendez-vous auprès d'un client
     * @param name
     * @param link
     * @param date
     * @param comm
     * @return Le mail sous forme de String
     */
    public String buildEmailAnnulationRdv(String name, String link, LocalDateTime date, String comm) {
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
                "                        <h1>"+ name +", vous avez annulé un rendez-vous.</h1>\n" +
                "                        <h2>Bonjour, votre demande a bien été traitée. Le rendez-vous ci-dessous a bien été annulé:</h2>\n" +
                "                        <div class=\"card_container\">\n" +
                "                          <div class=\"left_panel\"></div>\n" +
                "                          <div class=\"détails\">\n" +
                "                            <div class=\"détails_élément\">Date: "+date.toLocalDate()+"</div>\n" +
                "                            <div class=\"détails_élément\">Heure: "+date.toLocalTime()+"</div>\n" +
                "                            <div class=\"détails_élément\">Moyen de communication:"+comm+"</div>\n" +
                "                          </div>\n" +
                "                        </div>\n" +
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

    /**
     * Crée un mail personalisé pour confirmer aux admins la prise de rendez-vous d'un client
     * @param name
     * @param firstname
     * @param date
     * @param comm
     * @param link
     * @return Le mail sous forme de String
     */
    public String buildEmailConfirmationRdvPsy(String name, String firstname, LocalDateTime date, String comm, String link ){
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
                "                        <h1>Nouveau rendez-vous pour "+ name +" "+ firstname +".</h1>\n" +
                "                        <h2>Bonjour, vous avez un nouveau rendez-vous. Voici les détails concernant ce dernier:</h2>\n" +
                "                        <div class=\"card_container\">\n" +
                "                          <div class=\"left_panel\"></div>\n" +
                "                          <div class=\"détails\">\n" +
                "                            <div class=\"détails_élément\">Elève: "+firstname+" "+name+"</div>\n" +
                "                            <div class=\"détails_élément\">Date: "+date.toLocalDate()+"</div>\n" +
                "                            <div class=\"détails_élément\">Heure: "+date.toLocalTime()+"</div>\n" +
                "                            <div class=\"détails_élément\">Moyen de communication:"+comm+"</div>\n" +
                "                          </div>\n" +
                "                        </div>\n" +
                "                        <h2>Vous avez donc un rendez-vous avec "+firstname+" "+name+" le "+date.toLocalDate()+" à "+date.toLocalTime()+" sur "+comm+".</h2>\n" +
                "                        <h2>Pour accéder à votre site de réservation, cliquez sur le lien ci-dessous:</h2>\n" +
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


    /**
     * Crée un mail personalisé pour confirmer aux admins la modification du rendez-vous d'un client
     * @param name
     * @param firstname
     * @param ancienneDate
     * @param date
     * @param comm
     * @param link
     * @return Le mail sous forme de String
     */
    public String buildEmailModificationRdvPsy(String name, String firstname, LocalDateTime ancienneDate, LocalDateTime date, String comm, String link ){
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
                "                        <h1>Rendez-vous du "+ancienneDate.toLocalDate()+" avec "+ name +" "+ firstname +"modifié.</h1>\n" +
                "                        <h2>Bonjour, un de vos rendez vous a été modifié. Voici les nouveaux détails concernant ce dernier:</h2>\n" +
                "                        <div class=\"card_container\">\n" +
                "                          <div class=\"left_panel\"></div>\n" +
                "                          <div class=\"détails\">\n" +
                "                            <div class=\"détails_élément\">Elève: "+firstname+" "+name+"</div>\n" +
                "                            <div class=\"détails_élément\">Date: "+date.toLocalDate()+"</div>\n" +
                "                            <div class=\"détails_élément\">Heure: "+date.toLocalTime()+"</div>\n" +
                "                            <div class=\"détails_élément\">Moyen de communication:"+comm+"</div>\n" +
                "                          </div>\n" +
                "                        </div>\n" +
                "                        <h2>Vous avez donc un rendez-vous avec "+firstname+" "+name+" le "+date.toLocalDate()+" à "+date.toLocalTime()+" sur "+comm+".</h2>\n" +
                "                        <h2>Pour accéder à votre site de réservation, cliquez sur le lien ci-dessous:</h2>\n" +
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

    /**
     * Crée un mail personalisé pour confirmer la suppression d'un rendez-vous auprès des admins
     * @param name
     * @param firstname
     * @param date
     * @param comm
     * @param link
     * @return Le mail sous forme de String
     */
    public String buildEmailSuppressionPsy(String name, String firstname, LocalDateTime date, String comm, String link ){
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
                "                        <h1>Rendez-vous annulé par "+ name +" "+ firstname +".</h1>\n" +
                "                        <h2>Bonjour, l'un de vos rendez-vous a été annulé. Voici les détails concernant ce dernier:</h2>\n" +
                "                        <div class=\"card_container\">\n" +
                "                          <div class=\"left_panel\"></div>\n" +
                "                          <div class=\"détails\">\n" +
                "                            <div class=\"détails_élément\">Elève: "+firstname+" "+name+"</div>\n" +
                "                            <div class=\"détails_élément\">Date: "+date.toLocalDate()+"</div>\n" +
                "                            <div class=\"détails_élément\">Heure: "+date.toLocalTime()+"</div>\n" +
                "                            <div class=\"détails_élément\">Moyen de communication:"+comm+"</div>\n" +
                "                          </div>\n" +
                "                        </div>\n" +
                "                        <h2>Le rendez-vous du "+date.toLocalDate()+" à "+date.toLocalTime()+" sur "+comm+" avec "+firstname+" "+name+" a été annulé.</h2>\n" +
                "                        <h2>Pour accéder à votre site de réservation, cliquez sur le lien ci-dessous:</h2>\n" +
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
