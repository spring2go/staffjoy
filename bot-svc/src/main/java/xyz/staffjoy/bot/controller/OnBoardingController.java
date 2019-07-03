package xyz.staffjoy.bot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.staffjoy.bot.dto.OnboardWorkerRequest;
import xyz.staffjoy.bot.service.OnBoardingService;
import xyz.staffjoy.common.api.BaseResponse;

@RestController
@RequestMapping(value = "/v1")
@Validated
public class OnBoardingController {
    @Autowired
    private OnBoardingService onBoardingService;

    @PostMapping(value = "/onboard_worker")
    public BaseResponse onboardWorker(@RequestBody @Validated OnboardWorkerRequest request) {
        onBoardingService.onboardWorker(request);
        return BaseResponse.builder().message("onboarded worker").build();
    }

}
