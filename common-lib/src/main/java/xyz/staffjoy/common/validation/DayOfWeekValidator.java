package xyz.staffjoy.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class DayOfWeekValidator implements ConstraintValidator<DayOfWeek, String> {

    private List<String> daysOfWeek =
            Arrays.asList("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; // can be null
        String input = value.trim().toLowerCase();
        if (daysOfWeek.contains(input)) {
            return true;
        }
        return false;
    }
}
