package xyz.staffjoy.company.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import xyz.staffjoy.company.model.Company;
import xyz.staffjoy.company.model.Team;
import xyz.staffjoy.company.repo.CompanyRepo;
import xyz.staffjoy.company.repo.TeamRepo;
import xyz.staffjoy.company.repo.WorkerRepo;
import xyz.staffjoy.company.service.DirectoryService;

import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@DirtiesContext // avoid port conflict
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableFeignClients(basePackages = {"xyz.staffjoy.company.client"})
@Import(TestConfig.class)
@Slf4j
public class WorkerControllerTest {

    @Autowired
    WorkerRepo workerRepo;

    @Autowired
    CompanyClient companyClient;

    @MockBean
    CompanyRepo companyRepo;

    @MockBean
    TeamRepo teamRepo;

    @MockBean
    AccountClient accountClient;

    @MockBean
    DirectoryService directoryService;

    @Before
    public void setUp() {
        // cleanup
        workerRepo.deleteAll();
    }

    @Test
    public void testWorker() {
        // arrange mock
        when(accountClient.trackEvent(any(TrackEventRequest.class))).thenReturn(BaseResponse.builder().build());

        String companyId = UUID.randomUUID().toString();
        Company company = Company.builder()
                .name("test_company001")
                .id(companyId)
                .defaultDayWeekStarts("Sunday")
                .defaultTimezone(TimeZone.getDefault().getID())
                .build();
        when(companyRepo.findCompanyById(companyId)).thenReturn(company);

        Team team = Team.builder()
                .id(UUID.randomUUID().toString())
                .companyId(companyId)
                .name("test_team001")
                .color("#48B7AB")
                .dayWeekStarts("Monday")
                .timezone(TimeZone.getDefault().getID())
                .build();
        when(teamRepo.findById(team.getId())).thenReturn(Optional.of(team));

        // create worker1
        String userId1 = UUID.randomUUID().toString();
        DirectoryEntryDto directoryEntryDto1 = DirectoryEntryDto.builder()
                .companyId(companyId)
                .userId(userId1)
                .name("test_user001")
                .email("test_user001@staffjoy.xyz")
                .internalId(UUID.randomUUID().toString())
                .phoneNumber("18001999999")
                .photoUrl("https://staffjoy.xyz/photo/test01.png")
                .build();
        when(directoryService.getDirectoryEntry(companyId, userId1)).thenReturn(directoryEntryDto1);

        WorkerDto workerDto1 = WorkerDto.builder()
                .companyId(companyId)
                .teamId(team.getId())
                .userId(userId1)
                .build();
        GenericDirectoryResponse genericDirectoryResponse =
                companyClient.createWorker(AuthConstant.AUTHORIZATION_WHOAMI_SERVICE, workerDto1);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();
        DirectoryEntryDto directoryEntryDto2 = genericDirectoryResponse.getDirectoryEntry();
        assertThat(directoryEntryDto2).isEqualTo(directoryEntryDto1);

        // create worker2
        String userId2 = UUID.randomUUID().toString();
        DirectoryEntryDto directoryEntryDto3 = DirectoryEntryDto.builder()
                .companyId(companyId)
                .userId(userId2)
                .name("test_user002")
                .email("test_user002@staffjoy.xyz")
                .internalId(UUID.randomUUID().toString())
                .phoneNumber("18002888888")
                .photoUrl("https://staffjoy.xyz/photo/test02.png")
                .build();
        when(directoryService.getDirectoryEntry(companyId, userId2)).thenReturn(directoryEntryDto3);

        WorkerDto workerDto2 = WorkerDto.builder()
                .companyId(companyId)
                .teamId(team.getId())
                .userId(userId2)
                .build();
        genericDirectoryResponse =
                companyClient.createWorker(AuthConstant.AUTHORIZATION_WHOAMI_SERVICE, workerDto2);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();
        DirectoryEntryDto directoryEntryDto4 = genericDirectoryResponse.getDirectoryEntry();
        assertThat(directoryEntryDto4).isEqualTo(directoryEntryDto3);

        GetWorkerOfResponse workerOfResponse = companyClient.getWorkerOf(AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE, userId1);
        log.info(workerOfResponse.toString());
        assertThat(workerOfResponse.isSuccess()).isTrue();
        TeamDto teamFound = workerOfResponse.getWorkerOfList().getTeams().get(0);
        assertThat(teamFound.getId()).isEqualTo(team.getId());
        assertThat(teamFound.getCompanyId()).isEqualTo(team.getCompanyId());
        assertThat(teamFound.getTimezone()).isEqualTo(team.getTimezone());
        assertThat(teamFound.getColor()).isEqualTo(team.getColor());
        assertThat(teamFound.getDayWeekStarts()).isEqualTo(team.getDayWeekStarts());
        assertThat(teamFound.isArchived()).isEqualTo(team.isArchived());
        assertThat(teamFound.getName()).isEqualTo(team.getName());

        // list worker
        ListWorkerResponse listWorkerResponse =
                companyClient.listWorkers(AuthConstant.AUTHORIZATION_SUPPORT_USER, companyId, team.getId());
        log.info(listWorkerResponse.toString());
        assertThat(listWorkerResponse.isSuccess()).isTrue();
        WorkerEntries workerEntries = listWorkerResponse.getWorkerEntries();
        assertThat(workerEntries.getWorkers()).containsExactly(directoryEntryDto1, directoryEntryDto3);

        // get worker1
        genericDirectoryResponse = companyClient.getWorker(AuthConstant.AUTHORIZATION_WWW_SERVICE, companyId, team.getId(), userId1);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isTrue();
        DirectoryEntryDto directoryEntryDto5 = genericDirectoryResponse.getDirectoryEntry();
        assertThat(directoryEntryDto5).isEqualTo(directoryEntryDto1);

        // delete worker1
        BaseResponse baseResponse = companyClient.deleteWorker(AuthConstant.AUTHORIZATION_SUPPORT_USER, workerDto1);
        assertThat(baseResponse.isSuccess()).isTrue();

        // get worker1 again not found
        genericDirectoryResponse = companyClient.getWorker(AuthConstant.AUTHORIZATION_WWW_SERVICE, companyId, team.getId(), userId1);
        log.info(genericDirectoryResponse.toString());
        assertThat(genericDirectoryResponse.isSuccess()).isFalse();
        assertThat(genericDirectoryResponse.getCode()).isEqualTo(ResultCode.NOT_FOUND);
    }

    @After
    public void destroy() {
        // cleanup
        workerRepo.deleteAll();
    }
}
