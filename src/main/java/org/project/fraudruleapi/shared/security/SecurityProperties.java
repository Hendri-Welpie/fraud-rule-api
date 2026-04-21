package org.project.fraudruleapi.shared.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties("app.security")
public class SecurityProperties {

    private boolean enabled = false;
    private List<UserConfig> users = new ArrayList<>();

    @Getter
    @Setter
    public static class UserConfig {
        private String username;
        private String password;
        private String role = "ANALYST";
    }
}

