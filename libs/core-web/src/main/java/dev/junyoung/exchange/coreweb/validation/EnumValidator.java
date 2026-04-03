package dev.junyoung.exchange.coreweb.validation;

import dev.junyoung.exchange.coreweb.validation.annotation.ValidEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

    private List<String> validValues;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        validValues = Arrays.stream(constraintAnnotation.enumClass().getEnumConstants())
            .map(Enum::name)
            .toList();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        boolean valid = validValues.contains(value);
        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("다음 값 중 하나가 포함되어야 합니다. " + validValues).addConstraintViolation();
        }
        return valid;
    }
}
