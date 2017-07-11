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
package io.mifos.permittedfeignclient.config;

import feign.Client;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import io.mifos.anubis.config.EnableAnubis;
import io.mifos.core.api.util.AnnotatedErrorDecoder;
import io.mifos.core.api.util.TenantedTargetInterceptor;
import io.mifos.core.api.util.TokenedTargetInterceptor;
import io.mifos.identity.api.v1.client.IdentityManager;
import io.mifos.permittedfeignclient.LibraryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.netflix.feign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

/**
 * @author Myrle Krantz
 */
@EnableAnubis
@Configuration
public class PermittedFeignClientConfiguration {
  @Bean(name = LibraryConstants.LOGGER_NAME)
  public Logger logger() {
    return LoggerFactory.getLogger(LibraryConstants.LOGGER_NAME);
  }

  @Bean
  public IdentityManager identityManager(
          @SuppressWarnings("SpringJavaAutowiringInspection") final @Nonnull Client feignClient,
          final @Qualifier(LibraryConstants.LOGGER_NAME) @Nonnull Logger logger) {
    return Feign.builder()
            .contract(new SpringMvcContract())
            .client(feignClient) //Integrates to ribbon.
            .errorDecoder(new AnnotatedErrorDecoder(logger, IdentityManager.class))
            .requestInterceptor(new TenantedTargetInterceptor())
            .requestInterceptor(new TokenedTargetInterceptor())
            .decoder(new GsonDecoder())
            .encoder(new GsonEncoder())
            .target(IdentityManager.class, "http://identity-v1/identity/v1");
  }
}
