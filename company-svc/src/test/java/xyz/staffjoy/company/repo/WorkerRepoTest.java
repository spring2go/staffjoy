package xyz.staffjoy.company.repo;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.staffjoy.company.model.Worker;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringRunner.class)
@Slf4j
public class WorkerRepoTest {

    @Autowired
    WorkerRepo workerRepo;

    @Before
    public void setUp() {
        workerRepo.deleteAll();
    }

    @Test
    public void testWorker() {
        Worker worker1 = Worker.builder()
                .teamId("T100001")
                .userId("U100001")
                .build();
        Worker savedWorker1 = workerRepo.save(worker1);
        assertThat(savedWorker1).isEqualTo(worker1);

        Worker worker2 = Worker.builder()
                .teamId("T100001")
                .userId("U100002")
                .build();
        Worker savedWorker2 = workerRepo.save(worker2);
        assertThat(savedWorker2).isEqualTo(worker2);

        List<Worker> workers = workerRepo.findByTeamId("T100001");
        assertThat(workers).containsExactly(worker1, worker2);

        Worker worker3 = workerRepo.findByTeamIdAndUserId("T100001", "U100001");
        assertThat(worker3).isEqualTo(worker1);
        Worker worker4 = workerRepo.findByTeamIdAndUserId("T100003", "U100001");
        assertThat(worker4).isNull();

        workers = workerRepo.findByUserId("U100001");
        assertThat(workers).containsExactly(worker1);

        workerRepo.deleteWorker("T100001", "U100001");
        Worker worker5 = workerRepo.findByTeamIdAndUserId("T100001", "U100001");
        assertThat(worker5).isNull();
    }

    @After
    public void destroy() {
        workerRepo.deleteAll();
    }
}
