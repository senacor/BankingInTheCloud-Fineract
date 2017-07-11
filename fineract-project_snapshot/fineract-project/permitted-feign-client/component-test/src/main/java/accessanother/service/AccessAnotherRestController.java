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
package accessanother.service;

import accessanother.service.apiforother.AnotherWithApplicationPermissions;
import io.mifos.anubis.annotation.Permittable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Myrle Krantz
 */
@RestController
public class AccessAnotherRestController {
  private final AnotherWithApplicationPermissions anotherWithApplicationPermissions;

  @Autowired
  public AccessAnotherRestController(@SuppressWarnings("SpringJavaAutowiringInspection") final AnotherWithApplicationPermissions anotherWithApplicationPermissions) {

    this.anotherWithApplicationPermissions = anotherWithApplicationPermissions;
  }

  @RequestMapping(
          value = "/dummy",
          method = RequestMethod.POST
  )
  @Permittable()
  public @ResponseBody
  ResponseEntity<Void> resourceThatNeedsAnotherResource() {
    anotherWithApplicationPermissions.createFoo();
    return ResponseEntity.ok().build();
  }
}