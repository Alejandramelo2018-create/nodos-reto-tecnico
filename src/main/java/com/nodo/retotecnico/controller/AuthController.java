package com.nodo.retotecnico.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nodo.retotecnico.dto.AuthResponse;
import com.nodo.retotecnico.dto.LoginRequest;
import com.nodo.retotecnico.dto.OAuth2Response;
import com.nodo.retotecnico.dto.RegisterRequest;
import com.nodo.retotecnico.security.JwtUtil;
import com.nodo.retotecnico.service.UsersService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsersService usersService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(usersService.registerAdmin(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            String token = jwtUtil.createToken(request.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        usersService.registerUser(request);
        String token = jwtUtil.createToken(request.getUsername());
        return new AuthResponse(token);
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<OAuth2Response> oauth2Success(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return ResponseEntity.badRequest().body(new OAuth2Response(null, "Authentication failed", null, null, null));
        }

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String provider = oauth2User.getAttribute("provider") != null ? 
            oauth2User.getAttribute("provider") : "google";

        String usernameToUse = email != null ? email : name;
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");

        // Delegar la verificación y creación al servicio
        usersService.processOAuthPostLogin(usernameToUse, email, name, firstName, lastName);

        String token = jwtUtil.createToken(usernameToUse);

        OAuth2Response response = new OAuth2Response(
            token,
            "OAuth2 login successful",
            provider,
            email,
            name
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            jwtUtil.invalidateToken(token);
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logout exitoso");
    }
}
