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
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import io.mifos.core.api.config.ApiConfiguration;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.netflix.feign.support.SpringMvcContract;
import org.springframework.stereotype.Component;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@Component
public class ApiFactory {

  private final Logger logger;

  @Autowired
  public ApiFactory(@Qualifier(ApiConfiguration.LOGGER_NAME) final Logger logger) {
    this.logger = logger;
  }

  public <T> T create(final Class<T> clazz, final String target) {
    final CookieInterceptingClient client = new CookieInterceptingClient(target);
    return Feign.builder()
        .contract(new SpringMvcContract())
        .client(client)
        .errorDecoder(new AnnotatedErrorDecoder(logger, clazz))
        .requestInterceptor(new TenantedTargetInterceptor())
        .requestInterceptor(new TokenedTargetInterceptor())
        .requestInterceptor(client.getCookieInterceptor())
        .decoder(new GsonDecoder())
        .encoder(new GsonEncoder())
        .target(clazz, target);
  }

  public <T> FeignTargetWithCookieJar<T> createWithCookieJar(final Class<T> clazz, final String target) {
    final CookieInterceptingClient client = new CookieInterceptingClient(target);
    final T feignTarget = Feign.builder()
            .contract(new SpringMvcContract())
            .client(client)
            .errorDecoder(new AnnotatedErrorDecoder(logger, clazz))
            .requestInterceptor(new TenantedTargetInterceptor())
            .requestInterceptor(new TokenedTargetInterceptor())
            .requestInterceptor(client.getCookieInterceptor())
            .decoder(new GsonDecoder())
            .encoder(new GsonEncoder())
            .target(clazz, target);

    return new FeignTargetWithCookieJar<>(feignTarget, client);
  }
}
