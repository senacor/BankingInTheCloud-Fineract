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
package io.mifos.anubis.security;

import java.util.Objects;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
public class AnubisPrincipal {
  private final String user;
  private final String forApplicationName;
  private final String sourceApplicationName;

  AnubisPrincipal(String user, String forApplicationName, String sourceApplicationName) {
    this.user = user;
    this.forApplicationName = forApplicationName;
    this.sourceApplicationName = sourceApplicationName;
  }

  public String getUser() {
    return user;
  }

  public String getForApplicationName() {
    return forApplicationName;
  }

  public String getSourceApplicationName() {
    return sourceApplicationName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AnubisPrincipal that = (AnubisPrincipal) o;
    return Objects.equals(user, that.user) &&
            Objects.equals(forApplicationName, that.forApplicationName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, forApplicationName);
  }

  @Override
  public String toString() {
    return "AnubisPrincipal{" +
            "user='" + user + '\'' +
            ", forApplicationName='" + forApplicationName + '\'' +
            '}';
  }
}
