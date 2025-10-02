package org.project.fraudruleapi;

import org.junit.jupiter.api.Test;
import org.project.fraudruleapi.config.TestCacheConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@ActiveProfiles("test")
@Import(TestCacheConfiguration.class)
class ApplicationTests {

    @Test
    void contextLoads() {
    }

}
