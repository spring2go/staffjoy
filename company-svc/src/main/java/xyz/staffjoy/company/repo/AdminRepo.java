package xyz.staffjoy.company.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xyz.staffjoy.company.model.Admin;

import java.util.List;

@Repository
public interface AdminRepo extends JpaRepository<Admin, String> {
    List<Admin> findByCompanyId(String companyId);
    List<Admin> findByUserId(String userId);

    Admin findByCompanyIdAndUserId(String companyId, String userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Admin admin where admin.companyId = :companyId and admin.userId = :userId")
    @Transactional
    int deleteAdmin(@Param("companyId") String companyId, @Param("userId") String userId);
}
