package xyz.staffjoy.bot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.staffjoy.bot.dto.GreetingRequest;
import xyz.staffjoy.bot.service.GreetingService;
import xyz.staffjoy.common.api.BaseResponse;

@RestController
@RequestMapping(value = "/v1")
@Validated
public class GreetingController {

    @Autowired
    private GreetingService greetingService;

    @PostMapping(value = "/sms_greeting")
    BaseResponse sendSmsGreeting(@RequestBody @Validated GreetingRequest request) {
        greetingService.greeting(request.getUserId());
        return BaseResponse.builder().message("greeting sent").build();
    }
}
