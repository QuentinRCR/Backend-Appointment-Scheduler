package com.docto.protechdoctolib.communicationMeans;

import com.docto.protechdoctolib.creneaux.Creneaux;
import com.docto.protechdoctolib.creneaux.CreneauxDTO;
import com.docto.protechdoctolib.creneaux.HeuresDebutFin;
import com.docto.protechdoctolib.rendez_vous.Rendez_vous;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Met à disposition les apis pour gérer les CommunicationMeans
 */
@CrossOrigin //to allow cross-origin request from the vue application to the backend (hosted on the same computer)
@RestController
@RequestMapping("/api/communicationMean")
@Transactional
public class CommunicationMeanController {

    private CommunicationMeanDAO communicationMeanDAO;

    public CommunicationMeanController(CommunicationMeanDAO communicationMeanDAO) {
        this.communicationMeanDAO = communicationMeanDAO;
    }

    /**
     * Permet de renvoyer tous les moyens de communication qu'il y a dans la base de données
     * @return
     */
    @GetMapping(path = "/user")
    public List<CommunicationMeanDTO> findAll() {
        List<CommunicationMeans> aa = communicationMeanDAO.findAll();
        return aa.stream().map(CommunicationMeanDTO::new).collect(Collectors.toList());
    }

    /**
     * Supprime tous les moyens de communications qu'il y a dans la base de données
     */
    @DeleteMapping(path = "/admin/")
    public void deleteAll() {
        communicationMeanDAO.deleteAll();
    }

    /**
     * Ajoute un nouveau moyen de communication à la liste
     * @param dto
     * @return Le moyen de communication qui vient d'être créé
     */
    @PostMapping("/admin/create") // (8)
    public CommunicationMeanDTO create(@RequestBody CommunicationMeanDTO dto) {
        CommunicationMeans communicationMeans = communicationMeanDAO.save(new CommunicationMeans(dto.getCommunicationMean())); //Create new slot
        return new CommunicationMeanDTO(communicationMeans);
    }
}
