package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.*;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDTO userDTO;
    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("password123");
        userDTO.setBalance(BigDecimal.valueOf(100));
        userDTO.setActive(true);
        userDTO.setId(userId);

        user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setBalance(BigDecimal.valueOf(100));
        user.setActive(true);
    }

    @Test
    void shouldRegisterUserSuccessfully_WhenUsernameIsAvailable() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userMapper.toUser(userDTO)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.register(userDTO);

        assertEquals(userDTO, result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowUsernameTakenException_WhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(messageSource.getMessage(eq("user.exists.username"), any(), any())).thenReturn("Username already exists");

        assertThrows(UsernameTakenException.class, () -> userService.register(userDTO));
    }

    @Test
    void shouldUpdateUsernameSuccessfully_WhenUsernameIsChanged() {
        userDTO.setUsername("newUser");
        when(userRepository.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO updated = userService.updateUser("testuser", userDTO);

        assertEquals(userDTO, updated);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowUsernameUnchangedException_WhenUsernameIsUnchanged() {
        userDTO.setUsername("testuser");
        when(userRepository.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(messageSource.getMessage(eq("user.username.unchanged"), any(), any())).thenReturn("Username unchanged");

        assertThrows(UserUsernameUnchangedException.class,
                () -> userService.updateUser("testuser", userDTO));
    }

    @Test
    void shouldThrowUsernameTakenException_WhenNewUsernameIsTaken() {
        userDTO.setUsername("newUser");
        when(userRepository.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newUser")).thenReturn(true);
        when(messageSource.getMessage(eq("user.exists.username"), any(), any())).thenReturn("Username taken");

        assertThrows(UsernameTakenException.class,
                () -> userService.updateUser("testuser", userDTO));
    }

    @Test
    void shouldThrowUserNotFoundException_WhenUserDoesNotExistForUpdate() {
        String username = "nonexistentUser";
        UserDTO userDTO = new UserDTO();

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());

        when(messageSource.getMessage(eq("user.not-found.username"), any(), any()))
                .thenReturn("User not found");

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(username, userDTO)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findUserByUsername(username);
        verify(messageSource).getMessage(eq("user.not-found.username"), any(), any());
    }

    @Test
    void shouldGetUserByUsernameSuccessfully() {
        when(userRepository.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.getUserByUsername("testuser");

        assertEquals(userDTO, result);
    }

    @Test
    void shouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findUserByUsername("missing")).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("user.not-found.username"), any(), any())).thenReturn("User not found");

        assertThrows(UserNotFoundException.class, () -> userService.getUserByUsername("missing"));
    }

    @Test
    void shouldChangeUserStatusSuccessfully_WhenStatusIsChanged() {
        userDTO.setActive(false);
        user.setActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.changeAccountStatus(userDTO);

        assertEquals(userDTO, result);
    }

    @Test
    void shouldThrowUserStatusUnchangedException_WhenStatusIsUnchanged() {
        userDTO.setActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(messageSource.getMessage(eq("user.status.unchanged"), any(), any())).thenReturn("Status unchanged");

        assertThrows(UserStatusUnchangedException.class, () -> userService.changeAccountStatus(userDTO));
    }

    @Test
    void shouldChangeUserBalanceSuccessfully_WhenBalanceIsValid() {
        userDTO.setBalance(BigDecimal.valueOf(200));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.changeUserBalance(userDTO);

        assertEquals(userDTO, result);
    }

    @Test
    void shouldThrowBalanceInvalidException_WhenBalanceIsNull() {
        userDTO.setBalance(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(messageSource.getMessage(eq("user.balance.null"), any(), any())).thenReturn("Balance is null");

        assertThrows(UserBalanceInvalid.class, () -> userService.changeUserBalance(userDTO));
    }

    @Test
    void shouldThrowBalanceInvalidException_WhenBalanceIsNegative() {
        userDTO.setBalance(BigDecimal.valueOf(-50));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(messageSource.getMessage(eq("user.balance.negative"), any(), any())).thenReturn("Balance is negative");

        assertThrows(UserBalanceInvalid.class, () -> userService.changeUserBalance(userDTO));
    }

    @Test
    void shouldReturnTrueIfUserExistsById_WhenUserExists() {
        when(userRepository.existsById(userId)).thenReturn(true);
        assertTrue(userService.userExistsById(userId));
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.getUserById(userId);

        assertEquals(userDTO, result);
    }

    @Test
    void shouldThrowUserNotFoundException_WhenUserDoesNotExistById() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("user.not-found.id"), any(), any())).thenReturn("User not found");

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void shouldReturnAllActiveUsersSuccessfully() {
        when(userRepository.findAllByActive(true)).thenReturn(List.of(user));
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        List<UserDTO> result = userService.findAllActive();

        assertEquals(1, result.size());
        assertEquals(userDTO, result.get(0));
    }

}
