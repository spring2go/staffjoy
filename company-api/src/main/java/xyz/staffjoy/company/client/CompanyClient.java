package xyz.staffjoy.company.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.validation.Group1;
import xyz.staffjoy.common.validation.Group2;
import xyz.staffjoy.company.CompanyConstant;
import xyz.staffjoy.company.dto.*;

@FeignClient(name = CompanyConstant.SERVICE_NAME, path = "/v1/company", url = "${staffjoy.company-service-endpoint}")
public interface CompanyClient {
    // Company Apis
    @PostMapping(path = "/create")
    GenericCompanyResponse createCompany(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated({Group2.class}) CompanyDto companyDto);

    @GetMapping(path = "/list")
    ListCompanyResponse listCompanies(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam int offset, @RequestParam int limit);

    @GetMapping(path= "/get")
    GenericCompanyResponse getCompany(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam("company_id") String companyId);

    @PutMapping(path= "/update")
    GenericCompanyResponse updateCompany(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated({Group1.class}) CompanyDto companyDto);

    // Admin Apis
    @GetMapping(path = "/admin/list")
    ListAdminResponse listAdmins(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId);

    @GetMapping(path = "/admin/get")
    GenericDirectoryResponse getAdmin(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId, @RequestParam String userId);

    @PostMapping(path = "/admin/create")
    GenericDirectoryResponse createAdmin(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated DirectoryEntryRequest request);

    @DeleteMapping(path = "/admin/delete")
    BaseResponse deleteAdmin(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated DirectoryEntryRequest request);

    @GetMapping(path = "/admin/admin_of")
    GetAdminOfResponse getAdminOf(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String userId);

    // Directory Apis
    @PostMapping(path = "/directory/create")
    GenericDirectoryResponse createDirectory(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated NewDirectoryEntry request);

    @GetMapping(path = "/directory/list")
    ListDirectoryResponse listDirectories(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId, @RequestParam int offset, @RequestParam int limit);

    @GetMapping(path = "/directory/get")
    GenericDirectoryResponse getDirectoryEntry(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId, @RequestParam String userId);

    @PutMapping(path = "/directory/update")
    GenericDirectoryResponse updateDirectoryEntry(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated DirectoryEntryDto request);

    @GetMapping(path = "/directory/get_associations")
    GetAssociationResponse getAssociations(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId, @RequestParam int offset, @RequestParam int limit);

    // WorkerDto Apis
    @GetMapping(path = "/worker/list")
    ListWorkerResponse listWorkers(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId, @RequestParam String teamId);

    @GetMapping(path = "/worker/get")
    GenericDirectoryResponse getWorker(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam  String companyId, @RequestParam String teamId, @RequestParam String userId);

    @DeleteMapping(path = "/worker/delete")
    BaseResponse deleteWorker(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated WorkerDto workerDto);

    @GetMapping(path = "/worker/get_worker_of")
    GetWorkerOfResponse getWorkerOf(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String userId);

    @PostMapping(path = "/worker/create")
    GenericDirectoryResponse createWorker(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated WorkerDto workerDto);

    // Team Apis
    @PostMapping(path = "/team/create")
    GenericTeamResponse createTeam(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated CreateTeamRequest request);

    @GetMapping(path = "/team/list")
    ListTeamResponse listTeams(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId);

    @GetMapping(path = "/team/get")
    GenericTeamResponse getTeam(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId, @RequestParam String teamId);

    @PutMapping(path = "/team/update")
    GenericTeamResponse updateTeam(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated TeamDto teamDto);

    @GetMapping(path = "/team/get_worker_team_info")
    GenericWorkerResponse getWorkerTeamInfo(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam(required = false) String companyId, @RequestParam String userId);

    // Job Apis
    @PostMapping(path = "/job/create")
    GenericJobResponse createJob(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated CreateJobRequest request);

    @GetMapping(path = "/job/list")
    ListJobResponse listJobs(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId, @RequestParam String teamId);

    @GetMapping(path = "/job/get")
    GenericJobResponse getJob(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String jobId, @RequestParam String companyId, @RequestParam String teamId);

    @PutMapping(path = "/job/update")
    GenericJobResponse updateJob(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated JobDto jobDto);

    // Shift Apis
    @PostMapping(path = "/shift/create")
    GenericShiftResponse createShift(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated CreateShiftRequest request);

    @PostMapping(path = "/shift/list_worker_shifts")
    GenericShiftListResponse listWorkerShifts(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated WorkerShiftListRequest request);

    @PostMapping(path = "/shift/list_shifts")
    GenericShiftListResponse listShifts(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated ShiftListRequest request);

    @PostMapping(path = "/shift/bulk_publish")
    GenericShiftListResponse bulkPublishShifts(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated BulkPublishShiftsRequest request);

    @GetMapping(path = "/shift/get")
    GenericShiftResponse getShift(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String shiftId, @RequestParam String teamId, @RequestParam  String companyId);

    @PutMapping(path = "/shift/update")
    GenericShiftResponse updateShift(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated ShiftDto shiftDto);

    @DeleteMapping(path = "/shift/delete")
    BaseResponse deleteShift(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String shiftId, @RequestParam String teamId, @RequestParam String companyId);
}
