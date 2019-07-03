package xyz.staffjoy.account.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.staffjoy.account.TestConfig;
import xyz.staffjoy.account.client.AccountClient;
import xyz.staffjoy.account.dto.*;
import xyz.staffjoy.account.model.Account;
import xyz.staffjoy.account.repo.AccountRepo;
import xyz.staffjoy.account.repo.AccountSecretRepo;
import xyz.staffjoy.bot.client.BotClient;
import xyz.staffjoy.bot.dto.GreetingRequest;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.api.ResultCode;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.mail.client.MailClient;
import xyz.staffjoy.mail.dto.EmailRequest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableFeignClients(basePackages = {"xyz.staffjoy.account.client"})
@Import(TestConfig.class)
@Slf4j
public class AccountControllerTest {

    @Autowired
    AccountClient accountClient;

    @Autowired
    EnvConfig envConfig;

    @MockBean
    MailClient mailClient;

    @MockBean
    BotClient botClient;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private AccountSecretRepo accountSecretRepo;

    private Account newAccount;

    @Before
    public void setUp() {
        // sanity check
        accountRepo.deleteAll();
        // clear CURRENT_USER_HEADER for testing
        TestConfig.TEST_USER_ID = null;
    }

    @Test
    public void testChangeEmail() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create one account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();
        assertThat(accountDto.isConfirmedAndActive()).isFalse();

        // change email
        String changedEmail = "test123@staffjoy.xyz";
        EmailConfirmation emailConfirmation = EmailConfirmation.builder()
                .userId(accountDto.getId())
                .email(changedEmail)
                .build();
        BaseResponse baseResponse = accountClient.changeEmail(AuthConstant.AUTHORIZATION_WWW_SERVICE, emailConfirmation);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // verify email changed and account activated
        GetOrCreateRequest getOrCreateRequest = GetOrCreateRequest.builder()
                .email(changedEmail)
                .build();
        genericAccountResponse = accountClient.getOrCreateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, getOrCreateRequest);
        AccountDto foundAccountDto = genericAccountResponse.getAccount();
        assertThat(foundAccountDto.getEmail()).isEqualTo(changedEmail);
        assertThat(foundAccountDto.isConfirmedAndActive()).isTrue();

        // account not found
        emailConfirmation = EmailConfirmation.builder()
                .userId("not_existing_id")
                .email(changedEmail)
                .build();
        baseResponse = accountClient.changeEmail(AuthConstant.AUTHORIZATION_WWW_SERVICE, emailConfirmation);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isFalse();
        assertThat(baseResponse.getCode()).isEqualTo(ResultCode.NOT_FOUND);
    }

    @Test
    public void testRequestEmailChange() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create one account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();

        // request email change
        String updatedEmail = "test111@staffjoy.xyz";
        EmailChangeRequest emailChangeRequest = EmailChangeRequest.builder()
                .email(updatedEmail)
                .userId(accountDto.getId())
                .build();
        BaseResponse baseResponse = accountClient.requestEmailChange(AuthConstant.AUTHORIZATION_SUPPORT_USER, emailChangeRequest);
        assertThat(baseResponse.isSuccess()).isTrue();

        // capture and verify email sent
        String externalApex = "staffjoy-v2.local";
        String subject = "Confirm Your New Email Address";
        ArgumentCaptor<EmailRequest> argument = ArgumentCaptor.forClass(EmailRequest.class);
        verify(mailClient, times(2)).send(argument.capture());
        EmailRequest emailRequest = argument.getAllValues().get(1);
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(updatedEmail);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/activate/")).isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), name)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div>Hi");
    }

    @Test
    public void testRequestPasswordReset() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create one account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();

        // request password reset
        PasswordResetRequest passwordResetRequest = PasswordResetRequest.builder()
                .email(email)
                .build();
        BaseResponse baseResponse = accountClient.requestPasswordReset(AuthConstant.AUTHORIZATION_WWW_SERVICE, passwordResetRequest);
        assertThat(baseResponse.isSuccess()).isTrue();

        // capture and verify
        String subject = "Activate your Staffjoy account";
        String externalApex = "staffjoy-v2.local";
        ArgumentCaptor<EmailRequest> argument = ArgumentCaptor.forClass(EmailRequest.class);
        // 2 times, 1 for account creation, 1 for password reset
        verify(mailClient, times(2)).send(argument.capture());
        EmailRequest emailRequest = argument.getAllValues().get(1);
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(email);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/activate/")).isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), name)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div><p>Hi");

        // activate the account
        accountDto.setConfirmedAndActive(true);
        genericAccountResponse = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // request password reset
        passwordResetRequest = PasswordResetRequest.builder()
                .email(email)
                .build();
        baseResponse = accountClient.requestPasswordReset(AuthConstant.AUTHORIZATION_WWW_SERVICE, passwordResetRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // capture and verify
        subject = "Reset your Staffjoy password";
        argument = ArgumentCaptor.forClass(EmailRequest.class);
        // 3 times, 1 for account creation, 2 for password reset
        verify(mailClient, times(3)).send(argument.capture());
        emailRequest = argument.getAllValues().get(2);
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(email);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/reset/")).isEqualTo(2);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div>We received a request to reset the password on your account.");
    }

    @Test
    public void testUpdateAndVerifyPasswordValidation() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create one account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();

        // update password too short
        String password = "pass";
        UpdatePasswordRequest updatePasswordRequest = UpdatePasswordRequest.builder()
                .userId(accountDto.getId())
                .password(password)
                .build();
        BaseResponse baseResponse = accountClient.updatePassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, updatePasswordRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isFalse();
        assertThat(baseResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        // update password success
        password = "pass123456";
        updatePasswordRequest = UpdatePasswordRequest.builder()
                .userId(accountDto.getId())
                .password(password)
                .build();
        baseResponse = accountClient.updatePassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, updatePasswordRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // verify not found
        VerifyPasswordRequest verifyPasswordRequest = VerifyPasswordRequest.builder()
                .password(password)
                .email("test000@staffjoy.xyz")
                .build();
        genericAccountResponse = accountClient.verifyPassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, verifyPasswordRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.NOT_FOUND);

        // verify account not active
        verifyPasswordRequest = VerifyPasswordRequest.builder()
                .password(password)
                .email(email)
                .build();
        genericAccountResponse = accountClient.verifyPassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, verifyPasswordRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // activate the account
        accountDto.setConfirmedAndActive(true);
        genericAccountResponse = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        assertThat(genericAccountResponse.isSuccess()).isTrue();


        // verify wrong password
        verifyPasswordRequest = VerifyPasswordRequest.builder()
                .password("wrong_password")
                .email(email)
                .build();
        genericAccountResponse = accountClient.verifyPassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, verifyPasswordRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.UN_AUTHORIZED);
    }

    @Test
    public void testUpdateAndVerifyPassword() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();

        // activate the account
        accountDto.setConfirmedAndActive(true);
        genericAccountResponse = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // update password
        String password = "pass123456";
        UpdatePasswordRequest updatePasswordRequest = UpdatePasswordRequest.builder()
                .userId(accountDto.getId())
                .password(password)
                .build();
        BaseResponse baseResponse = accountClient.updatePassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, updatePasswordRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // verify password
        VerifyPasswordRequest verifyPasswordRequest = VerifyPasswordRequest.builder()
                .password(password)
                .email(accountDto.getEmail())
                .build();
        genericAccountResponse = accountClient.verifyPassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, verifyPasswordRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        assertThat(genericAccountResponse.getAccount()).isEqualTo(accountDto);
    }

    @Test
    public void testUpdateAccountValidation() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());
        when(botClient.sendSmsGreeting(any(GreetingRequest.class))).thenReturn(BaseResponse.builder().message("sms sent").build());

        // create first account
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18001801235";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // create second account
        name = "testAccount002";
        email = "test002@staffjoy.xyz";
        phoneNumber = "18001801236";
        String subject = "Confirm Your New Email Address";
        String externalApex = "staffjoy-v2.local";
        createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();

        // update
        String updatedName = "testAccountUpdate";
        accountDto.setName(updatedName);
        accountDto.setPhoneNumber("18001801237");
        // no current user id
        GenericAccountResponse genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.FAILURE);

        // set user id for testing
        TestConfig.TEST_USER_ID = accountDto.getId();
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isTrue();


        String oldId = accountDto.getId();
        // can't update not existing account
        accountDto.setId("not_existing_id");
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.NOT_FOUND);

        // reset id
        accountDto.setId(oldId);
        // can't update member since
        Instant oldMemberSince = accountDto.getMemberSince();
        accountDto.setMemberSince(oldMemberSince.minusSeconds(5));
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset member since
        accountDto.setMemberSince(oldMemberSince);
        // can't update to existing email
        String oldEmail = accountDto.getEmail();
        accountDto.setEmail("test001@staffjoy.xyz");
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset email
        accountDto.setEmail(oldEmail);
        // can't update to exiting phonenumber
        String oldPhoneNumber = accountDto.getPhoneNumber();
        accountDto.setPhoneNumber("18001801235");
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset phone
        accountDto.setPhoneNumber(oldPhoneNumber);
        // user can't activate him/herself
        accountDto.setConfirmedAndActive(true);
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset confirm&active
        accountDto.setConfirmedAndActive(false);
        // user can't change support parameter
        accountDto.setSupport(true);
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset support
        accountDto.setSupport(false);
        // user can't change photo url
        String photoUrl = accountDto.getPhotoUrl();
        accountDto.setPhotoUrl("updated_photo_url");
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset photo url
        accountDto.setPhotoUrl(photoUrl);
        // user updated his/her account successfully
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isTrue();

        // user can change his/her email
        oldEmail = accountDto.getEmail();
        String updatedEmail = "test003@staffjoy.xyz";
        accountDto.setEmail(updatedEmail);
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isTrue();
        accountDto = genericAccountResponse1.getAccount();
        // email should be restored to original one
        assertThat(accountDto.getEmail()).isEqualTo(oldEmail);

        // verify email change mail sent
        ArgumentCaptor<EmailRequest> argument = ArgumentCaptor.forClass(EmailRequest.class);
        // 3 times, 2 for account creation, 1 for email update
        verify(mailClient, times(3)).send(argument.capture());
        EmailRequest emailRequest = argument.getAllValues().get(2);
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(updatedEmail);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(updatedName);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/activate/")).isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), updatedName)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div>Hi");
    }

    @Test
    public void testUpdateAccount() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());
        when(botClient.sendSmsGreeting(any(GreetingRequest.class))).thenReturn(BaseResponse.builder().message("sms sent").build());

        // first account
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18001801236";
        String subject = "Activate your Staffjoy account";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();

        // update
        accountDto.setName("testAccountUpdate");
        accountDto.setConfirmedAndActive(true);
        accountDto.setPhoneNumber("18001801237");
        GenericAccountResponse genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isTrue();
        AccountDto updatedAccountDto = genericAccountResponse1.getAccount();
        assertThat(updatedAccountDto).isEqualTo(accountDto);

        // capture and verify
        ArgumentCaptor<GreetingRequest> argument = ArgumentCaptor.forClass(GreetingRequest.class);
        verify(botClient, times(1)).sendSmsGreeting(argument.capture());
        GreetingRequest greetingRequest = argument.getValue();
        assertThat(greetingRequest.getUserId()).isEqualTo(accountDto.getId());
    }

    @Test
    public void testGetAccount() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        // first account
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18001801236";
        String subject = "Activate your Staffjoy account";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();

        // get account fail
        genericAccountResponse = accountClient.getAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto.getId());
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.FAILURE);

        // get account success
        genericAccountResponse = accountClient.getAccount(AuthConstant.AUTHORIZATION_WHOAMI_SERVICE, accountDto.getId());
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto gotAccountDto = genericAccountResponse.getAccount();
        assertThat(accountDto).isEqualTo(gotAccountDto);
    }

    @Test
    public void testListAccounts() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        // first account
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18001801236";
        String subject = "Activate your Staffjoy account";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // list account and verify
        ListAccountResponse listAccountResponse = accountClient.listAccounts(AuthConstant.AUTHORIZATION_SUPPORT_USER, 0, 2);
        log.info((listAccountResponse.toString()));
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountList accountList = listAccountResponse.getAccountList();
        assertThat(accountList.getAccounts()).hasSize(1);
        assertThat(accountList.getLimit()).isEqualTo(2);
        assertThat(accountList.getOffset()).isEqualTo(0);

        // second account
        name = "testAccount002";
        email = "test002@staffjoy.xyz";
        phoneNumber = "18001801237";
        createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // list and verify
        listAccountResponse = accountClient.listAccounts(AuthConstant.AUTHORIZATION_SUPPORT_USER, 0, 2);
        log.info(listAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        accountList = listAccountResponse.getAccountList();
        assertThat(accountList.getAccounts()).hasSize(2);
        assertThat(accountList.getLimit()).isEqualTo(2);
        assertThat(accountList.getOffset()).isEqualTo(0);

        // third account
        name = "testAccount003";
        email = "test003@staffjoy.xyz";
        phoneNumber = "18001801238";
        createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // list and verify
        listAccountResponse = accountClient.listAccounts(AuthConstant.AUTHORIZATION_SUPPORT_USER, 1, 2);
        log.info(listAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        accountList = listAccountResponse.getAccountList();
        assertThat(accountList.getAccounts()).hasSize(1);
        assertThat(accountList.getLimit()).isEqualTo(2);
        assertThat(accountList.getOffset()).isEqualTo(1);
    }

    @Test
    public void testCreateAccountValidation() {
        String phoneNumber = "18001801236";
        // empty request
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .build();
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        // invalid email
        createAccountRequest = CreateAccountRequest.builder()
                .email("invalid_email")
                .build();
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        // invalid phone number
        createAccountRequest = CreateAccountRequest.builder()
                .phoneNumber("invalid_phonenumber")
                .build();
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        // invalid auth
        createAccountRequest = CreateAccountRequest.builder()
                .phoneNumber(phoneNumber)
                .build();
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_ANONYMOUS_WEB, createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.UN_AUTHORIZED);
    }

    @Test
    public void testGetAccountByPhoneNumber() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // get account by phonenumber
        genericAccountResponse = accountClient.getAccountByPhonenumber(AuthConstant.AUTHORIZATION_SUPPORT_USER, phoneNumber);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();
        assertThat(accountDto.getId()).isNotNull();
        assertThat(accountDto.getName()).isEqualTo(name);
        assertThat(accountDto.getEmail()).isEqualTo(email);
        assertThat(accountDto.getPhotoUrl()).isNotNull();
        assertThat(accountDto.getMemberSince()).isBeforeOrEqualTo(Instant.now());
        assertThat(accountDto.isSupport()).isFalse();
        assertThat(accountDto.isConfirmedAndActive()).isFalse();

        // invalid phone number
        genericAccountResponse = accountClient.getAccountByPhonenumber(AuthConstant.AUTHORIZATION_SUPPORT_USER, "invalid_phonenumber");
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        // phonenumber not exists
        genericAccountResponse = accountClient.getAccountByPhonenumber(AuthConstant.AUTHORIZATION_SUPPORT_USER, "18001801299");
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.NOT_FOUND);
    }


    @Test
    public void testCreateAccountSuccess() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        String subject = "Activate your Staffjoy account";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account and verify
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();
        assertThat(accountDto.getId()).isNotNull();
        assertThat(accountDto.getName()).isEqualTo(name);
        assertThat(accountDto.getEmail()).isEqualTo(email);
        assertThat(accountDto.getPhotoUrl()).isNotNull();
        assertThat(accountDto.getMemberSince()).isBeforeOrEqualTo(Instant.now());
        assertThat(accountDto.isSupport()).isFalse();
        assertThat(accountDto.isConfirmedAndActive()).isFalse();

        // capture and verify
        ArgumentCaptor<EmailRequest> argument = ArgumentCaptor.forClass(EmailRequest.class);
        verify(mailClient, times(1)).send(argument.capture());
        EmailRequest emailRequest = argument.getValue();
        assertThat(emailRequest.getTo()).isEqualTo(email);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + envConfig.getExternalApex() + "/activate/")).isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), name)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div><p>Hi");
    }

    @Test
    public void testCreateAccountDuplicate() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount001";
        String email = "test01@staffjoy.xyz";
        String phoneNumber = "18001801236";
        String subject = "Activate your Staffjoy account";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // email duplicate
        createAccountRequest = CreateAccountRequest.builder()
                .name("testAccount002")
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.FAILURE);

        // phone duplicate
        createAccountRequest = CreateAccountRequest.builder()
                .name("testAccount003")
                .email("test02@staffjoy.xyz")
                .phoneNumber(phoneNumber)
                .build();
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.FAILURE);
    }

    @After
    public void destroy() {
        accountRepo.deleteAll();
    }
}
