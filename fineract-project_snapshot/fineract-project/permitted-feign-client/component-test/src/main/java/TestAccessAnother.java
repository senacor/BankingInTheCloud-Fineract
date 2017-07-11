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
import accessanother.api.AccessAnother;
import accessanother.service.AccessAnotherConfiguration;
import io.mifos.another.api.Another;
import io.mifos.anubis.api.v1.domain.AllowedOperation;
import io.mifos.anubis.test.v1.TenantApplicationSecurityEnvironmentTestRule;
import io.mifos.core.api.config.EnableApiFactory;
import io.mifos.core.api.context.AutoUserContext;
import io.mifos.core.api.util.ApiFactory;
import io.mifos.core.lang.DateConverter;
import io.mifos.core.test.env.TestEnvironment;
import io.mifos.core.test.fixture.cassandra.CassandraInitializer;
import io.mifos.core.test.servicestarter.EurekaForTest;
import io.mifos.core.test.servicestarter.InitializedMicroservice;
import io.mifos.core.test.servicestarter.IntegrationTestEnvironment;
import io.mifos.identity.api.v1.client.IdentityManager;
import io.mifos.identity.api.v1.domain.Authentication;
import io.mifos.identity.api.v1.domain.Permission;
import io.mifos.permittedfeignclient.api.v1.client.ApplicationPermissionRequirements;
import io.mifos.permittedfeignclient.api.v1.domain.ApplicationPermission;
import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static accessanother.service.apiforother.AnotherWithApplicationPermissions.ENDPOINT_SET_IDENTIFIER;
import static io.mifos.core.test.env.TestEnvironment.RIBBON_USES_EUREKA_PROPERTY;
import static io.mifos.core.test.env.TestEnvironment.SPRING_CLOUD_DISCOVERY_ENABLED_PROPERTY;

/**
 * @author Myrle Krantz
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestAccessAnother {
  private static final String APP_NAME = "accessanother-v1";
  private static final String LOGGER_QUALIFIER = "test-logger";
  private static final String USER_NAME = "blah";

  @Configuration
  @EnableApiFactory
  @Import({AccessAnotherConfiguration.class})
  public static class TestConfiguration {
    public TestConfiguration() {
      super();
    }

    @Bean(name = LOGGER_QUALIFIER)
    public Logger logger() {
      return LoggerFactory.getLogger(APP_NAME + "-logger");
    }
  }

  private final static EurekaForTest eurekaForTest = new EurekaForTest();
  private final static CassandraInitializer cassandraInitializer = new CassandraInitializer();
  private final static IntegrationTestEnvironment integrationTestEnvironment = new IntegrationTestEnvironment(cassandraInitializer);
  private final static InitializedMicroservice<Another> another= new InitializedMicroservice<>(Another.class, "permitted-feign-client", "0.1.0-BUILD-SNAPSHOT", integrationTestEnvironment);

  @ClassRule
  public static TestRule orderedRules = RuleChain
          .outerRule(eurekaForTest)
          .around(cassandraInitializer)
          .around(integrationTestEnvironment)
          .around(another);


  private final static TestEnvironment testEnvironment = new TestEnvironment(APP_NAME);

  @BeforeClass
  public static void someExtraTestEnvironmentStuff() {
    testEnvironment.setKeyPair(integrationTestEnvironment.getSeshatKeyTimestamp(), integrationTestEnvironment.getSeshatPublicKey(), integrationTestEnvironment.getSeshatPrivateKey());
    testEnvironment.setProperty("eureka.client.serviceUrl.defaultZone", "http://localhost:8761/eureka");
    testEnvironment.setProperty(SPRING_CLOUD_DISCOVERY_ENABLED_PROPERTY, "true");
    testEnvironment.setProperty(RIBBON_USES_EUREKA_PROPERTY, "true");
    testEnvironment.setProperty("eureka.instance.hostname", "localhost");
    testEnvironment.setProperty("eureka.client.fetchRegistry", "true");
    testEnvironment.setProperty("eureka.registration.enabled", "true");
    testEnvironment.setProperty("eureka.instance.leaseRenewalIntervalInSeconds", "1"); //Speed up registration for test purposes.
    testEnvironment.setProperty("eureka.client.initialInstanceInfoReplicationIntervalSeconds", "0");  //Speed up initial registration for test purposes.
    testEnvironment.setProperty("eureka.client.instanceInfoReplicationIntervalSeconds", "1");
    testEnvironment.populate();
  }

  @Rule
  public final TenantApplicationSecurityEnvironmentTestRule tenantApplicationSecurityEnvironment
          = new TenantApplicationSecurityEnvironmentTestRule(APP_NAME,
          testEnvironment.serverURI(), integrationTestEnvironment.getSystemSecurityEnvironment(),
          this::waitForInitialize);

  @SuppressWarnings({"SpringAutowiredFieldsWarningInspection", "SpringJavaAutowiredMembersInspection"})
  @Autowired
  private ApiFactory apiFactory;

  @MockBean
  private IdentityManager identityManager;

  private AccessAnother accessAnother;
  private ApplicationPermissionRequirements applicationPermissionRequirements;

  @Before
  public void before()
  {
    another.setApiFactory(apiFactory);
    accessAnother = apiFactory.create(AccessAnother.class, testEnvironment.serverURI());
    applicationPermissionRequirements = apiFactory.create(ApplicationPermissionRequirements.class, testEnvironment.serverURI());
  }


  public boolean waitForInitialize() {
    try {
      TimeUnit.SECONDS.sleep(15);
      return true;
    } catch (final InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  public void permissionRequirementsListedProperly() {
    final List<ApplicationPermission> requiredPermissions = applicationPermissionRequirements.getRequiredPermissions();
    Assert.assertFalse(requiredPermissions.isEmpty());
    Assert.assertTrue(requiredPermissions.toString(), requiredPermissions.contains(
            new ApplicationPermission(ENDPOINT_SET_IDENTIFIER,
                    new Permission(accessanother.service.apiforother.AnotherWithApplicationPermissions.ANOTHER_FOO_PERMITTABLE_GROUP,
                            new HashSet<>(Arrays.asList(AllowedOperation.READ, AllowedOperation.CHANGE))))));
  }

  @Test
  public void canAccessAnother()
  {
    try (final AutoUserContext ignored = integrationTestEnvironment.createAutoUserContext(USER_NAME)) {
      Assert.assertFalse(another.api().getFoo());
    }

    mockIdentityManagerInteraction();
    try (final AutoUserContext ignored = tenantApplicationSecurityEnvironment.createAutoUserContext("blah")) {
      accessAnother.createDummy();
    }
    try (final AutoUserContext ignored = integrationTestEnvironment.createAutoUserContext("blah")) {
      Assert.assertTrue(another.api().getFoo());
    }
  }

  private void mockIdentityManagerInteraction() {
    final String token = tenantApplicationSecurityEnvironment.getSystemSecurityEnvironment()
            .getPermissionToken(USER_NAME, "another-v1", "/foo", AllowedOperation.CHANGE);

    final String expirationString = getExpirationString();
    final Authentication applicationAuthentication = new Authentication(token, expirationString, expirationString, null);
    Mockito.doReturn(applicationAuthentication).when(identityManager).refresh(Mockito.anyString());
  }

  private String getExpirationString() {
    final long issued = System.currentTimeMillis();
    final Date expiration = new Date(issued + TimeUnit.SECONDS.toMillis(30));
    final LocalDateTime localDateTimeExpiration = LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.of("UTC"));
    return DateConverter.toIsoString(localDateTimeExpiration);
  }
}
