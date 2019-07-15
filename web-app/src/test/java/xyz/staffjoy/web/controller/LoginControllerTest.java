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
import xyz.staffjoy.account.dto.*;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.api.ResultCode;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.auth.Sessions;
import xyz.staffjoy.common.env.EnvConfig;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class LoginControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    AccountClient accountClient;

    @Autowired
    EnvConfig envConfig;

    @Autowired
    PageFactory pageFactory;

    @Autowired
    LoginController loginController;

    @Test
    public void testAleadyLoggedIn() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/login")
                .header(AuthConstant.AUTHORIZATION_HEADER, AuthConstant.AUTHORIZATION_AUTHENTICATED_USER))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" +
                        HelperService.buildUrl("http", "myaccount." + envConfig.getExternalApex())))
                .andReturn();
    }

    @Test
    public void testGet() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.VIEW_LOGIN))
                .andExpect(content().string(containsString(pageFactory.buildLoginPage().getDescription())))
                .andReturn();
        log.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testLoginAndLogout() throws Exception {
        String name = "test_user";
        String email = "test@staffjoy.xyz";
        Instant memberSince = Instant.now().minus(100, ChronoUnit.DAYS);
        String userId = UUID.randomUUID().toString();
        AccountDto accountDto = AccountDto.builder()
                .id(userId)
                .name(name)
                .email(email)
                .memberSince(memberSince)
                .phoneNumber("18001112222")
                .confirmedAndActive(true)
                .photoUrl("http://www.staffjoy.xyz/photo/test_user.png")
                .build();
        when(accountClient.verifyPassword(anyString(), any(VerifyPasswordRequest.class)))
                .thenReturn(new GenericAccountResponse(accountDto));

        when(accountClient.trackEvent(any(TrackEventRequest.class)))
                .thenReturn(BaseResponse.builder().message("event tracked").build());
        when(accountClient.syncUser(any(SyncUserRequest.class)))
                .thenReturn(BaseResponse.builder().message("user synced").build());

        MvcResult mvcResult = mockMvc.perform(post("/login")
                ).andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" +
                        HelperService.buildUrl("http", "app." + envConfig.getExternalApex())))
                .andReturn();
        Cookie cookie = mvcResult.getResponse().getCookie(AuthConstant.COOKIE_NAME);
        assertThat(cookie).isNotNull();
        assertThat(cookie.getName()).isEqualTo(AuthConstant.COOKIE_NAME);
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getDomain()).isEqualTo(envConfig.getExternalApex());
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getValue()).isNotBlank();
        assertThat(cookie.getMaxAge()).isEqualTo(Sessions.SHORT_SESSION / 1000);

        // remember-me
        mvcResult = mockMvc.perform(post("/login")
                .param("remember-me", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" +
                        HelperService.buildUrl("http", "app." + envConfig.getExternalApex())))
                .andReturn();
        cookie = mvcResult.getResponse().getCookie(AuthConstant.COOKIE_NAME);
        assertThat(cookie).isNotNull();
        assertThat(cookie.getName()).isEqualTo(AuthConstant.COOKIE_NAME);
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getDomain()).isEqualTo(envConfig.getExternalApex());
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getValue()).isNotBlank();
        assertThat(cookie.getMaxAge()).isEqualTo(Sessions.LONG_SESSION / 1000);

        // redirect-to
        mvcResult = mockMvc.perform(post("/login")
                .param("return_to", "ical." + envConfig.getExternalApex() + "/test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:http://ical." + envConfig.getExternalApex() + "/test"))
                .andReturn();

        // redirect-to invalid
        mvcResult = mockMvc.perform(post("/login")
                .param("return_to", "signalx." + envConfig.getExternalApex() + "/test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" +
                        HelperService.buildUrl("http", "myaccount." + envConfig.getExternalApex())))
                .andReturn();

        // logout
        mvcResult = mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andReturn();
        cookie = mvcResult.getResponse().getCookie(AuthConstant.COOKIE_NAME);
        assertThat(cookie).isNotNull();
        assertThat(cookie.getName()).isEqualTo(AuthConstant.COOKIE_NAME);
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getDomain()).isEqualTo(envConfig.getExternalApex());
        assertThat(cookie.getValue()).isBlank();
        assertThat(cookie.getMaxAge()).isEqualTo(0);
    }

    @Test
    public void testLoginFail() throws Exception {
        GenericAccountResponse genericAccountResponse = new GenericAccountResponse();
        genericAccountResponse.setCode(ResultCode.UN_AUTHORIZED);
        genericAccountResponse.setMessage("Incorrect password");
        when(accountClient.verifyPassword(anyString(), any(VerifyPasswordRequest.class)))
                .thenReturn(genericAccountResponse);

        String email = "test@staffjoy.xyz";
        MvcResult mvcResult = mockMvc.perform(post("/login")
                .param("email", email)
                .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.VIEW_LOGIN))
                .andExpect(content().string(containsString(pageFactory.buildLoginPage().getDescription())))
                .andExpect(content().string(containsString(email)))
                .andExpect(content().string(containsString("Sorry, no account was found with that email and password.")))
                .andReturn();
        log.info(mvcResult.getResponse().getContentAsString());    }

    @Test
    public void testIsValidSub() {
        assertThat(loginController.isValidSub("http://account." + envConfig.getExternalApex() + "/test")).isTrue();
        assertThat(loginController.isValidSub("httpxxx://account." + envConfig.getExternalApex() + "/test")).isFalse();
        assertThat(loginController.isValidSub("http://ical." + envConfig.getExternalApex() + "/test")).isTrue();
        assertThat(loginController.isValidSub("http://accountx." + envConfig.getExternalApex() + "/test")).isFalse();
    }
}
