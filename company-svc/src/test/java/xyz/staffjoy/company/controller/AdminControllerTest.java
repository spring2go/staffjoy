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
import xyz.staffjoy.account.dto.TrackEventRequest;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.api.ResultCode;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.company.TestConfig;
import xyz.staffjoy.company.client.CompanyClient;
import xyz.staffjoy.company.dto.*;
import xyz.staffjoy.company.repo.AdminRepo;
import xyz.staffjoy.company.service.CompanyService;
import xyz.staffjoy.company.service.DirectoryService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@DirtiesContext // avoid port conflict
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableFeignClients(basePackages = {"xyz.staffjoy.company.client"})
@Import(TestConfig.class)
@Slf4j
public class AdminControllerTest {
    @Autowired
    AdminRepo adminRepo;

    @MockBean
    DirectoryService directoryService;

    @Autowired
    CompanyClient companyClient;

    @MockBean
    AccountClient accountClient;

    @MockBean
    CompanyService companyService;

    @Before
    public void setUp() {
        // cleanup
        adminRepo.deleteAll();
    }

    @Test
    public void testListThenDeleteAdmin() {
        // arrange mock
        when(accountClient.trackEvent(any(TrackEventRequest.class))).thenReturn(BaseResponse.builder().build());

        // first company
        String companyId1 = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18001892333";
        DirectoryEntryDto directoryEntryDto1 = DirectoryEntryDto.builder()
                .companyId(companyId1)
                .userId(userId)
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        when(directoryService.getDirectoryEntry(companyId1, userId)).thenReturn(directoryEntryDto1);

        // create admin001
        DirectoryEntryRequest directoryEntryRequest1 = DirectoryEntryRequest.builder()
                .companyId(companyId1)
                .userId(userId)
                .build();
        GenericDirectoryResponse genericDirectoryResponse =
                companyClient.createAdmin(AuthConstant.AUTHORIZATION_WWW_SERVICE, directoryEntryRequest1);
        // verify
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();
        DirectoryEntryDto directoryEntryDto2 = genericDirectoryResponse.getDirectoryEntry();
        assertThat(directoryEntryDto2).isEqualTo(directoryEntryDto1);

        // second company
        String companyId2 = UUID.randomUUID().toString();
        DirectoryEntryDto directoryEntryDto3 = DirectoryEntryDto.builder()
                .companyId(companyId2)
                .userId(userId)
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        when(directoryService.getDirectoryEntry(companyId2, userId)).thenReturn(directoryEntryDto3);
        // create admin002
        DirectoryEntryRequest directoryEntryRequest2 = DirectoryEntryRequest.builder()
                .companyId(companyId2)
                .userId(userId)
                .build();
        genericDirectoryResponse =
                companyClient.createAdmin(AuthConstant.AUTHORIZATION_WWW_SERVICE, directoryEntryRequest2);
        // verify
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();
        DirectoryEntryDto directoryEntryDto4 = genericDirectoryResponse.getDirectoryEntry();
        assertThat(directoryEntryDto4).isEqualTo(directoryEntryDto3);

        // list admins
        when(companyService.getCompany(companyId1)).thenReturn(CompanyDto.builder().id(companyId1).build());
        when(companyService.getCompany(companyId2)).thenReturn(CompanyDto.builder().id(companyId2).build());

        ListAdminResponse listAdminResponse = companyClient.listAdmins(AuthConstant.AUTHORIZATION_SUPPORT_USER, companyId1);
        // verify
        log.info(listAdminResponse.toString());
        assertThat(listAdminResponse.isSuccess()).isTrue();
        assertThat(listAdminResponse.getAdminEntries().getCompanyId()).isEqualTo(companyId1);
        assertThat(listAdminResponse.getAdminEntries().getAdmins().size()).isEqualTo(1);
        DirectoryEntryDto directoryEntryDto5 = listAdminResponse.getAdminEntries().getAdmins().get(0);
        assertThat(directoryEntryDto5).isEqualTo(directoryEntryDto1);

        listAdminResponse = companyClient.listAdmins(AuthConstant.AUTHORIZATION_SUPPORT_USER, companyId2);
        // verify
        log.info(listAdminResponse.toString());
        assertThat(listAdminResponse.isSuccess()).isTrue();
        assertThat(listAdminResponse.getAdminEntries().getCompanyId()).isEqualTo(companyId2);
        assertThat(listAdminResponse.getAdminEntries().getAdmins().size()).isEqualTo(1);
        DirectoryEntryDto directoryEntryDto6 = listAdminResponse.getAdminEntries().getAdmins().get(0);
        assertThat(directoryEntryDto6).isEqualTo(directoryEntryDto3);

        // test adminOf
        GetAdminOfResponse getAdminOfResponse = companyClient.getAdminOf(AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE, userId);
        // verify
        log.info(getAdminOfResponse.toString());
        assertThat(getAdminOfResponse.isSuccess()).isTrue();
        assertThat(getAdminOfResponse.getAdminOfList().getCompanies().size()).isEqualTo(2);
        List<String> companyIds = getAdminOfResponse.getAdminOfList().getCompanies().stream().map(o -> o.getId())
                .collect(Collectors.toList());
        assertThat(companyIds).containsExactly(companyId1, companyId2);

        // delete admin2
        BaseResponse baseResponse = companyClient.deleteAdmin(AuthConstant.AUTHORIZATION_SUPPORT_USER, directoryEntryRequest2);
        assertThat(baseResponse.isSuccess()).isTrue();

        // test adminOf again
        getAdminOfResponse = companyClient.getAdminOf(AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE, userId);
        // verify
        log.info(getAdminOfResponse.toString());
        assertThat(getAdminOfResponse.isSuccess()).isTrue();
        assertThat(getAdminOfResponse.getAdminOfList().getCompanies().size()).isEqualTo(1);
        companyIds = getAdminOfResponse.getAdminOfList().getCompanies().stream().map(o -> o.getId())
                .collect(Collectors.toList());
        assertThat(companyIds).containsExactly(companyId1);
    }

    @Test
    public void testCreateThenGetAdmin() {
        // arrange mock
        when(accountClient.trackEvent(any(TrackEventRequest.class))).thenReturn(BaseResponse.builder().build());

        String companyId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18001892333";
        DirectoryEntryDto directoryEntryDto1 = DirectoryEntryDto.builder()
                .companyId(companyId)
                .userId(userId)
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        when(directoryService.getDirectoryEntry(companyId, userId)).thenReturn(directoryEntryDto1);

        // create admin
        DirectoryEntryRequest directoryEntryRequest = DirectoryEntryRequest.builder()
                .companyId(companyId)
                .userId(userId)
                .build();
        GenericDirectoryResponse genericDirectoryResponse =
                companyClient.createAdmin(AuthConstant.AUTHORIZATION_WWW_SERVICE, directoryEntryRequest);
        // verify
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();
        DirectoryEntryDto directoryEntryDto2 = genericDirectoryResponse.getDirectoryEntry();
        assertThat(directoryEntryDto2).isEqualTo(directoryEntryDto1);

        // capture and verify event track request
        ArgumentCaptor<TrackEventRequest> argument1 = ArgumentCaptor.forClass(TrackEventRequest.class);
        verify(accountClient, times(1)).trackEvent(argument1.capture());
        TrackEventRequest trackEventRequest = argument1.getValue();
        assertThat(trackEventRequest.getUserId()).isEqualTo(TestConfig.TEST_USER_ID);
        assertThat(trackEventRequest.getEvent()).isEqualTo("admin_created");

        // create same will fail
        genericDirectoryResponse =
                companyClient.createAdmin(AuthConstant.AUTHORIZATION_WWW_SERVICE, directoryEntryRequest);
        assertThat(genericDirectoryResponse.isSuccess()).isFalse();
        assertThat(genericDirectoryResponse.getCode()).isEqualTo(ResultCode.FAILURE);

        // bypass validation
        when(companyService.getCompany(companyId)).thenReturn(null);
        genericDirectoryResponse = companyClient.getAdmin(AuthConstant.AUTHORIZATION_WWW_SERVICE, companyId, userId);
        // verify
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();
        DirectoryEntryDto directoryEntryDto3 = genericDirectoryResponse.getDirectoryEntry();
        assertThat(directoryEntryDto3).isEqualTo(directoryEntryDto1);
    }

    @After
    public void destroy() {
        adminRepo.deleteAll();
    }
}
