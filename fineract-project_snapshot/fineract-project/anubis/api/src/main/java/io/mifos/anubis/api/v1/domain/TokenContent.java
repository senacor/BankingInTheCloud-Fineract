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

import java.util.List;
import java.util.Objects;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class TokenContent {

  private List<TokenPermission> tokenPermissions;

  public TokenContent() {
  }

  public TokenContent(List<TokenPermission> tokenPermissions) {
    this.tokenPermissions = tokenPermissions;
  }

  public List<TokenPermission> getTokenPermissions() {
    return tokenPermissions;
  }

  public void setTokenPermissions(List<TokenPermission> tokenPermissions) {
    this.tokenPermissions = tokenPermissions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TokenContent that = (TokenContent) o;
    return Objects.equals(tokenPermissions, that.tokenPermissions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tokenPermissions);
  }

  @Override
  public String toString() {
    return "TokenContent{" +
            "tokenPermissions=" + tokenPermissions +
            '}';
  }
}
