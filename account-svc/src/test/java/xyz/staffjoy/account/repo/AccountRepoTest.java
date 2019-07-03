package xyz.staffjoy.account.repo;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import xyz.staffjoy.account.model.Account;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.staffjoy.account.model.AccountSecret;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.Assert.*;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringRunner.class)
public class AccountRepoTest {

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private AccountSecretRepo accountSecretRepo;

    private Account newAccount;

    @Before
    public void setUp() {
        newAccount = Account.builder()
                .name("testAccount")
                .email("test@staffjoy.net")
                .memberSince(LocalDateTime.of(2019, 1, 20, 12, 50).atZone(ZoneId.systemDefault()).toInstant())
                .confirmedAndActive(false)
                .photoUrl("https://staffjoy.xyz/photo/test.png")
                .phoneNumber("18001801266")
                .support(false)
                .build();
        // sanity check
        accountRepo.deleteAll();
    }

    @Test//(expected = DuplicateKeyException.class)
    public void createSampleAccount() {
        accountRepo.save(newAccount);
        assertTrue(accountRepo.existsById(newAccount.getId()));
    }


    @Test
    public void getAccountById() {
        accountRepo.save(newAccount);
        assertEquals(1, accountRepo.count());
        Account foundAccount = accountRepo.findById(newAccount.getId()).get();
        assertEquals(newAccount, foundAccount);
    }

    @Test
    public void findAccountByEmail() {
        // not existing
        Account foundAccount = accountRepo.findAccountByEmail("notexisting@staffjoy.net");
        assertNull(foundAccount);

        accountRepo.save(newAccount);
        assertEquals(1, accountRepo.count());
        foundAccount = accountRepo.findAccountByEmail(newAccount.getEmail());
        assertNotNull(foundAccount);
        assertEquals(newAccount.getId(), foundAccount.getId());
    }

    @Test
    public void findAccountByPhoneNumber() {
        // not existing
        Account foundAccount = accountRepo.findAccountByPhoneNumber("18001800180");
        assertNull(foundAccount);

        // create new
        accountRepo.save(newAccount);
        assertEquals(1, accountRepo.count());
        foundAccount = accountRepo.findAccountByPhoneNumber(newAccount.getPhoneNumber());
        assertEquals(newAccount.getId(), foundAccount.getId());
    }

    @Test
    public void listAccount() {
        Pageable pageRequest = PageRequest.of(0, 2);
        // test empty
        Page<Account> accounts = accountRepo.findAll(pageRequest);
        assertEquals(0, accounts.getTotalElements());

        // create 1 new
        accountRepo.save(newAccount);
        assertEquals(1, accountRepo.count());

        // create 2 more
        newAccount.setId(null);
        accountRepo.save(newAccount);
        assertEquals(2, accountRepo.count());
        newAccount.setId(null);
        accountRepo.save(newAccount);
        assertEquals(3, accountRepo.count());
        accounts = accountRepo.findAll(pageRequest);
        assertEquals(2, accounts.getNumberOfElements());
        pageRequest = pageRequest.next();
        accounts = accountRepo.findAll(pageRequest);
        assertEquals(1, accounts.getNumberOfElements());
        assertEquals(2, accounts.getTotalPages());
        assertEquals(3, accounts.getTotalElements());
    }

    @Test
    public void updateAccount() {
        // create new
        accountRepo.save(newAccount);
        assertEquals(1, accountRepo.count());

        Account toUpdateAccount = newAccount;
        toUpdateAccount.setName("update");
        toUpdateAccount.setEmail("update@staffjoy.xyz");
        accountRepo.save(toUpdateAccount);
        Account updatedAccount = accountRepo.save(toUpdateAccount);
        Account foundAccount = accountRepo.findById(updatedAccount.getId()).get();
        assertEquals(updatedAccount, foundAccount);

        toUpdateAccount.setConfirmedAndActive(true);
        toUpdateAccount.setSupport(true);
        toUpdateAccount.setPhoneNumber("19001900190");
        toUpdateAccount.setPhotoUrl("http://staffjoy.net/photo/update.png");
        updatedAccount = accountRepo.save(toUpdateAccount);
        foundAccount = accountRepo.findById(updatedAccount.getId()).get();
        assertEquals(updatedAccount, foundAccount);
    }

    @Test
    public void updateEmailAndActivateById() {
        // create new
        Account account = accountRepo.save(newAccount);
        assertEquals(1, accountRepo.count());
        assertFalse(account.isConfirmedAndActive());

        String toUpdateEmail = "update@staffjoy.xyz";
        int result = accountRepo.updateEmailAndActivateById(toUpdateEmail, newAccount.getId());
        assertEquals(1, result);

        Account updatedAccount = accountRepo.findAccountByEmail(toUpdateEmail);
        assertEquals(toUpdateEmail, updatedAccount.getEmail());
        assertTrue(updatedAccount.isConfirmedAndActive());
    }

    @Test
    public void updatePasswordById() {
        // create new
        accountRepo.save(newAccount);
        assertEquals(1, accountRepo.count());

        String passwordHash = "testhash";
        int result = accountSecretRepo.updatePasswordHashById(passwordHash, newAccount.getId());
        assertEquals(1, result);

        AccountSecret foundAccountSecret = accountSecretRepo.findAccountSecretByEmail(newAccount.getEmail());
        assertNotNull(foundAccountSecret);
        assertEquals(newAccount.getId(), foundAccountSecret.getId());
        assertEquals(newAccount.isConfirmedAndActive(), foundAccountSecret.isConfirmedAndActive());
        assertEquals(passwordHash, foundAccountSecret.getPasswordHash() );

    }

    @After
    public void destroy() {
        accountRepo.deleteAll();
    }

}
