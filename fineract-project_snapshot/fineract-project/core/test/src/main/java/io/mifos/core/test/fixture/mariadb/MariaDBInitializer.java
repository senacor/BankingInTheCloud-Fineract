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
package io.mifos.core.test.fixture.mariadb;

import ch.vorburger.mariadb4j.DB;
import io.mifos.core.mariadb.util.JdbcUrlBuilder;
import io.mifos.core.test.env.TestEnvironment;
import io.mifos.core.test.fixture.DataStoreTenantInitializer;

import java.sql.*;

@SuppressWarnings({"WeakerAccess", "unused", "SqlNoDataSourceInspection", "SqlDialectInspection"})
public final class MariaDBInitializer extends DataStoreTenantInitializer {

  private final boolean useExistingDB;
  private static DB db;

  public MariaDBInitializer() {
    this(false);
  }

  public MariaDBInitializer(final boolean useExistingDB) {
    super();
    this.useExistingDB = useExistingDB;
  }

  @Override
  public void initialize() throws Exception  {
    MariaDBInitializer.setup(useExistingDB);
  }

  @Override
  public void initializeTenant(final String tenantName) {
    MariaDBInitializer.createDatabaseTenant(tenantName);

  }

  @Override
  public void finish() {
    if (!useExistingDB) {
      try {
        MariaDBInitializer.tearDown();
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static void setup() throws Exception {
    setup(false);
  }
  public static void setup(final boolean useExistingDB) throws Exception {
    if (!useExistingDB) {
      MariaDBInitializer.startEmbeddedMariaDB();
      MariaDBInitializer.createDatabaseSeshat();
    }
  }

  public static void tearDown() throws Exception {
    if (MariaDBInitializer.db != null) {
      MariaDBInitializer.db.stop();
      MariaDBInitializer.db = null;
    }
  }

  private static void startEmbeddedMariaDB() throws Exception {
    if (MariaDBInitializer.db == null) {
      MariaDBInitializer.db = DB.newEmbeddedDB(3306);
      MariaDBInitializer.db.start();
    }
  }

  private static void createDatabaseSeshat() {
    try {
      Class.forName(System.getProperty(TestEnvironment.MARIADB_DRIVER_CLASS_PROPERTY));
    } catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException(ex.getMessage(), ex);
    }
    final String jdbcUrl = JdbcUrlBuilder
        .create(JdbcUrlBuilder.DatabaseType.MARIADB)
        .host(System.getProperty(TestEnvironment.MARIADB_HOST_PROPERTY))
        .port(System.getProperty(TestEnvironment.MARIADB_PORT_PROPERTY))
        .build();
    try (final Connection connection = DriverManager.getConnection(jdbcUrl,
        System.getProperty(TestEnvironment.MARIADB_USER_PROPERTY),
        System.getProperty(TestEnvironment.MARIADB_PASSWORD_PROPERTY))) {
      try (final Statement statement = connection.createStatement()) {
        // create meta database seshat
        statement.execute("CREATE DATABASE IF NOT EXISTS " + System.getProperty(TestEnvironment.MARIADB_DATABASE_NAME_PROPERTY));
        statement.execute("USE " + System.getProperty(TestEnvironment.MARIADB_DATABASE_NAME_PROPERTY));
        // create needed tenant management table
        statement.execute("CREATE TABLE IF NOT EXISTS tenants (" +
            "  identifier    VARCHAR(32) NOT NULL," +
            "  driver_class  VARCHAR(255) NOT NULL," +
            "  database_name VARCHAR(32) NOT NULL," +
            "  host          VARCHAR(32) NOT NULL," +
            "  port          VARCHAR(5)  NOT NULL," +
            "  a_user        VARCHAR(32) NOT NULL," +
            "  pwd           VARCHAR(32) NOT NULL," +
            "  PRIMARY KEY (identifier)" +
            ")");
      }
      connection.commit();
    } catch (final SQLException ex) {
      ex.printStackTrace();
    }
  }

  public static void createDatabaseTenant(final String identifier) {
    try {
      Class.forName(System.getProperty(TestEnvironment.MARIADB_DRIVER_CLASS_PROPERTY));
    } catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException(ex.getMessage(), ex);
    }
    final String jdbcUrl = JdbcUrlBuilder
        .create(JdbcUrlBuilder.DatabaseType.MARIADB)
        .host(System.getProperty(TestEnvironment.MARIADB_HOST_PROPERTY))
        .port(System.getProperty(TestEnvironment.MARIADB_PORT_PROPERTY))
        .build();
    try (final Connection connection = DriverManager.getConnection(jdbcUrl,
        System.getProperty(TestEnvironment.MARIADB_USER_PROPERTY),
        System.getProperty(TestEnvironment.MARIADB_PASSWORD_PROPERTY))) {
      try (final Statement statement = connection.createStatement()) {
        // create tenant database
        statement.execute("CREATE DATABASE IF NOT EXISTS " + identifier);
        // insert tenant connection info in management table
        try (final ResultSet resultSet = statement.executeQuery(
            "SELECT EXISTS (SELECT * FROM " +
                System.getProperty(TestEnvironment.MARIADB_DATABASE_NAME_PROPERTY) +
                ".tenants WHERE identifier = '" + identifier + "')")) {
          if (resultSet.next()
              && resultSet.getInt(1) == 0) {
            final MariaDBTenant mariaDBTenant = new MariaDBTenant();
            mariaDBTenant.setIdentifier(identifier);
            mariaDBTenant.setDriverClass(System.getProperty(TestEnvironment.MARIADB_DRIVER_CLASS_PROPERTY));
            mariaDBTenant.setDatabaseName(identifier);
            mariaDBTenant.setHost(System.getProperty(TestEnvironment.MARIADB_HOST_PROPERTY));
            mariaDBTenant.setPort(System.getProperty(TestEnvironment.MARIADB_PORT_PROPERTY));
            mariaDBTenant.setUser(System.getProperty(TestEnvironment.MARIADB_USER_PROPERTY));
            mariaDBTenant.setPassword(System.getProperty(TestEnvironment.MARIADB_PASSWORD_PROPERTY));
            mariaDBTenant.insert(connection);
          }
        }
      }
      connection.commit();
    } catch (final SQLException ex) {
      ex.printStackTrace();
    }
  }
}
