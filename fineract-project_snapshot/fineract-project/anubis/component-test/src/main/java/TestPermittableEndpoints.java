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
import io.mifos.anubis.api.v1.client.Anubis;
import io.mifos.anubis.api.v1.client.AnubisApiFactory;
import io.mifos.anubis.api.v1.domain.PermittableEndpoint;
import io.mifos.anubis.example.noinitialize.ExampleConfiguration;
import io.mifos.core.test.env.TestEnvironment;
import io.mifos.core.test.fixture.TenantDataStoreContextTestRule;
import io.mifos.core.test.fixture.cassandra.CassandraInitializer;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

/**
 * @author Myrle Krantz
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestPermittableEndpoints {
  private static final String APP_NAME = "anubis-v1";
  private static final String LOGGER_QUALIFIER = "test-logger";

  @Configuration
  @Import({ExampleConfiguration.class})
  public static class TestConfiguration {
    public TestConfiguration() {
      super();
    }

    @Bean(name=LOGGER_QUALIFIER)
    public Logger logger() {
      return LoggerFactory.getLogger("permittable-test-logger");
    }
  }

  @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
  @Autowired
  @Qualifier(value = LOGGER_QUALIFIER)
  Logger logger;

  private final static TestEnvironment testEnvironment = new TestEnvironment(APP_NAME);
  private final static CassandraInitializer cassandraInitializer = new CassandraInitializer();
  private final static TenantDataStoreContextTestRule tenantDataStoreContext = TenantDataStoreContextTestRule.forRandomTenantName(cassandraInitializer);

  @ClassRule
  public static TestRule orderClassRules = RuleChain
          .outerRule(testEnvironment)
          .around(cassandraInitializer)
          .around(tenantDataStoreContext);

  @Test
  public void shouldFindPermittableEndpoints() throws Exception {
    final Anubis anubis = AnubisApiFactory.create(TestPermittableEndpoints.testEnvironment.serverURI(), logger);
    final List<PermittableEndpoint> permittableEndpoints = anubis.getPermittableEndpoints();
    Assert.assertNotNull(permittableEndpoints);
    Assert.assertEquals(6, permittableEndpoints.size());
    Assert.assertTrue(permittableEndpoints.containsAll(Arrays.asList(
        new PermittableEndpoint("anubis-v1/dummy", "GET"),
        new PermittableEndpoint("anubis-v1/dummy", "DELETE"),
        new PermittableEndpoint("anubis-v1/dummy", "POST"),
        new PermittableEndpoint("anubis-v1/parameterized/*/with/*/parameters", "GET", "endpointGroup"),
        new PermittableEndpoint("anubis-v1/parameterized/{useridentifier}/with/*/parameters", "GET", "endpointGroupWithParameters"))));
    Assert.assertFalse(permittableEndpoints.contains(new PermittableEndpoint("anubis-v1/systemendpoint", "POST")));
  }
}
