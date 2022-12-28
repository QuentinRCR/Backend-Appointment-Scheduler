package com.docto.protechdoctolib.user;

import com.docto.protechdoctolib.registration.token.ConfirmationToken;
import com.docto.protechdoctolib.registration.token.ConfirmationTokenRepository;
import com.docto.protechdoctolib.registration.token.ConfirmationTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Fait des actions sur des Users
 */
@Service @Transactional @Slf4j
public class UserService implements UserDetailsService {

    private final static String USER_NOT_FOUND =
            "user with email %s not found";
    private final UserRepository userRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    public UserService(@Qualifier("users")UserRepository userRepository, ConfirmationTokenRepository confirmationTokenRepository, BCryptPasswordEncoder bCryptPasswordEncoder, ConfirmationTokenService confirmationTokenService) {
        this.userRepository = userRepository;
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.confirmationTokenService = confirmationTokenService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND,email)));
    }

    public List<User> getUsers(){
        return userRepository.findAll();

    }

    /** Fonction d'inscription
     * si l'email de la requête n'a pas déjà été pris par un autre utilisateur
     * Le mdp est crypté, l'utilisateur est sauvegardé et un token de confirmation est créé.
     * @param user
     * @return token de confirmation
     */
    public String signUpUser(User user){
        if(userRepository.findByEmail(user.getUsername()).isPresent()){
            if (userRepository.findByEmail(user.getUsername()).get().isEnabled()){
                throw new IllegalStateException("email already taken");
            }
            else{
               User user1=userRepository.findByEmail(user.getUsername()).get();
                user1.setCampus(user.getCampus());
                user1.setSkypeAccount(user.getSkypeAccount());
                user1.setNom(user.getNom());
                user1.setPrenom(user.getPrenom());
                user1.setPhonenumber(user.getPhonenumber());
                user1.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
                user=user1;
            }
        }
        else {
            String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());

            user.setPassword(encodedPassword);

            log.info("Registering new user to the database", user.getNom());
            userRepository.save(user);
        }

            String token = UUID.randomUUID().toString();

            ConfirmationToken confirmationToken = new ConfirmationToken(
                    token,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(15),
                    user
            );

            confirmationTokenService.saveConfirmationToken(confirmationToken);
            return token;

        }

    /**
     * Active le compte de l'utilisateur avec l'email en paramètre.
     * @param email
     */
    public int enableAppUser(String email) {
        return userRepository.enableUser(email);
    }
}
