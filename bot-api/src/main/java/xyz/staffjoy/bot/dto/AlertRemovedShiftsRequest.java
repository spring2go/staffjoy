package xyz.staffjoy.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.staffjoy.company.dto.ShiftDto;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlertRemovedShiftsRequest {
    @NotBlank
    private String userId;
    @Builder.Default
    private List<ShiftDto> oldShifts = new ArrayList<>();
}
