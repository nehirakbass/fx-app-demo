package com.fxapp.currencyconversion.service;

import com.fxapp.currencyconversion.dtos.user.CreateUserRequestDTO;
import com.fxapp.currencyconversion.dtos.user.CreateUserResponseDTO;
import com.fxapp.currencyconversion.dtos.user.UserDTO;
import java.util.List;

public interface UserService {
  public CreateUserResponseDTO createUser(CreateUserRequestDTO request);

  public List<UserDTO> getAllUsers();
}
