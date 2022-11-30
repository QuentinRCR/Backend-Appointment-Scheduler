package com.docto.protechdoctolib.rendez_vous;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.docto.protechdoctolib.creneaux.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@CrossOrigin
@RestController // (1)
@RequestMapping("/api/rendez_vous") // (2)
@Transactional // (3)
public class Rendez_vousController {

    private final CreneauxDAO creneauxDAO;
    private final Rendez_vousDAO rendez_vousDAO;

    private static final Logger logger = LogManager.getLogger(CreneauxController.class);

    public Rendez_vousController(CreneauxDAO creneauxDAO, Rendez_vousDAO rendez_vousDAO) {
        this.creneauxDAO = creneauxDAO;
        this.rendez_vousDAO = rendez_vousDAO;
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
}
