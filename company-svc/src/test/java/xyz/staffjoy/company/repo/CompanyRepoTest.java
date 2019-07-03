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
import xyz.staffjoy.company.model.Company;

import java.util.TimeZone;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringRunner.class)
@Slf4j
public class CompanyRepoTest {

    @Autowired
    private CompanyRepo companyRepo;

    private Company newCompany;

    @Before
    public void setUp() {
        newCompany = Company.builder()
                .name("testCompany")
                .archived(false)
                .defaultTimezone(TimeZone.getDefault().getID())
                .defaultDayWeekStarts("Monday")
                .build();
        companyRepo.deleteAll();
    }

    @Test
    public void testCreateCompany() {
        // create new
        Company savedCompany = companyRepo.save(newCompany);
        log.info(newCompany.toString());
        log.info(savedCompany.toString());
        assertNotNull(savedCompany);
        // check exists
        assertTrue(companyRepo.existsById(newCompany.getId()));
    }

    @Test
    public void testGetCompanyById() {
        // create new
        Company savedCompany = companyRepo.save(newCompany);
        assertNotNull(savedCompany);

        // find exists
        Company gotCompany = companyRepo.findCompanyById(newCompany.getId());
        assertEquals(savedCompany, gotCompany);
    }

    @Test
    public void testListCompany() {
        Pageable pageRequest = PageRequest.of(0, 2);
        // test empty
        Page<Company> companies = companyRepo.findAll(pageRequest);
        assertEquals(0, companies.getTotalElements());

        // create 1 new
        companyRepo.save(newCompany);
        assertEquals(1, companyRepo.count());

        // create 2 more
        newCompany.setId(null);
        companyRepo.save(newCompany);
        assertEquals(2, companyRepo.count());
        newCompany.setId(null);
        companyRepo.save(newCompany);
        assertEquals(3, companyRepo.count());

        companies = companyRepo.findAll(pageRequest);
        assertEquals(2, companies.getNumberOfElements());
        pageRequest = pageRequest.next();
        companies = companyRepo.findAll(pageRequest);
        assertEquals(1, companies.getNumberOfElements());
        assertEquals(2, companies.getTotalPages());
        assertEquals(3, companies.getTotalElements());
    }

    @Test
    public void testUpdateCompany() {
        // create new
        Company savedCompany = companyRepo.save(newCompany);
        assertNotNull(savedCompany);

        // update
        newCompany.setName("update");
        newCompany.setArchived(true);
        Company updatedCompany = companyRepo.save(newCompany);
        assertEquals(newCompany, updatedCompany);

        Company gotCompany = companyRepo.findCompanyById(newCompany.getId());
        assertEquals(newCompany, gotCompany);

        // update again
        newCompany.setDefaultTimezone("America/Cordoba");
        newCompany.setDefaultDayWeekStarts("Tuesday");
        updatedCompany = companyRepo.save(newCompany);
        assertEquals(newCompany, updatedCompany);

        gotCompany = companyRepo.findCompanyById(newCompany.getId());
        assertEquals(newCompany, gotCompany);
    }

    @After
    public void destroy() {
        companyRepo.deleteAll();
    }
}
