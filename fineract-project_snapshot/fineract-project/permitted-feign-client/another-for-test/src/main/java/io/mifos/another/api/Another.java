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
package io.mifos.another.api;

import io.mifos.anubis.api.v1.client.Anubis;
import io.mifos.core.api.util.CustomFeignClientsConfiguration;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@FeignClient(name="another-v1", path="/another/v1", configuration = CustomFeignClientsConfiguration.class)
public interface Another extends Anubis {
  @RequestMapping(value = "/foo", method = RequestMethod.POST,
          consumes = {MediaType.APPLICATION_JSON_VALUE},
          produces = {MediaType.ALL_VALUE})
  void createFoo();

  @RequestMapping(value = "/foo", method = RequestMethod.GET,
          consumes = {MediaType.APPLICATION_JSON_VALUE},
          produces = {MediaType.ALL_VALUE})
  boolean getFoo();
}
