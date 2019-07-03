package xyz.staffjoy.bot.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.staffjoy.account.client.AccountClient;
import xyz.staffjoy.account.dto.AccountDto;
import xyz.staffjoy.account.dto.GenericAccountResponse;
import xyz.staffjoy.bot.BotConstant;
import xyz.staffjoy.bot.client.BotClient;
import xyz.staffjoy.bot.dto.*;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.company.client.CompanyClient;
import xyz.staffjoy.company.dto.*;
import xyz.staffjoy.mail.client.MailClient;
import xyz.staffjoy.mail.dto.EmailRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext // avoid port conflict
@EnableFeignClients(basePackages = {"xyz.staffjoy.bot.client"})
@Slf4j
public class AlertControllerTest {
    @Autowired
    BotClient botClient;

    @MockBean
    AccountClient accountClient;

    @MockBean
    CompanyClient companyClient;

    @MockBean
    MailClient mailClient;

    String userId;
    String companyId;
    String teamId;
    String jobId;
    AccountDto accountDto;
    CompanyDto companyDto;
    JobDto jobDto;

    @Before
    public void setUp() {
        // arrange mock
        userId = UUID.randomUUID().toString();
        accountDto = AccountDto.builder()
                .name("test_user001")
                .phoneNumber("11111111111")
                .email("test_user001@staffjoy.xyz")
                .id(userId)
                .memberSince(Instant.now().minus(30, ChronoUnit.DAYS))
                .confirmedAndActive(true)
                .photoUrl("https://staffjoy.xyz/photo/test01.png")
                .build();
        when(accountClient.getAccount(AuthConstant.AUTHORIZATION_BOT_SERVICE, userId))
                .thenReturn(new GenericAccountResponse(accountDto));

        companyId = UUID.randomUUID().toString();
        companyDto = CompanyDto.builder()
                .name("test_company001")
                .defaultTimezone(TimeZone.getDefault().getID())
                .defaultDayWeekStarts("Monday")
                .id(companyId)
                .build();
        when(companyClient.getCompany(AuthConstant.AUTHORIZATION_BOT_SERVICE, companyId))
                .thenReturn(new GenericCompanyResponse(companyDto));

        teamId = UUID.randomUUID().toString();
        jobId = UUID.randomUUID().toString();
        jobDto = JobDto.builder()
                .id(jobId)
                .companyId(companyId)
                .color("#48B7AB")
                .teamId(teamId)
                .name("test_job001")
                .build();
        when(companyClient.getJob(AuthConstant.AUTHORIZATION_BOT_SERVICE, jobId, companyId, teamId))
                .thenReturn(new GenericJobResponse(jobDto));


        TeamDto teamDto = TeamDto.builder()
                .name("test_team001")
                .companyId(companyId)
                .color("#48B7AB")
                .dayWeekStarts("Monday")
                .timezone(TimeZone.getDefault().getID())
                .id(teamId)
                .build();
        when(companyClient.getTeam(AuthConstant.AUTHORIZATION_BOT_SERVICE, companyId, teamId))
                .thenReturn(new GenericTeamResponse(teamDto));

        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("mail sent").build());
    }

    @Test
    public void testNewShiftAlert() {
        ShiftDto shiftDto = ShiftDto.builder()
                .id(UUID.randomUUID().toString())
                .companyId(companyId)
                .teamId(teamId)
                .jobId(jobId)
                .published(true)
                .userId(userId)
                .start(Instant.now().plus(2, ChronoUnit.DAYS))
                .stop(Instant.now().plus(5, ChronoUnit.DAYS))
                .build();
        AlertNewShiftRequest alertNewShiftRequest = AlertNewShiftRequest.builder()
                .userId(userId)
                .newShift(shiftDto)
                .build();

        // new shift alert
        BaseResponse baseResponse =  botClient.alertNewShift(alertNewShiftRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // verify email
        ArgumentCaptor<EmailRequest> argument = ArgumentCaptor.forClass(EmailRequest.class);
        verify(mailClient, times(1)).send(argument.capture());
        EmailRequest emailRequest = argument.getValue();
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(accountDto.getEmail());
        assertThat(emailRequest.getName()).isEqualTo(accountDto.getName());
        assertThat(emailRequest.getSubject()).isEqualTo("New Shift Alert");
        assertThat(emailRequest.getHtmlBody()).contains(accountDto.getName(), companyDto.getName(), jobDto.getName());
    }

    @Test
    public void testNewShiftsAlert() {
        ShiftDto shiftDto1 = ShiftDto.builder()
                .id(UUID.randomUUID().toString())
                .companyId(companyId)
                .teamId(teamId)
                .jobId(jobId)
                .published(true)
                .userId(userId)
                .start(Instant.now().plus(2, ChronoUnit.DAYS))
                .stop(Instant.now().plus(3, ChronoUnit.DAYS))
                .build();
        ShiftDto shiftDto2 = ShiftDto.builder()
                .id(UUID.randomUUID().toString())
                .companyId(companyId)
                .teamId(teamId)
                .jobId(jobId)
                .published(true)
                .userId(userId)
                .start(Instant.now().plus(4, ChronoUnit.DAYS))
                .stop(Instant.now().plus(5, ChronoUnit.DAYS))
                .build();

        AlertNewShiftsRequest alertNewShiftsRequest = AlertNewShiftsRequest.builder()
                .userId(userId)
                .build();
        alertNewShiftsRequest.getNewShifts().add(shiftDto1);
        alertNewShiftsRequest.getNewShifts().add(shiftDto2);

        // new shift alert
        BaseResponse baseResponse =  botClient.alertNewShifts(alertNewShiftsRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // verify email
        ArgumentCaptor<EmailRequest> argument = ArgumentCaptor.forClass(EmailRequest.class);
        verify(mailClient, times(1)).send(argument.capture());
        EmailRequest emailRequest = argument.getValue();
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(accountDto.getEmail());
        assertThat(emailRequest.getName()).isEqualTo(accountDto.getName());
        assertThat(emailRequest.getSubject()).isEqualTo("New Shifts Alert");
        assertThat(emailRequest.getHtmlBody()).contains(accountDto.getName(), companyDto.getName(), jobDto.getName());
    }

    @Test
    public void testRemovedShift() {
        ShiftDto shiftDto1 = ShiftDto.builder()
                .id(UUID.randomUUID().toString())
                .companyId(companyId)
                .teamId(teamId)
                .jobId(jobId)
                .published(true)
                .userId(userId)
                .start(Instant.now().plus(2, ChronoUnit.DAYS))
                .stop(Instant.now().plus(3, ChronoUnit.DAYS))
                .build();
        ShiftDto shiftDto2 = ShiftDto.builder()
                .id(UUID.randomUUID().toString())
                .companyId(companyId)
                .teamId(teamId)
                .jobId(jobId)
                .published(true)
                .userId(userId)
                .start(Instant.now().plus(4, ChronoUnit.DAYS))
                .stop(Instant.now().plus(5, ChronoUnit.DAYS))
                .build();
        ShiftDto shiftDto3 = ShiftDto.builder()
                .id(UUID.randomUUID().toString())
                .companyId(companyId)
                .teamId(teamId)
                .jobId(jobId)
                .published(true)
                .userId(userId)
                .start(Instant.now().plus(6, ChronoUnit.DAYS))
                .stop(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        Instant startAfter = Instant.now();
        Instant startBefore = Instant.now().plus(BotConstant.SHIFT_WINDOW, ChronoUnit.DAYS);

        ShiftList shiftList = ShiftList.builder()
                .shiftStartAfter(startAfter)
                .shiftStartBefore(startBefore)
                .build();
        shiftList.getShifts().add(shiftDto2);
        shiftList.getShifts().add(shiftDto3);

        when(companyClient.listWorkerShifts(anyString(), any(WorkerShiftListRequest.class)))
                .thenReturn(new GenericShiftListResponse(shiftList));

        AlertRemovedShiftRequest alertRemovedShiftRequest = AlertRemovedShiftRequest.builder()
                .userId(userId)
                .oldShift(shiftDto1)
                .build();

        // removed shift alert
        BaseResponse baseResponse =  botClient.alertRemovedShift(alertRemovedShiftRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // verify email
        ArgumentCaptor<EmailRequest> argument = ArgumentCaptor.forClass(EmailRequest.class);
        verify(mailClient, times(1)).send(argument.capture());
        EmailRequest emailRequest = argument.getValue();
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(accountDto.getEmail());
        assertThat(emailRequest.getName()).isEqualTo(accountDto.getName());
        assertThat(emailRequest.getSubject()).isEqualTo("Removed Shift Alert");
        assertThat(emailRequest.getHtmlBody()).contains(accountDto.getName(), companyDto.getName());
    }

    @Test
    public void testRemovedShifts() {
        ShiftDto shiftDto1 = ShiftDto.builder()
                .id(UUID.randomUUID().toString())
                .companyId(companyId)
                .teamId(teamId)
                .jobId(jobId)
                .published(true)
                .userId(userId)
                .start(Instant.now().plus(2, ChronoUnit.DAYS))
                .stop(Instant.now().plus(3, ChronoUnit.DAYS))
                .build();
        ShiftDto shiftDto2 = ShiftDto.builder()
                .id(UUID.randomUUID().toString())
                .companyId(companyId)
                .teamId(teamId)
                .jobId(jobId)
                .published(true)
                .userId(userId)
                .start(Instant.now().plus(4, ChronoUnit.DAYS))
                .stop(Instant.now().plus(5, ChronoUnit.DAYS))
                .build();
        ShiftDto shiftDto3 = ShiftDto.builder()
                .id(UUID.randomUUID().toString())
                .companyId(companyId)
                .teamId(teamId)
                .jobId(jobId)
                .published(true)
                .userId(userId)
                .start(Instant.now().plus(6, ChronoUnit.DAYS))
                .stop(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();
        ShiftDto shiftDto4 = ShiftDto.builder()
                .id(UUID.randomUUID().toString())
                .companyId(companyId)
                .teamId(teamId)
                .jobId(jobId)
                .published(true)
                .userId(userId)
                .start(Instant.now().plus(8, ChronoUnit.DAYS))
                .stop(Instant.now().plus(9, ChronoUnit.DAYS))
                .build();

        Instant startAfter = Instant.now();
        Instant startBefore = Instant.now().plus(BotConstant.SHIFT_WINDOW, ChronoUnit.DAYS);

        ShiftList shiftList = ShiftList.builder()
                .shiftStartAfter(startAfter)
                .shiftStartBefore(startBefore)
                .build();
        shiftList.getShifts().add(shiftDto3);
        shiftList.getShifts().add(shiftDto4);

        when(companyClient.listWorkerShifts(anyString(), any(WorkerShiftListRequest.class)))
                .thenReturn(new GenericShiftListResponse(shiftList));

        AlertRemovedShiftsRequest alertRemovedShiftsRequest = AlertRemovedShiftsRequest.builder()
                .userId(userId)
                .build();
        alertRemovedShiftsRequest.getOldShifts().add(shiftDto1);
        alertRemovedShiftsRequest.getOldShifts().add(shiftDto2);

        // removed shift alert
        BaseResponse baseResponse =  botClient.alertRemovedShifts(alertRemovedShiftsRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // verify email
        ArgumentCaptor<EmailRequest> argument = ArgumentCaptor.forClass(EmailRequest.class);
        verify(mailClient, times(1)).send(argument.capture());
        EmailRequest emailRequest = argument.getValue();
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(accountDto.getEmail());
        assertThat(emailRequest.getName()).isEqualTo(accountDto.getName());
        assertThat(emailRequest.getSubject()).isEqualTo("Removed Shifts Alert");
        assertThat(emailRequest.getHtmlBody()).contains(accountDto.getName(), companyDto.getName());
    }

    @Test
    public void testChangedShift() {
        ShiftDto oldShiftDto = ShiftDto.builder()
                .id(UUID.randomUUID().toString())
                .companyId(companyId)
                .teamId(teamId)
                .jobId(jobId)
                .published(true)
                .userId(userId)
                .start(Instant.now().plus(2, ChronoUnit.DAYS))
                .stop(Instant.now().plus(3, ChronoUnit.DAYS))
                .build();
        ShiftDto newShiftDto = ShiftDto.builder()
                .id(UUID.randomUUID().toString())
                .companyId(companyId)
                .teamId(teamId)
                .jobId(jobId)
                .published(true)
                .userId(userId)
                .start(Instant.now().plus(4, ChronoUnit.DAYS))
                .stop(Instant.now().plus(5, ChronoUnit.DAYS))
                .build();

        AlertChangedShiftRequest alertChangedShiftRequest = AlertChangedShiftRequest.builder()
                .userId(userId)
                .oldShift(oldShiftDto)
                .newShift(newShiftDto)
                .build();
        // changed shift alert
        BaseResponse baseResponse = botClient.alertChangedShift(alertChangedShiftRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // verify email
        ArgumentCaptor<EmailRequest> argument = ArgumentCaptor.forClass(EmailRequest.class);
        verify(mailClient, times(1)).send(argument.capture());
        EmailRequest emailRequest = argument.getValue();
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(accountDto.getEmail());
        assertThat(emailRequest.getName()).isEqualTo(accountDto.getName());
        assertThat(emailRequest.getSubject()).isEqualTo("Changed Shift Alert");
        assertThat(emailRequest.getHtmlBody()).contains(accountDto.getName(), companyDto.getName());
    }
}
