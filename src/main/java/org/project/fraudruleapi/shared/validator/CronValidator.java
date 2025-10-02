package org.project.fraudruleapi.shared.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.ObjectUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CronValidator implements ConstraintValidator<ValidCron, String> {

    @Override
    public boolean isValid(String cronExpression,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (ObjectUtils.isEmpty(cronExpression)) {
            return false;
        } else {
            String regex = "^(((\\d+,)+\\d+|(\\d+([/\\-])\\d+)|(\\d+)|(\\*|\\?|L|W|LW|#[1-5]))\\s*){5,7}$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(cronExpression);

            return matcher.matches();
        }
    }
}