package xyz.staffjoy.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatePasswordRequest {
    @NotBlank
    private String userId;
    @NotBlank
    @Size(min = 6)
    private String password;
}
