package xyz.staffjoy.company.service;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.staffjoy.bot.dto.AlertNewShiftRequest;
import xyz.staffjoy.bot.dto.AlertRemovedShiftRequest;
import xyz.staffjoy.common.auditlog.LogEntry;
import xyz.staffjoy.common.auth.AuthContext;
import xyz.staffjoy.company.dto.*;
import xyz.staffjoy.company.model.Shift;
import xyz.staffjoy.company.repo.ShiftRepo;
import xyz.staffjoy.company.service.helper.ServiceHelper;
import xyz.staffjoy.company.service.helper.ShiftHelper;

import java.time.Instant;
import java.util.*;

@Service
public class ShiftService {

    static final ILogger logger = SLoggerFactory.getLogger(ShiftService.class);

    @Autowired
    ShiftRepo shiftRepo;

    @Autowired
    TeamService teamService;

    @Autowired
    JobService jobService;

    @Autowired
    DirectoryService directoryService;

    @Autowired
    ServiceHelper serviceHelper;

    @Autowired
    ShiftHelper shiftHelper;

    @Autowired
    ModelMapper modelMapper;

    public ShiftDto createShift(CreateShiftRequest req) {
        // validate and will throw exception if not exist
        teamService.getTeamWithCompanyIdValidation(req.getCompanyId(), req.getTeamId());

        if (!StringUtils.isEmpty(req.getJobId())) {
            // validate and will throw exception if not exist
            jobService.getJob(req.getJobId(), req.getCompanyId(), req.getTeamId());
        }

        if (!StringUtils.isEmpty(req.getUserId())) {
            directoryService.getDirectoryEntry(req.getCompanyId(), req.getUserId());
        }

        Shift shift = Shift.builder()
                .teamId(req.getTeamId())
                .jobId(req.getJobId())
                .start(req.getStart())
                .stop(req.getStop())
                .published(req.isPublished())
                .userId(req.getUserId())
                .build();
        try {
            shiftRepo.save(shift);
        } catch (Exception ex) {
            String errMsg = "could not create shift";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }


        LogEntry auditLog = LogEntry.builder()
                .currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz())
                .targetType("shift")
                .targetId(shift.getId())
                .companyId(req.getCompanyId())
                .teamId(req.getTeamId())
                .updatedContents(shift.toString())
                .build();

        logger.info("created shift", auditLog);

        ShiftDto shiftDto = shiftHelper.convertToDto(shift);
        shiftDto.setCompanyId(req.getCompanyId());

        if (!StringUtils.isEmpty(shift.getUserId()) && shift.isPublished()) {
            AlertNewShiftRequest alertNewShiftRequest = AlertNewShiftRequest.builder()
                    .userId(shiftDto.getUserId())
                    .newShift(shiftDto)
                    .build();
            serviceHelper.alertNewShiftAsync(alertNewShiftRequest);
        }

        serviceHelper.trackEventAsync("shift_created");
        if (req.isPublished()) {
            serviceHelper.trackEventAsync("shift_published");
        }

        return shiftDto;
    }

    public ShiftList listWorkerShifts(WorkerShiftListRequest req) {
        // validate and will throw exception if not exist
        teamService.getTeamWithCompanyIdValidation(req.getCompanyId(), req.getTeamId());

        ShiftList shiftList = ShiftList.builder()
                .shiftStartAfter(req.getShiftStartAfter())
                .shiftStartBefore(req.getShiftStartBefore())
                .build();

        List<Shift> shifts = shiftRepo.listWorkerShifts(req.getTeamId(), req.getWorkerId(), req.getShiftStartAfter(), req.getShiftStartBefore());

        return convertToShiftList(shiftList, shifts, req.getCompanyId());
    }

    public ShiftList listShifts(ShiftListRequest req) {
        // validate and will throw exception if not exist
        teamService.getTeamWithCompanyIdValidation(req.getCompanyId(), req.getTeamId());

        ShiftList shiftList = ShiftList.builder()
                .shiftStartAfter(req.getShiftStartAfter())
                .shiftStartBefore(req.getShiftStartBefore())
                .build();

        List<Shift> shifts = null;
        if (!StringUtils.isEmpty(req.getUserId()) && StringUtils.isEmpty(req.getJobId())) {
            shifts = shiftRepo.listWorkerShifts(req.getTeamId(), req.getUserId(), req.getShiftStartAfter(), req.getShiftStartBefore());
        }

        if (!StringUtils.isEmpty(req.getJobId()) && StringUtils.isEmpty(req.getUserId())) {
            shifts = shiftRepo.listShiftByJobId(req.getTeamId(), req.getJobId(), req.getShiftStartAfter(), req.getShiftStartBefore());
        }

        if (!StringUtils.isEmpty(req.getJobId()) && !StringUtils.isEmpty(req.getUserId())) {
            shifts = shiftRepo.listShiftByUserIdAndJobId(req.getTeamId(), req.getUserId(), req.getJobId(), req.getShiftStartAfter(), req.getShiftStartBefore());
        }

        if (StringUtils.isEmpty(req.getJobId()) && StringUtils.isEmpty(req.getUserId())) {
            shifts = shiftRepo.listShiftByTeamIdOnly(req.getTeamId(), req.getShiftStartAfter(), req.getShiftStartBefore());
        }

        return convertToShiftList(shiftList, shifts, req.getCompanyId());
    }

    private ShiftList convertToShiftList(ShiftList shiftList, List<Shift> shifts, String companyId) {
        for(Shift shift : shifts) {
            ShiftDto shiftDto = shiftHelper.convertToDto(shift);
            shiftDto.setCompanyId(companyId);
            shiftList.getShifts().add(shiftDto);
        }

        return shiftList;
    }

    private long quickTime(long startTime) {
        long endTime = System.currentTimeMillis();
        return (endTime - startTime) / 1000;
    }

    public ShiftList bulkPublishShifts(BulkPublishShiftsRequest req) {
        long startTime = System.currentTimeMillis();
        logger.info(String.format("time so far %d", quickTime(startTime)));

        ShiftListRequest shiftListRequest = ShiftListRequest.builder()
                .companyId(req.getCompanyId())
                .teamId(req.getTeamId())
                .userId(req.getUserId())
                .jobId(req.getJobId())
                .shiftStartAfter(req.getShiftStartAfter())
                .shiftStartBefore(req.getShiftStartBefore())
                .build();
        ShiftList orig = this.listShifts(shiftListRequest);

        ShiftList shiftList = ShiftList.builder()
                .shiftStartAfter(req.getShiftStartAfter())
                .shiftStartBefore(req.getShiftStartBefore())
                .build();

        // Keep track of notifications - user to orig shift
        Map<String, List<ShiftDto>> notifs = new HashMap<>();

        logger.info(String.format("before shifts update %d", quickTime(startTime)));

        for(ShiftDto shiftDto : orig.getShifts()) {
            // keep track of what changed for messaging purpose
            if (!StringUtils.isEmpty(shiftDto.getUserId()) &&
                    shiftDto.isPublished() != req.isPublished() &&
                    shiftDto.getStart().isAfter(Instant.now())) {
                List<ShiftDto> shiftDtos = notifs.get(shiftDto.getUserId());
                if (shiftDtos == null) {
                    shiftDtos = new ArrayList<>();
                    notifs.put(shiftDto.getUserId(), shiftDtos);
                }
                ShiftDto copy = shiftDto.toBuilder().build();
                shiftDtos.add(copy);
            }
            // do the change
            shiftDto.setPublished(req.isPublished());

            //shiftHelper.updateShiftAsync(shiftDto);
            shiftHelper.updateShift(shiftDto, true);
            shiftList.getShifts().add(shiftDto);
        }

        logger.info(String.format("before shifts notifications %d", quickTime(startTime)));

        serviceHelper.buildShiftNotificationAsync(notifs, req.isPublished());
        logger.info(String.format("total time %d", quickTime(startTime)));

        return shiftList;

    }

    public ShiftDto getShift(String shiftId, String teamId, String companyId) {
        return shiftHelper.getShift(shiftId, teamId, companyId);
    }

    public ShiftDto updateShift(ShiftDto shiftDtoToUpdate) {
        return shiftHelper.updateShift(shiftDtoToUpdate, false);
    }

    public void deleteShift(String shiftId, String teamId, String companyId) {
        ShiftDto orig = this.getShift(shiftId, teamId, companyId);

        try {
            shiftRepo.deleteShiftById(shiftId);
        } catch (Exception ex) {
            String errMsg = "failed to delete shift";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }

        LogEntry auditLog = LogEntry.builder()
                .currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz())
                .targetType("shift")
                .targetId(shiftId)
                .companyId(companyId)
                .teamId(teamId)
                .originalContents(orig.toString())
                .build();

        logger.info("deleted shift", auditLog);

        if (!StringUtils.isEmpty(orig.getUserId()) && orig.isPublished() && orig.getStart().isAfter(Instant.now())) {
            AlertRemovedShiftRequest alertRemovedShiftRequest = AlertRemovedShiftRequest.builder()
                    .userId(orig.getUserId())
                    .oldShift(orig)
                    .build();
            serviceHelper.alertRemovedShiftAsync(alertRemovedShiftRequest);
        }

        serviceHelper.trackEventAsync("shift_deleted");
    }

}
