package xyz.staffjoy.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.time.Instant;

import static xyz.staffjoy.company.dto.CreateShiftRequest.MAX_SHIFT_DURATION;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder=true)
public class ShiftDto {
    @NotBlank
    private String id;
    @NotBlank
    private String companyId;
    @NotBlank
    private String teamId;
    @NotNull
    private Instant start;
    @NotNull
    private Instant stop;
    private String userId;
    private String jobId;
    @NotNull
    private boolean published;

    @AssertTrue(message = "stop must be after start")
    private boolean stopIsAfterStart() {
        long duration = stop.toEpochMilli() - start.toEpochMilli();
        return duration > 0;
    }

    @AssertTrue(message = "Shifts exceed max allowed hour duration")
    private boolean withInMaxDuration() {
        long duration = stop.toEpochMilli() - start.toEpochMilli();
        return duration <= MAX_SHIFT_DURATION;
    }
}
