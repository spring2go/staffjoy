package xyz.staffjoy.bot;

public class BotConstant {
    public static final String SERVICE_NAME = "bot-service";

    public static final String ONBOARDING_SMS_TEMPLATE_CODE_1 = "SMS_153055065";
    public static final String ONBOARDING_SMS_TEMPLATE_CODE_2 = "SMS_153055066";
    public static final String ONBOARDING_SMS_TEMPLATE_CODE_3 = "SMS_153055067";

    public static final String ONBOARDING_EMAIL_TEMPLATE = "<div>%s Your manager just added you to %s on Staffjoy to share your work schedule.</div><br/><br/><div>When your manager publishes your shifts, we'll send them to you here. (To disable Staffjoy messages, reply STOP at any time)</div><br/><br/><div>Click <a href=\"%s\">this link</a> to sync your shifts to your phone's calendar app.</div>";
    public static final String GREETING_EMAIL_TEMPLATE = "<div>Welcome to Staffjoy!</div>";

    public static final String ALERT_NEW_SHIFT_SMS_TEMPLATE_CODE = "SMS_153055068";
    public static final String ALERT_NEW_SHIFTS_SMS_TEMPLATE_CODE = "SMS_153055069";
    public static final String ALERT_REMOVED_SHIFT_SMS_TEMPLATE_CODE = "SMS_153055070";
    public static final String ALERT_REMOVED_SHIFTS_SMS_TEMPLATE_CODE = "SMS_153055071";
    public static final String ALERT_CHANGED_SHIFT_SMS_TEMPLATE_CODE = "SMS_153055072";

    public static final String ALERT_NEW_SHIFT_EMAIL_TEMPLATE = "<div>%s Your %s manager just published a new%s shift for you: <br/><br/>%s</div>";
    public static final String ALERT_NEW_SHIFTS_EMAIL_TEMPLATE = "<div>%s Your %s manager just published %d new shifts that you are working: <br/><br/>%s</div>";
    public static final String ALERT_REMOVED_SHIFT_EMAIL_TEMPLATE = "<div>%s Your %s manager just removed you from a shift, so you are no longer working on it. Here is your new schedule: <br/><br/>%s</div>";
    public static final String ALERT_REMOVED_SHIFTS_EMAIL_TEMPLATE = "<div>%s Your %s manager just removed %d of your shifts so you are no longer working on it. <br/><br/> Your new shifts are: <br/><br/>%s</div>";
    public static final String ALERT_CHANGED_SHIFT_EMAIL_TEMPLATE = "<div>%s Your %s manager just changed your shift: <br/><br/>Old: %s<br/><br/>New:%s</div>";

    public static final String GREETING_SMS_TEMPLATE_CODE = "SMS_153055080";

    public static final String SMS_START_TIME_FORMAT = "EEE dd/MM hh:mm a";
    public static final String SMS_STOP_TIME_FORMAT = "EEE dd/MM hh:mm a";
    public static final String SMS_SHIFT_FORMAT = "%s - %s"; // print in start and stop

    // ShiftWindow is the number of days out the bot will inform the worker
    // of their schedule
    public static final int SHIFT_WINDOW = 10;
}
