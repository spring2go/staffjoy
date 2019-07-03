package xyz.staffjoy.ical.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.company.client.CompanyClient;
import xyz.staffjoy.company.dto.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class ICalControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CompanyClient companyClient;

    @Test
    public void testGetCalByUserId() throws Exception {
        // arrange mocks
        String companyId = UUID.randomUUID().toString();
        String teamId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        String jobId = UUID.randomUUID().toString();

        WorkerDto workerDto = WorkerDto.builder()
                .userId(userId)
                .teamId(teamId)
                .companyId(companyId)
                .build();
        GenericWorkerResponse genericWorkerResponse = new GenericWorkerResponse(workerDto);
        when(companyClient.getWorkerTeamInfo(AuthConstant.AUTHORIZATION_ICAL_SERVICE, null, userId))
                .thenReturn(genericWorkerResponse);

        CompanyDto companyDto = CompanyDto.builder()
                .id(companyId)
                .defaultDayWeekStarts(TimeZone.getDefault().getID())
                .defaultDayWeekStarts("Monday")
                .name("i_cool_company")
                .build();
        GenericCompanyResponse genericCompanyResponse = new GenericCompanyResponse(companyDto);
        when(companyClient.getCompany(AuthConstant.AUTHORIZATION_ICAL_SERVICE, companyId))
                .thenReturn(genericCompanyResponse);

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

        ShiftList shiftList = ShiftList.builder()
                .shiftStartAfter(Instant.now().minus(30, ChronoUnit.DAYS))
                .shiftStartBefore(Instant.now().plus(90, ChronoUnit.DAYS))
                .build();
        shiftList.getShifts().add(shiftDto1);
        shiftList.getShifts().add(shiftDto2);
        GenericShiftListResponse genericShiftListResponse = new GenericShiftListResponse(shiftList);
        when(companyClient.listWorkerShifts(anyString(), any(WorkerShiftListRequest.class)))
                .thenReturn(genericShiftListResponse);

        MvcResult mvcResult = mockMvc.perform(get("/" + userId + ".ics"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/calendar;charset=UTF-8");
        assertThat(mvcResult.getResponse().getHeader("Content-Disposition")).contains(userId);
        log.info(mvcResult.getResponse().getContentAsString());
        assertThat(mvcResult.getResponse().getContentAsString()).contains(userId, companyDto.getName());
    }
}
