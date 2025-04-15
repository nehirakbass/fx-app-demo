package com.fxapp.currencyconversion.dtos.user;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Data
public class UserDTO {
  private UUID userId;
  private String userName;
}
