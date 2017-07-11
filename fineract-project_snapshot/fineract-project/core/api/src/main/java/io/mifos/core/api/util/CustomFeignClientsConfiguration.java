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
package io.mifos.core.api.util;

import feign.Feign;
import feign.Target;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.feign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import static io.mifos.core.api.config.ApiConfiguration.LOGGER_NAME;

/**
 * @author Myrle Krantz
 */
//@EnableApiFactory (for logger)
@SuppressWarnings({"unused"})
public class CustomFeignClientsConfiguration extends FeignClientsConfiguration {
  private static class AnnotatedErrorDecoderFeignBuilder extends Feign.Builder {
    private final Logger logger;

    AnnotatedErrorDecoderFeignBuilder(final Logger logger) {
      this.logger = logger;
    }

    public <T> T target(Target<T> target) {
      this.errorDecoder(new AnnotatedErrorDecoder(logger, target.type()));
      return build().newInstance(target);
    }
  }

  @Bean
  @ConditionalOnMissingBean
  public TenantedTargetInterceptor tenantedTargetInterceptor()
  {
    return new TenantedTargetInterceptor();
  }

  @Bean
  @ConditionalOnMissingBean
  public TokenedTargetInterceptor tokenedTargetInterceptor()
  {
    return new TokenedTargetInterceptor();
  }

  @Bean
  @ConditionalOnMissingBean
  public Decoder feignDecoder() {
    return new GsonDecoder();
  }

  @Bean
  @ConditionalOnMissingBean
  public Encoder feignEncoder() {
    return new GsonEncoder();
  }

  @Bean(name = LOGGER_NAME)
  public Logger logger() {
    return LoggerFactory.getLogger(LOGGER_NAME);
  }

  @Bean
  @Scope("prototype")
  @ConditionalOnMissingBean
  public Feign.Builder feignBuilder(@Qualifier(LOGGER_NAME) final Logger logger) {
    return new AnnotatedErrorDecoderFeignBuilder(logger);
  }
}