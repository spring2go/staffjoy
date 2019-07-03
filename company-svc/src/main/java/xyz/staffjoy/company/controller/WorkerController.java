package xyz.staffjoy.company.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.auth.AuthContext;
import xyz.staffjoy.common.auth.Authorize;
import xyz.staffjoy.company.dto.*;
import xyz.staffjoy.company.service.PermissionService;
import xyz.staffjoy.company.service.WorkerService;

@RestController
@RequestMapping("/v1/company/worker")
@Validated
public class WorkerController {
    @Autowired
    WorkerService workerService;

    @Autowired
    PermissionService permissionService;

    @GetMapping(path = "/list")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public ListWorkerResponse listWorkers(@RequestParam String companyId, @RequestParam String teamId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionTeamWorker(companyId, teamId);
        }
        WorkerEntries workerEntries = workerService.listWorkers(companyId, teamId);
        return new ListWorkerResponse(workerEntries);
    }

    @GetMapping(path = "/get")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_WWW_SERVICE
    })
    public GenericDirectoryResponse getWorker(@RequestParam  String companyId, @RequestParam String teamId, @RequestParam String userId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionTeamWorker(companyId, teamId);
        }
        DirectoryEntryDto directoryEntryDto = workerService.getWorker(companyId, teamId, userId);
        return new GenericDirectoryResponse(directoryEntryDto);
    }

    @DeleteMapping(path = "/delete")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public BaseResponse deleteWorker(@RequestBody @Validated WorkerDto workerDto) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionCompanyAdmin(workerDto.getCompanyId());
        }
        workerService.deleteWorker(workerDto.getCompanyId(), workerDto.getTeamId(), workerDto.getUserId());
        return BaseResponse.builder().message("worker has been deleted").build();
    }

    @GetMapping(path = "/get_worker_of")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE,
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            // This is an internal endpoint
            AuthConstant.AUTHORIZATION_WHOAMI_SERVICE
    })
    public GetWorkerOfResponse getWorkerOf(@RequestParam String userId) {
        WorkerOfList workerOfList = workerService.getWorkerOf(userId);
        return new GetWorkerOfResponse(workerOfList);
    }

    @PostMapping(path = "/create")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_WHOAMI_SERVICE
    })
    public GenericDirectoryResponse createWorker(@RequestBody @Validated WorkerDto workerDto) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionCompanyAdmin(workerDto.getCompanyId());
        }
        DirectoryEntryDto directoryEntryDto = workerService.createWorker(workerDto);
        return new GenericDirectoryResponse(directoryEntryDto);
    }
}
