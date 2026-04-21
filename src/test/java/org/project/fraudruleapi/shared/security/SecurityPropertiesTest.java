package org.project.fraudruleapi.shared.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityPropertiesTest {

    @Test
    void shouldHaveDefaultValues() {
        SecurityProperties props = new SecurityProperties();
        assertThat(props.isEnabled()).isFalse();
        assertThat(props.getUsers()).isEmpty();
    }

    @Test
    void shouldSetEnabled() {
        SecurityProperties props = new SecurityProperties();
        props.setEnabled(true);
        assertThat(props.isEnabled()).isTrue();
    }

    @Test
    void shouldConfigureUsers() {
        SecurityProperties props = new SecurityProperties();

        SecurityProperties.UserConfig admin = new SecurityProperties.UserConfig();
        admin.setUsername("admin");
        admin.setPassword("admin");
        admin.setRole("ADMIN");

        SecurityProperties.UserConfig analyst = new SecurityProperties.UserConfig();
        analyst.setUsername("analyst");
        analyst.setPassword("analyst");
        analyst.setRole("ANALYST");

        props.setUsers(List.of(admin, analyst));

        assertThat(props.getUsers()).hasSize(2);
        assertThat(props.getUsers().get(0).getUsername()).isEqualTo("admin");
        assertThat(props.getUsers().get(0).getRole()).isEqualTo("ADMIN");
        assertThat(props.getUsers().get(1).getUsername()).isEqualTo("analyst");
        assertThat(props.getUsers().get(1).getRole()).isEqualTo("ANALYST");
    }

    @Test
    void userConfig_shouldHaveDefaultRole() {
        SecurityProperties.UserConfig user = new SecurityProperties.UserConfig();
        assertThat(user.getRole()).isEqualTo("ANALYST");
    }
}

