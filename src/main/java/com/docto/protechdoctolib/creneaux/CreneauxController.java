package com.docto.protechdoctolib.creneaux;

import com.docto.protechdoctolib.rendez_vous.Rendez_vous;
import com.docto.protechdoctolib.rendez_vous.Rendez_vousDAO;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@CrossOrigin //to allow cross-origin request from the vue application to the backend (hosted on the same computer)
@RestController
@RequestMapping("/api/creneaux")
@Transactional
public class CreneauxController {

    private final CreneauxDAO creneauxDAO;
    private final HeuresDebutFinDAO heuresDebutFinDAO;

    private final Rendez_vousDAO rendez_vousDAO;

    private static final Logger logger = LogManager.getLogger(CreneauxController.class);

    public CreneauxController(CreneauxDAO creneauxDAO, HeuresDebutFinDAO heuresDebutFinDAO, Rendez_vousDAO rendez_vousDAO) {
        this.creneauxDAO = creneauxDAO;
        this.heuresDebutFinDAO = heuresDebutFinDAO;
        this.rendez_vousDAO = rendez_vousDAO;
    }

    /**
     * Donne la liste de tous les créneaux
     *
     * @return une liste de tous les créneaux
     */
    @GetMapping(path = "/user")
    public List<CreneauxDTO> findAll() {
        logger.info("la fonction findAll à été utilisé");
        List<Creneaux> listCreneau= creneauxDAO.findAll();
        if(listCreneau.size()==0){
            logger.debug("La liste de créneaux renvoyés par findAll est vide");
        }
        return listCreneau.subList(0,Integer.min(30,listCreneau.size())).stream().map(CreneauxDTO::new).collect(Collectors.toList()); //the subList is to avoid loading useless slots
    }

    /**
     * Renvoi le créneau ayant pour id le paramètre
     *
     * @param id
     * @return créneau
     */
    @GetMapping(path = "/user/{id}")
    public CreneauxDTO findById(@PathVariable Long id) {
        logger.info("la fonction findById à été utilisé avec l'id " + id.toString());
        CreneauxDTO creneauId = creneauxDAO.findById(id).map(CreneauxDTO::new).orElse(null);
        if (creneauId == null) {
            logger.error("id créneau non trouvé dans finById");
            throw new ResponseStatusException( //if not found throw 404 error
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        } else {
            return creneauId;
        }

    }

    /**
     * Supprime le créneau ayant pour id le paramètre et supprime tous les rendez-vous qui se trouvaient dans ce créneau
     *
     * @param id id du créneau à supprimer
     */
    @DeleteMapping(path = "/admin/{id}")
    public void deleteParId(@PathVariable Long id) {
        logger.info("le créneau avec l'id " + id.toString() + " a été supprimé");
        try {
            creneauxDAO.deleteById(id); //delete the slot
            List<Rendez_vous> listRendezVous = rendez_vousDAO.findAllByIdCreneau(id); //get appointments that where registered in this slot
            for (Rendez_vous rdv: listRendezVous){ //Delete them
                rendez_vousDAO.deleteById(rdv.getId());
            };
        } catch (EmptyResultDataAccessException e) {
            logger.warn("Le créneau à supprimé n'existe pas");
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        }
    }


    /**
     * Prend un dto de créneau en paramètre, crée ce créneau dans la db si son id est null et le modifie si son id existe déjà
     *
     * @param dto
     * @return le dto du créneau crée
     */
    @PostMapping("/admin/create_or_modify") // (8)
    public CreneauxDTO create_or_modify(@RequestBody CreneauxDTO dto) {
        Creneaux creneaux = null;
        HeuresDebutFin heuresDebutFin1 = null;
        List listHeureDebutFin = new ArrayList<>(); //To store what is added in order to return the corrected dto

        // On creation id is not defined
        if (dto.getId() == null) {
            creneaux = creneauxDAO.save(new Creneaux(null, dto.getDateDebut(), dto.getDateFin(), dto.getJours(), dto.getHeuresDebutFin().stream().map(HeuresDebutFin::new).collect(Collectors.toList()))); //Create new slot
            for (HeuresDebutFin heuresDebutFin : dto.getHeuresDebutFin().stream().map(HeuresDebutFin::new).collect(Collectors.toList())) { //add each element from the list of heuresDebutFin to the corresponding db
                heuresDebutFin.setIdCreneaux(creneaux.getId()); //Assign the correct slot id
                HeuresDebutFin saved = heuresDebutFinDAO.save(heuresDebutFin);
                listHeureDebutFin.add(saved);
            }

            logger.info("un nouveau créneau a été crée, il a pour id " + creneaux.getId().toString());
        } else { //modify slot (id is not null)
            try {
                creneaux = creneauxDAO.getReferenceById(dto.getId());  // Assign each of the new values
                creneaux.setDateDebut(dto.getDateDebut());
                creneaux.setDateFin(dto.getDateFin());
                creneaux.setJours(dto.getJours());
                creneaux.setHeuresDebutFin(dto.getHeuresDebutFin().stream().map(HeuresDebutFin::new).collect(Collectors.toList()));

                //Delete and recreate all the slots to avoid issues with the number of slots existing
                for (HeuresDebutFin ancienheuresDebutFin : heuresDebutFinDAO.findByIdCreneaux(dto.getId())) { //add every old slot
                    heuresDebutFinDAO.deleteById(ancienheuresDebutFin.getIdPlage());
                }

                for (HeuresDebutFin heuresDebutFin : dto.getHeuresDebutFin().stream().map(HeuresDebutFin::new).collect(Collectors.toList())) { //Create all new slot
                    heuresDebutFin.setIdCreneaux(creneaux.getId());
                    HeuresDebutFin save = heuresDebutFinDAO.save(heuresDebutFin);
                    listHeureDebutFin.add(save);
                }

                logger.info("le créneau avec l'id " + creneaux.getId().toString() + " a été modifié");

            } catch (EntityNotFoundException e) { //if slot not found, throw 404 error
                logger.error("id créneau non trouvé, pour en créer un nouveau, mettre null pour son id");
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "entity not found"
                );
            }
        }
        creneaux.setHeuresDebutFin(listHeureDebutFin);
        return new CreneauxDTO(creneaux);
    }
}