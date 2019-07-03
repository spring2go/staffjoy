package xyz.staffjoy.company.dto;

import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftList {
    @Builder.Default
    private List<ShiftDto> shifts = new ArrayList<>();
    private Instant shiftStartAfter;
    private Instant shiftStartBefore;
}
