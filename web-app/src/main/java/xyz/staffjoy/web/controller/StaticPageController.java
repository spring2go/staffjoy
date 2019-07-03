package xyz.staffjoy.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import xyz.staffjoy.web.view.Constant;
import xyz.staffjoy.web.view.PageFactory;

@Controller
public class StaticPageController {

    @Autowired
    private PageFactory pageFactory;

    @RequestMapping(value="")
    public String getHome(Model model) {
        model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, pageFactory.buildHomePage());
        return Constant.VIEW_HOME;
    }

    @RequestMapping(value="/about")
    public String getAbout(Model model) {
        model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, pageFactory.buildAboutPage());
        return Constant.VIEW_ABOUT;
    }

    @RequestMapping(value="/careers")
    public String getCareers(Model model) {
        model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, pageFactory.buildCareersPage());
        return Constant.VIEW_CAREERS;
    }

    @RequestMapping(value="/pricing")
    public String getPricing(Model model) {
        model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, pageFactory.buildPricingPage());
        return Constant.VIEW_PRICING;
    }

    @RequestMapping(value="/privacy-policy")
    public String getPrivacyPolicy(Model model) {
        model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, pageFactory.buildPrivacyPolicyPage());
        return Constant.VIEW_PRIVACY_POLICY;
    }

    @RequestMapping(value="/sign-up")
    public String getSignup(Model model) {
        model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, pageFactory.buildSignupPage());
        return Constant.VIEW_SIGNUP;
    }

    @RequestMapping(value="/early-access")
    public String getEarlyAccess(Model model) {
        model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, pageFactory.buildEarlyPage());
        return Constant.VIEW_EARLY_ACCESS;
    }

    @RequestMapping(value="/terms")
    public String getTerms(Model model) {
        model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, pageFactory.buildTermsPage());
        return Constant.VIEW_TERMS;
    }
}
