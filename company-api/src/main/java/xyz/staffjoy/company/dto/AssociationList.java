package xyz.staffjoy.company.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssociationList {
    @Builder.Default
    private List<Association> accounts = new ArrayList<>();
    private int limit;
    private int offset;
}
