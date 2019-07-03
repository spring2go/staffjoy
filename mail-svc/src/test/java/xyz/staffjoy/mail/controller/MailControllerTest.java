package xyz.staffjoy.mail.controller;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.dm.model.v20151123.SingleSendMailRequest;
import com.aliyuncs.dm.model.v20151123.SingleSendMailResponse;
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
import xyz.staffjoy.mail.MailConstant;
import xyz.staffjoy.mail.client.MailClient;
import xyz.staffjoy.mail.dto.EmailRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@DirtiesContext // avoid port conflict
@EnableFeignClients(basePackages = {"xyz.staffjoy.mail.client"})
@Slf4j
public class MailControllerTest {
    @Autowired
    MailClient mailClient;

    @MockBean
    IAcsClient iAcsClient;

    @Test
    public void testSendMail() throws ClientException {

        SingleSendMailResponse singleSendMailResponse = new SingleSendMailResponse();
        singleSendMailResponse.setRequestId("mock_id");
        when(iAcsClient.getAcsResponse(any(SingleSendMailRequest.class))).thenReturn(singleSendMailResponse);

        String email = "test@jskillcloud.com";
        String name = "test_user";
        String subject = "test_subject";
        String htmlBody = "test html body...";
        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .name(name)
                .subject(subject)
                .htmlBody(htmlBody)
                .build();
        BaseResponse baseResponse = mailClient.send(emailRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // verify email
        ArgumentCaptor<SingleSendMailRequest> argument = ArgumentCaptor.forClass(SingleSendMailRequest.class);
        verify(iAcsClient, times(1)).getAcsResponse(argument.capture());
        SingleSendMailRequest singleSendMailRequest = argument.getValue();
        assertThat(singleSendMailRequest.getAccountName()).isEqualTo(MailConstant.FROM);
        assertThat(singleSendMailRequest.getFromAlias()).isEqualTo(MailConstant.FROM_NAME);
        assertThat(singleSendMailRequest.getAddressType()).isEqualTo(1);
        assertThat(singleSendMailRequest.getToAddress()).isEqualTo(emailRequest.getTo());
        assertThat(singleSendMailRequest.getReplyToAddress()).isEqualTo(false);
        assertThat(singleSendMailRequest.getSubject()).endsWith(emailRequest.getSubject());
        assertThat(singleSendMailRequest.getHtmlBody()).isEqualTo(emailRequest.getHtmlBody());

        // aliyun fail
        when(iAcsClient.getAcsResponse(any(SingleSendMailRequest.class))).thenThrow(new ClientException("aliyun fail"));

        // even aliyun fail, send sms still succeed since async send
        baseResponse = mailClient.send(emailRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();
    }
}
