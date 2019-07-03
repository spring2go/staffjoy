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
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.company.TestConfig;
import xyz.staffjoy.company.client.CompanyClient;
import xyz.staffjoy.company.dto.*;
import xyz.staffjoy.company.model.Admin;
import xyz.staffjoy.company.model.Company;
import xyz.staffjoy.company.model.Worker;
import xyz.staffjoy.company.repo.AdminRepo;
import xyz.staffjoy.company.repo.CompanyRepo;
import xyz.staffjoy.company.repo.TeamRepo;
import xyz.staffjoy.company.repo.WorkerRepo;

import java.util.Arrays;
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
public class TeamControllerTest {
    @Autowired
    TeamRepo teamRepo;

    @Autowired
    CompanyClient companyClient;

    @MockBean
    AccountClient accountClient;

    @MockBean
    CompanyRepo companyRepo;

    @MockBean
    AdminRepo adminRepo;

    @MockBean
    WorkerRepo workerRepo;

    @Before
    public void setUp() {
        // cleanup
        teamRepo.deleteAll();
    }

    @Test
    public void testTeam() {
        // arrange mock
        when(accountClient.trackEvent(any(TrackEventRequest.class))).thenReturn(BaseResponse.builder().build());

        String companyId = UUID.randomUUID().toString();
        Company company = Company.builder()
                .name("test_company001")
                .id(companyId)
                .defaultDayWeekStarts("Sunday")
                .defaultTimezone(TimeZone.getDefault().getID())
                .build();

        // crate team1
        CreateTeamRequest createTeamRequest1 = CreateTeamRequest.builder()
                .name("test_team001")
                .companyId(companyId)
                .dayWeekStarts("Sunday")
                .timezone(TimeZone.getDefault().getID())
                .color("#48B7AB")
                .build();
        when(companyRepo.findCompanyById(companyId)).thenReturn(company);
        // for admin permission validation
        TestConfig.TEST_USER_ID = UUID.randomUUID().toString();
        Admin admin1 = Admin.builder().userId(TestConfig.TEST_USER_ID).companyId(companyId).build();
        when(adminRepo.findByCompanyIdAndUserId(companyId, TestConfig.TEST_USER_ID)).thenReturn(admin1);

        // save team1
        GenericTeamResponse genericTeamResponse =
                companyClient.createTeam(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, createTeamRequest1);
        log.info(genericTeamResponse.toString());
        assertThat(genericTeamResponse.isSuccess()).isTrue();
        TeamDto team1 = genericTeamResponse.getTeam();

        // crate team2
        CreateTeamRequest createTeamRequest2 = CreateTeamRequest.builder()
                .name("test_team002")
                .companyId(companyId)
                .dayWeekStarts("Sunday")
                .timezone(TimeZone.getDefault().getID())
                .color("#48B7AB")
                .build();
        // for admin permission validation
        TestConfig.TEST_USER_ID = UUID.randomUUID().toString();
        Admin admin2 = Admin.builder().userId(TestConfig.TEST_USER_ID).companyId(companyId).build();
        when(adminRepo.findByCompanyIdAndUserId(companyId, TestConfig.TEST_USER_ID)).thenReturn(admin2);

        // save team2
        genericTeamResponse =
                companyClient.createTeam(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, createTeamRequest2);
        log.info(genericTeamResponse.toString());
        assertThat(genericTeamResponse.isSuccess()).isTrue();
        TeamDto team2 = genericTeamResponse.getTeam();

        // list teams
        ListTeamResponse listTeamResponse = companyClient.listTeams(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, companyId);
        log.info(listTeamResponse.toString());
        assertThat(listTeamResponse.isSuccess()).isTrue();
        TeamList teamList = listTeamResponse.getTeamList();
        assertThat(teamList.getTeams()).containsExactly(team1, team2);


        // update team
        TeamDto teamToUpdate = team2;
        teamToUpdate.setName("test_team002_update");
        teamToUpdate.setArchived(true);
        genericTeamResponse = companyClient.updateTeam(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, teamToUpdate);
        log.info(genericTeamResponse.toString());
        assertThat(genericTeamResponse.isSuccess()).isTrue();
        TeamDto teamUpdated = genericTeamResponse.getTeam();
        assertThat(teamUpdated).isEqualTo(teamToUpdate);

        // get team
        genericTeamResponse = companyClient.getTeam(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, companyId, team2.getId());
        log.info(genericTeamResponse.toString());
        assertThat(genericTeamResponse.isSuccess()).isTrue();
        TeamDto team2Found = genericTeamResponse.getTeam();
        assertThat(team2Found).isEqualTo(teamUpdated);

        // getWorkerTeamInfo
        Worker worker = Worker.builder().userId(TestConfig.TEST_USER_ID).teamId(team2Found.getId()).build();
        when(workerRepo.findByUserId(TestConfig.TEST_USER_ID)).thenReturn(Arrays.asList(worker));
        GenericWorkerResponse genericWorkerResponse =
                companyClient.getWorkerTeamInfo(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, null, TestConfig.TEST_USER_ID);
        assertThat(genericWorkerResponse.isSuccess()).isTrue();
        log.info(genericWorkerResponse.toString());
        WorkerDto workerDto =genericWorkerResponse.getWorker();
        assertThat(workerDto.getCompanyId()).isEqualTo(companyId);
        assertThat(workerDto.getTeamId()).isEqualTo(team2Found.getId());
        assertThat(workerDto.getUserId()).isEqualTo(TestConfig.TEST_USER_ID);
    }


    @After
    public void destroy() {
        teamRepo.deleteAll();
    }
}
