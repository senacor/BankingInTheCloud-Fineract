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
package io.mifos.core.api.util;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Myrle Krantz
 */
public class UserContext {
  private final String user;
  private final String accessToken;

  UserContext(@Nonnull String user, @Nonnull String accessToken) {
    this.user = user;
    this.accessToken = accessToken;
  }

  @SuppressWarnings("WeakerAccess")
  @Nonnull public String getUser() {
    return user;
  }

  @SuppressWarnings("WeakerAccess")
  @Nonnull public String getAccessToken() {
    return accessToken;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserContext that = (UserContext) o;
    return Objects.equals(user, that.user) &&
            Objects.equals(accessToken, that.accessToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, accessToken);
  }
}
