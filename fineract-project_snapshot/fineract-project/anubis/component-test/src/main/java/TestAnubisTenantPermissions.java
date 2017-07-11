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

import io.mifos.anubis.api.v1.domain.AllowedOperation;
import io.mifos.anubis.example.noinitialize.Example;
import io.mifos.anubis.example.noinitialize.ExampleConfiguration;
import io.mifos.anubis.example.noinitialize.UserContext;
import io.mifos.anubis.test.v1.TenantApplicationSecurityEnvironmentTestRule;
import io.mifos.core.api.context.AutoSeshat;
import io.mifos.core.api.context.AutoUserContext;
import io.mifos.core.api.util.InvalidTokenException;
import io.mifos.core.api.util.NotFoundException;
import io.mifos.core.lang.AutoTenantContext;
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
public class TestAnubisTenantPermissions {
  private static final String APP_NAME = "anubis-v1";
  private static final String DUMMY_URI = "/dummy";
  private static final String DESIGNATOR_URI = "/parameterized/{useridentifier}/with/*/parameters";
  private static final String USER_NAME = "Meryre";

  @Configuration
  @EnableFeignClients(basePackages = {"io.mifos.anubis.example.noinitialize"})
  @RibbonClient(name = APP_NAME)
  @Import({ExampleConfiguration.class})
  static public class TestConfiguration {
    public TestConfiguration() {
      super();
    }

    @Bean()
    public Logger logger() {
      return LoggerFactory.getLogger(APP_NAME + "-logger");
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
  public final TenantApplicationSecurityEnvironmentTestRule tenantApplicationSecurityEnvironment = new TenantApplicationSecurityEnvironmentTestRule(testEnvironment);


  @SuppressWarnings({"SpringAutowiredFieldsWarningInspection", "SpringJavaAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
  @Autowired
  Example example;

  @Test
  public void readPermissionShouldWorkToRead()
  {
    try (final AutoUserContext ignored = setPermissionContext(DUMMY_URI, AllowedOperation.READ))
    {
      example.getDummy();
    }
  }

  @Test(expected = NotFoundException.class)
  public void readPermissionShouldNotWorkToWrite()
  {
    try (final AutoUserContext ignored = setPermissionContext(DUMMY_URI, AllowedOperation.READ))
    {
      example.createDummy();
    }
  }

  @Test(expected = NotFoundException.class)
  public void readPermissionShouldNotWorkToDelete()
  {
    try (final AutoUserContext ignored = setPermissionContext(DUMMY_URI, AllowedOperation.READ))
    {
      example.deleteDummy();
    }
  }

  @Test
  public void changePermissionShouldWorkToWrite()
  {
    try (final AutoUserContext ignored = setPermissionContext(DUMMY_URI, AllowedOperation.CHANGE))
    {
      example.createDummy();
    }
  }

  @Test(expected = NotFoundException.class)
  public void changePermissionShouldNotWorkToRead()
  {
    try (final AutoUserContext ignored = setPermissionContext(DUMMY_URI, AllowedOperation.CHANGE))
    {
      example.getDummy();
    }
  }

  @Test(expected = NotFoundException.class)
  public void changePermissionShouldNotWorkToDelete()
  {
    try (final AutoUserContext ignored = setPermissionContext(DUMMY_URI, AllowedOperation.CHANGE))
    {
      example.deleteDummy();
    }
  }

  @Test
  public void deletePermissionShouldWorkToDelete()
  {
    try (final AutoUserContext ignored = setPermissionContext(DUMMY_URI, AllowedOperation.DELETE))
    {
      example.deleteDummy();
    }
  }

  @Test(expected = NotFoundException.class)
  public void deletePermissionShouldNotWorkToRead()
  {
    try (final AutoUserContext ignored = setPermissionContext(DUMMY_URI, AllowedOperation.DELETE))
    {
      example.getDummy();
    }
  }

  @Test(expected = NotFoundException.class)
  public void deletePermissionShouldNotWorkToChange()
  {
    try (final AutoUserContext ignored = setPermissionContext(DUMMY_URI, AllowedOperation.DELETE))
    {
      example.createDummy();
    }
  }

  @Test(expected = InvalidTokenException.class)
  public void tokenForWrongTenantShouldNotWork()
  {
    final String permissionToken;
    try (final AutoTenantContext ignored = TestEnvironment.createRandomTenantContext()) {
      permissionToken = tenantApplicationSecurityEnvironment.getPermissionToken(USER_NAME, DUMMY_URI, AllowedOperation.READ);
    }

    try (final AutoUserContext ignored = new AutoUserContext(USER_NAME, permissionToken))
    {
      example.getDummy();
    }
  }

  @Test(expected = InvalidTokenException.class)
  public void expiredTokenShouldNotWork() throws InterruptedException {
    final String permissionToken;
    try (final AutoTenantContext ignored = TestEnvironment.createRandomTenantContext()) {
      permissionToken = tenantApplicationSecurityEnvironment.getPermissionToken(USER_NAME, DUMMY_URI, AllowedOperation.READ);
    }

    Thread.sleep(150);

    try (final AutoUserContext ignored = new AutoUserContext(USER_NAME, permissionToken))
    {
      example.getDummy();
    }
  }

  @Test(expected = InvalidTokenException.class)
  public void tokenForWrongUserShouldNotWork() throws InterruptedException {
    final String permissionToken = tenantApplicationSecurityEnvironment.getPermissionToken(USER_NAME, DUMMY_URI, AllowedOperation.READ);

    try (final AutoUserContext ignored = new AutoUserContext("Menna", permissionToken))
    {
      example.getDummy();
    }
  }

  @Test(expected = NotFoundException.class)
  public void requestForAnotherUsersInformationWhenYoureOnlyPermittedToAccessOwnShouldNotWork()
  {
    try (final AutoUserContext ignored = setPermissionContext(DESIGNATOR_URI, AllowedOperation.READ))
    {
      example.parameterized("wrong_user_name", "silly_parameter");
    }
  }

  @Test
  public void requestYourOwnInformationWhenYoureOnlyPermittedToAccessOwnShouldWork()
  {
    try (final AutoUserContext ignored = setPermissionContext(DESIGNATOR_URI, AllowedOperation.READ))
    {
      final String ret = example.parameterized(USER_NAME, "silly_parameter");
      Assert.assertEquals(ret, USER_NAME+"silly_parameter"+42);
    }
  }

  @Test
  public void tenantTokenForSystemEndpointShouldNotWorkRegardlessOfPermissions()
  {
    try (final AutoSeshat ignored = new AutoSeshat(tenantApplicationSecurityEnvironment.systemToken()))
    {
      example.callSystemEndpoint();
    }
    catch (final InvalidTokenException e)
    {
      Assert.fail("call to system endpoint with system token should succeed.");
    }

    try (final AutoUserContext ignored = setPermissionContext("/systemendpoint", AllowedOperation.CHANGE))
    {
      example.callSystemEndpoint();
    }
  }

  @Test
  public void userNameShouldBeCorrectlySetInUserContext()
  {
    try (final AutoUserContext ignored = setPermissionContext("/usercontext", AllowedOperation.READ))
    {
      final UserContext context = example.getUserContext();
      Assert.assertEquals(USER_NAME, context.getUserIdentifier());
    }
  }

  private AutoUserContext setPermissionContext(final String uri, final AllowedOperation allowedOperation)
  {
    final String permissionToken = tenantApplicationSecurityEnvironment.getPermissionToken(USER_NAME, uri, allowedOperation);

    return new AutoUserContext(USER_NAME, permissionToken);
  }
}
