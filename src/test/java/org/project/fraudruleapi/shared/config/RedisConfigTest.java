package org.project.fraudruleapi.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RedisConfigTest {

    @Test
    void lettuceConnectionFactory_shouldCreateFactory() {
        RedisConfig config = new RedisConfig();
        ReflectionTestUtils.setField(config, "hostName", "localhost");
        ReflectionTestUtils.setField(config, "port", 6379);
        ReflectionTestUtils.setField(config, "timeout", Duration.ofMillis(2000));

        LettuceConnectionFactory factory = config.lettuceConnectionFactory();
        assertThat(factory).isNotNull();
        assertThat(factory.getHostName()).isEqualTo("localhost");
        assertThat(factory.getPort()).isEqualTo(6379);
    }

    @Test
    void redisTemplate_shouldBeConfigured() {
        RedisConfig config = new RedisConfig();
        ReflectionTestUtils.setField(config, "hostName", "localhost");
        ReflectionTestUtils.setField(config, "port", 6379);
        ReflectionTestUtils.setField(config, "timeout", Duration.ofMillis(2000));

        LettuceConnectionFactory factory = config.lettuceConnectionFactory();
        RedisTemplate<String, Object> template = config.redisTemplate(factory);
        assertThat(template).isNotNull();
        assertThat(template.getKeySerializer()).isNotNull();
        assertThat(template.getValueSerializer()).isNotNull();
    }

    @Test
    void reactiveRedisTemplate_shouldBeConfigured() {
        RedisConfig config = new RedisConfig();
        ReflectionTestUtils.setField(config, "hostName", "localhost");
        ReflectionTestUtils.setField(config, "port", 6379);
        ReflectionTestUtils.setField(config, "timeout", Duration.ofMillis(2000));

        LettuceConnectionFactory factory = config.lettuceConnectionFactory();
        factory.afterPropertiesSet();
        ReactiveRedisTemplate<String, String> template = config.reactiveRedisTemplate(factory);
        assertThat(template).isNotNull();
    }
}

