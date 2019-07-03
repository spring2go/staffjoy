package xyz.staffjoy.company.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.staffjoy.company.model.Team;

import java.util.List;

@Repository
public interface TeamRepo extends JpaRepository<Team, String> {
    List<Team> findByCompanyId(String companyId);
}
