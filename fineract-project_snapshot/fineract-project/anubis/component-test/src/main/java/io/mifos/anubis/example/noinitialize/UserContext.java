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
package io.mifos.anubis.example.noinitialize;

import java.util.Objects;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("WeakerAccess")
public class UserContext {
  private String userIdentifier;
  private String token;

  public UserContext() {
  }

  public UserContext(
      final String userIdentifier,
      final String token) {
    this.userIdentifier = userIdentifier;
    this.token = token;
  }

  public String getUserIdentifier() {
    return userIdentifier;
  }

  public void setUserIdentifier(String userIdentifier) {
    this.userIdentifier = userIdentifier;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof UserContext))
      return false;
    UserContext that = (UserContext) o;
    return Objects.equals(userIdentifier, that.userIdentifier) && Objects.equals(token, that.token);
  }

  @Override public int hashCode() {
    return Objects.hash(userIdentifier, token);
  }

  @Override public String toString() {
    return "UserContext{" +
        "userIdentifier='" + userIdentifier + '\'' +
        ", token='" + token + '\'' +
        '}';
  }
}
