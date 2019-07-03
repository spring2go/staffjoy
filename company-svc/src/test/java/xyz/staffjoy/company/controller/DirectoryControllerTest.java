package xyz.staffjoy.company.controller;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.staffjoy.account.client.AccountClient;
import xyz.staffjoy.account.dto.AccountDto;
import xyz.staffjoy.account.dto.GenericAccountResponse;
import xyz.staffjoy.account.dto.GetOrCreateRequest;
import xyz.staffjoy.account.dto.TrackEventRequest;
import xyz.staffjoy.bot.client.BotClient;
import xyz.staffjoy.bot.dto.OnboardWorkerRequest;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.api.ResultCode;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.company.TestConfig;
import xyz.staffjoy.company.client.CompanyClient;
import xyz.staffjoy.company.dto.*;
import xyz.staffjoy.company.repo.CompanyRepo;
import xyz.staffjoy.company.repo.DirectoryRepo;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@DirtiesContext // avoid port conflict
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableFeignClients(basePackages = {"xyz.staffjoy.company.client"})
@Import(TestConfig.class)
@Slf4j
public class DirectoryControllerTest {
    @Autowired
    CompanyClient companyClient;

    @MockBean
    AccountClient accountClient;

    @MockBean
    BotClient botClient;

    @MockBean
    CompanyRepo companyRepo;

    @Autowired
    DirectoryRepo directoryRepo;

    @Before
    public void setUp() {
        // cleanup
        directoryRepo.deleteAll();
    }

    @Test
    public void testGetAndUpdateDirectory() {
        // arrange mocks
        String companyId = UUID.randomUUID().toString();
        String interalId = UUID.randomUUID().toString();

        // create directory
        when(companyRepo.existsById(companyId)).thenReturn(true);

        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18001981226";
        Instant memberSince = Instant.now().minusSeconds(24 * 3600 * 30); // 30 days ago
        String photoUrl = "https://staffjoy.xyz/photo/test.png";
        AccountDto accountDto = AccountDto.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .memberSince(memberSince)
                .photoUrl(photoUrl)
                .build();
        GenericAccountResponse genericAccountResponse = new GenericAccountResponse(accountDto);
        when(accountClient.getOrCreateAccount(anyString(), any(GetOrCreateRequest.class)))
                .thenReturn(genericAccountResponse);

        when(botClient.onboardWorker(any(OnboardWorkerRequest.class))).thenReturn(BaseResponse.builder().message("worker onboard").build());
        when(accountClient.trackEvent(any(TrackEventRequest.class))).thenReturn(BaseResponse.builder().build());

        // create directory and verify
        NewDirectoryEntry newDirectoryEntry = NewDirectoryEntry.builder()
                .internalId(interalId)
                .companyId(companyId)
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        GenericDirectoryResponse genericDirectoryResponse = companyClient.createDirectory(AuthConstant.AUTHORIZATION_WWW_SERVICE, newDirectoryEntry);
        log.info(genericDirectoryResponse.toString());
        DirectoryEntryDto directoryEntryDto1 = genericDirectoryResponse.getDirectoryEntry();
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();

        // test get directory
        when(accountClient.getAccount(AuthConstant.AUTHORIZATION_COMPANY_SERVICE, accountDto.getId()))
                .thenReturn(genericAccountResponse);
        genericDirectoryResponse = companyClient.getDirectoryEntry(AuthConstant.AUTHORIZATION_WWW_SERVICE, companyId, accountDto.getId());
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();
        DirectoryEntryDto directoryEntryDto2 = genericDirectoryResponse.getDirectoryEntry();
        assertThat(directoryEntryDto2).isEqualTo(directoryEntryDto1);
        assertThat(directoryEntryDto2.getInternalId()).isEqualTo(interalId);

        // to update
        directoryEntryDto2.setInternalId(UUID.randomUUID().toString());
        genericDirectoryResponse = companyClient.updateDirectoryEntry(AuthConstant.AUTHORIZATION_SUPPORT_USER, directoryEntryDto2);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();
        DirectoryEntryDto directoryEntryDto3 = genericDirectoryResponse.getDirectoryEntry();
        assertThat(directoryEntryDto3).isEqualTo(directoryEntryDto2);

        // test get again
        genericDirectoryResponse = companyClient.getDirectoryEntry(AuthConstant.AUTHORIZATION_WWW_SERVICE, companyId, accountDto.getId());
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();
        DirectoryEntryDto directoryEntryDto4 = genericDirectoryResponse.getDirectoryEntry();
        assertThat(directoryEntryDto4).isEqualTo(directoryEntryDto2);

        // to update account and direcotry both failure
        accountDto.setConfirmedAndActive(true);
        genericAccountResponse = new GenericAccountResponse(accountDto);
        when(accountClient.getOrCreateAccount(anyString(), any(GetOrCreateRequest.class)))
                .thenReturn(genericAccountResponse);

        String updatedName = "testAccount002";
        String updatedEmail = "test002@staffjoy.xyz";
        String updatedPhoneNumber = "18001982337";
        directoryEntryDto2.setInternalId(UUID.randomUUID().toString());
        directoryEntryDto2.setName(updatedName);
        genericDirectoryResponse = companyClient.updateDirectoryEntry(AuthConstant.AUTHORIZATION_SUPPORT_USER, directoryEntryDto2);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isFalse();
        assertThat(genericDirectoryResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        accountDto.setConfirmedAndActive(false);
        accountDto.setSupport(true);
        genericAccountResponse = new GenericAccountResponse(accountDto);
        when(accountClient.getOrCreateAccount(anyString(), any(GetOrCreateRequest.class)))
                .thenReturn(genericAccountResponse);

        directoryEntryDto2.setInternalId(UUID.randomUUID().toString());
        directoryEntryDto2.setName(updatedName);
        genericDirectoryResponse = companyClient.updateDirectoryEntry(AuthConstant.AUTHORIZATION_SUPPORT_USER, directoryEntryDto2);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isFalse();
        assertThat(genericDirectoryResponse.getCode()).isEqualTo(ResultCode.UN_AUTHORIZED);

        // to update account and directory success
        accountDto.setSupport(false);
        AccountDto accountDto2 = AccountDto.builder()
                .id(accountDto.getId())
                .name(updatedName)
                .email(updatedEmail)
                .phoneNumber(updatedPhoneNumber)
                .memberSince(memberSince)
                .photoUrl(photoUrl)
                .build();
        when(accountClient.updateAccount(anyString(), any(AccountDto.class)))
                .thenReturn(new GenericAccountResponse(accountDto2));

        directoryEntryDto2.setEmail(updatedEmail);
        directoryEntryDto2.setPhoneNumber(updatedPhoneNumber);
        genericDirectoryResponse = companyClient.updateDirectoryEntry(AuthConstant.AUTHORIZATION_SUPPORT_USER, directoryEntryDto2);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();

        // capture and verify worker onboard request
        ArgumentCaptor<OnboardWorkerRequest> argument = ArgumentCaptor.forClass(OnboardWorkerRequest.class);
        // 2 times, 1 for create directory, 1 for update directory
        verify(botClient, times(2)).onboardWorker(argument.capture());
        OnboardWorkerRequest onboardWorkerRequest = argument.getAllValues().get(1);
        assertThat(onboardWorkerRequest.getCompanyId()).isEqualTo(companyId);
        assertThat(onboardWorkerRequest.getUserId()).isEqualTo(accountDto2.getId());

    }

    @Test
    public void testGetDirectory() {
        // arrange mocks
        String companyId = UUID.randomUUID().toString();
        String interalId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        // not found test
        GenericDirectoryResponse genericDirectoryResponse = companyClient.getDirectoryEntry(AuthConstant.AUTHORIZATION_WWW_SERVICE, companyId, userId);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isFalse();
        assertThat(genericDirectoryResponse.getCode()).isEqualTo(ResultCode.NOT_FOUND);

        // create directory
        when(companyRepo.existsById(companyId)).thenReturn(true);

        String name = "testAccount";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18001981226";
        Instant memberSince = Instant.now().minusSeconds(24 * 3600 * 30); // 30 days ago
        String photoUrl = "https://staffjoy.xyz/photo/test.png";
        AccountDto accountDto = AccountDto.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .memberSince(memberSince)
                .photoUrl(photoUrl)
                .build();
        GenericAccountResponse genericAccountResponse = new GenericAccountResponse(accountDto);
        when(accountClient.getOrCreateAccount(anyString(), any(GetOrCreateRequest.class)))
                .thenReturn(genericAccountResponse);

        when(botClient.onboardWorker(any(OnboardWorkerRequest.class))).thenReturn(BaseResponse.builder().message("worker onboard").build());
        when(accountClient.trackEvent(any(TrackEventRequest.class))).thenReturn(BaseResponse.builder().build());

        // create directory and verify
        NewDirectoryEntry newDirectoryEntry = NewDirectoryEntry.builder()
                .internalId(interalId)
                .companyId(companyId)
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        genericDirectoryResponse = companyClient.createDirectory(AuthConstant.AUTHORIZATION_WWW_SERVICE, newDirectoryEntry);
        log.info(genericDirectoryResponse.toString());
        DirectoryEntryDto directoryEntryDto1 = genericDirectoryResponse.getDirectoryEntry();
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();

        // test get directory
        when(accountClient.getAccount(AuthConstant.AUTHORIZATION_COMPANY_SERVICE, accountDto.getId()))
                .thenReturn(genericAccountResponse);
        genericDirectoryResponse = companyClient.getDirectoryEntry(AuthConstant.AUTHORIZATION_WWW_SERVICE, companyId, accountDto.getId());
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();
        DirectoryEntryDto directoryEntryDto2 = genericDirectoryResponse.getDirectoryEntry();
        assertThat(directoryEntryDto2).isEqualTo(directoryEntryDto1);
    }

    @Test
    public void testListDirectory() {

        // arrange mocks
        String companyId = UUID.randomUUID().toString();
        String interalId = UUID.randomUUID().toString();

        // empty test
        ListDirectoryResponse listDirectoryResponse = companyClient.listDirectories(AuthConstant.AUTHORIZATION_SUPPORT_USER, companyId, 0, 2);
        log.info(listDirectoryResponse.toString());
        assertThat(listDirectoryResponse.isSuccess()).isTrue();
        DirectoryList directoryList = listDirectoryResponse.getDirectoryList();
        assertThat(directoryList.getOffset()).isEqualTo(0);
        assertThat(directoryList.getLimit()).isEqualTo(2);
        assertThat(directoryList.getAccounts().size()).isEqualTo(0);

        // create first directory
        when(companyRepo.existsById(companyId)).thenReturn(true);

        String name = "testAccount";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18001981226";
        Instant memberSince = Instant.now().minusSeconds(24 * 3600 * 30); // 30 days ago
        String photoUrl = "https://staffjoy.xyz/photo/test.png";
        AccountDto accountDto = AccountDto.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .memberSince(memberSince)
                .photoUrl(photoUrl)
                .build();
        GenericAccountResponse genericAccountResponse = new GenericAccountResponse(accountDto);
        when(accountClient.getOrCreateAccount(anyString(), any(GetOrCreateRequest.class)))
                .thenReturn(genericAccountResponse);
        when(accountClient.getAccount(AuthConstant.AUTHORIZATION_COMPANY_SERVICE, accountDto.getId()))
                .thenReturn(genericAccountResponse);

        when(botClient.onboardWorker(any(OnboardWorkerRequest.class))).thenReturn(BaseResponse.builder().message("worker onboard").build());
        when(accountClient.trackEvent(any(TrackEventRequest.class))).thenReturn(BaseResponse.builder().build());

        // create directory and verify
        NewDirectoryEntry newDirectoryEntry = NewDirectoryEntry.builder()
                .internalId(interalId)
                .companyId(companyId)
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        GenericDirectoryResponse genericDirectoryResponse = companyClient.createDirectory(AuthConstant.AUTHORIZATION_WWW_SERVICE, newDirectoryEntry);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();

        // verify list
        listDirectoryResponse = companyClient.listDirectories(AuthConstant.AUTHORIZATION_SUPPORT_USER, companyId, 0, 2);
        log.info(listDirectoryResponse.toString());
        assertThat(listDirectoryResponse.isSuccess()).isTrue();
        directoryList = listDirectoryResponse.getDirectoryList();
        assertThat(directoryList.getOffset()).isEqualTo(0);
        assertThat(directoryList.getLimit()).isEqualTo(2);
        assertThat(directoryList.getAccounts().size()).isEqualTo(1);

        // create second directory
        when(companyRepo.existsById(companyId)).thenReturn(true);
        interalId = UUID.randomUUID().toString();
        name = "testAccount002";
        email = "test002@staffjoy.xyz";
        phoneNumber = "18001981336";
        memberSince = Instant.now().minusSeconds(24 * 3600 * 30); // 30 days ago
        photoUrl = "https://staffjoy.xyz/photo/test002.png";
        accountDto = AccountDto.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .memberSince(memberSince)
                .photoUrl(photoUrl)
                .build();
        genericAccountResponse = new GenericAccountResponse(accountDto);
        when(accountClient.getOrCreateAccount(anyString(), any(GetOrCreateRequest.class)))
                .thenReturn(genericAccountResponse);
        when(accountClient.getAccount(AuthConstant.AUTHORIZATION_COMPANY_SERVICE, accountDto.getId()))
                .thenReturn(genericAccountResponse);

        when(botClient.onboardWorker(any(OnboardWorkerRequest.class))).thenReturn(BaseResponse.builder().message("worker onboard").build());
        when(accountClient.trackEvent(any(TrackEventRequest.class))).thenReturn(BaseResponse.builder().build());

        // create directory and verify
        newDirectoryEntry = NewDirectoryEntry.builder()
                .internalId(interalId)
                .companyId(companyId)
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        genericDirectoryResponse = companyClient.createDirectory(AuthConstant.AUTHORIZATION_WWW_SERVICE, newDirectoryEntry);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();

        // create third directory
        interalId = UUID.randomUUID().toString();
        name = "testAccount003";
        email = "test003@staffjoy.xyz";
        phoneNumber = "18001981556";
        memberSince = Instant.now().minusSeconds(24 * 3600 * 30); // 30 days ago
        photoUrl = "https://staffjoy.xyz/photo/test003.png";
        accountDto = AccountDto.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .memberSince(memberSince)
                .photoUrl(photoUrl)
                .build();
        genericAccountResponse = new GenericAccountResponse(accountDto);
        when(accountClient.getOrCreateAccount(anyString(), any(GetOrCreateRequest.class)))
                .thenReturn(genericAccountResponse);
        when(accountClient.getAccount(AuthConstant.AUTHORIZATION_COMPANY_SERVICE, accountDto.getId()))
                .thenReturn(genericAccountResponse);

        when(botClient.onboardWorker(any(OnboardWorkerRequest.class))).thenReturn(BaseResponse.builder().message("worker onboard").build());
        when(accountClient.trackEvent(any(TrackEventRequest.class))).thenReturn(BaseResponse.builder().build());

        // create directory and verify
        newDirectoryEntry = NewDirectoryEntry.builder()
                .internalId(interalId)
                .companyId(companyId)
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        genericDirectoryResponse = companyClient.createDirectory(AuthConstant.AUTHORIZATION_WWW_SERVICE, newDirectoryEntry);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();

        // verify first page
        listDirectoryResponse = companyClient.listDirectories(AuthConstant.AUTHORIZATION_SUPPORT_USER, companyId, 0, 2);
        log.info(listDirectoryResponse.toString());
        assertThat(listDirectoryResponse.isSuccess()).isTrue();
        directoryList = listDirectoryResponse.getDirectoryList();
        assertThat(directoryList.getOffset()).isEqualTo(0);
        assertThat(directoryList.getLimit()).isEqualTo(2);
        assertThat(directoryList.getAccounts().size()).isEqualTo(2);

        // verify second page
        listDirectoryResponse = companyClient.listDirectories(AuthConstant.AUTHORIZATION_SUPPORT_USER, companyId, 1, 2);
        log.info(listDirectoryResponse.toString());
        assertThat(listDirectoryResponse.isSuccess()).isTrue();
        directoryList = listDirectoryResponse.getDirectoryList();
        assertThat(directoryList.getOffset()).isEqualTo(1);
        assertThat(directoryList.getLimit()).isEqualTo(2);
        assertThat(directoryList.getAccounts().size()).isEqualTo(1);
    }

    @Test
    public void testCreateDirectoryFailure() {
        // arrange mocks
        String companyId = UUID.randomUUID().toString();
        String interalId = UUID.randomUUID().toString();
        when(companyRepo.existsById(companyId)).thenReturn(false);

        String name = "testAccount";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18001981226";
        Instant memberSince = Instant.now().minusSeconds(24 * 3600 * 30); // 30 days ago
        String photoUrl = "https://staffjoy.xyz/photo/test.png";


        // create directory and verify
        NewDirectoryEntry newDirectoryEntry = NewDirectoryEntry.builder()
                .internalId(interalId)
                .companyId(companyId)
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        GenericDirectoryResponse genericDirectoryResponse = companyClient.createDirectory(AuthConstant.AUTHORIZATION_WWW_SERVICE, newDirectoryEntry);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isFalse();
        assertThat(genericDirectoryResponse.getCode()).isEqualTo(ResultCode.NOT_FOUND);

        when(companyRepo.existsById(companyId)).thenReturn(true);
        when(accountClient.getOrCreateAccount(anyString(), any(GetOrCreateRequest.class)))
                .thenThrow(new RuntimeException("mock exception"));

        genericDirectoryResponse = companyClient.createDirectory(AuthConstant.AUTHORIZATION_WWW_SERVICE, newDirectoryEntry);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isFalse();
        assertThat(genericDirectoryResponse.getCode()).isEqualTo(ResultCode.FAILURE);

        GenericAccountResponse genericAccountResponse = new GenericAccountResponse();
        genericAccountResponse.setCode(ResultCode.FAILURE);
        genericAccountResponse.setMessage("mock view message");
        when(accountClient.getOrCreateAccount(anyString(), any(GetOrCreateRequest.class)))
                .thenReturn(genericAccountResponse);
        genericDirectoryResponse = companyClient.createDirectory(AuthConstant.AUTHORIZATION_WWW_SERVICE, newDirectoryEntry);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isFalse();
        assertThat(genericDirectoryResponse.getCode()).isEqualTo(ResultCode.FAILURE);
    }

    @Test
    public void testCreateDirectory() {
        // arrange mocks
        String companyId = UUID.randomUUID().toString();
        String interalId = UUID.randomUUID().toString();
        when(companyRepo.existsById(companyId)).thenReturn(true);

        String name = "testAccount";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18001981226";
        Instant memberSince = Instant.now().minusSeconds(24 * 3600 * 30); // 30 days ago
        String photoUrl = "https://staffjoy.xyz/photo/test.png";
        AccountDto accountDto = AccountDto.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .memberSince(memberSince)
                .photoUrl(photoUrl)
                .build();
        GenericAccountResponse genericAccountResponse = new GenericAccountResponse(accountDto);
        when(accountClient.getOrCreateAccount(anyString(), any(GetOrCreateRequest.class)))
                .thenReturn(genericAccountResponse);

        when(botClient.onboardWorker(any(OnboardWorkerRequest.class))).thenReturn(BaseResponse.builder().message("worker onboard").build());
        when(accountClient.trackEvent(any(TrackEventRequest.class))).thenReturn(BaseResponse.builder().build());

        // create directory and verify
        NewDirectoryEntry newDirectoryEntry = NewDirectoryEntry.builder()
                .internalId(interalId)
                .companyId(companyId)
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        GenericDirectoryResponse genericDirectoryResponse = companyClient.createDirectory(AuthConstant.AUTHORIZATION_WWW_SERVICE, newDirectoryEntry);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();
        DirectoryEntryDto directoryEntryDto = genericDirectoryResponse.getDirectoryEntry();
        assertThat(directoryEntryDto.getUserId()).isEqualTo(accountDto.getId());
        assertThat(directoryEntryDto.getName()).isEqualTo(name);
        assertThat(directoryEntryDto.getEmail()).isEqualTo(email);
        assertThat(directoryEntryDto.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(directoryEntryDto.getCompanyId()).isEqualTo(companyId);
        assertThat(directoryEntryDto.getInternalId()).isEqualTo(interalId);
        assertThat(directoryEntryDto.getPhotoUrl()).isEqualTo(photoUrl);

        // capture and verify event track request
        ArgumentCaptor<TrackEventRequest> argument1 = ArgumentCaptor.forClass(TrackEventRequest.class);
        verify(accountClient, times(1)).trackEvent(argument1.capture());
        TrackEventRequest trackEventRequest = argument1.getValue();
        assertThat(trackEventRequest.getUserId()).isEqualTo(TestConfig.TEST_USER_ID);
        assertThat(trackEventRequest.getEvent()).isEqualTo("directoryentry_created");

        // capture and verify worker onboard request
        ArgumentCaptor<OnboardWorkerRequest> argument2 = ArgumentCaptor.forClass(OnboardWorkerRequest.class);
        verify(botClient, times(1)).onboardWorker(argument2.capture());
        OnboardWorkerRequest onboardWorkerRequest = argument2.getValue();
        assertThat(onboardWorkerRequest.getCompanyId()).isEqualTo(companyId);
        assertThat(onboardWorkerRequest.getUserId()).isEqualTo(accountDto.getId());

        // already exists, can't save again
        genericDirectoryResponse = companyClient.createDirectory(AuthConstant.AUTHORIZATION_WWW_SERVICE, newDirectoryEntry);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isFalse();
        assertThat(genericDirectoryResponse.getCode()).isEqualTo(ResultCode.FAILURE);
    }

    @After
    public void destroy() {
        directoryRepo.deleteAll();
    }
}
