package com.fiap.sus.liveops.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AsyncConfigTest {

    @InjectMocks
    private AsyncConfig asyncConfig;

    @Test
    void getAsyncExecutor_ShouldReturnConfiguredThreadPoolTaskExecutor() {
        Executor executor = asyncConfig.getAsyncExecutor();

        assertNotNull(executor);
        assertInstanceOf(ThreadPoolTaskExecutor.class, executor);
    }

    @Test
    void getAsyncExecutor_ShouldConfigureCorePoolSizeToFive() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.getAsyncExecutor();

        assertNotNull(executor);
        assertEquals(5, executor.getCorePoolSize());
    }

    @Test
    void getAsyncExecutor_ShouldConfigureMaxPoolSizeToTwenty() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.getAsyncExecutor();

        assertNotNull(executor);
        assertEquals(20, executor.getMaxPoolSize());
    }

    @Test
    void getAsyncExecutor_ShouldConfigureQueueCapacityToOneHundred() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.getAsyncExecutor();

        assertNotNull(executor);
        assertEquals(100, executor.getQueueCapacity());
    }

    @Test
    void getAsyncExecutor_ShouldConfigureThreadNamePrefixAsAsync() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.getAsyncExecutor();

        assertNotNull(executor);
        assertEquals("Async-", executor.getThreadNamePrefix());
    }

    @Test
    void getAsyncExecutor_ShouldConfigureAbortPolicyAsRejectedExecutionHandler() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.getAsyncExecutor();

        assertNotNull(executor);
        assertInstanceOf(ThreadPoolExecutor.AbortPolicy.class, executor.getThreadPoolExecutor().getRejectedExecutionHandler());
    }

    @Test
    void getAsyncExecutor_ShouldReturnInitializedExecutor() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.getAsyncExecutor();

        assertNotNull(executor);
        assertNotNull(executor.getThreadPoolExecutor());
        assertFalse(executor.getThreadPoolExecutor().isShutdown());
    }

    @Test
    void getAsyncExecutor_WithAbortPolicy_ShouldRejectTasksWhenQueueIsFull() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.getAsyncExecutor();
        assertNotNull(executor);
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        executor.initialize();

        executor.execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        executor.execute(() -> {});

        assertThrows(RejectedExecutionException.class, () -> {
            executor.execute(() -> {});
        });

        executor.shutdown();
    }

    @Test
    void getAsyncUncaughtExceptionHandler_ShouldReturnNonNullHandler() {
        AsyncUncaughtExceptionHandler handler = asyncConfig.getAsyncUncaughtExceptionHandler();

        assertNotNull(handler);
    }

    @Test
    void getAsyncUncaughtExceptionHandler_ShouldHandleExceptionWithoutThrowing() throws NoSuchMethodException {
        AsyncUncaughtExceptionHandler handler = asyncConfig.getAsyncUncaughtExceptionHandler();
        Method method = AsyncConfig.class.getMethod("getAsyncExecutor");
        Exception exception = new RuntimeException("Test exception");
        Object[] params = new Object[]{"param1", "param2"};

        assertDoesNotThrow(() -> {
            assertNotNull(handler);
            handler.handleUncaughtException(exception, method, params);
        });
    }

    @Test
    void getAsyncUncaughtExceptionHandler_WithNullException_ShouldHandleGracefully() throws NoSuchMethodException {
        AsyncUncaughtExceptionHandler handler = asyncConfig.getAsyncUncaughtExceptionHandler();
        Method method = AsyncConfig.class.getMethod("getAsyncExecutor");
        Object[] params = new Object[]{};

        assertDoesNotThrow(() -> {
            assertNotNull(handler);
            handler.handleUncaughtException(null, method, params);
        });
    }

    @Test
    void getAsyncUncaughtExceptionHandler_WithEmptyParams_ShouldHandleGracefully() throws NoSuchMethodException {
        AsyncUncaughtExceptionHandler handler = asyncConfig.getAsyncUncaughtExceptionHandler();
        Method method = AsyncConfig.class.getMethod("getAsyncExecutor");
        Exception exception = new IllegalArgumentException("Invalid argument");

        assertDoesNotThrow(() -> {
            assertNotNull(handler);
            handler.handleUncaughtException(exception, method, new Object[]{});
        });
    }

    @Test
    void getAsyncUncaughtExceptionHandler_WithMultipleParams_ShouldHandleGracefully() throws NoSuchMethodException {
        AsyncUncaughtExceptionHandler handler = asyncConfig.getAsyncUncaughtExceptionHandler();
        Method method = AsyncConfig.class.getMethod("getAsyncExecutor");
        Exception exception = new NullPointerException("Null value");
        Object[] params = new Object[]{"param1", 123, true, null};

        assertDoesNotThrow(() -> {
            assertNotNull(handler);
            handler.handleUncaughtException(exception, method, params);
        });
    }

}

