package xyz.staffjoy.company.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.staffjoy.common.api.ResultCode;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.auth.AuthContext;
import xyz.staffjoy.common.auth.Authorize;
import xyz.staffjoy.company.dto.*;
import xyz.staffjoy.company.service.PermissionService;
import xyz.staffjoy.company.service.TeamService;

@RestController
@RequestMapping("/v1/company/team")
@Validated
public class TeamController {
    @Autowired
    TeamService teamService;

    @Autowired
    PermissionService permissionService;

    @PostMapping(path = "/create")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_WWW_SERVICE
    })
    public GenericTeamResponse createTeam(@RequestBody @Validated CreateTeamRequest request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionCompanyAdmin(request.getCompanyId());
        }

        TeamDto teamDto = this.teamService.createTeam(request);

        return new GenericTeamResponse(teamDto);
    }

    @GetMapping(path = "/list")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public ListTeamResponse listTeams(@RequestParam String companyId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionCompanyAdmin(companyId);
        }

        TeamList teamList = this.teamService.listTeams(companyId);

        return new ListTeamResponse(teamList);
    }

    @GetMapping(path = "/get")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE,
            AuthConstant.AUTHORIZATION_BOT_SERVICE,
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_ICAL_SERVICE,
            AuthConstant.AUTHORIZATION_WHOAMI_SERVICE
    })
    public GenericTeamResponse getTeam(@RequestParam String companyId, @RequestParam String teamId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionTeamWorker(companyId, teamId);
        }

        TeamDto teamDto = this.teamService.getTeamWithCompanyIdValidation(companyId, teamId);

        return new GenericTeamResponse(teamDto);
    }

    @PutMapping(path = "/update")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericTeamResponse updateTeam(@RequestBody @Validated TeamDto teamDto) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionCompanyAdmin(teamDto.getCompanyId());
        }

        TeamDto updatedTeamDto = this.teamService.updateTeam(teamDto);

        return new GenericTeamResponse(updatedTeamDto);
    }

    @GetMapping(path = "/get_worker_team_info")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_ICAL_SERVICE
    })
    public GenericWorkerResponse getWorkerTeamInfo(@RequestParam(required = false) String companyId, @RequestParam String userId) {
        GenericWorkerResponse response = new GenericWorkerResponse();
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            if (!userId.equals(AuthContext.getUserId())) { // user can access their own entry
                if (StringUtils.isEmpty(companyId)) {
                    response.setCode(ResultCode.PARAM_MISS);
                    response.setMessage("missing companyId");
                    return response;
                }
                permissionService.checkPermissionCompanyAdmin(companyId);
            }
        }
        WorkerDto workerDto = this.teamService.getWorkerTeamInfo(userId);
        response.setWorker(workerDto);
        return response;
    }

}
