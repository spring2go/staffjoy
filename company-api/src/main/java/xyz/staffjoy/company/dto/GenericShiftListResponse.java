package xyz.staffjoy.company.dto;

import lombok.*;
import xyz.staffjoy.common.api.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GenericShiftListResponse extends BaseResponse {
    private ShiftList shiftList;
}
