package xyz.staffjoy.sms.controller;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.sms.client.SmsClient;
import xyz.staffjoy.sms.dto.SmsRequest;
import xyz.staffjoy.sms.props.AppProps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@DirtiesContext // avoid port conflict
@EnableFeignClients(basePackages = {"xyz.staffjoy.sms.client"})
@Slf4j
public class SmsControllerTest {
    @Autowired
    SmsClient smsClient;

    @MockBean
    IAcsClient iAcsClient;

    @Autowired
    private AppProps appProps;

    @Test
    public void testSendSms() throws ClientException {
        SendSmsResponse sendSmsResponse = new SendSmsResponse();
        sendSmsResponse.setCode("OK");
        sendSmsResponse.setRequestId("mock_id");
        when(iAcsClient.getAcsResponse(any(SendSmsRequest.class))).thenReturn(sendSmsResponse);

        String phoneNumber = "18001112222";
        String templateCode = "TESTCODE_XXXXXX";
        String templateParam = "TESTPARAM_YYYYYY";
        SmsRequest smsRequest = SmsRequest.builder()
                .to(phoneNumber)
                .templateCode(templateCode)
                .templateParam(templateParam)
                .build();
        BaseResponse baseResponse = smsClient.send(AuthConstant.AUTHORIZATION_BOT_SERVICE, smsRequest);
        assertThat(baseResponse.isSuccess()).isTrue();

        // verify sms
        ArgumentCaptor<SendSmsRequest> argument = ArgumentCaptor.forClass(SendSmsRequest.class);
        verify(iAcsClient, times(1)).getAcsResponse(argument.capture());
        SendSmsRequest sendSmsRequest = argument.getValue();
        assertThat(sendSmsRequest.getPhoneNumbers()).isEqualTo(phoneNumber);
        assertThat(sendSmsRequest.getTemplateCode()).isEqualTo(templateCode);
        assertThat(sendSmsRequest.getTemplateParam()).isEqualTo(templateParam);
        assertThat(sendSmsRequest.getSignName()).isEqualTo(appProps.getAliyunSmsSignName());

        // aliyun fail
        sendSmsResponse.setCode("FAIL");
        when(iAcsClient.getAcsResponse(any(SendSmsRequest.class))).thenReturn(sendSmsResponse);

        // even aliyun fail, send sms still succeed since async send
        baseResponse = smsClient.send(AuthConstant.AUTHORIZATION_BOT_SERVICE, smsRequest);
        assertThat(baseResponse.isSuccess()).isTrue();
    }
}
