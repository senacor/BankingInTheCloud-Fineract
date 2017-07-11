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
package io.mifos.rhythm.service.internal.repository;

import javax.persistence.*;
import java.util.Objects;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Entity
@Table(name = "khepri_apps")
public class ApplicationEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "tenant_identifier", nullable = false)
  private String tenantIdentifier;

  @Column(name = "application_identifier", nullable = false)
  private String applicationIdentifier;

  @Column(name = "permittable_identifier")
  private String consumerPermittableGroupIdentifier;

  public ApplicationEntity() {
  }

  public ApplicationEntity(String tenantIdentifier, String applicationIdentifier, String consumerPermittableGroupIdentifier) {
    this.tenantIdentifier = tenantIdentifier;
    this.applicationIdentifier = applicationIdentifier;
    this.consumerPermittableGroupIdentifier = consumerPermittableGroupIdentifier;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTenantIdentifier() {
    return tenantIdentifier;
  }

  public void setTenantIdentifier(String tenantIdentifier) {
    this.tenantIdentifier = tenantIdentifier;
  }

  public String getApplicationIdentifier() {
    return applicationIdentifier;
  }

  public void setApplicationIdentifier(String applicationIdentifier) {
    this.applicationIdentifier = applicationIdentifier;
  }

  public String getConsumerPermittableGroupIdentifier() {
    return consumerPermittableGroupIdentifier;
  }

  public void setConsumerPermittableGroupIdentifier(String consumerPermittableGroupIdentifier) {
    this.consumerPermittableGroupIdentifier = consumerPermittableGroupIdentifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ApplicationEntity that = (ApplicationEntity) o;
    return Objects.equals(tenantIdentifier, that.tenantIdentifier) &&
            Objects.equals(applicationIdentifier, that.applicationIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tenantIdentifier, applicationIdentifier);
  }

  @Override
  public String toString() {
    return "ApplicationEntity{" +
            "id=" + id +
            ", tenantIdentifier='" + tenantIdentifier + '\'' +
            ", applicationIdentifier='" + applicationIdentifier + '\'' +
            ", consumerPermittableGroupIdentifier='" + consumerPermittableGroupIdentifier + '\'' +
            '}';
  }
}
