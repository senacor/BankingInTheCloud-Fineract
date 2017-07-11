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
package io.mifos.core.test.servicestarter;

import io.mifos.anubis.test.v1.SystemSecurityEnvironment;
import io.mifos.core.api.context.AutoUserContext;
import io.mifos.core.lang.security.RsaKeyPairFactory;
import io.mifos.core.test.fixture.DataStoreTenantInitializer;
import io.mifos.core.test.fixture.TenantDataStoreTestContext;
import org.junit.rules.ExternalResource;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

import static io.mifos.core.test.env.TestEnvironment.*;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("WeakerAccess")
public class IntegrationTestEnvironment extends ExternalResource {



  static String getJava()
  {
    final String javaHome = System.getProperty("java.home");
    return javaHome + File.separator + "bin" + File.separator + "java";
  }

  static String getArtifactoryDirectory()
  {
    final String homeDirectory = System.getProperty("user.home");
    return homeDirectory + File.separator + ".m2" + File.separator + "repository";
  }

  private final String tenantName;
  private int nextPort;
  private final Set<Integer> ports;
  private final RsaKeyPairFactory.KeyPairHolder keyPairHolder;
  private final SystemSecurityEnvironment systemSecurityEnvironment;
  private final DataStoreTenantInitializer[] dataStoreTenantInitializers;
  private final List<String> applicationNames;
  private TenantDataStoreTestContext tenantDataStoreTestContext;

  @SuppressWarnings("unused")
  public IntegrationTestEnvironment(final DataStoreTenantInitializer... dataStoreTenantInitializers) {
    this(null, dataStoreTenantInitializers);
  }

  public IntegrationTestEnvironment(final String tenantName, final DataStoreTenantInitializer... dataStoreTenantInitializers) {
    this.tenantName = tenantName;
    final Properties properties = System.getProperties();
    properties.setProperty(CASSANDRA_CLUSTER_NAME_PROPERTY, CASSANDRA_CLUSTER_NAME_DEFAULT);
    properties.setProperty(CASSANDRA_CONTACT_POINTS_PROPERTY, CASSANDRA_CONTACT_POINTS_DEFAULT);
    properties.setProperty(CASSANDRA_META_KEYSPACE_PROPERTY, CASSANDRA_META_KEYSPACE_DEFAULT);
    properties.setProperty(CASSANDRA_CONSISTENCY_LEVEL_READ_PROPERTY, CASSANDRA_CONSISTENCY_LEVEL_DEFAULT);
    properties.setProperty(CASSANDRA_CONSISTENCY_LEVEL_WRITE_PROPERTY, CASSANDRA_CONSISTENCY_LEVEL_DEFAULT);
    properties.setProperty(CASSANDRA_CONSISTENCY_LEVEL_DELETE_PROPERTY, CASSANDRA_CONSISTENCY_LEVEL_DEFAULT);
    properties.setProperty(MARIADB_DRIVER_CLASS_PROPERTY, MARIADB_DRIVER_CLASS_DEFAULT);
    properties.setProperty(MARIADB_DATABASE_NAME_PROPERTY, MARIADB_DATABASE_NAME_DEFAULT);
    properties.setProperty(MARIADB_HOST_PROPERTY, MARIADB_HOST_DEFAULT);
    properties.setProperty(MARIADB_PORT_PROPERTY, MARIADB_PORT_DEFAULT);
    properties.setProperty(MARIADB_USER_PROPERTY, MARIADB_USER_DEFAULT);
    properties.setProperty(MARIADB_PASSWORD_PROPERTY, MARIADB_PASSWORD_DEFAULT);
    properties.setProperty(HYSTRIX_ENABLED_PROPERTY, HYSTRIX_ENABLED_DEFAULT);
    properties.setProperty(RIBBON_USES_EUREKA_PROPERTY, "true");
    this.keyPairHolder = RsaKeyPairFactory.createKeyPair();
    properties.setProperty(SYSTEM_PUBLIC_KEY_TIMESTAMP_PROPERTY, this.keyPairHolder.getTimestamp());
    properties.setProperty(SYSTEM_PUBLIC_KEY_MODULUS_PROPERTY, this.keyPairHolder.publicKey().getModulus().toString());
    properties.setProperty(SYSTEM_PUBLIC_KEY_EXPONENT_PROPERTY, this.keyPairHolder.publicKey().getPublicExponent().toString());

    this.systemSecurityEnvironment = new SystemSecurityEnvironment(keyPairHolder.getTimestamp(), keyPairHolder.publicKey(), keyPairHolder.privateKey());
    this.dataStoreTenantInitializers = dataStoreTenantInitializers;

    nextPort = 2020;
    this.ports = new HashSet<>();
    //Prevent the following ports from being allocated to Microservices.
    this.ports.add(0);
    this.ports.add(3306); //MySQL
    this.ports.add(9142); //Cassandra
    this.ports.add(ActiveMQForTest.PORT);
    this.ports.add(EurekaForTest.PORT);

    this.applicationNames = new ArrayList<>();
  }

  @Override
  protected void before() {
    if (tenantName == null)
      tenantDataStoreTestContext = TenantDataStoreTestContext.forRandomTenantName(dataStoreTenantInitializers);
    else
      tenantDataStoreTestContext = TenantDataStoreTestContext.forDefinedTenantName(tenantName, dataStoreTenantInitializers);
  }

  @Override
  protected void after() {
    tenantDataStoreTestContext.close();
  }

  Integer getFreshPort() {
    while (ports.contains(nextPort) || !available(nextPort))
    {
      nextPort += 1;
    }

    ports.add(nextPort);
    return nextPort;
  }

  private static boolean available(int port) {
    try (final Socket ignored = new Socket("localhost", port)) {
      return false;
    } catch (final IOException ignored) {
      return true;
    }
  }

  public String getSeshatKeyTimestamp() {
    return this.keyPairHolder.getTimestamp();
  }

  public RSAPublicKey getSeshatPublicKey() {
    return this.keyPairHolder.publicKey();
  }

  public RSAPrivateKey getSeshatPrivateKey() {
    return this.keyPairHolder.privateKey();
  }

  public SystemSecurityEnvironment getSystemSecurityEnvironment() {
    return systemSecurityEnvironment;
  }

  void addApplication(final String applicationName) {
    applicationNames.add(applicationName);
  }

  @SuppressWarnings("unused")
  public AutoUserContext createAutoUserContext(final String userName) {
    return systemSecurityEnvironment.createAutoUserContext(userName, applicationNames);
  }
}
