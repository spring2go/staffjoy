package xyz.staffjoy.company.repo;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.staffjoy.company.model.Shift;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringRunner.class)
@Slf4j
public class ShiftRepoTest {
    @Autowired
    ShiftRepo shiftRepo;

    @Before
    public void setUp() {
        shiftRepo.deleteAll();
    }


    @Test
    public void testShift() {
        Instant now = Instant.now();
        Shift shift1 = Shift.builder()
                .teamId("T100001").userId("U100001").jobId("J100001")
                .published(true)
                .start(now)
                .stop(now.plus(3, ChronoUnit.DAYS))
                .build();
        Shift savedShift1 = shiftRepo.save(shift1);
        assertThat(savedShift1).isEqualTo(shift1);
        Shift shift2 = Shift.builder()
                .teamId("T100001").userId("U100001").jobId("J100001")
                .published(true)
                .start(now.plus(2, ChronoUnit.DAYS))
                .stop(now.plus(4, ChronoUnit.DAYS))
                .build();
        Shift savedShift2 = shiftRepo.save(shift2);
        assertThat(savedShift2).isEqualTo(shift2);

        List<Shift> shifts = shiftRepo.listWorkerShifts(
                "T100001",
                "U100001",
                now.minus(1, ChronoUnit.DAYS),
                now.plus(3, ChronoUnit.DAYS)
                );
        assertThat(shifts).containsExactly(shift1, shift2);

        shifts = shiftRepo.listWorkerShifts(
                "T100001",
                "U100001",
                now.minus(1, ChronoUnit.DAYS),
                now.plus(1, ChronoUnit.DAYS));
        assertThat(shifts).containsExactly(shift1);

        shifts = shiftRepo.listWorkerShifts(
                "T100001",
                "U100001",
                now.plus(1, ChronoUnit.DAYS),
                now.plus(3, ChronoUnit.DAYS));
        assertThat(shifts).containsExactly(shift2);

        shifts = shiftRepo.listShiftByUserId("T100001",
                "U100002",
                now.minus(1, ChronoUnit.DAYS),
                now.plus(3, ChronoUnit.DAYS));
        assertThat(shifts).isEmpty();
        shifts = shiftRepo.listShiftByUserId("T100001",
                "U100001",
                now.minus(1, ChronoUnit.DAYS),
                now.plus(3, ChronoUnit.DAYS));
        assertThat(shifts).containsExactly(shift1, shift2);

        shifts = shiftRepo.listShiftByJobId("T100001",
                "J100002",
                now.minus(1, ChronoUnit.DAYS),
                now.plus(3, ChronoUnit.DAYS));
        assertThat(shifts).isEmpty();
        shifts = shiftRepo.listShiftByJobId("T100001",
                "J100001",
                now.minus(1, ChronoUnit.DAYS),
                now.plus(3, ChronoUnit.DAYS));
        assertThat(shifts).containsExactly(shift1, shift2);

        shifts = shiftRepo.listShiftByUserIdAndJobId(
                "T100001",
                "U100001",
                "J100001",
                now.minus(1, ChronoUnit.DAYS),
                now.plus(3, ChronoUnit.DAYS));
        assertThat(shifts).containsExactly(shift1, shift2);

        shifts = shiftRepo.listShiftByTeamIdOnly("T100001",
                now.minus(1, ChronoUnit.DAYS),
                now.plus(3, ChronoUnit.DAYS)
                );
        assertThat(shifts).containsExactly(shift1, shift2);

        Shift found = shiftRepo.findShiftById(shift2.getId());
        assertThat(found).isEqualTo(shift2);

        int count = shiftRepo.getPeopleOnShifts();
        assertThat(count).isEqualTo(1);

        // FIXME mysql specific, incompatible with h2
        /*
        List<ShiftRepo.IScheduledPerWeek> scheduledPerWeek = shiftRepo.getScheduledPerweekList();
        assertThat(scheduledPerWeek.size()).isEqualTo(1);
        assertThat(scheduledPerWeek.get(0).getCount()).isEqualTo(1);
        */

        Shift shiftToUpdate = Shift.builder().id(shift2.getId())
                .teamId("T100002").userId("U100002").jobId("J100002")
                .published(true)
                .start(now.minus(3, ChronoUnit.DAYS))
                .stop(now.plus(5, ChronoUnit.DAYS))
                .build();
        Shift updatedShift = shiftRepo.save(shiftToUpdate);
        assertThat(updatedShift).isEqualTo(shiftToUpdate);

        found = shiftRepo.findShiftById(shiftToUpdate.getId());
        assertThat(shiftToUpdate).isEqualTo(found);

        count = shiftRepo.getPeopleOnShifts();
        assertThat(count).isEqualTo(2);

        int result = shiftRepo.deleteShiftById(shiftToUpdate.getId());
        assertThat(result).isEqualTo(1);
        found = shiftRepo.findShiftById(shiftToUpdate.getId());
        assertThat(found).isNull();

    }

    @After
    public void destroy() {
        shiftRepo.deleteAll();
    }
}
