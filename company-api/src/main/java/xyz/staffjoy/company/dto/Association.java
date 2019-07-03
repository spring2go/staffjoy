package xyz.staffjoy.company.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Association {
    private DirectoryEntryDto account;
    @Builder.Default
    private List<TeamDto> teams = new ArrayList<TeamDto>();
    private Boolean admin;
}
