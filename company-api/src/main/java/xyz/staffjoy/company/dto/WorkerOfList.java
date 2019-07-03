package xyz.staffjoy.company.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerOfList {
    private String userId;
    @Builder.Default
    private List<TeamDto> teams = new ArrayList<TeamDto>();
}
