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
import io.mifos.anubis.example.simple.Example;
import io.mifos.anubis.example.simple.ExampleConfiguration;
import io.mifos.anubis.example.simple.Metrics;
import io.mifos.anubis.example.simple.MetricsFeignClient;
import io.mifos.anubis.test.v1.SystemSecurityEnvironment;
import io.mifos.anubis.test.v1.TenantApplicationSecurityEnvironmentTestRule;
import io.mifos.core.api.context.AutoUserContext;
import io.mifos.core.api.util.NotFoundException;
import io.mifos.core.test.env.TestEnvironment;
import io.mifos.core.test.fixture.TenantDataStoreContextTestRule;
import io.mifos.core.test.fixture.cassandra.CassandraInitializer;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Myrle Krantz
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestSystemToken {
  private static final String APP_NAME = "anubis-v1";

  @Configuration
  @EnableFeignClients(basePackages = {"io.mifos.anubis.example.simple"})
  @RibbonClient(name = APP_NAME)
  @Import({ExampleConfiguration.class})
  static public class TestConfiguration {
    public TestConfiguration() {
      super();
    }

    @Bean()
    public Logger logger() {
      return LoggerFactory.getLogger("system-token-logger");
    }
  }

  private final static TestEnvironment testEnvironment = new TestEnvironment(APP_NAME);
  private final static CassandraInitializer cassandraInitializer = new CassandraInitializer();
  private final static TenantDataStoreContextTestRule tenantDataStoreContext = TenantDataStoreContextTestRule.forRandomTenantName(cassandraInitializer);

  @ClassRule
  public static TestRule orderClassRules = RuleChain
          .outerRule(testEnvironment)
          .around(cassandraInitializer)
          .around(tenantDataStoreContext);

  @Rule
  public final TenantApplicationSecurityEnvironmentTestRule tenantApplicationSecurityEnvironment
          = new TenantApplicationSecurityEnvironmentTestRule(testEnvironment);

  @SuppressWarnings({"SpringAutowiredFieldsWarningInspection", "SpringJavaAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
  @Autowired
  protected MetricsFeignClient metricsFeignClient;

  @SuppressWarnings({"SpringAutowiredFieldsWarningInspection", "SpringJavaAutowiredMembersInspection"})
  @Autowired
  Example example;


  @Test
  public void shouldBeAbleToGetContactSpringEndpoint() throws Exception {
    try (final AutoUserContext ignored = tenantApplicationSecurityEnvironment.createAutoSeshatContext()) {
      final Metrics metrics = metricsFeignClient.getMetrics();
      Assert.assertTrue(metrics.getThreads() > 0);
    }
  }

  @Test
  public void shouldBeAbleToGetForForeignApplication() throws Exception {
    final TenantApplicationSecurityEnvironmentTestRule tenantForeignApplicationSecurityEnvironment
            = new TenantApplicationSecurityEnvironmentTestRule("foreign-v1", testEnvironment.serverURI(),
            new SystemSecurityEnvironment(testEnvironment.getSystemKeyTimestamp(), testEnvironment.getSystemPublicKey(), testEnvironment.getSystemPrivateKey()));
    try (final AutoUserContext ignored = tenantForeignApplicationSecurityEnvironment.createAutoSeshatContext()) {
      final boolean ret = example.forApplication("foreign-v1");
      Assert.assertTrue(ret);
    }
  }

  @Test(expected = NotFoundException.class)
  public void shouldNotBeAbleToGetForForeignApplicationWhenForeignApplicationNotEnabled() throws Exception {
    final TenantApplicationSecurityEnvironmentTestRule tenantForeignApplicationSecurityEnvironment
            = new TenantApplicationSecurityEnvironmentTestRule("foreign-v1", testEnvironment.serverURI(),
            new SystemSecurityEnvironment(testEnvironment.getSystemKeyTimestamp(), testEnvironment.getSystemPublicKey(), testEnvironment.getSystemPrivateKey()));
    try (final AutoUserContext ignored = tenantForeignApplicationSecurityEnvironment.createAutoSeshatContext()) {
      example.notForApplication("foreign-v1");
      Assert.fail("Shouldn't be able to access for a foreign token in this case.");
    }
  }
}