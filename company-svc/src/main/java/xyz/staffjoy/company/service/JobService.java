package xyz.staffjoy.company.service;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.staffjoy.common.api.ResultCode;
import xyz.staffjoy.common.auditlog.LogEntry;
import xyz.staffjoy.common.auth.AuthContext;
import xyz.staffjoy.common.error.ServiceException;
import xyz.staffjoy.company.dto.CreateJobRequest;
import xyz.staffjoy.company.dto.JobDto;
import xyz.staffjoy.company.dto.JobList;
import xyz.staffjoy.company.model.Job;
import xyz.staffjoy.company.repo.JobRepo;
import xyz.staffjoy.company.service.helper.ServiceHelper;

import java.util.List;

@Service
public class JobService {
    static final ILogger logger = SLoggerFactory.getLogger(JobService.class);

    @Autowired
    JobRepo jobRepo;

    @Autowired
    TeamService teamService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    ServiceHelper serviceHelper;

    public JobDto createJob(CreateJobRequest request) {
        // validate and will throw exception if not exist
        teamService.getTeamWithCompanyIdValidation(request.getCompanyId(), request.getTeamId());

        Job job = Job.builder()
                .name(request.getName())
                .color(request.getColor())
                .teamId(request.getTeamId())
                .build();

        try {
            jobRepo.save(job);
        } catch(Exception ex) {
            String errMsg = "could not create job";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }

        LogEntry auditLog = LogEntry.builder()
                .currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz())
                .targetType("job")
                .targetId(job.getId())
                .companyId(request.getCompanyId())
                .teamId(job.getTeamId())
                .updatedContents(job.toString())
                .build();

        logger.info("created job", auditLog);

        serviceHelper.trackEventAsync("job_created");

        JobDto jobDto = this.convertToDto(job);
        jobDto.setCompanyId(request.getCompanyId());

        return jobDto;
    }

    public JobList listJobs(String companyId, String teamId) {
        // validate and will throw exception if not exist
        teamService.getTeamWithCompanyIdValidation(companyId, teamId);

        JobList jobList = JobList.builder().build();
        List<Job> jobs = jobRepo.findJobByTeamId(teamId);
        for (Job job : jobs) {
            JobDto jobDto = this.convertToDto(job);
            jobDto.setCompanyId(companyId);
            jobList.getJobs().add(jobDto);
        }

        return jobList;
    }

    public JobDto getJob(String jobId, String companyId, String teamId) {
        // validate and will throw exception if not exist
        teamService.getTeamWithCompanyIdValidation(companyId, teamId);

        Job job = jobRepo.findJobById(jobId);
        if (job == null) {
            throw new ServiceException(ResultCode.NOT_FOUND, "job not found");
        }

        JobDto jobDto = this.convertToDto(job);
        jobDto.setCompanyId(companyId);

        return jobDto;
    }

    public JobDto updateJob(JobDto jobDtoToUpdate) {
        // validate and will throw exception if not exist
        teamService.getTeamWithCompanyIdValidation(jobDtoToUpdate.getCompanyId(), jobDtoToUpdate.getTeamId());

        JobDto orig = this.getJob(jobDtoToUpdate.getId(), jobDtoToUpdate.getCompanyId(), jobDtoToUpdate.getTeamId());
        Job jobToUpdate = convertToModel(jobDtoToUpdate);

        try {
            jobRepo.save(jobToUpdate);
        } catch (Exception ex) {
            String errMsg = "could not update job";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }

        LogEntry auditLog = LogEntry.builder()
                .currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz())
                .targetType("job")
                .targetId(jobDtoToUpdate.getId())
                .companyId(jobDtoToUpdate.getCompanyId())
                .teamId(jobDtoToUpdate.getTeamId())
                .originalContents(orig.toString())
                .updatedContents(jobDtoToUpdate.toString())
                .build();

        logger.info("updated job", auditLog);

        serviceHelper.trackEventAsync("job_updated");

        return jobDtoToUpdate;
    }

    JobDto convertToDto(Job job) {
        return modelMapper.map(job, JobDto.class);
    }

    Job convertToModel(JobDto jobDto) {
        return modelMapper.map(jobDto, Job.class);
    }
}
