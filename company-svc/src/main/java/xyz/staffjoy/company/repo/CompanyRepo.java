package xyz.staffjoy.company.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.staffjoy.company.model.Company;

@Repository
public interface CompanyRepo extends JpaRepository<Company, String> {
    Company findCompanyById(String id);
}
