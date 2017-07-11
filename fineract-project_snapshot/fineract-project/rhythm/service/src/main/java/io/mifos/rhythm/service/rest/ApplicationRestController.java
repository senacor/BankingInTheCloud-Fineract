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
package io.mifos.rhythm.service.rest;

import io.mifos.anubis.annotation.AcceptedTokenType;
import io.mifos.anubis.annotation.Permittable;
import io.mifos.core.command.gateway.CommandGateway;
import io.mifos.rhythm.service.internal.command.DeleteApplicationCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static io.mifos.core.lang.config.TenantHeaderFilter.TENANT_HEADER;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@RestController
@RequestMapping("/applications")
public class ApplicationRestController {

  private final CommandGateway commandGateway;

  @Autowired
  public ApplicationRestController(final CommandGateway commandGateway) {
    super();
    this.commandGateway = commandGateway;
  }

  @Permittable(value = AcceptedTokenType.SYSTEM)
  @RequestMapping(
          value = "/{applicationidentifier}",
          method = RequestMethod.DELETE,
          consumes = MediaType.ALL_VALUE,
          produces = MediaType.ALL_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> deleteApplication(
          @RequestHeader(TENANT_HEADER) final String tenantIdentifier,
          @PathVariable("applicationidentifier") final String applicationIdentifier) throws InterruptedException {
    this.commandGateway.process(new DeleteApplicationCommand(tenantIdentifier, applicationIdentifier));
    return ResponseEntity.accepted().build();
  }
}