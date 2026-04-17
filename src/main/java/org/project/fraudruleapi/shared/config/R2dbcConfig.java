package org.project.fraudruleapi.shared.config;

import org.project.fraudruleapi.shared.converter.JsonNodeToJsonConverter;
import org.project.fraudruleapi.shared.converter.JsonToJsonNodeConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;

import java.util.List;

@Configuration
public class R2dbcConfig {

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return new R2dbcCustomConversions(
                R2dbcCustomConversions.StoreConversions.NONE,
                List.of(
                        new JsonToJsonNodeConverter(),
                        new JsonNodeToJsonConverter()
                )
        );
    }
}