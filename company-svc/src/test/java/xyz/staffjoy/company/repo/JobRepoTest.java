package xyz.staffjoy.company.repo;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.staffjoy.company.model.Job;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringRunner.class)
@Slf4j
public class JobRepoTest {
    @Autowired
    JobRepo jobRepo;

    @Before
    public void setUp() {
        jobRepo.deleteAll();
    }

    @Test
    public void testJob() {
        Job job1 = Job.builder()
                .archived(false)
                .color("#48B7AB")
                .teamId("T100001")
                .name("test_job1")
                .build();
        Job savedJob1 = jobRepo.save(job1);
        assertThat(savedJob1).isEqualTo(job1);

        Job job2 = Job.builder()
                .archived(false)
                .color("#48B7AB")
                .teamId("T100001")
                .name("test_job2")
                .build();
        Job savedJob2 = jobRepo.save(job2);
        assertThat(savedJob2).isEqualTo(job2);

        List<Job> jobs = jobRepo.findJobByTeamId("T100001");
        assertThat(jobs).containsExactly(job1, job2);

        Job foundJob = jobRepo.findJobById(job2.getId());
        assertThat(foundJob).isEqualTo(job2);

        job2.setArchived(true);
        job2.setName("test_jobx");
        job2.setColor("#48B7CC");
        job2.setTeamId("T10000X");
        Job updatedJob2 = jobRepo.save(job2);
        foundJob = jobRepo.findJobById(job2.getId());
        assertThat(foundJob).isEqualTo(updatedJob2);
    }

    @After
    public void destroy() {
        jobRepo.deleteAll();
    }
}
