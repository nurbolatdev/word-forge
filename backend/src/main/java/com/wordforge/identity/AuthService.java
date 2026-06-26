package com.wordforge.identity;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    AuthDto.TokenResponse register(AuthDto.RegisterRequest req) {
        if (userRepo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User(req.email(), passwordEncoder.encode(req.password()));
        if (req.nativeLang() != null) user.setNativeLang(req.nativeLang());
        user = userRepo.save(user);
        return new AuthDto.TokenResponse(jwtService.createToken(user.getId()), user.getId(), user.getEmail());
    }

    AuthDto.TokenResponse login(AuthDto.LoginRequest req) {
        User user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return new AuthDto.TokenResponse(jwtService.createToken(user.getId()), user.getId(), user.getEmail());
    }
}
