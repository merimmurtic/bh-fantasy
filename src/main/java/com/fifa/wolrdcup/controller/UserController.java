package com.fifa.wolrdcup.controller;

import com.fifa.wolrdcup.model.User;
import com.fifa.wolrdcup.repository.UserRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/user/me")
    @Secured("ROLE_USER")
    public User user(OAuth2Authentication authentication) {
        User user = (User) authentication.getUserAuthentication().getPrincipal();
        return userRepository.findByPrincipalId(user.getPrincipalId());
    }
}
