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
package io.mifos.core.test.fixture;

import org.junit.rules.ExternalResource;

/**
 * @author Myrle Krantz
 */
public abstract class DataStoreTenantInitializer extends ExternalResource {
  protected abstract void initialize() throws Exception;
  protected abstract void initializeTenant(final String tenantName);
  protected abstract void finish();

  @Override
  protected void before() throws Exception {
    initialize();
  }

  @Override
  protected void after() {
    finish();
  }
}
