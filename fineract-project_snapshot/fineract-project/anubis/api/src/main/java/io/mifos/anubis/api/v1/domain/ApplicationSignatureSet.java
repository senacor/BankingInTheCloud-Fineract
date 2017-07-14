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
package io.mifos.anubis.api.v1.domain;

import io.mifos.anubis.api.v1.validation.ValidKeyTimestamp;

import javax.validation.Valid;
import java.util.Objects;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ApplicationSignatureSet {
  @ValidKeyTimestamp
  private String timestamp;
  @Valid
  private Signature applicationSignature;
  @Valid
  private Signature identityManagerSignature;

  public ApplicationSignatureSet() {
  }

  public ApplicationSignatureSet(String timestamp, Signature applicationSignature, Signature identityManagerSignature) {
    this.timestamp = timestamp;
    this.applicationSignature = applicationSignature;
    this.identityManagerSignature = identityManagerSignature;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public Signature getApplicationSignature() {
    return applicationSignature;
  }

  public void setApplicationSignature(Signature applicationSignature) {
    this.applicationSignature = applicationSignature;
  }

  public Signature getIdentityManagerSignature() {
    return identityManagerSignature;
  }

  public void setIdentityManagerSignature(Signature identityManagerSignature) {
    this.identityManagerSignature = identityManagerSignature;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ApplicationSignatureSet that = (ApplicationSignatureSet) o;
    return Objects.equals(timestamp, that.timestamp) &&
            Objects.equals(applicationSignature, that.applicationSignature) &&
            Objects.equals(identityManagerSignature, that.identityManagerSignature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, applicationSignature, identityManagerSignature);
  }

  @Override
  public String toString() {
    return "ApplicationSignatureSet{" +
            "timestamp='" + timestamp + '\'' +
            ", applicationSignature=" + applicationSignature +
            ", identityManagerSignature=" + identityManagerSignature +
            '}';
  }
}
