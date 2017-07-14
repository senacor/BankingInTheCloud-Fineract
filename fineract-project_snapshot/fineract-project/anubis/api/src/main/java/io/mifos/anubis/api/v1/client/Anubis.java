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

import io.mifos.anubis.api.v1.domain.ApplicationSignatureSet;
import io.mifos.anubis.api.v1.domain.PermittableEndpoint;
import io.mifos.anubis.api.v1.domain.Signature;
import io.mifos.anubis.api.v1.validation.ValidKeyTimestamp;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@SuppressWarnings("WeakerAccess")
@FeignClient
public interface Anubis {
  @RequestMapping(
      value = "/permittables",
      method = RequestMethod.GET,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.ALL_VALUE
  )
  List<PermittableEndpoint> getPermittableEndpoints();

  @RequestMapping(value = "/signatures", method = RequestMethod.GET,
          consumes = {MediaType.APPLICATION_JSON_VALUE},
          produces = {MediaType.ALL_VALUE})
  List<String> getAllSignatureSets();

  @RequestMapping(value = "/signatures/{timestamp}", method = RequestMethod.POST,
          consumes = {MediaType.APPLICATION_JSON_VALUE},
          produces = {MediaType.ALL_VALUE})
  ApplicationSignatureSet createSignatureSet(@PathVariable("timestamp") @ValidKeyTimestamp String timestamp,
                                             @RequestBody Signature identityManagerSignature);

  @RequestMapping(value = "/signatures/{timestamp}", method = RequestMethod.GET,
          consumes = {MediaType.APPLICATION_JSON_VALUE},
          produces = {MediaType.ALL_VALUE})
  ApplicationSignatureSet getSignatureSet(@PathVariable("timestamp") String timestamp);

  @RequestMapping(value = "/signatures/_latest", method = RequestMethod.GET,
          consumes = {MediaType.APPLICATION_JSON_VALUE},
          produces = {MediaType.ALL_VALUE})
  ApplicationSignatureSet getLatestSignatureSet();

  @RequestMapping(value = "/signatures/{timestamp}", method = RequestMethod.DELETE,
          consumes = {MediaType.APPLICATION_JSON_VALUE},
          produces = {MediaType.ALL_VALUE})
  void deleteSignatureSet(@PathVariable("timestamp") String timestamp);

  @RequestMapping(value = "/signatures/{timestamp}/application", method = RequestMethod.GET,
          consumes = {MediaType.APPLICATION_JSON_VALUE},
          produces = {MediaType.ALL_VALUE})
  Signature getApplicationSignature(@PathVariable("timestamp") String timestamp);

  @RequestMapping(value = "/signatures/_latest/application", method = RequestMethod.GET,
          consumes = {MediaType.APPLICATION_JSON_VALUE},
          produces = {MediaType.ALL_VALUE})
  Signature getLatestApplicationSignature();

  @RequestMapping(value = "/initialize", method = RequestMethod.POST,
          consumes = {MediaType.APPLICATION_JSON_VALUE},
          produces = {MediaType.ALL_VALUE})
  void initializeResources();
}
