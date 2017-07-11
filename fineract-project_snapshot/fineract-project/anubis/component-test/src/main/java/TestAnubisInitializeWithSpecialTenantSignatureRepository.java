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
import io.mifos.anubis.example.nokeystorage.Example;
import io.mifos.anubis.example.nokeystorage.ExampleConfiguration;
import io.mifos.anubis.test.v1.SystemSecurityEnvironment;
import io.mifos.core.api.context.AutoSeshat;
import io.mifos.core.lang.AutoTenantContext;
import io.mifos.core.lang.TenantContextHolder;
import io.mifos.core.test.env.TestEnvironment;
import io.mifos.core.test.fixture.TenantDataStoreContextTestRule;
import io.mifos.core.test.fixture.cassandra.CassandraInitializer;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
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
public class TestAnubisInitializeWithSpecialTenantSignatureRepository {
  private static final String APP_NAME = "anubis-v1";

  @Configuration
  @EnableFeignClients(basePackages = {"io.mifos.anubis.example.nokeystorage"})
  @RibbonClient(name = APP_NAME)
  @Import({ExampleConfiguration.class})
  static public class TestConfiguration {
    public TestConfiguration() {
      super();
    }

    @Bean()
    public Logger logger() {
      return LoggerFactory.getLogger("initialize-with-special-tenant-signature-repository-test-logger");
    }
  }

  @ClassRule
  public final static TestEnvironment testEnvironment = new TestEnvironment(APP_NAME);

  @ClassRule
  public final static CassandraInitializer cassandraInitializer = new CassandraInitializer();

  @Rule
  public final TenantDataStoreContextTestRule tenantDataStoreContext = TenantDataStoreContextTestRule.forRandomTenantName(cassandraInitializer);

  @SuppressWarnings({"SpringAutowiredFieldsWarningInspection", "SpringJavaAutowiredMembersInspection"})
  @Autowired
  protected Example example;

  @Test
  public void test()
  {
    final SystemSecurityEnvironment systemSecurityEnvironment = new SystemSecurityEnvironment(
            testEnvironment.getSystemKeyTimestamp(),
            testEnvironment.getSystemPublicKey(),
            testEnvironment.getSystemPrivateKey());

    final String systemToken = systemSecurityEnvironment.systemToken(APP_NAME);

    try (final AutoTenantContext ignored = new AutoTenantContext(TenantContextHolder.checkedGetIdentifier())) {
      try (final AutoSeshat ignored2 = new AutoSeshat(systemToken)) {
        example.initialize();
      }}
  }
}
