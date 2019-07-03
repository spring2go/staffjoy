package xyz.staffjoy.whoami.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.auth.AuthContext;
import xyz.staffjoy.common.auth.Authorize;
import xyz.staffjoy.whoami.dto.FindWhoAmIResponse;
import xyz.staffjoy.whoami.dto.GetIntercomSettingResponse;
import xyz.staffjoy.whoami.dto.IAmDto;
import xyz.staffjoy.whoami.dto.IntercomSettingsDto;
import xyz.staffjoy.whoami.service.WhoAmIService;

@RestController
@RequestMapping("/v1")
public class WhoAmIController {

    @Autowired
    WhoAmIService whoAmIService;

    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    @GetMapping
    public FindWhoAmIResponse findWhoAmI() {
        String userId = AuthContext.getUserId();
        IAmDto iAmDto = whoAmIService.findWhoIAm(userId);

        String authz = AuthContext.getAuthz();
        if (AuthConstant.AUTHORIZATION_SUPPORT_USER.equals(authz)) {
            iAmDto.setSupport(true);
        }

        return new FindWhoAmIResponse(iAmDto);
    }

    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    @GetMapping(value = "/intercom")
    public GetIntercomSettingResponse getIntercomSettings() {
        String userId = AuthContext.getUserId();
        IntercomSettingsDto intercomSettingsDto = whoAmIService.findIntercomSettings(userId);

        return new GetIntercomSettingResponse(intercomSettingsDto);
    }
}
