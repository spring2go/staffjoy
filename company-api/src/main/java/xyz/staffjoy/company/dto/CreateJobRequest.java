package xyz.staffjoy.company.dto;

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
public class CreateJobRequest {
    @NotBlank
    private String companyId;
    @NotBlank
    private String teamId;
    @NotBlank
    private String name;
    @NotBlank
    private String color;
}
