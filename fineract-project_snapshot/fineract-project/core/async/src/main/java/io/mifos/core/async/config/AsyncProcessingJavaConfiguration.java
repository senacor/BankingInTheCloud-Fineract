/*
 * Copyright 2017 The Mifos Initiative.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mifos.core.async.config;

import io.mifos.core.async.core.DelegatingContextExecutor;
import io.mifos.core.async.util.AsyncConstants;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncProcessingJavaConfiguration
    implements AsyncConfigurer {

  private final Environment env;

  @Autowired
  public AsyncProcessingJavaConfiguration(final Environment env) {
    super();
    this.env = env;
  }

  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(
        Integer.valueOf(this.env.getProperty(AsyncConstants.CORE_POOL_SIZE_PROP, AsyncConstants.CORE_POOL_SIZE_DEFAULT)));
    executor.setMaxPoolSize(
        Integer.valueOf(this.env.getProperty(AsyncConstants.MAX_POOL_SIZE_PROP, AsyncConstants.MAX_POOL_SIZE_DEFAULT)));
    executor.setQueueCapacity(
        Integer.valueOf(this.env.getProperty(AsyncConstants.QUEUE_CAPACITY_PROP, AsyncConstants.QUEUE_CAPACITY_DEFAULT)));
    executor.setThreadNamePrefix(
        this.env.getProperty(AsyncConstants.THREAD_NAME_PROP, AsyncConstants.THREAD_NAME_DEFAULT));
    executor.initialize();

    return new DelegatingContextExecutor(new DelegatingSecurityContextAsyncTaskExecutor(executor));
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return new SimpleAsyncUncaughtExceptionHandler();
  }
}
