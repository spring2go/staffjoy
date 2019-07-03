package xyz.staffjoy.company.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xyz.staffjoy.company.model.Shift;

import java.time.Instant;
import java.util.List;

@Repository
public interface ShiftRepo extends JpaRepository<Shift, String> {
    Shift findShiftById(String shiftId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Shift shift where shift.id = :shiftId")
    @Transactional
    int deleteShiftById(@Param("shiftId") String shiftId);

    @Query(
            value = "select cast(a.weekname as char) as week, greatest(a.count, coalesce(b.count,0)) as count from (select 0 as count, str_to_date(concat(year(start), week(start), ' Monday'), '%X%V %W') as weekname from shift where start < NOW() group by weekname) as a left join (select count(distinct(user_id)) as count, str_to_date(concat(year(start), week(start), ' Monday'), '%X%V %W') as weekname from shift where start < NOW() and user_id != '' and published is true group by weekname) as b on a.weekname = b.weekname",
            nativeQuery = true
    )
    List<IScheduledPerWeek> getScheduledPerWeekList();

    interface IScheduledPerWeek {

        String getWeek();

        int getCount();

    }

    @Query(
            value = "select count(distinct(user_id)) from shift where shift.start <= NOW() and shift.stop > NOW() and user_id <> '' and shift.published = true",
            nativeQuery = true
    )
    int getPeopleOnShifts();

    // order by start asc
    @Query(
            value = "select shift from Shift shift where shift.teamId = :teamId and shift.userId = :userId and shift.start >= :startTime and shift.start < :endTime order by shift.start asc"
    )
    List<Shift> listWorkerShifts(@Param("teamId") String teamId, @Param("userId") String userId, @Param("startTime") Instant start, @Param("endTime") Instant end);
    // no order
    @Query(
            value = "select shift from Shift shift where shift.teamId = :teamId and shift.userId = :userId and shift.start >= :startTime and shift.start < :endTime"
    )
    List<Shift> listShiftByUserId(@Param("teamId") String teamId, @Param("userId") String userId, @Param("startTime") Instant start, @Param("endTime") Instant end);

    @Query(
            value = "select shift from Shift shift where shift.teamId = :teamId and shift.jobId = :jobId and shift.start >= :startTime and shift.start < :endTime"
    )
    List<Shift> listShiftByJobId(@Param("teamId") String teamId, @Param("jobId") String jobId, @Param("startTime") Instant start, @Param("endTime") Instant end);

    @Query(
            value = "select shift from Shift shift where shift.teamId = :teamId and shift.userId = :userId and shift.jobId = :jobId and shift.start >= :startTime and shift.start < :endTime"
    )
    List<Shift> listShiftByUserIdAndJobId(@Param("teamId") String teamId, @Param("userId") String userId, @Param("jobId") String jobId, @Param("startTime") Instant start, @Param("endTime") Instant end);

    @Query(
            value = "select shift from Shift shift where shift.teamId = :teamId and shift.start >= :startTime and shift.start < :endTime"
    )
    List<Shift> listShiftByTeamIdOnly(@Param("teamId") String teamId, @Param("startTime") Instant start, @Param("endTime") Instant end);
}
