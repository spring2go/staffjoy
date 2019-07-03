package xyz.staffjoy.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsRequest {
    @NotBlank(message = "Please provide a phone number")
    private String to;
    @NotBlank(message = "Please provide a template code")
    private String templateCode;
    private String templateParam;
}
