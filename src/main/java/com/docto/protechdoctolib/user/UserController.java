package com.docto.protechdoctolib.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;

import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@CrossOrigin //to allow cross-origin request from the vue application to the backend (hosted on the same computer)
@RestController
@RequestMapping("/api/user")
@Transactional
public class UserController {

    private final UserRepository userDAO;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserController(UserRepository userDAO, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userDAO = userDAO;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * Donne la liste de tous les utilisateurs
     *
     * @return une liste de tous les créneaux
     */
    @GetMapping
    public List<UserDTO> findAll() {
        return userDAO.findAll().stream().map(UserDTO::new).collect(Collectors.toList());
    }

    /**
     * Renvoi toutes les infos utiles au front-end
     * @param id
     * @return
     */
    @GetMapping(path = "/{id}")
    public UserDTO findById(@PathVariable Long id) {
        UserDTO userDTO = userDAO.findById(id).map(UserDTO::new).orElse(null);
        if (userDTO == null) {
            throw new ResponseStatusException( //if not found throw 404 error
                    HttpStatus.NOT_FOUND, "user not found"
            );
        } else {
            return userDTO;
        }

    }


    /**
     * Prend un dto de User en paramètre,et modifier l'utilisateur en question avec les paramètres
     *
     * @param dto
     * @return le dto du créneau crée
     */
    @PostMapping("/modify") // (8)
    public UserDTO modify(@RequestBody UserDTO dto) {
        User user = null;


        if (dto.getId() == null) { // Throw error if no user id defined
            throw new ResponseStatusException( //if not found throw 404 error
                    HttpStatus.UNPROCESSABLE_ENTITY, "user id not specified"
            );
        }
        else { //modify slot (id is not null)
            try {
                user = userDAO.getReferenceById(dto.getId());  // Assign each of the new values
                user.setPrenom(dto.getFirstName());
                user.setNom(dto.getLastName());
                user.setPhonenumber(dto.getPhoneNumber());
                user.setSkypeAccount(dto.getSkypeAccount());
                user.setCampus(dto.getCampus());
                user.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));

            } catch (EntityNotFoundException e) { //if slot not found, throw 404 error
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "user id not found"
                );
            }
        }

        return new UserDTO(user);
    }
}