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
import xyz.staffjoy.account.dto.CreateAccountRequest;
import xyz.staffjoy.account.dto.GenericAccountResponse;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.web.view.Constant;
import xyz.staffjoy.web.view.PageFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

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
public class SignupControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    AccountClient accountClient;

    @Autowired
    EnvConfig envConfig;

    @Autowired
    PageFactory pageFactory;

    @Test
    public void testSignup() throws Exception {

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
                .confirmedAndActive(false)
                .photoUrl("http://www.staffjoy.xyz/photo/test_user.png")
                .build();

        when(accountClient.createAccount(anyString(), any(CreateAccountRequest.class)))
                .thenReturn(new GenericAccountResponse(accountDto));

        // email empty
        MvcResult mvcResult = mockMvc.perform(post("/confirm")
                .param("email", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(SignupController.SIGN_UP_REDIRECT_PATH))
                .andReturn();

        mvcResult = mockMvc.perform(post("/confirm")
                .param("email", accountDto.getEmail()).param("name", accountDto.getName()))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.VIEW_CONFIRM))
                .andExpect(content().string(containsString(pageFactory.buildConfirmPage().getDescription())))
                .andReturn();
    }

}
