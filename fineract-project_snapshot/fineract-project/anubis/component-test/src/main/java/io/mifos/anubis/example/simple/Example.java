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
package io.mifos.anubis.example.simple;

import io.mifos.core.api.util.CustomFeignClientsConfiguration;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Myrle Krantz
 */
@FeignClient(name="anubis-v1", path="/anubis/v1", configuration = CustomFeignClientsConfiguration.class)
public interface Example {
  @RequestMapping(value = "initialize", method = RequestMethod.GET)
  boolean initialized();

  @RequestMapping(value = "initialize", method = RequestMethod.DELETE)
  void uninitialize();

  @RequestMapping(value = "foo", method = RequestMethod.GET)
  boolean foo();

  @RequestMapping(value = "{applicationidentifier}/forapplication", method = RequestMethod.GET)
  boolean forApplication(@PathVariable("applicationidentifier") final String applicationIdentifier);

  @RequestMapping(value = "{applicationidentifier}/notforapplication", method = RequestMethod.GET)
  boolean notForApplication(@PathVariable("applicationidentifier") final String applicationIdentifier);
}
