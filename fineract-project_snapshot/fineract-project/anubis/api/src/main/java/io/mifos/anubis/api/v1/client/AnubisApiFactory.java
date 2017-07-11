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
package io.mifos.anubis.api.v1.client;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import io.mifos.core.api.util.AnnotatedErrorDecoder;
import io.mifos.core.api.util.TenantedTargetInterceptor;
import io.mifos.core.api.util.TokenedTargetInterceptor;
import org.slf4j.Logger;
import org.springframework.cloud.netflix.feign.support.SpringMvcContract;

@SuppressWarnings("unused")
public interface AnubisApiFactory {

  static Anubis create(final String target, final Logger logger) {
    return Feign.builder()
            .contract(new SpringMvcContract())
            .errorDecoder(new AnnotatedErrorDecoder(logger, Anubis.class))
            .requestInterceptor(new TenantedTargetInterceptor())
            .requestInterceptor(new TokenedTargetInterceptor())
            .decoder(new GsonDecoder())
            .encoder(new GsonEncoder())
            .target(Anubis.class, target);
  }
}
