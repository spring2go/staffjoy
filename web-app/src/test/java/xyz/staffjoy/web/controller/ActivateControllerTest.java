package xyz.staffjoy.web.controller;

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
import xyz.staffjoy.account.client.AccountClient;
import xyz.staffjoy.account.dto.AccountDto;
import xyz.staffjoy.account.dto.GenericAccountResponse;
import xyz.staffjoy.account.dto.UpdatePasswordRequest;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.auth.Sessions;
import xyz.staffjoy.common.crypto.Sign;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.company.client.CompanyClient;
import xyz.staffjoy.company.dto.*;
import xyz.staffjoy.web.props.AppProps;
import xyz.staffjoy.web.service.HelperService;
import xyz.staffjoy.web.view.Constant;
import xyz.staffjoy.web.view.PageFactory;

import javax.servlet.http.Cookie;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class ActivateControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    AccountClient accountClient;
    @MockBean
    CompanyClient companyClient;

    @Autowired
    EnvConfig envConfig;

    @Autowired
    AppProps appProps;

    @Autowired
    PageFactory pageFactory;

    @Test
    public void testActivateAccount() throws Exception {
        String name = "test_user";
        String email = "test@staffjoy.xyz";
        Instant memberSince = Instant.now().minus(100, ChronoUnit.DAYS);
        String userId = UUID.randomUUID().toString();
        String token = Sign.generateEmailConfirmationToken(userId, email, appProps.getSigningSecret());

        AccountDto accountDto = AccountDto.builder()
                .id(userId)
                .name(name)
                .email(email)
                .memberSince(memberSince)
                .phoneNumber("18001112222")
                .confirmedAndActive(true)
                .photoUrl("http://www.staffjoy.xyz/photo/test_user.png")
                .build();

        when(accountClient.getAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, userId))
                .thenReturn(new GenericAccountResponse(accountDto));
        when(accountClient.updateAccount(anyString(), any(AccountDto.class)))
                .thenReturn(new GenericAccountResponse(accountDto));
        when(accountClient.updatePassword(anyString(), any(UpdatePasswordRequest.class)))
                .thenReturn(BaseResponse.builder().build());
        when(companyClient.getWorkerOf(AuthConstant.AUTHORIZATION_WWW_SERVICE, userId))
                .thenReturn(new GetWorkerOfResponse(WorkerOfList.builder().build()));
        when(companyClient.getAdminOf(AuthConstant.AUTHORIZATION_WWW_SERVICE, userId))
                .thenReturn(new GetAdminOfResponse(AdminOfList.builder().build()));

        MvcResult mvcResult = mockMvc.perform(post("/activate/" + token)
                .param("password", "newpassxxx")
                .param("name", accountDto.getName())
                .param("tos", "true")
                .param("phonenumber", accountDto.getPhoneNumber()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" +
                        HelperService.buildUrl("http", "www." + envConfig.getExternalApex(), "/new_company/")))
                .andReturn();
        Cookie cookie = mvcResult.getResponse().getCookie(AuthConstant.COOKIE_NAME);
        assertThat(cookie).isNotNull();
        assertThat(cookie.getName()).isEqualTo(AuthConstant.COOKIE_NAME);
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getDomain()).isEqualTo(envConfig.getExternalApex());
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getValue()).isNotBlank();
        assertThat(cookie.getMaxAge()).isEqualTo(Sessions.SHORT_SESSION / 1000);

        // company admin
        AdminOfList adminOfList = AdminOfList.builder().userId(userId).build();
        adminOfList.getCompanies().add(CompanyDto.builder().build());
        when(companyClient.getAdminOf(AuthConstant.AUTHORIZATION_WWW_SERVICE, userId))
                .thenReturn(new GetAdminOfResponse(adminOfList));
        mvcResult = mockMvc.perform(post("/activate/" + token)
                .param("password", "newpassxxx")
                .param("name", accountDto.getName())
                .param("tos", "true")
                .param("phonenumber", accountDto.getPhoneNumber()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" +
                        HelperService.buildUrl("http", "app." + envConfig.getExternalApex())))
                .andReturn();

        when(companyClient.getAdminOf(AuthConstant.AUTHORIZATION_WWW_SERVICE, userId))
                .thenReturn(new GetAdminOfResponse(AdminOfList.builder().build()));
        // worker of teams
        WorkerOfList workerOfList = WorkerOfList.builder().userId(userId).build();
        workerOfList.getTeams().add(TeamDto.builder().build());
        when(companyClient.getWorkerOf(AuthConstant.AUTHORIZATION_WWW_SERVICE, userId))
                .thenReturn(new GetWorkerOfResponse(workerOfList));
        mvcResult = mockMvc.perform(post("/activate/" + token)
                .param("password", "newpassxxx")
                .param("name", accountDto.getName())
                .param("tos", "true")
                .param("phonenumber", accountDto.getPhoneNumber()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" +
                        HelperService.buildUrl("http", "myaccount." + envConfig.getExternalApex())))
                .andReturn();

        // password too short
        mvcResult = mockMvc.perform(post("/activate/" + token)
                .param("password", "pass")
                .param("name", accountDto.getName())
                .param("tos", "true")
                .param("phonenumber", accountDto.getPhoneNumber()))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.VIEW_ACTIVATE))
                .andExpect(content().string(containsString(pageFactory.buildActivatePage().getTitle())))
                .andExpect(content().string(containsString(token)))
                .andExpect(content().string(containsString("Your password must be at least 6 characters long")))
                .andReturn();

        // no tos
        mvcResult = mockMvc.perform(post("/activate/" + token)
                .param("password", "newpassxxx")
                .param("name", accountDto.getName())
                .param("phonenumber", accountDto.getPhoneNumber()))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.VIEW_ACTIVATE))
                .andExpect(content().string(containsString(pageFactory.buildActivatePage().getTitle())))
                .andExpect(content().string(containsString(token)))
                .andExpect(content().string(containsString("You must agree to the terms and conditions by selecting the checkbox.")))
                .andReturn();
    }
}
