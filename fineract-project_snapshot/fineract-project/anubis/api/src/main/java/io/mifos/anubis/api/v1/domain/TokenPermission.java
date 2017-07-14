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

import java.util.Objects;
import java.util.Set;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class TokenPermission {
  private String path;
  private Set<AllowedOperation> allowedOperations;

  public TokenPermission() {
  }

  public TokenPermission(String path, Set<AllowedOperation> allowedOperations) {
    this.path = path;
    this.allowedOperations = allowedOperations;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Set<AllowedOperation> getAllowedOperations() {
    return allowedOperations;
  }

  public void setAllowedOperations(Set<AllowedOperation> allowedOperations) {
    this.allowedOperations = allowedOperations;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TokenPermission that = (TokenPermission) o;
    return Objects.equals(path, that.path) &&
            Objects.equals(allowedOperations, that.allowedOperations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, allowedOperations);
  }

  @Override
  public String toString() {
    return "TokenPermission{" +
            "path='" + path + '\'' +
            ", allowedOperations=" + allowedOperations +
            '}';
  }
}