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

import org.junit.Assert;
import org.junit.Test;

public class ReplicationStrategyResolverTest {

  private static final String SIMPLE_STRATEGY = "{'class': 'SimpleStrategy', 'replication_factor': 3}";
  private static final String NETWORK_STRATEGY = "{'class': 'NetworkTopologyStrategy', 'DC1': 2, 'DC2': 2}";

  public ReplicationStrategyResolverTest() {
    super();
  }

  @Test
  public void shouldCreateSimpleStrategy() {
    final String simpleStrategy = ReplicationStrategyResolver.replicationStrategy("Simple", "3");
    Assert.assertEquals(SIMPLE_STRATEGY, simpleStrategy);
  }

  @Test
  public void shouldCreateNetworkTopologyStrategy() {
    final String networkStrategy = ReplicationStrategyResolver.replicationStrategy("Network", "DC1:2,DC2:2");
    Assert.assertEquals(NETWORK_STRATEGY, networkStrategy);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailUnknownStrategy() {
    ReplicationStrategyResolver.replicationStrategy("unknown", "1");
    Assert.fail();
  }
}