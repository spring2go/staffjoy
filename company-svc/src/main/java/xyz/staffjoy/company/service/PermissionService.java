package xyz.staffjoy.company.service;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.staffjoy.common.auth.AuthContext;
import xyz.staffjoy.common.auth.PermissionDeniedException;
import xyz.staffjoy.company.model.Admin;
import xyz.staffjoy.company.model.Directory;
import xyz.staffjoy.company.model.Worker;
import xyz.staffjoy.company.repo.AdminRepo;
import xyz.staffjoy.company.repo.DirectoryRepo;
import xyz.staffjoy.company.repo.WorkerRepo;
import xyz.staffjoy.company.service.helper.ServiceHelper;


/**
 * Each permission has a public convenience checker, and a private relationship checker.
 * Recall that support users have a different authorization, and will not use these functions.
 *
 * PermissionCompanyAdmin checks that the current user is an admin of the given company
 */
@Service
public class PermissionService {

    static final ILogger logger = SLoggerFactory.getLogger(PermissionService.class);

    @Autowired
    private SentryClient sentryClient;

    @Autowired
    AdminRepo adminRepo;

    @Autowired
    WorkerRepo workerRepo;

    @Autowired
    DirectoryRepo directoryRepo;

    @Autowired
    ServiceHelper serviceHelper;

    public void checkPermissionCompanyAdmin(String companyId) {
        String currentUserId = checkAndGetCurrentUserId();

        Admin admin = null;
        try {
            admin = adminRepo.findByCompanyIdAndUserId(companyId, currentUserId);
        } catch (Exception ex) {
            String errMsg = "failed to check company admin permissions";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        if (admin == null) {
            throw new PermissionDeniedException("you do not have admin access to this service");
        }
    }

    // PermissionTeamWorker checks whether a user is a worker of a given team in a given company, or is an admin of that company
    public void checkPermissionTeamWorker(String companyId, String teamId) {
        String currentUserId = checkAndGetCurrentUserId();

        // Check if company admin
        try {
            Admin admin = adminRepo.findByCompanyIdAndUserId(companyId, currentUserId);
            if (admin != null) { // Admin - allow access
                return;
            }
        } catch (Exception ex) {
            String errMsg = "failed to check company admin permissions";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }

        Worker worker = null;
        try {
            worker = workerRepo.findByTeamIdAndUserId(teamId, currentUserId);
        } catch (Exception ex) {
            String errMsg = "failed to check teamDto member permissions";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        if (worker == null) {
            throw new PermissionDeniedException("you are not associated with this company");
        }
    }

    // PermissionCompanyDirectory checks whether a user exists in the directory of a company. It is the lowest level of security.
    // The user may no longer be associated with a team (i.e. may be a former employee)
    public void checkPermissionCompanyDirectory(String companyId) {
        String currentUserId = checkAndGetCurrentUserId();

        Directory directory = null;
        try {
            directory = directoryRepo.findByCompanyIdAndUserId(companyId, currentUserId);
        } catch (Exception ex) {
            String errMsg = "failed to check directory existence";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        if (directory == null) {
            throw new PermissionDeniedException("you are not associated with this company");
        }
    }

    private String checkAndGetCurrentUserId() {
        String currentUserId = AuthContext.getUserId();
        if (StringUtils.isEmpty(currentUserId)) {
            String errMsg = "failed to find current user id";
            serviceHelper.handleErrorAndThrowException(logger, errMsg);
        }
        return currentUserId;
    }
}
