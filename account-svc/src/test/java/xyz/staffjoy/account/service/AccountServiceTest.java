package xyz.staffjoy.account.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import xyz.staffjoy.account.AccountConstant;
import xyz.staffjoy.account.dto.AccountDto;
import xyz.staffjoy.account.model.Account;
import xyz.staffjoy.account.props.AppProps;
import xyz.staffjoy.account.repo.AccountRepo;
import xyz.staffjoy.account.repo.AccountSecretRepo;
import xyz.staffjoy.account.service.helper.ServiceHelper;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.mail.client.MailClient;
import xyz.staffjoy.mail.dto.EmailRequest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {

    @Mock
    private AccountRepo accountRepo;

    @Mock
    private AccountSecretRepo accountSecretRepo;

    @Mock
    private AppProps appProps;

    @Mock
    private EnvConfig envConfig;

    @Mock
    private MailClient mailClient;

    @Mock
    private ServiceHelper serviceHelper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountService accountService;

    @Test
    public void testSendEmail() {
        String externalApex = "staffjoy-v2.local";
        when(appProps.getSigningSecret()).thenReturn("test_secret");
        when(envConfig.getExternalApex()).thenReturn(externalApex);
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String userId = UUID.randomUUID().toString();
        String email = "test@jskillcloud.com";
        String name = "test_name";
        String subject = "Activate your Staffjoy account";
        String template = AccountConstant.ACTIVATE_ACCOUNT_TMPL;
        boolean activateOrConfirm = true;

        accountService.sendEmail(userId, email, name, subject, template, true);

        ArgumentCaptor<EmailRequest> argument = ArgumentCaptor.forClass(EmailRequest.class);
        verify(mailClient, times(1)).send(argument.capture());
        EmailRequest emailRequest = argument.getValue();
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(email);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/activate/")).isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), name)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div><p>Hi");

        template = AccountConstant.CONFIRM_EMAIL_TMPL;
        accountService.sendEmail(userId, email, name, subject, template, true);

        verify(mailClient, times(2)).send(argument.capture());
        emailRequest = argument.getValue();
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(email);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/activate/")).isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), name)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div>Hi");

        subject = "Reset your Staffjoy password";
        template = AccountConstant.RESET_PASSWORD_TMPL;
        accountService.sendEmail(userId, email, name, subject, template, false);

        verify(mailClient, times(3)).send(argument.capture());
        emailRequest = argument.getValue();
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(email);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/reset/")).isEqualTo(2);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div>We received a request to reset the password on your account.");
    }

    @Test
    public void testModelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        Account account = Account.builder().id("123456")
                .name("testAccount")
                .email("test@staffjoy.net")
                .memberSince(Instant.now())
                .confirmedAndActive(true)
                .photoUrl("https://staffjoy.xyz/photo/test.png")
                .phoneNumber("18001801266")
                .support(false)
                .build();

        AccountDto accountDto = modelMapper.map(account, AccountDto.class);
        validateAccount(accountDto, account);

        Account account1 = modelMapper.map(accountDto, Account.class);
        validateAccount(accountDto, account1);
    }

    void validateAccount(AccountDto accountDto, Account account) {
        assertThat(account.getId()).isEqualTo(accountDto.getId());
        assertThat(account.getName()).isEqualTo(accountDto.getName());
        assertThat(account.getEmail()).isEqualTo(accountDto.getEmail());
        assertThat(account.getMemberSince()).isEqualTo(accountDto.getMemberSince());
        assertThat(account.isConfirmedAndActive()).isEqualTo(accountDto.isConfirmedAndActive());
        assertThat(account.getPhotoUrl()).isEqualTo(accountDto.getPhotoUrl());
        assertThat(account.getPhoneNumber()).isEqualTo(accountDto.getPhoneNumber());
        assertThat(account.isSupport()).isEqualTo(accountDto.isSupport());
    }

}
