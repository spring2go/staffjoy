package xyz.staffjoy.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OnboardWorkerRequest {
    @NotBlank
    private String companyId;
    @NotBlank
    private String userId;
}
