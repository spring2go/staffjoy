package xyz.staffjoy.company.repo;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.staffjoy.company.model.Directory;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringRunner.class)
@Slf4j
public class DirectoryRepoTest {
    @Autowired
    DirectoryRepo directoryRepo;

    @Before
    public void setUp() {
        directoryRepo.deleteAll();
    }

    @Test
    public void testDirectory() {
        Directory directory1 = Directory.builder()
                .userId("U100001")
                .companyId("C100001")
                .internalId("I100001")
                .build();
        Directory savedDirectory1 = directoryRepo.save(directory1);
        assertThat(savedDirectory1).isEqualTo(directory1);

        Directory directory2 = Directory.builder()
                .userId("U100002")
                .companyId("C100001")
                .internalId("I100002")
                .build();
        Directory savedDirectory2 = directoryRepo.save(directory2);
        assertThat(savedDirectory2).isEqualTo(directory2);

        // test findByCompanyIdAndUserId
        Directory foundDirectory = directoryRepo.findByCompanyIdAndUserId("C100002", "U100001");
        assertThat(foundDirectory).isNull();
        foundDirectory = directoryRepo.findByCompanyIdAndUserId("C100001", "U100001");
        assertThat(foundDirectory).isNotNull();
        assertThat(foundDirectory).isEqualTo(directory1);


        // test pagination
        Pageable pageRequest = PageRequest.of(1, 1);
        Page<Directory> directoryPage = directoryRepo.findByCompanyId("C100001", pageRequest);
        assertThat(directoryPage.getTotalPages()).isEqualTo(2);
        assertThat(directoryPage.getTotalElements()).isEqualTo(2);

        pageRequest = PageRequest.of(0, 2);
        directoryPage = directoryRepo.findByCompanyId("C100001", pageRequest);
        assertThat(directoryPage.getTotalPages()).isEqualTo(1);
        assertThat(directoryPage.getTotalElements()).isEqualTo(2);

        // test update
        directory1.setInternalId("I10000X");
        int result = directoryRepo.updateInternalIdByCompanyIdAndUserId(directory1.getInternalId(), "C100001", "U100001");
        assertTrue(result == 1);
        foundDirectory = directoryRepo.findByCompanyIdAndUserId("C100001", "U100001");
        assertThat(foundDirectory).isNotNull();
        assertThat(foundDirectory).isEqualTo(directory1);

        result = directoryRepo.updateInternalIdByCompanyIdAndUserId(directory1.getInternalId(), "C100003", "U100001");
        assertTrue(result == 0);
    }

    @After
    public void destroy() {
        directoryRepo.deleteAll();
    }

}
