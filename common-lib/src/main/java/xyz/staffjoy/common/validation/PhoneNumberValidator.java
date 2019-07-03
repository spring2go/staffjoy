package xyz.staffjoy.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    @Override
    public boolean isValid(String phoneField, ConstraintValidatorContext context) {
        if (phoneField == null) return true; // can be null
        return phoneField != null && phoneField.matches("[0-9]+")
                && (phoneField.length() > 8) && (phoneField.length() < 14);
    }
}
