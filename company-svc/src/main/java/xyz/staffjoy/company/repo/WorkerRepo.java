package xyz.staffjoy.company.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xyz.staffjoy.company.model.Worker;

import java.util.List;

@Repository
public interface WorkerRepo extends JpaRepository<Worker, String> {
    List<Worker> findByTeamId(String teamId);
    List<Worker> findByUserId(String userId);
    Worker findByTeamIdAndUserId(String teamId, String userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Worker worker where worker.teamId = :teamId and worker.userId = :userId")
    @Transactional
    int deleteWorker(@Param("teamId") String teamId, @Param("userId") String userId);
}
