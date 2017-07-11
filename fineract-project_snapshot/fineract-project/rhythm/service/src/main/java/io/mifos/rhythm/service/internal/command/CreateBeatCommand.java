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
package io.mifos.rhythm.service.internal.command;

import io.mifos.rhythm.api.v1.domain.Beat;

/**
 * @author Myrle Krantz
 */
public class CreateBeatCommand {
  private final String tenantIdentifier;

  private final String applicationIdentifier;

  private final Beat instance;

  public CreateBeatCommand(final String tenantIdentifier, final String applicationIdentifier, final Beat instance) {
    super();
    this.tenantIdentifier = tenantIdentifier;
    this.applicationIdentifier = applicationIdentifier;
    this.instance = instance;
  }

  public String getTenantIdentifier() {
    return tenantIdentifier;
  }

  public String getApplicationIdentifier() {
    return applicationIdentifier;
  }

  public Beat getInstance() {
    return this.instance;
  }

  @Override
  public String toString() {
    return "CreateBeatCommand{" +
            "tenantIdentifier='" + tenantIdentifier + '\'' +
            ", applicationIdentifier='" + applicationIdentifier + '\'' +
            ", instance=" + instance.getIdentifier() +
            '}';
  }
}
