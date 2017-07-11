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
package io.mifos.permittedfeignclient.service;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Myrle Krantz
 */
class TokenCacheKey {
  private final String user;
  private final String tenant;
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private final Optional<String> endpointSet;

  TokenCacheKey(final String user, final String tenant, final @Nullable String endpointSet) {
    this.user = user;
    this.tenant = tenant;
    this.endpointSet = Optional.ofNullable(endpointSet);
  }

  String getUser() {
    return user;
  }

  String getTenant() {
    return tenant;
  }

  Optional<String> getEndpointSet() {
    return endpointSet;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TokenCacheKey that = (TokenCacheKey) o;
    return Objects.equals(user, that.user) &&
            Objects.equals(tenant, that.tenant) &&
            Objects.equals(endpointSet, that.endpointSet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, tenant, endpointSet);
  }

  @Override
  public String toString() {
    return "TokenCacheKey{" +
            "user='" + user + '\'' +
            ", tenant='" + tenant + '\'' +
            ", endpointSet='" + endpointSet + '\'' +
            '}';
  }
}
