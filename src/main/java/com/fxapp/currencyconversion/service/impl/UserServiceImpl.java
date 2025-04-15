package com.fxapp.currencyconversion.service.impl;

import com.fxapp.currencyconversion.constants.ResultCode;
import com.fxapp.currencyconversion.dtos.user.CreateUserRequestDTO;
import com.fxapp.currencyconversion.dtos.user.CreateUserResponseDTO;
import com.fxapp.currencyconversion.dtos.user.UserDTO;
import com.fxapp.currencyconversion.entities.User;
import com.fxapp.currencyconversion.exception.FxException;
import com.fxapp.currencyconversion.repos.UserRepository;
import com.fxapp.currencyconversion.service.UserService;
import com.fxapp.currencyconversion.service.mapper.UserMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @PostConstruct
  public void initUsers() {
    if (userRepository.count() == 0) {
      userRepository.saveAll(
          List.of(new User(null, "jane-doe"), new User(null, "john-doe"), new User(null, "doe")));
    }
  }

  @Override
  public CreateUserResponseDTO createUser(CreateUserRequestDTO request) {
    if (StringUtils.isEmpty(request.getUserName())) {
      throw new FxException(ResultCode.NULL_USER_NAME);
    }

    User user = User.builder().username(request.getUserName()).build();

    user = userRepository.saveAndFlush(user);
    log.info("User {} created with id {}", user.getUsername(), user.getId());

    return CreateUserResponseDTO.builder().userId(user.getId()).build();
  }

  @Override
  public List<UserDTO> getAllUsers() {
    List<User> allUsers = userRepository.findAll();

    return allUsers.stream().map(UserMapper::mapConversionHistoryToDTO).toList();
  }
}
