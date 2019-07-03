package xyz.staffjoy.company.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminEntries {
    private String companyId;
    @Builder.Default
    private List<DirectoryEntryDto> admins = new ArrayList<DirectoryEntryDto>();
}
