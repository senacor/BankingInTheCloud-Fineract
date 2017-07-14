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
package io.mifos.core.cassandra.core;

import io.mifos.core.cassandra.util.CassandraConnectorConstants;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class OptionProviderTest {

  public OptionProviderTest() {
    super();
  }

  @Test
  public void shouldFillOptions() {
    final Environment environment = Mockito.mock(Environment.class);
    Mockito.
        when(
            environment.getProperty(CassandraConnectorConstants.CONSISTENCY_LEVEL_DELETE_PROP,
                CassandraConnectorConstants.CONSISTENCY_LEVEL_PROP_DEFAULT))
        .thenReturn(CassandraConnectorConstants.CONSISTENCY_LEVEL_PROP_DEFAULT);

    Mockito.
        when(
            environment.getProperty(CassandraConnectorConstants.CONSISTENCY_LEVEL_READ_PROP,
                CassandraConnectorConstants.CONSISTENCY_LEVEL_PROP_DEFAULT))
        .thenReturn(CassandraConnectorConstants.CONSISTENCY_LEVEL_PROP_DEFAULT);

    Mockito.
        when(
            environment.getProperty(CassandraConnectorConstants.CONSISTENCY_LEVEL_WRITE_PROP,
                CassandraConnectorConstants.CONSISTENCY_LEVEL_PROP_DEFAULT))
        .thenReturn(CassandraConnectorConstants.CONSISTENCY_LEVEL_PROP_DEFAULT);

    Assert.assertNotNull(OptionProvider.readConsistencyLevel(environment));
    Assert.assertNotNull(OptionProvider.writeConsistencyLevel(environment));
    Assert.assertNotNull(OptionProvider.deleteConsistencyLevel(environment));
    Assert.assertNotNull(OptionProvider.readConsistencyLevel(environment));
    Assert.assertNotNull(OptionProvider.writeConsistencyLevel(environment));
    Assert.assertNotNull(OptionProvider.deleteConsistencyLevel(environment));

    Mockito.verify(environment, Mockito.times(3)).getProperty(Mockito.anyString(), Mockito.anyString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailFillOptionsEnvironmentNull() {
    //noinspection ConstantConditions
    OptionProvider.readConsistencyLevel(null);
  }
}
