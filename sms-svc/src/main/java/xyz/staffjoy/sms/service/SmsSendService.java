package xyz.staffjoy.sms.service;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import io.sentry.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import xyz.staffjoy.sms.config.AppConfig;
import xyz.staffjoy.sms.props.AppProps;
import xyz.staffjoy.sms.dto.SmsRequest;

@Service
public class SmsSendService {

    static final ILogger logger = SLoggerFactory.getLogger(SmsSendService.class);

    @Autowired
    private AppProps appProps;

    @Autowired
    private IAcsClient acsClient;

    @Autowired
    SentryClient sentryClient;

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void sendSmsAsync(SmsRequest smsRequest) {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(smsRequest.getTo());
        request.setSignName(appProps.getAliyunSmsSignName());
        request.setTemplateCode(smsRequest.getTemplateCode());
        request.setTemplateParam(smsRequest.getTemplateParam());

        try {
            SendSmsResponse response = acsClient.getAcsResponse(request);
            if ("OK".equals(response.getCode())) {
                logger.info("SMS sent - " + response.getRequestId(),
                        "to", smsRequest.getTo(),
                        "template_code", smsRequest.getTemplateCode(),
                        "template_param", smsRequest.getTemplateParam());
            } else {
                Context sentryContext = sentryClient.getContext();
                sentryContext.addTag("to", smsRequest.getTo());
                sentryContext.addTag("template_code", smsRequest.getTemplateCode());
                sentryClient.sendMessage("bad aliyun sms response " + response.getCode());
                logger.error("failed to send: bad aliyun sms response " + response.getCode());
            }
        } catch (ClientException ex) {
            Context sentryContext = sentryClient.getContext();
            sentryContext.addTag("to", smsRequest.getTo());
            sentryContext.addTag("template_code", smsRequest.getTemplateCode());
            sentryClient.sendException(ex);
            logger.error("failed to make aliyun sms request ", ex);
        }
    }
}
