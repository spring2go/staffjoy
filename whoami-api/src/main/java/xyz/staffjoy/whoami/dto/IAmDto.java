package xyz.staffjoy.whoami.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.staffjoy.company.dto.AdminOfList;
import xyz.staffjoy.company.dto.WorkerOfList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IAmDto {
    private boolean support;
    private String userId;
    private WorkerOfList workerOfList;
    private AdminOfList adminOfList;
}
