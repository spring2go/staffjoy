package xyz.staffjoy.bot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.staffjoy.bot.dto.*;
import xyz.staffjoy.bot.service.AlertService;
import xyz.staffjoy.common.api.BaseResponse;

@RestController
@RequestMapping(value = "/v1")
@Validated
public class AlertController {

    @Autowired
    private AlertService alertService;

    @PostMapping(value = "alert_new_shift")
    public BaseResponse alertNewShift(@RequestBody @Validated AlertNewShiftRequest request) {
        alertService.alertNewShift(request);
        return BaseResponse.builder().message("new shift alerted").build();
    }

    @PostMapping(value = "alert_new_shifts")
    public BaseResponse alertNewShifts(@RequestBody @Validated AlertNewShiftsRequest request) {
        alertService.alertNewShifts(request);
        return BaseResponse.builder().message("new shifts alerted").build();
    }

    @PostMapping(value = "alert_removed_shift")
    public BaseResponse alertRemovedShift(@RequestBody @Validated AlertRemovedShiftRequest request) {
        alertService.alertRemovedShift(request);
        return BaseResponse.builder().message("removed shift alerted").build();
    }

    @PostMapping(value = "alert_removed_shifts")
    public BaseResponse alertRemovedShifts(@RequestBody @Validated AlertRemovedShiftsRequest request) {
        alertService.alertRemovedShifts(request);
        return BaseResponse.builder().message("removed shifts alerted").build();
    }

    @PostMapping(value = "alert_changed_shifts")
    public BaseResponse alertChangedShifts(@RequestBody @Validated AlertChangedShiftRequest request) {
        alertService.alertChangedShift(request);
        return BaseResponse.builder().message("changed shifts alerted").build();
    }

}
