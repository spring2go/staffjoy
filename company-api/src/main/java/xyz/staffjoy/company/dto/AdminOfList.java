package xyz.staffjoy.company.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOfList {
    private String userId;
    @Builder.Default
    private List<CompanyDto> companies = new ArrayList<CompanyDto>();
}
