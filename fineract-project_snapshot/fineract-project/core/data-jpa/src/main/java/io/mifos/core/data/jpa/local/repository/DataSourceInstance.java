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
package io.mifos.core.data.jpa.local.repository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "data_source_instances")
public class DataSourceInstance {

  @Id
  @Column(name = "identifier")
  private String identifier;
  @Column(name = "driver_class")
  private String driverClass;
  @Column(name = "jdbc_url")
  private String jdbcUrl;
  @Column(name = "username")
  private String username;
  @Column(name = "password")
  private String password;

  public DataSourceInstance() {
    super();
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public void setIdentifier(final String identifier) {
    this.identifier = identifier;
  }

  public String getDriverClass() {
    return this.driverClass;
  }

  public void setDriverClass(final String driverClass) {
    this.driverClass = driverClass;
  }

  public String getJdbcUrl() {
    return this.jdbcUrl;
  }

  public void setJdbcUrl(final String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }
}
