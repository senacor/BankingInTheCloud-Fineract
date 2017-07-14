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
import io.mifos.core.lang.ServiceException;
import io.mifos.rhythm.api.v1.domain.Beat;
import io.mifos.rhythm.service.internal.command.CreateBeatCommand;
import io.mifos.rhythm.service.internal.command.DeleteBeatCommand;
import io.mifos.rhythm.service.internal.service.BeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static io.mifos.core.lang.config.TenantHeaderFilter.TENANT_HEADER;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@RestController
@RequestMapping("/applications/{applicationidentifier}/beats")
public class BeatRestController {

  private final CommandGateway commandGateway;
  private final BeatService beatService;

  @Autowired
  public BeatRestController(final CommandGateway commandGateway,
                            final BeatService beatService) {
    super();
    this.commandGateway = commandGateway;
    this.beatService = beatService;
  }

  @Permittable(value = AcceptedTokenType.SYSTEM)
  @RequestMapping(
          method = RequestMethod.GET,
          consumes = MediaType.ALL_VALUE,
          produces = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  List<Beat> getAllBeatsForApplication(
          @RequestHeader(TENANT_HEADER) final String tenantIdentifier,
          @PathVariable("applicationidentifier") final String applicationIdentifier) {
    return this.beatService.findAllEntities(tenantIdentifier, applicationIdentifier);
  }

  @Permittable(value = AcceptedTokenType.SYSTEM)
  @RequestMapping(
          value = "/{beatidentifier}",
          method = RequestMethod.GET,
          consumes = MediaType.ALL_VALUE,
          produces = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Beat> getBeat(
          @RequestHeader(TENANT_HEADER) final String tenantIdentifier,
          @PathVariable("applicationidentifier") final String applicationIdentifier,
          @PathVariable("beatidentifier") final String beatIdentifier) {
    return this.beatService.findByIdentifier(tenantIdentifier, applicationIdentifier, beatIdentifier)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> ServiceException.notFound("Instance with identifier " + applicationIdentifier + " doesn't exist."));
  }

  @Permittable(value = AcceptedTokenType.SYSTEM, permittedEndpoint = "/applications/{applicationidentifier}/beats", acceptTokenIntendedForForeignApplication = true) //Allow apps to use this endpoint in their provisioning code.
  @RequestMapping(
          method = RequestMethod.POST,
          consumes = MediaType.APPLICATION_JSON_VALUE,
          produces = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> createBeat(
          @RequestHeader(TENANT_HEADER) final String tenantIdentifier,
          @PathVariable("applicationidentifier") final String applicationIdentifier,
          @RequestBody @Valid final Beat instance) throws InterruptedException {
    this.commandGateway.process(new CreateBeatCommand(tenantIdentifier, applicationIdentifier, instance));
    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.SYSTEM)
  @RequestMapping(
          value = "/{beatidentifier}",
          method = RequestMethod.DELETE,
          consumes = MediaType.ALL_VALUE,
          produces = MediaType.ALL_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> deleteBeat(
          @RequestHeader(TENANT_HEADER) final String tenantIdentifier,
          @PathVariable("applicationidentifier") final String applicationIdentifier,
          @PathVariable("beatidentifier") final String beatIdentifier) throws InterruptedException {
    this.commandGateway.process(new DeleteBeatCommand(tenantIdentifier, applicationIdentifier, beatIdentifier));
    return ResponseEntity.accepted().build();
  }
}