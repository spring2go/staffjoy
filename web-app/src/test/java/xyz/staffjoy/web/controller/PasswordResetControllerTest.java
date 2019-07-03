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
import xyz.staffjoy.account.dto.PasswordResetRequest;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.web.view.Constant;
import xyz.staffjoy.web.view.PageFactory;

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
public class PasswordResetControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    AccountClient accountClient;

    @Autowired
    EnvConfig envConfig;

    @Autowired
    PageFactory pageFactory;

    @Test
    public void testPasswordReset() throws Exception {
        // get request
        MvcResult mvcResult = mockMvc.perform(get(ResetController.PASSWORD_RESET_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.VIEW_RESET))
                .andExpect(content().string(containsString(pageFactory.buildResetPage().getDescription())))
                .andReturn();

        when(accountClient.requestPasswordReset(anyString(), any(PasswordResetRequest.class)))
                .thenReturn(BaseResponse.builder().build());

        // post request
        mvcResult = mockMvc.perform(post(ResetController.PASSWORD_RESET_PATH)
                .param("email", "test@staffjoy.xyz"))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.VIEW_CONFIRM))
                .andExpect(content().string(containsString(pageFactory.buildResetConfirmPage().getDescription())))
                .andReturn();
    }

}
