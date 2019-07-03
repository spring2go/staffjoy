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
import xyz.staffjoy.company.dto.CompanyDto;
import xyz.staffjoy.company.dto.CompanyList;
import xyz.staffjoy.company.dto.ListCompanyResponse;
import xyz.staffjoy.company.dto.GenericCompanyResponse;
import xyz.staffjoy.company.repo.CompanyRepo;

import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@DirtiesContext // avoid port conflict
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableFeignClients(basePackages = {"xyz.staffjoy.company.client"})
@Import(TestConfig.class)
@Slf4j
public class CompanyControllerTest {
    @Autowired
    CompanyClient companyClient;

    @MockBean
    AccountClient accountClient;

    @Autowired
    private CompanyRepo companyRepo;

    @Before
    public void setUp() {
        // cleanup
        companyRepo.deleteAll();
    }

    @Test
    public void testUpdateCompany() {
        when(accountClient.trackEvent(any(TrackEventRequest.class))).thenReturn(BaseResponse.builder().message("company created").build());

        // create one company
        CompanyDto companyDto = CompanyDto.builder()
                .name("test_company001")
                .defaultDayWeekStarts("Monday")
                .defaultTimezone(TimeZone.getDefault().getID())
                .build();
        GenericCompanyResponse genericCompanyResponse = companyClient.createCompany(AuthConstant.AUTHORIZATION_SUPPORT_USER, companyDto);
        CompanyDto createdCompany = genericCompanyResponse.getCompany();
        assertThat(genericCompanyResponse.isSuccess()).isTrue();

        // update
        companyDto.setId(createdCompany.getId());
        companyDto.setName("test_company_update001");
        companyDto.setArchived(true);
        genericCompanyResponse = companyClient.updateCompany(AuthConstant.AUTHORIZATION_SUPPORT_USER, companyDto);
        log.info(genericCompanyResponse.toString());
        assertThat(genericCompanyResponse.isSuccess()).isTrue();
        CompanyDto updatedCompany = genericCompanyResponse.getCompany();
        assertThat(updatedCompany).isEqualTo(companyDto);

        // update not found
        companyDto.setId(UUID.randomUUID().toString());
        companyDto.setName("test_company_update002");
        companyDto.setArchived(true);
        genericCompanyResponse = companyClient.updateCompany(AuthConstant.AUTHORIZATION_SUPPORT_USER, companyDto);
        log.info(genericCompanyResponse.toString());
        assertThat(genericCompanyResponse.isSuccess()).isFalse();
        assertThat(genericCompanyResponse.getCode()).isEqualTo(ResultCode.NOT_FOUND);
    }

    @Test
    public void testListCompany() {
        when(accountClient.trackEvent(any(TrackEventRequest.class))).thenReturn(BaseResponse.builder().message("company created").build());

        // first company
        CompanyDto companyDto = CompanyDto.builder()
                .name("test_company001")
                .defaultDayWeekStarts("Monday")
                .defaultTimezone(TimeZone.getDefault().getID())
                .build();
        GenericCompanyResponse genericCompanyResponse = companyClient.createCompany(AuthConstant.AUTHORIZATION_WWW_SERVICE, companyDto);
        assertThat(genericCompanyResponse.isSuccess()).isTrue();

        // list company and verify
        ListCompanyResponse listCompanyResponse = companyClient.listCompanies(AuthConstant.AUTHORIZATION_SUPPORT_USER, 0, 2);
        log.info((listCompanyResponse.toString()));
        assertThat(listCompanyResponse.isSuccess()).isTrue();
        CompanyList companyList = listCompanyResponse.getCompanyList();
        assertThat(companyList.getCompanies()).hasSize(1);
        assertThat(companyList.getLimit()).isEqualTo(2);
        assertThat(companyList.getOffset()).isEqualTo(0);

        // second company
        companyDto = CompanyDto.builder()
                .name("test_company002")
                .defaultDayWeekStarts("Sunday")
                .defaultTimezone(TimeZone.getDefault().getID())
                .build();
        genericCompanyResponse = companyClient.createCompany(AuthConstant.AUTHORIZATION_WWW_SERVICE, companyDto);
        assertThat(genericCompanyResponse.isSuccess()).isTrue();

        // list company and verify
        listCompanyResponse = companyClient.listCompanies(AuthConstant.AUTHORIZATION_SUPPORT_USER, 0, 2);
        log.info((listCompanyResponse.toString()));
        assertThat(listCompanyResponse.isSuccess()).isTrue();
        companyList = listCompanyResponse.getCompanyList();
        assertThat(companyList.getCompanies()).hasSize(2);
        assertThat(companyList.getLimit()).isEqualTo(2);
        assertThat(companyList.getOffset()).isEqualTo(0);

        // third company
        companyDto = CompanyDto.builder()
                .name("test_company003")
                .defaultDayWeekStarts("Monday")
                .defaultTimezone(TimeZone.getDefault().getID())
                .build();
        genericCompanyResponse = companyClient.createCompany(AuthConstant.AUTHORIZATION_WWW_SERVICE, companyDto);
        assertThat(genericCompanyResponse.isSuccess()).isTrue();

        // list company and verify
        listCompanyResponse = companyClient.listCompanies(AuthConstant.AUTHORIZATION_SUPPORT_USER, 1, 2);
        log.info((listCompanyResponse.toString()));
        assertThat(listCompanyResponse.isSuccess()).isTrue();
        companyList = listCompanyResponse.getCompanyList();
        assertThat(companyList.getCompanies()).hasSize(1);
        assertThat(companyList.getLimit()).isEqualTo(2);
        assertThat(companyList.getOffset()).isEqualTo(1);
    }

    @Test
    public void testCreateThenGetCompany() {
        when(accountClient.trackEvent(any(TrackEventRequest.class))).thenReturn(BaseResponse.builder().message("company created").build());

        // create a new company
        CompanyDto companyDto = CompanyDto.builder()
                .name("test_company")
                .defaultDayWeekStarts("Monday")
                .defaultTimezone(TimeZone.getDefault().getID())
                .build();
        GenericCompanyResponse genericCompanyResponse = companyClient.createCompany(AuthConstant.AUTHORIZATION_WWW_SERVICE, companyDto);
        assertThat(genericCompanyResponse.isSuccess()).isTrue();
        CompanyDto createdCompany = genericCompanyResponse.getCompany();
        companyDto.setId(createdCompany.getId());
        assertThat(createdCompany).isEqualTo(companyDto);

        // capture and verify track event sent
        ArgumentCaptor<TrackEventRequest> argument = ArgumentCaptor.forClass(TrackEventRequest.class);
        verify(accountClient, times(1)).trackEvent(argument.capture());
        TrackEventRequest trackEventRequest = argument.getValue();
        assertThat(trackEventRequest.getUserId()).isEqualTo(TestConfig.TEST_USER_ID);
        assertThat(trackEventRequest.getEvent()).isEqualTo("company_created");

        // test getCompany
        genericCompanyResponse = companyClient.getCompany(AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE, createdCompany.getId());
        log.info(genericCompanyResponse.toString());
        assertThat(genericCompanyResponse.isSuccess()).isTrue();
        assertThat(genericCompanyResponse.getCompany()).isEqualTo(createdCompany);
    }

    @After
    public void destroy() {
        companyRepo.deleteAll();
    }
}
