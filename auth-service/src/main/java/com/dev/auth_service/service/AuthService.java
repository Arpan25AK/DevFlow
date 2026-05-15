package com.dev.auth_service.service;

import com.dev.auth_service.entity.User;
import com.dev.auth_service.repo.UserRepository;
import com.dev.auth_service.security.JwtUtill;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtill jwtUtill;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtill jwtUtill){
        this.jwtUtill = jwtUtill;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public String registerUser(String email,String rawpassword){

        if(userRepository.existsByEmail(email)) throw new RuntimeException("user already found");

        String hashedPassword = passwordEncoder.encode(rawpassword);

        User newUser = User.builder()
                .email(email)
                .password(hashedPassword)
                .role("ROLE-DEVELOPER")
                .build();

        userRepository.save(newUser);

        return "new User registered";
    }

    public String login(String email, String rawpassord){

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("user not found"));

        if(!passwordEncoder.matches(rawpassord, user.getPassword())){
            throw new RuntimeException("incorrect password");
        }

        return jwtUtill.generateToken(user.getId().toString(), user.getRole());
    }
}
