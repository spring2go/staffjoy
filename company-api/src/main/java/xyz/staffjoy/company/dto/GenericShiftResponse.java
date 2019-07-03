package xyz.staffjoy.company.dto;

import lombok.*;
import xyz.staffjoy.common.api.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GenericShiftResponse extends BaseResponse {
    private ShiftDto shift;
}
