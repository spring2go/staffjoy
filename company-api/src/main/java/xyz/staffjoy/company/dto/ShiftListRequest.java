package xyz.staffjoy.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftListRequest {
    @NotBlank
    private String companyId;
    @NotBlank
    private String teamId;
    private String userId;
    private String jobId;
    @NotNull
    private Instant shiftStartAfter;
    @NotNull
    private Instant shiftStartBefore;

    @AssertTrue(message = "shift_start_after must be before shift_start_before")
    private boolean correctAfterAndBefore() {
        long duration = shiftStartAfter.toEpochMilli() - shiftStartBefore.toEpochMilli();
        return duration < 0;
    }
}
