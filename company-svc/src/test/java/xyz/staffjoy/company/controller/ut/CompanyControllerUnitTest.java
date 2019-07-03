package xyz.staffjoy.company.controller.ut;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import xyz.staffjoy.common.api.ResultCode;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.company.dto.CompanyDto;
import xyz.staffjoy.company.dto.CompanyList;
import xyz.staffjoy.company.dto.ListCompanyResponse;
import xyz.staffjoy.company.dto.GenericCompanyResponse;
import xyz.staffjoy.company.service.CompanyService;

import static org.assertj.core.api.Java6Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.TimeZone;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CompanyControllerUnitTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CompanyService companyService;

    @Autowired
    ObjectMapper objectMapper;

    CompanyDto newCompanyDto;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() {
        newCompanyDto = CompanyDto.builder()
                .archived(false)
                .name("test-company")
                .defaultDayWeekStarts("Monday")
                .defaultTimezone(TimeZone.getDefault().getID())
                .build();
    }

    @Test()
    public void testCreateCompanyAuthorizeMissing() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/v1/company/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(newCompanyDto)))
                .andExpect(status().isOk())
                .andReturn();

        GenericCompanyResponse genericCompanyResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GenericCompanyResponse.class);
        assertThat(genericCompanyResponse.isSuccess()).isFalse();
        assertThat(genericCompanyResponse.getCode()).isEqualTo(ResultCode.UN_AUTHORIZED);
    }

    @Test
    public void testCreateCompanyPermissionDeniedException() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/v1/company/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthConstant.AUTHORIZATION_HEADER, AuthConstant.AUTHORIZATION_COMPANY_SERVICE)
                .content(objectMapper.writeValueAsBytes(newCompanyDto)))
                .andExpect(status().isOk())
                .andReturn();

        GenericCompanyResponse genericCompanyResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GenericCompanyResponse.class);
        assertThat(genericCompanyResponse.isSuccess()).isFalse();
        assertThat(genericCompanyResponse.getCode()).isEqualTo(ResultCode.UN_AUTHORIZED);
    }

    @Test
    public void testCreateCompanyInvalidDayWeekStarts() throws Exception {

        newCompanyDto.setDefaultDayWeekStarts("mondayday");

        MvcResult mvcResult = mockMvc.perform(post("/v1/company/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthConstant.AUTHORIZATION_HEADER, AuthConstant.AUTHORIZATION_SUPPORT_USER)
                .content(objectMapper.writeValueAsBytes(newCompanyDto)))
                .andExpect(status().isOk())
                .andReturn();

        GenericCompanyResponse genericCompanyResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GenericCompanyResponse.class);
        assertThat(genericCompanyResponse.isSuccess()).isFalse();
        assertThat(genericCompanyResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);
        assertThat(genericCompanyResponse.getMessage()).startsWith("defaultDayWeekStarts").endsWith("Unknown day of week");
    }

    @Test
    public void testCreateCompanyInvalidTimezone() throws Exception {

        newCompanyDto.setDefaultTimezone("nonexisting-tinezone");

        MvcResult mvcResult = mockMvc.perform(post("/v1/company/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthConstant.AUTHORIZATION_HEADER, AuthConstant.AUTHORIZATION_SUPPORT_USER)
                .content(objectMapper.writeValueAsBytes(newCompanyDto)))
                .andExpect(status().isOk())
                .andReturn();

        GenericCompanyResponse genericCompanyResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GenericCompanyResponse.class);
        assertThat(genericCompanyResponse.isSuccess()).isFalse();
        assertThat(genericCompanyResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);
        assertThat(genericCompanyResponse.getMessage()).startsWith("defaultTimezone").endsWith("Invalid timezone");
    }

    @Test
    public void testCreateCompanyEmptyName() throws Exception {

        newCompanyDto.setName(null);

        MvcResult mvcResult = mockMvc.perform(post("/v1/company/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthConstant.AUTHORIZATION_HEADER, AuthConstant.AUTHORIZATION_SUPPORT_USER)
                .content(objectMapper.writeValueAsBytes(newCompanyDto)))
                .andExpect(status().isOk())
                .andReturn();

        GenericCompanyResponse genericCompanyResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GenericCompanyResponse.class);
        assertThat(genericCompanyResponse.isSuccess()).isFalse();
        assertThat(genericCompanyResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);
        assertThat(genericCompanyResponse.getMessage()).startsWith("name").endsWith("must not be blank");
    }

    @Test
    public void testCreateCompanySuccessfully() throws Exception {
        when(companyService.createCompany(any(CompanyDto.class))).thenReturn(newCompanyDto);

        MvcResult mvcResult = mockMvc.perform(post("/v1/company/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthConstant.AUTHORIZATION_HEADER, AuthConstant.AUTHORIZATION_SUPPORT_USER)
                .content(objectMapper.writeValueAsString(newCompanyDto)))
                .andExpect(status().isOk())
                .andReturn();

        verify(companyService, times(1)).createCompany(eq(newCompanyDto));

        GenericCompanyResponse genericCompanyResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GenericCompanyResponse.class);
        assertThat(genericCompanyResponse.isSuccess()).isTrue();
        assertThat(genericCompanyResponse.getCompany()).isEqualTo(newCompanyDto);
    }

    @Test
    public void testListCompanySuccessfully() throws Exception {
        CompanyList expectedCompanyList = new CompanyList(Arrays.asList(newCompanyDto), 1, 1);
        when(companyService.listCompanies(anyInt(), anyInt())).thenReturn(expectedCompanyList);

        MvcResult mvcResult = mockMvc.perform(get("/v1/company/list?limit=1&offset=1")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthConstant.AUTHORIZATION_HEADER, AuthConstant.AUTHORIZATION_SUPPORT_USER))
                .andExpect(status().isOk())
                .andReturn();

        verify(companyService, times(1)).listCompanies(eq(1), eq(1));
        ListCompanyResponse listCompanyResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ListCompanyResponse.class);
        assertThat(listCompanyResponse.isSuccess()).isTrue();
        assertThat(listCompanyResponse.getCompanyList()).isEqualTo(expectedCompanyList);

    }

    @Test
    public void testListCompanyMissingParam() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/v1/company/list")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthConstant.AUTHORIZATION_HEADER, AuthConstant.AUTHORIZATION_SUPPORT_USER))
                .andExpect(status().isOk())
                .andReturn();

        ListCompanyResponse listCompanyResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ListCompanyResponse.class);
        assertThat(listCompanyResponse.isSuccess()).isFalse();
        assertThat(listCompanyResponse.getCode()).isEqualTo(ResultCode.PARAM_MISS);
        assertThat(listCompanyResponse.getMessage()).startsWith("Missing Request Parameter:").endsWith("offset");
    }

    @Test
    public void testGetCompanySuccessfully() throws Exception {
        String id = UUID.randomUUID().toString();
        newCompanyDto.setId(id);
        CompanyDto expectedCompanyDto = newCompanyDto;
        when(companyService.getCompany(id)).thenReturn(expectedCompanyDto);

        MvcResult mvcResult = mockMvc.perform(get("/v1/company/get?company_id=" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthConstant.AUTHORIZATION_HEADER, AuthConstant.AUTHORIZATION_BOT_SERVICE))
                .andExpect(status().isOk())
                .andReturn();

        verify(companyService, times(1)).getCompany(eq(id));
        GenericCompanyResponse genericCompanyResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GenericCompanyResponse.class);
        assertThat(genericCompanyResponse.isSuccess()).isTrue();
        assertThat(genericCompanyResponse.getCompany()).isEqualTo(expectedCompanyDto);
    }

    @Test
    public void testGetCompanyMissingPathVariable() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/v1/company/get")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthConstant.AUTHORIZATION_HEADER, AuthConstant.AUTHORIZATION_SUPPORT_USER))
                .andExpect(status().isOk())
                .andReturn();

        GenericCompanyResponse genericCompanyResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GenericCompanyResponse.class);
        assertThat(genericCompanyResponse.isSuccess()).isFalse();
        assertThat(genericCompanyResponse.getCode()).isEqualTo(ResultCode.PARAM_MISS);
        assertThat(genericCompanyResponse.getMessage()).startsWith("Missing Request Parameter:").endsWith("company_id");
    }

    @Test
    public void testUpdateCompanySuccessfully() throws Exception {
        String id = UUID.randomUUID().toString();
        newCompanyDto.setId(id);
        CompanyDto companyDtoToUpdate = newCompanyDto;
        when(companyService.updateCompany(eq(companyDtoToUpdate))).thenReturn(companyDtoToUpdate);

        MvcResult mvcResult = mockMvc.perform(put("/v1/company/update")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthConstant.AUTHORIZATION_HEADER, AuthConstant.AUTHORIZATION_SUPPORT_USER)
                .content(objectMapper.writeValueAsString(companyDtoToUpdate)))
                .andExpect(status().isOk())
                .andReturn();

        GenericCompanyResponse genericCompanyResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GenericCompanyResponse.class);
        assertThat(genericCompanyResponse.isSuccess()).isTrue();
        assertThat(genericCompanyResponse.getCompany()).isEqualTo(companyDtoToUpdate);
    }

    @Test
    public void testUpdateCompanyMissingId() throws Exception {
        CompanyDto companyDtoToUpdate = newCompanyDto; // no id
        when(companyService.updateCompany(eq(companyDtoToUpdate))).thenReturn(companyDtoToUpdate);
        MvcResult mvcResult = mockMvc.perform(put("/v1/company/update")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthConstant.AUTHORIZATION_HEADER, AuthConstant.AUTHORIZATION_SUPPORT_USER)
                .content(objectMapper.writeValueAsString(companyDtoToUpdate)))
                .andExpect(status().isOk())
                .andReturn();

        GenericCompanyResponse genericCompanyResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GenericCompanyResponse.class);
        assertThat(genericCompanyResponse.isSuccess()).isFalse();
        assertThat(genericCompanyResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);
        assertThat(genericCompanyResponse.getMessage()).startsWith("id:must not be blank");

    }
}
