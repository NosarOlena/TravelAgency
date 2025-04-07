package com.epam.finaltask.service;

import java.util.List;
import java.util.UUID;

import com.epam.finaltask.dto.UserDTO;

public interface UserService {
    UserDTO register(UserDTO userDTO);

    UserDTO updateUser(String username, UserDTO userDTO);

    UserDTO getUserByUsername(String username);
    UserDTO getUserById(UUID userId);

    UserDTO changeAccountStatus(UserDTO userDTO);
    UserDTO changeUserBalance(UserDTO userDTO);

    List<UserDTO> findAll();
    List<UserDTO> findAllActive();
    List<UserDTO> findAllBlocked();

    boolean userExistsById(UUID userId);
}
