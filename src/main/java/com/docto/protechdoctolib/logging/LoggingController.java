package com.docto.protechdoctolib.logging;

import com.docto.protechdoctolib.user.User;
import com.docto.protechdoctolib.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LoggingController {
    private final UserService userService;

    public LoggingController(UserService userService) {
        this.userService = userService;
    }
    // TODO CHECH VIDEO 31:50 if bug
    @GetMapping("/users")
    public ResponseEntity<List<User>>getUsers(){
        return ResponseEntity.ok().body(userService.getUsers());
    }
}
