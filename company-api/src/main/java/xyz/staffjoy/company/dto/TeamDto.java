package xyz.staffjoy.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.staffjoy.common.validation.DayOfWeek;
import xyz.staffjoy.common.validation.Timezone;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamDto {
    @NotBlank
    private String id;
    @NotBlank
    private String companyId;
    @NotBlank
    private String name;
    private boolean archived;
    @Timezone
    @NotBlank
    private String timezone;
    @DayOfWeek
    @NotBlank
    private String dayWeekStarts;
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
    @NotBlank
    private String color;
}
