package xyz.staffjoy.company.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobList {
    @Builder.Default
    private List<JobDto> jobs = new ArrayList<>();
}
