package com.nodo.retotecnico.service;

import java.util.List;

import com.nodo.retotecnico.dto.RegisterRequest;
import com.nodo.retotecnico.model.User;

public interface  UsersService {

    List<User> getAllUsers();
    User getUsersById(Integer id);
    Integer createUser(User user);
    Integer registerUser(RegisterRequest request);
    String registerAdmin(RegisterRequest request);
    User updateUser(Integer id, User user);
    void deleteUser(Integer id);
    User updateUserRole(Integer id, String role);
    void processOAuthPostLogin(String username, String email, String name, String firstName, String lastName);
}