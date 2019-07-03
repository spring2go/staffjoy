package xyz.staffjoy.company.service;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import xyz.staffjoy.account.client.AccountClient;
import xyz.staffjoy.account.dto.AccountDto;
import xyz.staffjoy.account.dto.GenericAccountResponse;
import xyz.staffjoy.account.dto.GetOrCreateRequest;
import xyz.staffjoy.bot.dto.OnboardWorkerRequest;
import xyz.staffjoy.common.api.ResultCode;
import xyz.staffjoy.common.auditlog.LogEntry;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.auth.AuthContext;
import xyz.staffjoy.common.error.ServiceException;
import xyz.staffjoy.company.dto.*;
import xyz.staffjoy.company.model.Directory;
import xyz.staffjoy.company.repo.CompanyRepo;
import xyz.staffjoy.company.repo.DirectoryRepo;
import xyz.staffjoy.company.service.helper.ServiceHelper;

@Service
public class DirectoryService {

    static final ILogger logger = SLoggerFactory.getLogger(DirectoryService.class);

    @Autowired
    private CompanyRepo companyRepo;

    @Autowired
    private DirectoryRepo directoryRepo;

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private ServiceHelper serviceHelper;

    @Autowired
    private WorkerService workerService;

    @Autowired
    private AdminService adminService;

    public DirectoryEntryDto createDirectory(NewDirectoryEntry req) {
        boolean companyExists = companyRepo.existsById(req.getCompanyId());
        if (!companyExists) {
            throw new ServiceException(ResultCode.NOT_FOUND, "Company with specified id not found");
        }

        GetOrCreateRequest getOrCreateRequest = GetOrCreateRequest.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phoneNumber(req.getPhoneNumber())
                .build();
        GenericAccountResponse genericAccountResponse = null;
        try {
            genericAccountResponse = accountClient.getOrCreateAccount(AuthConstant.AUTHORIZATION_COMPANY_SERVICE, getOrCreateRequest);
        } catch (Exception ex) {
            String errMsg = "could not get or create user";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        if (!genericAccountResponse.isSuccess()) {
            serviceHelper.handleErrorAndThrowException(logger, genericAccountResponse.getMessage());
        }

        AccountDto account = genericAccountResponse.getAccount();

        DirectoryEntryDto directoryEntryDto = DirectoryEntryDto.builder().internalId(req.getInternalId()).companyId(req.getCompanyId()).build();
        copyAccountToDirectory(account, directoryEntryDto);

        boolean directoryExists = directoryRepo.findByCompanyIdAndUserId(req.getCompanyId(), account.getId()) != null;
        if (directoryExists) {
            throw new ServiceException("relationship already exists");
        }

        Directory directory = Directory.builder()
                .companyId(req.getCompanyId())
                .userId(account.getId())
                .internalId(req.getInternalId())
                .build();
        try {
            directoryRepo.save(directory);
        } catch (Exception ex) {
            String errMsg = "could not create entry";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }

        LogEntry auditLog = LogEntry.builder()
                .currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz())
                .targetType("directory")
                .targetId(directoryEntryDto.getUserId())
                .companyId(req.getCompanyId())
                .teamId("")
                .updatedContents(directoryEntryDto.toString())
                .build();

        logger.info("updated directory", auditLog);

        OnboardWorkerRequest onboardWorkerRequest = OnboardWorkerRequest.builder()
                .companyId(req.getCompanyId())
                .userId(directoryEntryDto.getUserId())
                .build();
        serviceHelper.onboardWorkerAsync(onboardWorkerRequest);

        serviceHelper.trackEventAsync("directoryentry_created");

        return directoryEntryDto;
    }

    public DirectoryList listDirectory(String companyId, int offset, int limit) {

        if (limit <= 0) {
            limit = 20;
        }
        DirectoryList directoryList = DirectoryList.builder().limit(limit).offset(offset).build();
        PageRequest pageRequest = PageRequest.of(offset, limit);
        Page<Directory> directoryPage = directoryRepo.findByCompanyId(companyId, pageRequest);

        for(Directory directory : directoryPage.getContent()) {
            DirectoryEntryDto directoryEntryDto = DirectoryEntryDto.builder()
                    .companyId(companyId)
                    .internalId(directory.getInternalId())
                    .userId(directory.getUserId())
                    .build();

            GenericAccountResponse resp = null;
            try {
                resp = accountClient.getAccount(AuthConstant.AUTHORIZATION_COMPANY_SERVICE, directoryEntryDto.getUserId());
            } catch (Exception ex) {
                String errMsg = "could not get account";
                serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
            }

            if (!resp.isSuccess()) {
                serviceHelper.handleErrorAndThrowException(logger, resp.getMessage());
            }

            AccountDto account = resp.getAccount();
            copyAccountToDirectory(account, directoryEntryDto);

            directoryList.getAccounts().add(directoryEntryDto);
        }

        return directoryList;
    }

    public DirectoryEntryDto getDirectoryEntry(String companyId, String userId) {
        DirectoryEntryDto directoryEntryDto = DirectoryEntryDto.builder().userId(userId).companyId(companyId).build();
        Directory directory = directoryRepo.findByCompanyIdAndUserId(companyId, userId);
        if (directory == null) {
            throw new ServiceException(ResultCode.NOT_FOUND, "directory entry not found for user in this company");
        }
        directoryEntryDto.setInternalId(directory.getInternalId());

        GenericAccountResponse resp = null;
        try {
            resp = accountClient.getAccount(AuthConstant.AUTHORIZATION_COMPANY_SERVICE, userId);
        } catch (Exception ex) {
            String errMsg = "view fetching account";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }

        if (!resp.isSuccess()) {
            serviceHelper.handleErrorAndThrowException(logger, resp.getMessage());
        }

        AccountDto account = resp.getAccount();
        copyAccountToDirectory(account, directoryEntryDto);

        return directoryEntryDto;
    }

    public DirectoryEntryDto updateDirectoryEntry(DirectoryEntryDto request) {
        DirectoryEntryDto orig = this.getDirectoryEntry(request.getCompanyId(), request.getUserId());

        GenericAccountResponse genericAccountResponse1 = null;
        try {
            genericAccountResponse1 = accountClient.getAccount(AuthConstant.AUTHORIZATION_COMPANY_SERVICE, orig.getUserId());
        } catch (Exception ex) {
            String errMsg = "getting account failed";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }

        if (!genericAccountResponse1.isSuccess()) {
            serviceHelper.handleErrorAndThrowException(logger, genericAccountResponse1.getMessage());
        }

        AccountDto account = genericAccountResponse1.getAccount();

        boolean accountUpdateRequested =
                !request.getName().equals(orig.getName()) ||
                !request.getEmail().equals(orig.getEmail()) ||
                !request.getPhoneNumber().equals(orig.getPhoneNumber());
        if(account.isConfirmedAndActive() && accountUpdateRequested) {
            throw new ServiceException(ResultCode.PARAM_VALID_ERROR, "this user is active, so they cannot be modified");
        } else if (account.isSupport() && accountUpdateRequested) {
            throw new ServiceException(ResultCode.UN_AUTHORIZED, "you cannot change this account");
        }

        if (accountUpdateRequested) {
            account.setName(request.getName());
            account.setPhoneNumber(request.getPhoneNumber());
            account.setEmail(request.getEmail());
            GenericAccountResponse genericAccountResponse2 = null;
            try {
                genericAccountResponse2 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_COMPANY_SERVICE, account);
            } catch (Exception ex) {
                String errMsg = "view updating account";
                serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
            }

            if (!genericAccountResponse2.isSuccess()) {
                serviceHelper.handleErrorAndThrowException(logger, genericAccountResponse2.getMessage());
            }

            copyAccountToDirectory(account, request);
        }

        try {
            directoryRepo.updateInternalIdByCompanyIdAndUserId(request.getInternalId(), request.getCompanyId(), request.getUserId());
        } catch (Exception ex) {
            String errMsg = "fail to update directory";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }

        LogEntry auditLog = LogEntry.builder()
                .currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz())
                .targetType("directory")
                .targetId(account.getId())
                .companyId(request.getCompanyId())
                .teamId("")
                .originalContents(orig.toString())
                .updatedContents(request.toString())
                .build();

        logger.info("updated directory entry for account", auditLog);

        if (!request.isConfirmedAndActive() &&
                (!orig.getPhoneNumber().equals(request.getPhoneNumber()) || ("".equals(request.getPhoneNumber()) && !orig.getEmail().equals(request.getEmail())))) {
            OnboardWorkerRequest onboardWorkerRequest = OnboardWorkerRequest.builder()
                    .companyId(request.getCompanyId())
                    .userId(request.getUserId())
                    .build();
            serviceHelper.onboardWorkerAsync(onboardWorkerRequest);
        }

        serviceHelper.trackEventAsync("directoryentry_updated");

        return request;
    }

    public AssociationList getAssociations(String companyId, int offset, int limit) {
        // this handles permissions
        DirectoryList directoryList = this.listDirectory(companyId, offset, limit);

        AssociationList associationList = AssociationList.builder().offset(offset).limit(limit).build();
        for(DirectoryEntryDto directoryEntryDto : directoryList.getAccounts()) {
            Association association = Association.builder().account(directoryEntryDto).build();
            WorkerOfList workerOfList = workerService.getWorkerOf(directoryEntryDto.getUserId());
            for(TeamDto teamDto : workerOfList.getTeams()) {
                if (teamDto.getCompanyId().equals(companyId)) {
                    association.getTeams().add(teamDto);
                }

                DirectoryEntryDto admin = adminService.getAdmin(companyId, directoryEntryDto.getUserId());
                if (admin != null) {
                    association.setAdmin(true);
                } else {
                    association.setAdmin(false);
                }
            }

            associationList.getAccounts().add(association);
        }

        return associationList;
    }

    private void copyAccountToDirectory(AccountDto a, DirectoryEntryDto d) {
        d.setUserId(a.getId());
        d.setName(a.getName());
        d.setConfirmedAndActive(a.isConfirmedAndActive());
        d.setPhoneNumber(a.getPhoneNumber());
        d.setPhotoUrl(a.getPhotoUrl());
        d.setEmail(a.getEmail());
    }
}
