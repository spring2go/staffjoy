package xyz.staffjoy.company.repo;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.staffjoy.company.model.Team;

import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringRunner.class)
@Slf4j
public class TeamRepoTest {

    @Autowired
    TeamRepo teamRepo;

    @Before
    public void setUp() {
        teamRepo.deleteAll();
    }

    @Test
    public void testTeam() {
        Team team1 = Team.builder().name("test_team1")
                .companyId("C100001")
                .dayWeekStarts("Monday")
                .timezone(TimeZone.getDefault().getID())
                .color("48B7AB")
                .build();
        Team savedTeam1 = teamRepo.save(team1);

        assertThat(savedTeam1).isEqualTo(team1);

        Team team2 = Team.builder().name("test_team2")
                .companyId("C100001")
                .dayWeekStarts("Monday")
                .timezone(TimeZone.getDefault().getID())
                .color("48B7AB")
                .build();
        Team savedTeam2 = teamRepo.save(team2);

        assertThat(savedTeam2).isEqualTo(team2);

        List<Team> teams = teamRepo.findByCompanyId("C100001");
        assertThat(teams.size()).isEqualTo(2);
        assertThat(teams).containsExactly(team1, team2);

        Team team3 = Team.builder().id(team1.getId()).name("test_team3")
                .companyId("C100001")
                .dayWeekStarts("Monday")
                .timezone(TimeZone.getDefault().getID())
                .color("48B7CD")
                .build();
        Team updatedTeam = teamRepo.save(team3);
        assertThat(updatedTeam).isEqualTo(team3);

        Team team4 = teamRepo.findById(team1.getId()).get();
        assertThat(team4).isEqualTo(team3);

        Team team5 = teamRepo.findById("null_id").orElse(null);
        assertThat(team5).isNull();
    }

    @After
    public void destroy() {
        teamRepo.deleteAll();
    }

}
