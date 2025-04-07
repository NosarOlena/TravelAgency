package com.epam.finaltask.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.ConflictException;
import com.epam.finaltask.exception.UserBalanceInvalid;
import com.epam.finaltask.exception.UserNotFoundException;
import com.epam.finaltask.exception.UserStatusUnchangedException;
import com.epam.finaltask.exception.UserUsernameUnchangedException;
import com.epam.finaltask.exception.UsernameTakenException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	private final MessageSource messageSource;

	private final PasswordEncoder passwordEncoder;

	@Override
	public UserDTO register(UserDTO userDTO) {
		if(userRepository.existsByUsername(userDTO.getUsername())){
			String message = messageSource.getMessage("user.exists.username",
					new Object[]{userDTO.getUsername()}, LocaleContextHolder.getLocale());
			log.warn("Registration failed: username {} already taken", userDTO.getUsername());
			throw new UsernameTakenException(message);
		}

		String encodedPassword = passwordEncoder.encode(userDTO.getPassword());

		User user = userMapper.toUser(userDTO);

		user.setPassword(encodedPassword);

		User savedUser = userRepository.save(user);

		log.info("User registered: id={}, username={}", savedUser.getId(), savedUser.getUsername());

		return userMapper.toUserDTO(savedUser);
	}

	@Override
	public UserDTO updateUser(String username, UserDTO userDTO) {
		User user = userRepository.findUserByUsername(username).orElseThrow(() -> {
			String message = messageSource.getMessage("user.not-found.username",
					new Object[]{username}, LocaleContextHolder.getLocale());
			log.warn("Update failed: user {} not found", username);
			return new UserNotFoundException(message);
		});

		if (userDTO.getUsername() != null) {
			if(userDTO.getUsername().equals(username)){
				String message = messageSource.getMessage("user.username.unchanged",
						null, LocaleContextHolder.getLocale());
				log.warn("Update failed: username {} unchanged", username);
				throw new UserUsernameUnchangedException(message);
			}

			if(userRepository.existsByUsername(userDTO.getUsername())){
				String message = messageSource.getMessage("user.exists.username",
						new Object[]{userDTO.getUsername()}, LocaleContextHolder.getLocale());
				log.warn("Update failed: new username {} already taken", userDTO.getUsername());
				throw new UsernameTakenException(message);
			}
			user.setUsername(userDTO.getUsername());
		}
		if (userDTO.getPassword() != null) {
			user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		}
		if (userDTO.getPhoneNumber() != null) {
			user.setPhoneNumber(userDTO.getPhoneNumber());
		}

		User savedUser = userRepository.save(user);

		log.info("User with id {} updated", username);

		return userMapper.toUserDTO(savedUser);
	}

	@Override
	public UserDTO getUserByUsername(String username) {
		User user = userRepository.findUserByUsername(username).orElseThrow(() -> {
			String message = messageSource.getMessage("user.not-found.username",
					new Object[]{username}, LocaleContextHolder.getLocale());
			return new UserNotFoundException(message);
		});

		return userMapper.toUserDTO(user);
	}

	@Override
	public UserDTO changeAccountStatus(UserDTO userDTO) {
		User user = findUserByIdOrUsername(userDTO);

		if(user.isActive() == userDTO.getActive()){
			throw new UserStatusUnchangedException(messageSource.getMessage("user.status.unchanged",
					null, LocaleContextHolder.getLocale()));
		}

		user.setActive(userDTO.getActive());

		User savedUser = userRepository.save(user);

		log.info("User status changed: id={}, active={}", savedUser.getId(), savedUser.isActive());

		return userMapper.toUserDTO(savedUser);
	}

	@Override
	public UserDTO changeUserBalance(UserDTO userDTO) {
		User user = findUserByIdOrUsername(userDTO);

		if (userDTO.getBalance() == null) {
			log.warn("Balance change failed: null balance for user id {}", user.getId());
			throw new UserBalanceInvalid(messageSource.getMessage(
					"user.balance.null", null, LocaleContextHolder.getLocale()));
		}

		if (userDTO.getBalance().compareTo(BigDecimal.ZERO) < 0) {
			log.warn("Balance change failed: negative balance {} for user id {}",
					userDTO.getBalance(), user.getId());
			throw new UserBalanceInvalid(messageSource.getMessage(
					"user.balance.negative", null, LocaleContextHolder.getLocale()));
		}

		BigDecimal oldBalance = user.getBalance();
		user.setBalance(userDTO.getBalance());

		User savedUser = userRepository.save(user);

		log.info("Balance updated: userId={}, oldBalance={}, newBalance={}",
				savedUser.getId(), oldBalance, savedUser.getBalance());

		return userMapper.toUserDTO(savedUser);
	}

	@Override
	public UserDTO getUserById(UUID userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> {
			String message = messageSource.getMessage("user.not-found.id",
					new Object[]{userId}, LocaleContextHolder.getLocale());
			log.warn("User with id {} not found", userId);
			return new UserNotFoundException(message);
		});

		log.info("User found: id={}", user.getId());

		return userMapper.toUserDTO(user);
	}


	@Override
	public List<UserDTO> findAll(){
		log.info("Fetching all users");
		return mapToDtoList(userRepository.findAll());
	}

	@Override
	public List<UserDTO> findAllActive() {
		log.info("Fetching all ACTIVE users");
		return mapToDtoList(userRepository.findAllByActive(true));
	}

	@Override
	public List<UserDTO> findAllBlocked() {
		log.info("Fetching all BLOCKED users");
		return mapToDtoList(userRepository.findAllByActive(false));
	}

	@Override
	public boolean userExistsById(UUID userId) {
		return userRepository.existsById(userId);
	}

	private User findUserByIdOrUsername(UserDTO userDTO) {
		UUID userId = userDTO.getId();
		String username = userDTO.getUsername();

		User user;

		User userById = null;
		User userByUsername = null;
		if(userId != null){
			userById = userRepository.findById(userId).orElse(null);
		}
		if(username != null){
			userByUsername = userRepository.findUserByUsername(username).orElse(null);
		}

		if(userById != null && userByUsername != null && !userById.equals(userByUsername)){
			throw new ConflictException(messageSource.getMessage("user.conflict.id-username",
					new Object[] {userId, username}, LocaleContextHolder.getLocale()));

		}

		user = userById != null ? userById : userByUsername;

		if (user == null) {
			throw new UserNotFoundException(messageSource.getMessage(
					"user.not-found", null, LocaleContextHolder.getLocale()));
		}

		return user;
	}

	private List<UserDTO> mapToDtoList(List<User> users) {
		return (users == null || users.isEmpty()) ? List.of() :
				users.stream()
						.map(userMapper::toUserDTO)
						.toList();
	}

}
