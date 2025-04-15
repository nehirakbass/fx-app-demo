package com.fxapp.currencyconversion.controller;

import com.fxapp.currencyconversion.dtos.user.CreateUserRequestDTO;
import com.fxapp.currencyconversion.dtos.user.CreateUserResponseDTO;
import com.fxapp.currencyconversion.dtos.user.UserDTO;
import com.fxapp.currencyconversion.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/create-user")
  @Operation(summary = "Creates new user")
  public ResponseEntity<CreateUserResponseDTO> createUser(
      @RequestBody CreateUserRequestDTO request) {
    return ResponseEntity.ok(userService.createUser(request));
  }

  @GetMapping("/get-all-users")
  @Operation(summary = "Retrieves all users")
  public ResponseEntity<List<UserDTO>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }
}
