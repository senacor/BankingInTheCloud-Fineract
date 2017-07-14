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
package io.mifos;

import ch.vorburger.mariadb4j.DB;
import io.mifos.anubis.api.v1.domain.AllowedOperation;
import io.mifos.anubis.api.v1.domain.ApplicationSignatureSet;
import io.mifos.core.api.config.EnableApiFactory;
import io.mifos.core.api.context.AutoSeshat;
import io.mifos.core.api.context.AutoUserContext;
import io.mifos.core.api.util.ApiConstants;
import io.mifos.core.api.util.ApiFactory;
import io.mifos.core.lang.TenantContextHolder;
import io.mifos.core.test.env.TestEnvironment;
import io.mifos.core.test.listener.EventRecorder;
import io.mifos.core.test.servicestarter.ActiveMQForTest;
import io.mifos.core.test.servicestarter.EurekaForTest;
import io.mifos.core.test.servicestarter.IntegrationTestEnvironment;
import io.mifos.core.test.servicestarter.Microservice;
import io.mifos.identity.api.v1.client.IdentityManager;
import io.mifos.identity.api.v1.domain.*;
import io.mifos.identity.api.v1.events.EventConstants;
import io.mifos.office.api.v1.client.OrganizationManager;
import io.mifos.office.api.v1.domain.ContactDetail;
import io.mifos.office.api.v1.domain.Employee;
import io.mifos.office.api.v1.domain.Office;
import io.mifos.provisioner.api.v1.client.Provisioner;
import io.mifos.provisioner.api.v1.domain.*;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@RunWith(SpringRunner.class)
@SpringBootTest()
public class WorkflowTenantProvisioning {
  private static final String CLIENT_ID = "luckyLeprachaun";
  private static Microservice<Provisioner> provisionerService;
  private static Microservice<IdentityManager> identityService;
  private static Microservice<OrganizationManager> officeClient;
  private static DB EMBEDDED_MARIA_DB;

  @Configuration
  @ActiveMQForTest.EnableActiveMQListen
  @EnableApiFactory
  @ComponentScan("io.mifos.listener")
  public static class TestConfiguration {
    public TestConfiguration() {
      super();
    }

    @Bean()
    public Logger logger() {
      return LoggerFactory.getLogger("test-logger");
    }
  }

  @ClassRule
  public static final EurekaForTest eurekaForTest = new EurekaForTest();

  @ClassRule
  public static final ActiveMQForTest activeMQForTest = new ActiveMQForTest();

  @ClassRule
  public static final IntegrationTestEnvironment integrationTestEnvironment = new IntegrationTestEnvironment();

  @Autowired
  private ApiFactory apiFactory;
  @Autowired
  EventRecorder eventRecorder;


  public WorkflowTenantProvisioning() {
    super();
  }

  @BeforeClass
  public static void setup() throws Exception {

    // start embedded Cassandra
    EmbeddedCassandraServerHelper.startEmbeddedCassandra(TimeUnit.SECONDS.toMillis(30L));
    // start embedded MariaDB
    EMBEDDED_MARIA_DB = DB.newEmbeddedDB(3306);
    EMBEDDED_MARIA_DB.start();

    provisionerService = new Microservice<>(Provisioner.class, "provisioner", "0.1.0-BUILD-SNAPSHOT", integrationTestEnvironment);
    final TestEnvironment provisionerTestEnvironment = provisionerService.getProcessEnvironment();
    provisionerTestEnvironment.addSystemPrivateKeyToProperties();
    provisionerTestEnvironment.setProperty("system.initialclientid", CLIENT_ID);
    provisionerService.start();

    identityService = new Microservice<>(IdentityManager.class, "identity", "0.1.0-BUILD-SNAPSHOT", integrationTestEnvironment);
    identityService.start();
    officeClient = new Microservice<>(OrganizationManager.class, "office", "0.1.0-BUILD-SNAPSHOT", integrationTestEnvironment);
    officeClient.start();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    officeClient.kill();
    identityService.kill();
    provisionerService.kill();

    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    EMBEDDED_MARIA_DB.stop();
  }

  @Before
  public void before()
  {
    provisionerService.setApiFactory(apiFactory);
    identityService.setApiFactory(apiFactory);
    officeClient.setApiFactory(apiFactory);
  }

  @Test
  public void test() throws InterruptedException {
    final String tenantAdminPassword = provisionAppsViaSeshat();

    final String username = "antony";
    final Authentication adminPasswordOnlyAuthentication = identityService.api().login(username, tenantAdminPassword);
    try (final AutoUserContext ignored = new AutoUserContext(username, adminPasswordOnlyAuthentication.getAccessToken()))
    {
      identityService.api().changeUserPassword(username, new Password(tenantAdminPassword));
      Assert.assertTrue(eventRecorder.wait(EventConstants.OPERATION_PUT_USER_PASSWORD, username));
    }

    final Authentication adminAuthentication = identityService.api().login(username, tenantAdminPassword);

    final UserWithPassword officeAdministratorUser;
    final UserWithPassword employeeUser;
    final Role employeeRole;
    try (final AutoUserContext ignored = new AutoUserContext(username, adminAuthentication.getAccessToken())) {
      checkCreationOfPermittableGroupsInIsis();
      //TODO: fix tokens for this call
      // checkSignatureSetTimeStampsLineup();

      employeeRole = makeEmployeeRole();
      final Role officeAdministratorRole = makeOfficeAdministratorRole();

      identityService.api().createRole(employeeRole);
      identityService.api().createRole(officeAdministratorRole);

      officeAdministratorUser = new UserWithPassword();
      officeAdministratorUser.setIdentifier("narmer");
      officeAdministratorUser.setPassword(encodePassword("3100BC"));
      officeAdministratorUser.setRole(officeAdministratorRole.getIdentifier());

      identityService.api().createUser(officeAdministratorUser);
      Assert.assertTrue(eventRecorder.wait(EventConstants.OPERATION_POST_USER, officeAdministratorUser.getIdentifier()));

      identityService.api().logout();
    }

    final Authentication officeAdministratorPasswordOnlyAuthentication = identityService.api().login(officeAdministratorUser.getIdentifier(), officeAdministratorUser.getPassword());
    try (final AutoUserContext ignored = new AutoUserContext(officeAdministratorUser.getIdentifier(), officeAdministratorPasswordOnlyAuthentication.getAccessToken()))
    {
      identityService.api().changeUserPassword(officeAdministratorUser.getIdentifier(), new Password(officeAdministratorUser.getPassword()));
      Assert.assertTrue(eventRecorder.wait(EventConstants.OPERATION_PUT_USER_PASSWORD, officeAdministratorUser.getIdentifier()));
    }

    final Authentication officeAdministratorAuthentication = identityService.api().login(officeAdministratorUser.getIdentifier(), officeAdministratorUser.getPassword());

    try (final AutoUserContext ignored = new AutoUserContext(officeAdministratorUser.getIdentifier(), officeAdministratorAuthentication.getAccessToken())) {
      final Set<Permission> userPermissions = identityService.api().getUserPermissions(officeAdministratorUser.getIdentifier());
      Assert.assertTrue(userPermissions.contains(new Permission(io.mifos.office.api.v1.PermittableGroupIds.EMPLOYEE_MANAGEMENT, AllowedOperation.ALL)));
      Assert.assertTrue(userPermissions.contains(new Permission(io.mifos.office.api.v1.PermittableGroupIds.OFFICE_MANAGEMENT, AllowedOperation.ALL)));

      final Office office = new Office();
      office.setIdentifier("abydos");
      office.setName("Abydos");
      office.setDescription("First bank of the nile");
      WorkflowTenantProvisioning.officeClient.api().createOffice(office);

      Assert.assertTrue(this.eventRecorder.wait(io.mifos.office.api.v1.EventConstants.OPERATION_POST_OFFICE, office.getIdentifier()));

      employeeUser = new UserWithPassword();
      employeeUser.setIdentifier("iryhor");
      employeeUser.setPassword(encodePassword("3150BC"));
      employeeUser.setRole(employeeRole.getIdentifier());

      identityService.api().createUser(employeeUser);

      final Employee employee = new Employee();
      employee.setIdentifier(employeeUser.getIdentifier());
      employee.setGivenName("Iry");
      employee.setSurname("Hor");
      employee.setAssignedOffice("abydos");
      WorkflowTenantProvisioning.officeClient.api().createEmployee(employee);

      Assert.assertTrue(this.eventRecorder.wait(io.mifos.office.api.v1.EventConstants.OPERATION_POST_EMPLOYEE, employee.getIdentifier()));

      identityService.api().logout();
    }

    final Authentication employeePasswordOnlyAuthentication = identityService.api().login(employeeUser.getIdentifier(), employeeUser.getPassword());
    try (final AutoUserContext ignored = new AutoUserContext(employeeUser.getIdentifier(), employeePasswordOnlyAuthentication.getAccessToken()))
    {
      identityService.api().changeUserPassword(employeeUser.getIdentifier(), new Password(employeeUser.getPassword()));
      Assert.assertTrue(eventRecorder.wait(EventConstants.OPERATION_PUT_USER_PASSWORD, employeeUser.getIdentifier()));
    }

    final Authentication employeeAuthentication = identityService.api().login(employeeUser.getIdentifier(), employeeUser.getPassword());

    try (final AutoUserContext ignored = new AutoUserContext(employeeUser.getIdentifier(), employeeAuthentication.getAccessToken())) {
      final ContactDetail contactDetail = new ContactDetail();
      contactDetail.setType(ContactDetail.Type.EMAIL.toString());
      contactDetail.setValue("iryhor@ancient.eg");
      contactDetail.setGroup(ContactDetail.Group.PRIVATE.toString());
      officeClient.api().setContactDetails(employeeUser.getIdentifier(), Collections.singletonList(contactDetail));

      Assert.assertTrue(this.eventRecorder.wait(io.mifos.office.api.v1.EventConstants.OPERATION_PUT_CONTACT_DETAIL, employeeUser.getIdentifier()));

      final Employee employee = officeClient.api().findEmployee(employeeUser.getIdentifier());
      Assert.assertNotNull(employeeUser);

      Assert.assertEquals(employee.getIdentifier(), employeeUser.getIdentifier());
      Assert.assertEquals(employee.getAssignedOffice(), "abydos");
      Assert.assertEquals(employee.getGivenName(), "Iry");
      Assert.assertEquals(employee.getSurname(), "Hor");
      Assert.assertEquals(employee.getContactDetails(), Collections.singletonList(contactDetail));

      identityService.api().logout();
    }
  }

  @SuppressWarnings("unused")
  private void checkSignatureSetTimeStampsLineup() {
    //TODO: Need to put this in a *system* call context rather than a tenant call context.
    final ApplicationSignatureSet latestSignatureSet = identityService.api().getLatestSignatureSet();
    identityService.api().getApplicationSignature(officeClient.name(), latestSignatureSet.getTimestamp());
  }

  private void checkCreationOfPermittableGroupsInIsis() throws InterruptedException {
    Assert.assertTrue(this.eventRecorder.wait(EventConstants.OPERATION_POST_PERMITTABLE_GROUP, io.mifos.office.api.v1.PermittableGroupIds.EMPLOYEE_MANAGEMENT));
    Assert.assertTrue(this.eventRecorder.wait(EventConstants.OPERATION_POST_PERMITTABLE_GROUP, io.mifos.office.api.v1.PermittableGroupIds.OFFICE_MANAGEMENT));
    Assert.assertTrue(this.eventRecorder.wait(EventConstants.OPERATION_POST_PERMITTABLE_GROUP, io.mifos.office.api.v1.PermittableGroupIds.SELF_MANAGEMENT));

    identityService.api().getPermittableGroup(io.mifos.identity.api.v1.PermittableGroupIds.ROLE_MANAGEMENT);
    identityService.api().getPermittableGroup(io.mifos.identity.api.v1.PermittableGroupIds.IDENTITY_MANAGEMENT);
    identityService.api().getPermittableGroup(io.mifos.identity.api.v1.PermittableGroupIds.SELF_MANAGEMENT);
    identityService.api().getPermittableGroup(io.mifos.office.api.v1.PermittableGroupIds.EMPLOYEE_MANAGEMENT);
    identityService.api().getPermittableGroup(io.mifos.office.api.v1.PermittableGroupIds.OFFICE_MANAGEMENT);
    identityService.api().getPermittableGroup(io.mifos.office.api.v1.PermittableGroupIds.SELF_MANAGEMENT);
  }

  private String provisionAppsViaSeshat() throws InterruptedException {
    final AuthenticationResponse authenticationResponse
            = provisionerService.api().authenticate(CLIENT_ID, ApiConstants.SYSTEM_SU, "oS/0IiAME/2unkN1momDrhAdNKOhGykYFH/mJN20");

    try (final AutoSeshat ignored = new AutoSeshat(authenticationResponse.getToken())) {
      final Tenant tenant = makeTenant();

      provisionerService.api().createTenant(tenant);

      final Application isisApp = new Application();
      isisApp.setName(identityService.name());
      isisApp.setHomepage(identityService.uri());
      isisApp.setDescription("identity manager");
      isisApp.setVendor("fineract");

      provisionerService.api().createApplication(isisApp);

      final AssignedApplication isisAssigned = new AssignedApplication();
      isisAssigned.setName(identityService.name());

      final IdentityManagerInitialization isisAdminPassword
              = provisionerService.api().assignIdentityManager(tenant.getIdentifier(), isisAssigned);

      final Application horusApp = new Application();
      horusApp.setName(officeClient.name());
      horusApp.setHomepage(officeClient.uri());
      horusApp.setDescription("organization manager");
      horusApp.setVendor("fineract");

      provisionerService.api().createApplication(horusApp);

      final AssignedApplication horusAssigned = new AssignedApplication();
      horusAssigned.setName(officeClient.name());

      provisionerService.api().assignApplications(tenant.getIdentifier(), Collections.singletonList(horusAssigned));

      Assert.assertTrue(this.eventRecorder.wait(io.mifos.office.api.v1.EventConstants.INITIALIZE, io.mifos.office.api.v1.EventConstants.INITIALIZE));

      return isisAdminPassword.getAdminPassword();
    }
  }

  private static String encodePassword(final String password) {
    return Base64Utils.encodeToString(password.getBytes());
  }

  private Tenant makeTenant() {
    final Tenant tenant = new Tenant();
    tenant.setName("dudette");
    tenant.setIdentifier(TenantContextHolder.checkedGetIdentifier());
    tenant.setDescription("oogie boogie woman");

    final CassandraConnectionInfo cassandraConnectionInfo = new CassandraConnectionInfo();
    cassandraConnectionInfo.setClusterName("Test Cluster");
    cassandraConnectionInfo.setContactPoints("127.0.0.1:9142");
    cassandraConnectionInfo.setKeyspace("comp_test");
    cassandraConnectionInfo.setReplicas("3");
    cassandraConnectionInfo.setReplicationType("Simple");
    tenant.setCassandraConnectionInfo(cassandraConnectionInfo);

    final DatabaseConnectionInfo databaseConnectionInfo = new DatabaseConnectionInfo();
    databaseConnectionInfo.setDriverClass("org.mariadb.jdbc.Driver");
    databaseConnectionInfo.setDatabaseName("comp_test");
    databaseConnectionInfo.setHost("localhost");
    databaseConnectionInfo.setPort("3306");
    databaseConnectionInfo.setUser("root");
    databaseConnectionInfo.setPassword("mysql");
    tenant.setDatabaseConnectionInfo(databaseConnectionInfo);
    return tenant;
  }

  private Role makeOfficeAdministratorRole() {
    final Permission employeeCreationPermision = new Permission();
    employeeCreationPermision.setAllowedOperations(AllowedOperation.ALL);
    employeeCreationPermision.setPermittableEndpointGroupIdentifier(io.mifos.office.api.v1.PermittableGroupIds.EMPLOYEE_MANAGEMENT);

    final Permission officeCreationPermision = new Permission();
    officeCreationPermision.setAllowedOperations(AllowedOperation.ALL);
    officeCreationPermision.setPermittableEndpointGroupIdentifier(io.mifos.office.api.v1.PermittableGroupIds.OFFICE_MANAGEMENT);

    final Permission userCreationPermission = new Permission();
    userCreationPermission.setAllowedOperations(Collections.singleton(AllowedOperation.CHANGE));
    userCreationPermission.setPermittableEndpointGroupIdentifier(io.mifos.identity.api.v1.PermittableGroupIds.IDENTITY_MANAGEMENT);

    final Role role = new Role();
    role.setIdentifier("office_administrator");
    role.setPermissions(Arrays.asList(employeeCreationPermision, officeCreationPermision, userCreationPermission));

    return role;
  }

  private Role makeEmployeeRole() {
    final Set<AllowedOperation> noDeleteOperation = new HashSet<>();
    noDeleteOperation.add(AllowedOperation.CHANGE);
    noDeleteOperation.add(AllowedOperation.READ);

    final Permission employeeSelfPermission = new Permission();
    employeeSelfPermission.setAllowedOperations(noDeleteOperation);
    employeeSelfPermission.setPermittableEndpointGroupIdentifier(io.mifos.office.api.v1.PermittableGroupIds.SELF_MANAGEMENT);

    final Role role = new Role();
    role.setIdentifier("employee");
    role.setPermissions(Collections.singletonList(employeeSelfPermission));
    return role;
  }
}
