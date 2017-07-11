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
package io.mifos.anubis.controller;

import io.mifos.anubis.annotation.AcceptedTokenType;
import io.mifos.anubis.annotation.Permittable;
import io.mifos.anubis.api.v1.domain.ApplicationSignatureSet;
import io.mifos.anubis.api.v1.domain.Signature;
import io.mifos.anubis.api.v1.validation.ValidKeyTimestamp;
import io.mifos.anubis.repository.TenantAuthorizationDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Myrle Krantz
 */
@RestController
@RequestMapping()
public class SignatureCreatorRestController {

  private final TenantAuthorizationDataRepository tenantAuthorizationDataRepository;

  @Autowired
  public SignatureCreatorRestController(final TenantAuthorizationDataRepository tenantAuthorizationDataRepository) {
    this.tenantAuthorizationDataRepository = tenantAuthorizationDataRepository;
  }

  @Permittable(AcceptedTokenType.SYSTEM)
  @RequestMapping(
          value = "/signatures/{timestamp}",
          method = RequestMethod.POST,
          consumes = {MediaType.ALL_VALUE},
          produces = {MediaType.APPLICATION_JSON_VALUE})
  public
  @ResponseBody
  ResponseEntity<ApplicationSignatureSet> createSignatureSet(
          @PathVariable("timestamp") @ValidKeyTimestamp final String timestamp,
          @RequestBody @Valid final Signature identityManagerSignature) {
    return ResponseEntity.ok(
            new ApplicationSignatureSet(
                    timestamp,
                    tenantAuthorizationDataRepository.createSignatureSet(timestamp, identityManagerSignature),
                    identityManagerSignature));
  }
}
