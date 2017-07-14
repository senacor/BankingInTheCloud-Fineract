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

import io.mifos.core.test.env.TestEnvironment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection", "WeakerAccess"})
class MariaDBTenant {

  private static final int INDEX_IDENTIFIER = 1;
  private static final int INDEX_DRIVER_CLASS = 2;
  private static final int INDEX_DATABASE_NAME = 3;
  private static final int INDEX_HOST = 4;
  private static final int INDEX_PORT = 5;
  private static final int INDEX_USER = 6;
  private static final int INDEX_PASSWORD = 7;

  private static final String TABLE_NAME = System.getProperty(TestEnvironment.MARIADB_DATABASE_NAME_PROPERTY) + ".tenants";
  private static final String INSERT_STMT = " INSERT INTO " +
      MariaDBTenant.TABLE_NAME +
      " (identifier, driver_class, database_name, host, port, a_user, pwd) " +
      " values " +
      " (?, ?, ?, ?, ?, ?, ?) ";

  private String identifier;
  private String driverClass;
  private String databaseName;
  private String host;
  private String port;
  private String user;
  private String password;

  MariaDBTenant() {
    super();
  }

  void insert(final Connection connection) throws SQLException {
    try (final PreparedStatement insertTenantStatement = connection.prepareStatement(MariaDBTenant.INSERT_STMT)) {
      insertTenantStatement.setString(INDEX_IDENTIFIER, this.getIdentifier());
      insertTenantStatement.setString(INDEX_DRIVER_CLASS, this.getDriverClass());
      insertTenantStatement.setString(INDEX_DATABASE_NAME, this.getDatabaseName());
      insertTenantStatement.setString(INDEX_HOST, this.getHost());
      insertTenantStatement.setString(INDEX_PORT, this.getPort());
      insertTenantStatement.setString(INDEX_USER, this.getUser());
      insertTenantStatement.setString(INDEX_PASSWORD, this.getPassword());
      insertTenantStatement.execute();
    }
  }

  String getIdentifier() {
    return identifier;
  }

  void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  String getDriverClass() {
    return driverClass;
  }

  void setDriverClass(String driverClass) {
    this.driverClass = driverClass;
  }

  String getDatabaseName() {
    return databaseName;
  }

  void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

  String getHost() {
    return host;
  }

  void setHost(String host) {
    this.host = host;
  }

  String getPort() {
    return port;
  }

  void setPort(String port) {
    this.port = port;
  }

  String getUser() {
    return user;
  }

  void setUser(String user) {
    this.user = user;
  }

  String getPassword() {
    return password;
  }

  void setPassword(String password) {
    this.password = password;
  }
}
