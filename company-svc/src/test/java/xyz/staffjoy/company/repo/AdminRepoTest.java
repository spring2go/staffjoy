package xyz.staffjoy.company.repo;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.staffjoy.company.model.Admin;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringRunner.class)
@Slf4j
public class AdminRepoTest {
    @Autowired
    AdminRepo adminRepo;

    @Before
    public void setUp() {
        adminRepo.deleteAll();
    }

    @Test
    public void testAdmin() {
        Admin admin1 = Admin.builder()
                .companyId("C100001")
                .userId("U100001")
                .build();
        Admin savedAdmin = adminRepo.save(admin1);
        assertThat(savedAdmin).isEqualTo(admin1);

        Admin admin2 = Admin.builder()
                .companyId("C100001")
                .userId("U100002")
                .build();
        adminRepo.save(admin2);

        Admin admin3 = Admin.builder()
                .companyId("C100001")
                .userId("U100003")
                .build();
        adminRepo.save(admin3);

        assertThat(adminRepo.findByCompanyIdAndUserId("C100001", "U100005")).isNull();
        assertThat(adminRepo.findByCompanyIdAndUserId("C100001", "U100002")).isNotNull();

        List<Admin> foundAdmins = adminRepo.findByCompanyId("C100001");
        assertThat(foundAdmins.size()).isEqualTo(3);
        assertThat(foundAdmins).containsExactly(admin1, admin2, admin3);

        foundAdmins = adminRepo.findByUserId("U100003");
        assertThat(foundAdmins.size()).isEqualTo(1);
        assertThat(foundAdmins).containsExactly(admin3);

        int count = adminRepo.deleteAdmin("C100002", "U100003");
        assertThat(count).isEqualTo(0);
        count = adminRepo.deleteAdmin("C100001", "U100003");
        assertThat(count).isEqualTo(1);

        foundAdmins = adminRepo.findByUserId("U100004");
        assertThat(foundAdmins.size()).isEqualTo(0);
    }

    @After
    public void destroy() {
        adminRepo.deleteAll();
    }
}
