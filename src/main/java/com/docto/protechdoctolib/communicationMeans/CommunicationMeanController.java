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

@CrossOrigin //to allow cross-origin request from the vue application to the backend (hosted on the same computer)
@RestController
@RequestMapping("/api/communicationMean")
@Transactional
public class CommunicationMeanController {

    private CommunicationMeanDAO communicationMeanDAO;

    public CommunicationMeanController(CommunicationMeanDAO communicationMeanDAO) {
        this.communicationMeanDAO = communicationMeanDAO;
    }

    @GetMapping(path = "/user")
    public List<CommunicationMeanDTO> findAll() {
        List<CommunicationMeans> aa = communicationMeanDAO.findAll();
        return aa.stream().map(CommunicationMeanDTO::new).collect(Collectors.toList());
    }

    @DeleteMapping(path = "/admin/")
    public void deleteAll() {
        communicationMeanDAO.deleteAll();
    }

    @PostMapping("/admin/create_or_modify") // (8)
    public CommunicationMeanDTO create_or_modify(@RequestBody CommunicationMeanDTO dto) {
        CommunicationMeans communicationMeans;
        communicationMeans = communicationMeanDAO.save(new CommunicationMeans(dto.getCommunicationMean())); //Create new slot
        return new CommunicationMeanDTO(communicationMeans);
    }
}
