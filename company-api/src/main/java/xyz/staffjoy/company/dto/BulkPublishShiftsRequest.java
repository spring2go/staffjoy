package xyz.staffjoy.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkPublishShiftsRequest {
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
    private boolean published;
}
