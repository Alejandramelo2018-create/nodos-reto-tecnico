package com.nodo.retotecnico.serviceImpl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nodo.retotecnico.dto.RegisterRequest;
import com.nodo.retotecnico.model.User;
import com.nodo.retotecnico.repository.UserRepository;
import com.nodo.retotecnico.service.UsersService;

@Service
public class UsersServiceImpl implements UsersService{

    @Autowired
    private UserRepository UserRepository;

    @Autowired
    private com.nodo.retotecnico.repository.UserRepository specificUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAllUsers() {
        return UserRepository.findAll();
    }

    @Override
    public User getUsersById(Integer id){
        return UserRepository.findById(id).orElse(null);
    }
    
    @Override
    public Integer createUser(User user) {
        // Fallback for creating user directly if needed
        return UserRepository.save(user).getId();
    }

    public Integer registerUser(RegisterRequest request) {
        if (UserRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya está en uso.");
        }
        if (request.getEmail() != null && UserRepository.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("El correo electrónico ya está en uso.");
        }
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setName(request.getFirstName() + " " + request.getLastName());
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setCountry(request.getCountry());
        newUser.setRole("ROLE_USER");
        newUser.setRegistrationDate(new Date());
        newUser.setEmail(request.getEmail() != null ? request.getEmail() : request.getUsername() + "@example.com");

        return UserRepository.save(newUser).getId();
    }

    public String registerAdmin(RegisterRequest request) {
        var existing = UserRepository.findByUsername(request.getUsername());
        if (existing.isPresent()) {
            throw new RuntimeException("El nombre de usuario ya está en uso.");
        }

        registerUser(request);
        User createdUser = UserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Error creating admin user"));
        createdUser.setRole("ROLE_ADMIN");
        UserRepository.save(createdUser);

        return "Admin user created";
    }

    @Override
    public void processOAuthPostLogin(String username, String email, String name, String firstName, String lastName) {
        if (UserRepository.findByUsername(username).isEmpty()) {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setRole("ROLE_USER");
            newUser.setRegistrationDate(new Date());
            newUser.setPassword(""); // OAuth2 users might not need a password
            UserRepository.save(newUser);
        }
    }

     @Override
    public User updateUser(Integer id, User user){
        User existingUser = UserRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("User no found"));

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());

        return UserRepository.save(existingUser);
    }
    @Override
    public void deleteUser(Integer id){
        if (!UserRepository.existsById(id)){
            throw new RuntimeException("User no found");
        }
        UserRepository.deleteById(id);
    }

    @Override
    public User updateUserRole(Integer id, String role) {
        User user = UserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        return UserRepository.save(user);
    }
}


