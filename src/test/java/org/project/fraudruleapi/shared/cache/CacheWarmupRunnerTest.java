package org.project.fraudruleapi.shared.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheWarmupRunnerTest {

    @Mock
    private CacheWarmer cacheWarmer1;

    @Mock
    private CacheWarmer cacheWarmer2;

    @Test
    void run_shouldCallWarmUpOnAllCacheWarmers() {
        CacheWarmupRunner runner = new CacheWarmupRunner(List.of(cacheWarmer1, cacheWarmer2));

        runner.run();

        verify(cacheWarmer1, times(1)).warmUp();
        verify(cacheWarmer2, times(1)).warmUp();
    }

    @Test
    void run_shouldContinueOnException() {
        CacheWarmupRunner runner = new CacheWarmupRunner(List.of(cacheWarmer1, cacheWarmer2));
        doThrow(new RuntimeException("Warmup failed")).when(cacheWarmer1).warmUp();

        runner.run();

        verify(cacheWarmer1, times(1)).warmUp();
        verify(cacheWarmer2, times(1)).warmUp();
    }
}
