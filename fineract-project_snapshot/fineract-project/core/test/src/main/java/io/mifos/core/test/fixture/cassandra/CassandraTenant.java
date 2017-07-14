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
package io.mifos.core.test.fixture.cassandra;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@SuppressWarnings({"WeakerAccess", "unused"})
@Table(name = "tenants")
public class CassandraTenant {

  @PartitionKey
  @Column(name = "identifier")
  private String identifier;
  @Column(name = "cluster_name")
  private String clusterName;
  @Column(name = "contact_points")
  private String contactPoints;
  @Column(name = "keyspace_name")
  private String keyspaceName;
  @Column(name = "replication_type")
  private String replicationType;
  @Column(name = "replicas")
  private String replicas;
  @Column(name = "name")
  private String name;
  @Column(name = "description")
  private String description;

  public CassandraTenant() {
    super();
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public String getContactPoints() {
    return contactPoints;
  }

  public void setContactPoints(String contactPoints) {
    this.contactPoints = contactPoints;
  }

  public String getKeyspaceName() {
    return keyspaceName;
  }

  public void setKeyspaceName(String keyspaceName) {
    this.keyspaceName = keyspaceName;
  }

  public String getReplicationType() {
    return replicationType;
  }

  public void setReplicationType(String replicationType) {
    this.replicationType = replicationType;
  }

  public String getReplicas() {
    return replicas;
  }

  public void setReplicas(String replicas) {
    this.replicas = replicas;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CassandraTenant that = (CassandraTenant) o;

    return identifier.equals(that.identifier);
  }

  @Override
  public int hashCode() {
    return identifier.hashCode();
  }
}
