package com.fxapp.currencyconversion.dtos.user;

import java.util.UUID;
import lombok.*;

@Builder
@Getter
@Setter
@Data
@AllArgsConstructor
public class CreateUserResponseDTO {
  private UUID userId;
}
