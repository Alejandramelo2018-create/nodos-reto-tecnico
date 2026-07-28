package com.nodo.retotecnico.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.nodo.retotecnico.model.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentUserDTO {
    private Integer id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String country;
    private String role;
    private Boolean betaTester;
    private Integer completedChallenges;
    private Boolean hasPassword;

    public static CurrentUserDTO fromUser(User user) {
        return new CurrentUserDTO(
            user.getId(),
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getCountry(),
            user.getRole(),
            user.getBetaTester(),
            user.getCompletedChallenges(),
            user.getPassword() != null && !user.getPassword().isEmpty()
        );
    }
}
