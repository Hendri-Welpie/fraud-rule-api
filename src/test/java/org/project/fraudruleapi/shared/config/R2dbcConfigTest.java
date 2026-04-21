package org.project.fraudruleapi.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;

import static org.assertj.core.api.Assertions.assertThat;

class R2dbcConfigTest {

    @Test
    void r2dbcCustomConversions_shouldRegisterConverters() {
        R2dbcConfig config = new R2dbcConfig();
        R2dbcCustomConversions conversions = config.r2dbcCustomConversions();
        assertThat(conversions).isNotNull();
    }
}

