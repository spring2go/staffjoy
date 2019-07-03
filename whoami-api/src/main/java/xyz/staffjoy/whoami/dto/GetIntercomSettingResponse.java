package xyz.staffjoy.whoami.dto;

import lombok.*;
import xyz.staffjoy.common.api.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GetIntercomSettingResponse extends BaseResponse {
    private IntercomSettingsDto intercomSettings;
}
