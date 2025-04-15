package com.fxapp.currencyconversion.service.mapper;

import com.fxapp.currencyconversion.dtos.user.UserDTO;
import com.fxapp.currencyconversion.entities.User;

public class UserMapper {
  public static UserDTO mapConversionHistoryToDTO(User user) {
    return UserDTO.builder().userId(user.getId()).userName(user.getUsername()).build();
  }
}
