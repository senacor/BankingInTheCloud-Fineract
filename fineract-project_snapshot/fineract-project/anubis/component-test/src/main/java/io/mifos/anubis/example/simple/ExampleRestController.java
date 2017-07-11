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

import io.mifos.anubis.annotation.AcceptedTokenType;
import io.mifos.anubis.annotation.Permittable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@RestController
@RequestMapping()
public class ExampleRestController {
  private boolean initialized = false;

  @RequestMapping(value = "/initialize", method = RequestMethod.POST)
  @Permittable(AcceptedTokenType.SYSTEM)
  public ResponseEntity<Void> initialize()
  {
    initialized = true;
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(value = "/initialize", method = RequestMethod.GET)
  @Permittable(AcceptedTokenType.GUEST)
  public ResponseEntity<Boolean> isInitialized()
  {
    return new ResponseEntity<>(initialized, HttpStatus.OK);
  }

  @RequestMapping(value = "/initialize", method = RequestMethod.DELETE)
  @Permittable(AcceptedTokenType.GUEST)
  public ResponseEntity<Void> uninitialize()
  {
    initialized = false;
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(value = "/foo", method = RequestMethod.GET)
  @Permittable(AcceptedTokenType.TENANT)
  public ResponseEntity<Boolean> foo() {
    return ResponseEntity.ok(false);
  }

  @RequestMapping(value = "/{applicationidentifier}/forapplication", method = RequestMethod.GET)
  @Permittable(value = AcceptedTokenType.SYSTEM, permittedEndpoint = "/{applicationidentifier}/forapplication", acceptTokenIntendedForForeignApplication = true)
  public ResponseEntity<Boolean> forApplication(@PathVariable("applicationidentifier") final String applicationIdentifier) {
    return ResponseEntity.ok(true);
  }

  @SuppressWarnings("DefaultAnnotationParam")
  @RequestMapping(value = "/{applicationidentifier}/notforapplication", method = RequestMethod.GET)
  @Permittable(value = AcceptedTokenType.SYSTEM, permittedEndpoint = "/{applicationidentifier}/forapplication", acceptTokenIntendedForForeignApplication = false)
  public ResponseEntity<Boolean> notForApplication(@PathVariable("applicationidentifier") final String applicationIdentifier) {
    return ResponseEntity.ok(true);
  }
}
