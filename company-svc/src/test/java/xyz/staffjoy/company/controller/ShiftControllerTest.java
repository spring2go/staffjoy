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
import xyz.staffjoy.bot.client.BotClient;
import xyz.staffjoy.bot.dto.*;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.api.ResultCode;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.company.TestConfig;
import xyz.staffjoy.company.client.CompanyClient;
import xyz.staffjoy.company.dto.*;
import xyz.staffjoy.company.model.Company;
import xyz.staffjoy.company.model.Job;
import xyz.staffjoy.company.model.Team;
import xyz.staffjoy.company.repo.CompanyRepo;
import xyz.staffjoy.company.repo.JobRepo;
import xyz.staffjoy.company.repo.ShiftRepo;
import xyz.staffjoy.company.repo.TeamRepo;
import xyz.staffjoy.company.service.DirectoryService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
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
public class ShiftControllerTest {
    @Autowired
    ShiftRepo shiftRepo;

    @Autowired
    CompanyClient companyClient;

    @MockBean
    AccountClient accountClient;
    @MockBean
    CompanyRepo companyRepo;
    @MockBean
    TeamRepo teamRepo;
    @MockBean
    DirectoryService directoryService;
    @MockBean
    JobRepo jobRepo;

    @MockBean
    BotClient botClient;

    private Company company;
    private Team team;
    private Job job;
    private DirectoryEntryDto directoryEntryDto1;
    private DirectoryEntryDto directoryEntryDto2;
    private ShiftDto shiftDto1;
    private ShiftDto shiftDto2;

    @Before
    public void setUp() {
        // cleanup
        shiftRepo.deleteAll();

        // arrange mock
        when(accountClient.trackEvent(any(TrackEventRequest.class))).thenReturn(BaseResponse.builder().build());

        String companyId = UUID.randomUUID().toString();
        company = Company.builder()
                .name("test_company001")
                .id(companyId)
                .defaultDayWeekStarts("Sunday")
                .defaultTimezone(TimeZone.getDefault().getID())
                .build();
        when(companyRepo.findCompanyById(company.getId())).thenReturn(company);

        team = Team.builder()
                .id(UUID.randomUUID().toString())
                .companyId(company.getId())
                .name("test_team001")
                .color("#48B7AB")
                .dayWeekStarts("Monday")
                .timezone(TimeZone.getDefault().getID())
                .build();
        when(teamRepo.findById(team.getId())).thenReturn(Optional.of(team));

        job = Job.builder()
                .id(UUID.randomUUID().toString())
                .name("test_job001")
                .teamId(team.getId())
                .color("48B7CC")
                .build();
        when(jobRepo.findJobById(job.getId())).thenReturn(job);

        // create worker1
        String userId1 = UUID.randomUUID().toString();
        directoryEntryDto1 = DirectoryEntryDto.builder()
                .companyId(company.getId())
                .userId(userId1)
                .name("test_user001")
                .email("test_user001@staffjoy.xyz")
                .internalId(UUID.randomUUID().toString())
                .phoneNumber("18001999999")
                .photoUrl("https://staffjoy.xyz/photo/test01.png")
                .build();
        when(directoryService.getDirectoryEntry(company.getId(), userId1)).thenReturn(directoryEntryDto1);

        // create worker2
        String userId2 = UUID.randomUUID().toString();
        directoryEntryDto2 = DirectoryEntryDto.builder()
                .companyId(company.getId())
                .userId(userId2)
                .name("test_user002")
                .email("test_user002@staffjoy.xyz")
                .internalId(UUID.randomUUID().toString())
                .phoneNumber("18001123456")
                .photoUrl("https://staffjoy.xyz/photo/test02.png")
                .build();
        when(directoryService.getDirectoryEntry(company.getId(), userId2)).thenReturn(directoryEntryDto2);
    }

    @Test
    public void testBulkPublishShifts() {
        this.createTwoShiftsAndVerify();

        BulkPublishShiftsRequest bulkPublishShiftsRequest1 = BulkPublishShiftsRequest.builder()
                .companyId(company.getId())
                .jobId(job.getId())
                .teamId(team.getId())
                .shiftStartAfter(Instant.now().minus(1, ChronoUnit.DAYS))
                .shiftStartBefore(Instant.now().plus(5, ChronoUnit.DAYS))
                .published(false)
                .build();

        // arrange mock
        when(botClient.alertRemovedShifts(any(AlertRemovedShiftsRequest.class)))
                .thenReturn(BaseResponse.builder().message("Removed Shifts Alerted").build());

        // unpublish
        GenericShiftListResponse shiftListResponse =
                companyClient.bulkPublishShifts(AuthConstant.AUTHORIZATION_SUPPORT_USER, bulkPublishShiftsRequest1);
        log.info(shiftListResponse.toString());
        assertThat(shiftListResponse.isSuccess()).isTrue();
        ShiftList shiftList = shiftListResponse.getShiftList();
        ShiftDto shiftDto1Clone = shiftDto1.toBuilder().published(false).build();
        ShiftDto shiftDto2Clone = shiftDto2.toBuilder().published(false).build();
        assertThat(shiftList.getShifts()).containsExactly(shiftDto1Clone, shiftDto2Clone);

        // capture and verify removed shifts alert
        ArgumentCaptor<AlertRemovedShiftsRequest> argument1 = ArgumentCaptor.forClass(AlertRemovedShiftsRequest.class);
        verify(botClient, times(1)).alertRemovedShifts(argument1.capture());
        AlertRemovedShiftsRequest alertRemovedShiftsRequest = argument1.getValue();
        assertThat(alertRemovedShiftsRequest.getUserId()).isEqualTo(shiftDto1.getUserId());
        assertThat(alertRemovedShiftsRequest.getOldShifts()).containsExactly(shiftDto1, shiftDto2);

        // publish again
        BulkPublishShiftsRequest bulkPublishShiftsRequest2 = BulkPublishShiftsRequest.builder()
                .companyId(company.getId())
                .jobId(job.getId())
                .teamId(team.getId())
                .shiftStartAfter(Instant.now().minus(1, ChronoUnit.DAYS))
                .shiftStartBefore(Instant.now().plus(5, ChronoUnit.DAYS))
                .published(true)
                .build();

        // arrange mock
        when(botClient.alertNewShifts(any(AlertNewShiftsRequest.class)))
                .thenReturn(BaseResponse.builder().message("New Shifts Alerted").build());

        shiftListResponse =
                companyClient.bulkPublishShifts(AuthConstant.AUTHORIZATION_SUPPORT_USER, bulkPublishShiftsRequest2);
        log.info(shiftListResponse.toString());
        assertThat(shiftListResponse.isSuccess()).isTrue();
        shiftList = shiftListResponse.getShiftList();
        assertThat(shiftList.getShifts()).containsExactly(shiftDto1, shiftDto2);

        // capture and verify new shifts alert
        ArgumentCaptor<AlertNewShiftsRequest> argument2 = ArgumentCaptor.forClass(AlertNewShiftsRequest.class);
        verify(botClient, times(1)).alertNewShifts(argument2.capture());
        AlertNewShiftsRequest alertNewShiftsRequest = argument2.getValue();
        assertThat(alertNewShiftsRequest.getUserId()).isEqualTo(shiftDto1.getUserId());
        assertThat(alertNewShiftsRequest.getNewShifts()).containsExactly(shiftDto1Clone, shiftDto2Clone);
    }

    @Test
    public void deleteShift() {
        this.createTwoShiftsAndVerify();

        // arrange mock
        when(botClient.alertRemovedShift(any(AlertRemovedShiftRequest.class)))
                .thenReturn(BaseResponse.builder().message("Removed Shift Alerted").build());

        // delete shift1
        BaseResponse baseResponse =
                companyClient.deleteShift(AuthConstant.AUTHORIZATION_SUPPORT_USER, shiftDto1.getId(), shiftDto1.getTeamId(), shiftDto1.getCompanyId());
        assertThat(baseResponse.isSuccess()).isTrue();

        // capture and verify removed shift alert
        ArgumentCaptor<AlertRemovedShiftRequest> argument1 = ArgumentCaptor.forClass(AlertRemovedShiftRequest.class);
        verify(botClient, times(1)).alertRemovedShift(argument1.capture());
        AlertRemovedShiftRequest alertRemovedShiftRequest = argument1.getValue();
        assertThat(alertRemovedShiftRequest.getUserId()).isEqualTo(directoryEntryDto1.getUserId());
        assertThat(alertRemovedShiftRequest.getOldShift()).isEqualTo(shiftDto1);

        // verify deleted
        GenericShiftResponse genericShiftResponse =
                companyClient.getShift(AuthConstant.AUTHORIZATION_SUPPORT_USER, shiftDto1.getId(), team.getId(), company.getId());
        assertThat(genericShiftResponse.isSuccess()).isFalse();
        assertThat(genericShiftResponse.getCode()).isEqualTo(ResultCode.NOT_FOUND);

        // listWorkerShifts
        ShiftListRequest shiftListRequest1 = ShiftListRequest.builder()
                .userId(directoryEntryDto1.getUserId())
                .companyId(company.getId())
                .teamId(team.getId())
                .shiftStartAfter(Instant.now().minus(1, ChronoUnit.DAYS))
                .shiftStartBefore(Instant.now().plus(5, ChronoUnit.DAYS))
                .build();
        GenericShiftListResponse shiftListResponse = companyClient.listShifts(AuthConstant.AUTHORIZATION_SUPPORT_USER, shiftListRequest1);
        log.info(shiftListResponse.toString());
        assertThat(shiftListResponse.isSuccess()).isTrue();
        ShiftList shiftList = shiftListResponse.getShiftList();
        // only shift2 left
        assertThat(shiftList.getShifts()).containsExactly(shiftDto2);
    }

    @Test
    public void testUpdateShift() {
        this.createTwoShiftsAndVerify();

        // no real update
        GenericShiftResponse genericShiftResponse =
                companyClient.updateShift(AuthConstant.AUTHORIZATION_SUPPORT_USER, shiftDto1);
        assertThat(genericShiftResponse.isSuccess()).isTrue();
        assertThat(genericShiftResponse.getShift()).isEqualTo(shiftDto1);


        // unpublish
        when(botClient.alertRemovedShift(any(AlertRemovedShiftRequest.class)))
                .thenReturn(BaseResponse.builder().message("Removed Shift Alerted").build());
        ShiftDto shiftDto1Clone1 = shiftDto1.toBuilder().build();
        shiftDto1Clone1.setPublished(false);
        genericShiftResponse =
                companyClient.updateShift(AuthConstant.AUTHORIZATION_SUPPORT_USER, shiftDto1Clone1);
        assertThat(genericShiftResponse.isSuccess()).isTrue();
        assertThat(genericShiftResponse.getShift()).isEqualTo(shiftDto1Clone1);

        // capture and verify removed shift alert
        ArgumentCaptor<AlertRemovedShiftRequest> argument1 = ArgumentCaptor.forClass(AlertRemovedShiftRequest.class);
        verify(botClient, times(1)).alertRemovedShift(argument1.capture());
        AlertRemovedShiftRequest alertRemovedShiftRequest = argument1.getValue();
        assertThat(alertRemovedShiftRequest.getUserId()).isEqualTo(directoryEntryDto1.getUserId());
        assertThat(alertRemovedShiftRequest.getOldShift()).isEqualTo(shiftDto1);

        // update stop time
        ShiftDto shiftDto1Clone2 = shiftDto1Clone1.toBuilder().build();
        shiftDto1Clone2.setStop(Instant.now().plus(10, ChronoUnit.DAYS));
        genericShiftResponse =
                companyClient.updateShift(AuthConstant.AUTHORIZATION_SUPPORT_USER, shiftDto1Clone2);
        assertThat(genericShiftResponse.isSuccess()).isTrue();
        assertThat(genericShiftResponse.getShift()).isEqualTo(shiftDto1Clone2);

        // republish again
        ShiftDto shiftDto1Clone3 = shiftDto1Clone2.toBuilder().build();
        shiftDto1Clone3.setPublished(true);
        genericShiftResponse =
                companyClient.updateShift(AuthConstant.AUTHORIZATION_SUPPORT_USER, shiftDto1Clone3);
        assertThat(genericShiftResponse.isSuccess()).isTrue();
        assertThat(genericShiftResponse.getShift()).isEqualTo(shiftDto1Clone3);

        // capture and verify new shift alert
        ArgumentCaptor<AlertNewShiftRequest> argument2 = ArgumentCaptor.forClass(AlertNewShiftRequest.class);
        // 3 times, 2 for create new shifts, 1 for republish
        verify(botClient, times(3)).alertNewShift(argument2.capture());
        AlertNewShiftRequest alertNewShiftRequest = argument2.getAllValues().get(2);
        assertThat(alertNewShiftRequest.getUserId()).isEqualTo(shiftDto1Clone3.getUserId());
        assertThat(alertNewShiftRequest.getNewShift()).isEqualTo(shiftDto1Clone3);

        // update published
        when(botClient.alertChangedShift(any(AlertChangedShiftRequest.class)))
                .thenReturn(BaseResponse.builder().message("Changed Shift Alerted").build());
        ShiftDto shiftDto1Clone4 = shiftDto1Clone3.toBuilder().build();
        shiftDto1Clone4.setStop(Instant.now().plus(8, ChronoUnit.DAYS));
        genericShiftResponse =
                companyClient.updateShift(AuthConstant.AUTHORIZATION_SUPPORT_USER, shiftDto1Clone4);
        assertThat(genericShiftResponse.isSuccess()).isTrue();
        assertThat(genericShiftResponse.getShift()).isEqualTo(shiftDto1Clone4);

        // capture and verify change shift alert
        ArgumentCaptor<AlertChangedShiftRequest> argument3 = ArgumentCaptor.forClass(AlertChangedShiftRequest.class);
        // 3 times, 2 for create new shifts, 1 for republish
        verify(botClient, times(1)).alertChangedShift(argument3.capture());
        AlertChangedShiftRequest alertChangedShiftRequest = argument3.getValue();
        assertThat(alertChangedShiftRequest.getUserId()).isEqualTo(directoryEntryDto1.getUserId());
        assertThat(alertChangedShiftRequest.getOldShift()).isEqualTo(shiftDto1Clone3);
        assertThat(alertChangedShiftRequest.getNewShift()).isEqualTo(shiftDto1Clone4);

        // updated userId
        ShiftDto shiftDto1Clone5 = shiftDto1Clone4.toBuilder().build();
        String userId3 = UUID.randomUUID().toString();
        shiftDto1Clone5.setUserId(userId3);

        // let validation pass
        when(directoryService.getDirectoryEntry(company.getId(), userId3)).thenReturn(null);
        genericShiftResponse =
                companyClient.updateShift(AuthConstant.AUTHORIZATION_SUPPORT_USER, shiftDto1Clone5);
        assertThat(genericShiftResponse.isSuccess()).isTrue();
        assertThat(genericShiftResponse.getShift()).isEqualTo(shiftDto1Clone5);

        // capture and verify new shift alert
        ArgumentCaptor<AlertNewShiftRequest> argument4 = ArgumentCaptor.forClass(AlertNewShiftRequest.class);
        // 4 times, 2 for create new shifts, 1 for republish, 1 for update userId
        verify(botClient, times(4)).alertNewShift(argument4.capture());
        AlertNewShiftRequest alertNewShiftRequest2 = argument4.getAllValues().get(3);
        assertThat(alertNewShiftRequest2.getUserId()).isEqualTo(shiftDto1Clone5.getUserId());
        assertThat(alertNewShiftRequest2.getNewShift()).isEqualTo(shiftDto1Clone5);

        // capture and verify removed shift alert
        ArgumentCaptor<AlertRemovedShiftRequest> argument5 = ArgumentCaptor.forClass(AlertRemovedShiftRequest.class);
        // 2 times, 1 for unpublish, 1 for update userId
        verify(botClient, times(2)).alertRemovedShift(argument5.capture());
        AlertRemovedShiftRequest alertRemovedShiftRequest2 = argument5.getAllValues().get(1);
        assertThat(alertRemovedShiftRequest2.getUserId()).isEqualTo(shiftDto1Clone4.getUserId());
        assertThat(alertRemovedShiftRequest2.getOldShift()).isEqualTo(shiftDto1Clone4);
    }

    @Test
    public void testListShifts() {
        this.createTwoShiftsAndVerify();

        // listWorkerShifts
        ShiftListRequest shiftListRequest1 = ShiftListRequest.builder()
                .userId(directoryEntryDto1.getUserId())
                .companyId(company.getId())
                .teamId(team.getId())
                .shiftStartAfter(Instant.now().minus(1, ChronoUnit.DAYS))
                .shiftStartBefore(Instant.now().plus(5, ChronoUnit.DAYS))
                .build();
        GenericShiftListResponse shiftListResponse = companyClient.listShifts(AuthConstant.AUTHORIZATION_SUPPORT_USER, shiftListRequest1);
        log.info(shiftListResponse.toString());
        assertThat(shiftListResponse.isSuccess()).isTrue();
        ShiftList shiftList = shiftListResponse.getShiftList();
        assertThat(shiftList.getShifts()).containsExactly(shiftDto1, shiftDto2);

        // listShiftByJobId
        ShiftListRequest shiftListRequest2 = ShiftListRequest.builder()
                .jobId(job.getId())
                .companyId(company.getId())
                .teamId(team.getId())
                .shiftStartAfter(Instant.now().minus(1, ChronoUnit.DAYS))
                .shiftStartBefore(Instant.now().plus(5, ChronoUnit.DAYS))
                .build();
        shiftListResponse = companyClient.listShifts(AuthConstant.AUTHORIZATION_SUPPORT_USER, shiftListRequest2);
        log.info(shiftListResponse.toString());
        assertThat(shiftListResponse.isSuccess()).isTrue();
        shiftList = shiftListResponse.getShiftList();
        assertThat(shiftList.getShifts()).containsExactly(shiftDto1, shiftDto2);

        // listShiftByUserIdAndJobId
        ShiftListRequest shiftListRequest3 = ShiftListRequest.builder()
                .jobId(job.getId())
                .userId(directoryEntryDto1.getUserId())
                .companyId(company.getId())
                .teamId(team.getId())
                .shiftStartAfter(Instant.now().minus(1, ChronoUnit.DAYS))
                .shiftStartBefore(Instant.now().plus(5, ChronoUnit.DAYS))
                .build();
        shiftListResponse = companyClient.listShifts(AuthConstant.AUTHORIZATION_SUPPORT_USER, shiftListRequest3);
        log.info(shiftListResponse.toString());
        assertThat(shiftListResponse.isSuccess()).isTrue();
        shiftList = shiftListResponse.getShiftList();
        assertThat(shiftList.getShifts()).containsExactly(shiftDto1, shiftDto2);

        // listShiftByTeamIdOnly
        ShiftListRequest shiftListRequest4 = ShiftListRequest.builder()
                .companyId(company.getId())
                .teamId(team.getId())
                .shiftStartAfter(Instant.now().minus(1, ChronoUnit.DAYS))
                .shiftStartBefore(Instant.now().plus(5, ChronoUnit.DAYS))
                .build();
        shiftListResponse = companyClient.listShifts(AuthConstant.AUTHORIZATION_SUPPORT_USER, shiftListRequest4);
        log.info(shiftListResponse.toString());
        assertThat(shiftListResponse.isSuccess()).isTrue();
        shiftList = shiftListResponse.getShiftList();
        assertThat(shiftList.getShifts()).containsExactly(shiftDto1, shiftDto2);
    }

    @Test
    public void testListWorkerShifts() {
        this.createTwoShiftsAndVerify();

        // list worker shifts
        WorkerShiftListRequest workerShiftListRequest = WorkerShiftListRequest.builder()
                .companyId(company.getId())
                .workerId(directoryEntryDto1.getUserId())
                .teamId(team.getId())
                .shiftStartAfter(Instant.now().minus(1, ChronoUnit.DAYS))
                .shiftStartBefore(Instant.now().plus(5, ChronoUnit.DAYS))
                .build();
        GenericShiftListResponse genericShiftListResponse =
                companyClient.listWorkerShifts(AuthConstant.AUTHORIZATION_SUPPORT_USER, workerShiftListRequest);
        log.info(genericShiftListResponse.toString());
        assertThat(genericShiftListResponse.isSuccess()).isTrue();
        ShiftList shiftList = genericShiftListResponse.getShiftList();
        assertThat(shiftList.getShifts()).containsExactly(shiftDto1, shiftDto2);

        // verify shifts
        GenericShiftResponse genericShiftResponse =
                companyClient.getShift(AuthConstant.AUTHORIZATION_SUPPORT_USER, shiftDto1.getId(), team.getId(), company.getId());
        assertThat(genericShiftResponse.isSuccess()).isTrue();
        assertThat(genericShiftResponse.getShift()).isEqualTo(shiftDto1);

        genericShiftResponse =
                companyClient.getShift(AuthConstant.AUTHORIZATION_SUPPORT_USER, shiftDto2.getId(), team.getId(), company.getId());
        assertThat(genericShiftResponse.isSuccess()).isTrue();
        assertThat(genericShiftResponse.getShift()).isEqualTo(shiftDto2);
    }

    public void createTwoShiftsAndVerify() {

        when(botClient.alertNewShift(any(AlertNewShiftRequest.class)))
                .thenReturn(BaseResponse.builder().message("New Shift Alerted").build());

        // create and verity shift
        CreateShiftRequest createShiftRequest1 = CreateShiftRequest.builder()
                .companyId(company.getId())
                .jobId(job.getId())
                .teamId(team.getId())
                .userId(directoryEntryDto1.getUserId())
                .published(true)
                .start(Instant.now().plus(1, ChronoUnit.DAYS))
                .stop(Instant.now().plus(3, ChronoUnit.DAYS))
                .build();
        GenericShiftResponse genericShiftResponse = companyClient.createShift(AuthConstant.AUTHORIZATION_SUPPORT_USER, createShiftRequest1);
        log.info(genericShiftResponse.toString());
        assertThat(genericShiftResponse.isSuccess()).isTrue();
        shiftDto1 = genericShiftResponse.getShift();
        assertThat(shiftDto1.getUserId()).isEqualTo(createShiftRequest1.getUserId());
        assertThat(shiftDto1.getCompanyId()).isEqualTo(createShiftRequest1.getCompanyId());
        assertThat(shiftDto1.getTeamId()).isEqualTo(createShiftRequest1.getTeamId());
        assertThat(shiftDto1.getJobId()).isEqualTo(createShiftRequest1.getJobId());
        assertThat(shiftDto1.getStart()).isEqualTo(createShiftRequest1.getStart());
        assertThat(shiftDto1.getStop()).isEqualTo(createShiftRequest1.getStop());
        assertThat(shiftDto1.isPublished()).isEqualTo(createShiftRequest1.isPublished());

        // capture and verify new shift alert
        ArgumentCaptor<AlertNewShiftRequest> argument = ArgumentCaptor.forClass(AlertNewShiftRequest.class);
        verify(botClient, times(1)).alertNewShift(argument.capture());
        AlertNewShiftRequest alertNewShiftRequest = argument.getValue();
        assertThat(alertNewShiftRequest.getUserId()).isEqualTo(directoryEntryDto1.getUserId());
        assertThat(alertNewShiftRequest.getNewShift()).isEqualTo(shiftDto1);

        verify(accountClient, times(2)).trackEvent(any(TrackEventRequest.class));

        // create and verity shift
        CreateShiftRequest createShiftRequest2 = CreateShiftRequest.builder()
                .companyId(company.getId())
                .jobId(job.getId())
                .teamId(team.getId())
                .userId(directoryEntryDto1.getUserId())
                .published(true)
                .start(Instant.now().plus(2, ChronoUnit.DAYS))
                .stop(Instant.now().plus(4, ChronoUnit.DAYS))
                .build();
        genericShiftResponse = companyClient.createShift(AuthConstant.AUTHORIZATION_SUPPORT_USER, createShiftRequest2);
        log.info(genericShiftResponse.toString());
        assertThat(genericShiftResponse.isSuccess()).isTrue();
        shiftDto2 = genericShiftResponse.getShift();
        assertThat(shiftDto2.getUserId()).isEqualTo(createShiftRequest2.getUserId());
        assertThat(shiftDto2.getCompanyId()).isEqualTo(createShiftRequest2.getCompanyId());
        assertThat(shiftDto2.getTeamId()).isEqualTo(createShiftRequest2.getTeamId());
        assertThat(shiftDto2.getJobId()).isEqualTo(createShiftRequest2.getJobId());
        assertThat(shiftDto2.getStart()).isEqualTo(createShiftRequest2.getStart());
        assertThat(shiftDto2.getStop()).isEqualTo(createShiftRequest2.getStop());
        assertThat(shiftDto2.isPublished()).isEqualTo(createShiftRequest2.isPublished());
    }

    @Test
    public void testCreateShift() {
        this.createTwoShiftsAndVerify();
    }

    @After
    public void destroy() {
        shiftRepo.deleteAll();
    }
}
