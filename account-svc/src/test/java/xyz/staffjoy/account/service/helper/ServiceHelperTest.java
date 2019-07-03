package xyz.staffjoy.account.service.helper;

import io.intercom.api.CustomAttribute;
import io.intercom.api.User;
import io.sentry.SentryClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import xyz.staffjoy.account.model.Account;
import xyz.staffjoy.account.repo.AccountRepo;
import xyz.staffjoy.bot.client.BotClient;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.company.client.CompanyClient;
import xyz.staffjoy.company.dto.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ServiceHelperTest {

    @Mock
    private CompanyClient companyClient;

    @Mock
    private AccountRepo accountRepo;

    @Mock
    private SentryClient sentryClient;

    @Mock
    private BotClient botClient;

    @Mock
    private EnvConfig envConfig;

    @InjectMocks
    @Spy
    private ServiceHelper serviceHelper;

    @Test
    public void testIsAlmostSameInstant() {
        Instant now = Instant.now();
        Instant twoSecondsLater = now.plusSeconds(2);
        assertThat(serviceHelper.isAlmostSameInstant(now, twoSecondsLater)).isFalse();

        Instant oneSecondLater = now.plusSeconds(1);
        assertThat(serviceHelper.isAlmostSameInstant(now, oneSecondLater)).isFalse();

        Instant haveSecondLater = now.plus(500000, ChronoUnit.MICROS);
        assertThat(serviceHelper.isAlmostSameInstant(now, haveSecondLater)).isTrue();
    }

    @Test
    public void testSyncUserAsync() {
        Account account = Account.builder()
                .id(UUID.randomUUID().toString())
                .name("test_user")
                .email("test_user@jskillcloud.com")
                .phoneNumber("1122334455")
                .confirmedAndActive(true)
                .memberSince(Instant.now())
                .photoUrl("http://test/test.png")
                .build();

        when(envConfig.isDebug()).thenReturn(false);
        when(accountRepo.findAccountById(anyString())).thenReturn(account);
        doNothing().when(serviceHelper).syncUserWithIntercom(any(User.class), eq(account.getId()));

        String companyId = UUID.randomUUID().toString();
        WorkerOfList workerOfList = WorkerOfList.builder()
                .userId(account.getId())
                .build();
        workerOfList.getTeams().add(TeamDto.builder().name("test_team").companyId(companyId).build());
        when(companyClient.getWorkerOf(anyString(), eq(account.getId()))).thenReturn(new GetWorkerOfResponse(workerOfList));

        CompanyDto companyDto = CompanyDto.builder()
                .id(companyId)
                .name("test_company")
                .build();
        when(companyClient.getCompany(anyString(), eq(companyId))).thenReturn(new GenericCompanyResponse(companyDto));

        AdminOfList adminOfList = AdminOfList.builder()
                .userId(account.getId())
                .build();
        adminOfList.getCompanies().add(companyDto);
        when(companyClient.getAdminOf(anyString(), eq(account.getId()))).thenReturn(new GetAdminOfResponse(adminOfList));

        serviceHelper.syncUserAsync(account.getId());

        ArgumentCaptor<User> argument1 = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> argument2 = ArgumentCaptor.forClass(String.class);

        verify(serviceHelper, times(1)).syncUserWithIntercom(argument1.capture(), argument2.capture());
        User user = argument1.getValue();
        String userId = argument2.getValue();
        log.info(user.toString());
        log.info(userId);
        assertThat(user.getUserId()).isEqualTo(account.getId());
        assertThat(user.getEmail()).isEqualTo(account.getEmail());
        assertThat(user.getName()).isEqualTo(account.getName());
        assertThat(user.getSignedUpAt()).isEqualTo(account.getMemberSince().toEpochMilli());
        assertThat(user.getAvatar().getImageURL().toString()).isEqualTo(account.getPhotoUrl());
        assertThat(user.getCustomAttributes().get("v2")).isEqualTo(CustomAttribute.newBooleanAttribute("v2", true));
        assertThat(user.getCustomAttributes().get("phonenumber")).isEqualTo(CustomAttribute.newStringAttribute("phonenumber", account.getPhoneNumber()));
        assertThat(user.getCustomAttributes().get("confirmed_and_active")).isEqualTo(CustomAttribute.newBooleanAttribute("confirmed_and_active", account.isConfirmedAndActive()));
        assertThat(user.getCustomAttributes().get("is_worker")).isEqualTo(CustomAttribute.newBooleanAttribute("is_worker", true));
        assertThat(user.getCustomAttributes().get("is_admin")).isEqualTo(CustomAttribute.newBooleanAttribute("is_admin", true));
        assertThat(user.getCustomAttributes().get("is_staffjoy_support")).isEqualTo(CustomAttribute.newBooleanAttribute("is_staffjoy_support", account.isSupport()));

        io.intercom.api.Company iCompany = user.getCompanyCollection().next();
        assertThat(iCompany.getCompanyID()).isEqualTo(companyId);
        assertThat(iCompany.getName()).isEqualTo(companyDto.getName());
    }

}
