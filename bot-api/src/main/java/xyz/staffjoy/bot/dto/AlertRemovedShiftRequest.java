package xyz.staffjoy.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.staffjoy.company.dto.ShiftDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlertRemovedShiftRequest {
    @NotBlank
    private String userId;
    @NotNull
    private ShiftDto oldShift;
}
