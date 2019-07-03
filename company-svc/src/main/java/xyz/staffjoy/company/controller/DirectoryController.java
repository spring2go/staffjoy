package xyz.staffjoy.company.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.auth.AuthContext;
import xyz.staffjoy.common.auth.Authorize;
import xyz.staffjoy.company.dto.*;
import xyz.staffjoy.company.service.DirectoryService;
import xyz.staffjoy.company.service.PermissionService;

@RestController
@RequestMapping("/v1/company/directory")
@Validated
public class DirectoryController {
    @Autowired
    DirectoryService directoryService;

    @Autowired
    PermissionService permissionService;

    @PostMapping(path = "/create")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_WWW_SERVICE
    })
    public GenericDirectoryResponse createDirectory(@RequestBody @Validated NewDirectoryEntry request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionCompanyAdmin(request.getCompanyId());
        }
        DirectoryEntryDto directoryEntryDto = directoryService.createDirectory(request);
        return new GenericDirectoryResponse(directoryEntryDto);
    }

    @GetMapping(path = "/list")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public ListDirectoryResponse listDirectories(@RequestParam String companyId,
                                                 @RequestParam(defaultValue = "0") int offset,
                                                 @RequestParam(defaultValue = "0") int limit) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz()))  {
            permissionService.checkPermissionCompanyAdmin(companyId);
        }
        DirectoryList directoryList = directoryService.listDirectory(companyId, offset, limit);
        return new ListDirectoryResponse(directoryList);
    }

    @GetMapping(path = "/get")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_WHOAMI_SERVICE,
            AuthConstant.AUTHORIZATION_WWW_SERVICE
    })
    public GenericDirectoryResponse getDirectoryEntry(@RequestParam String companyId, @RequestParam String userId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            // user can access their own entry
            if (!userId.equals(AuthContext.getUserId())) {
                permissionService.checkPermissionCompanyAdmin(companyId);
            }
        }
        DirectoryEntryDto directoryEntryDto = directoryService.getDirectoryEntry(companyId, userId);
        return new GenericDirectoryResponse(directoryEntryDto);
    }

    @PutMapping(path = "/update")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericDirectoryResponse updateDirectoryEntry(@RequestBody @Validated DirectoryEntryDto request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionCompanyAdmin(request.getCompanyId());
        }
        DirectoryEntryDto directoryEntryDto = directoryService.updateDirectoryEntry(request);
        return new GenericDirectoryResponse(directoryEntryDto);
    }

    @GetMapping(path = "/get_associations")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GetAssociationResponse getAssociations(@RequestParam String companyId,
                                                  @RequestParam(defaultValue = "0") int offset,
                                                  @RequestParam(defaultValue = "0") int limit) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionCompanyAdmin(companyId);
        }
        AssociationList associationList = directoryService.getAssociations(companyId, offset, limit);
        return new GetAssociationResponse(associationList);
    }
}
