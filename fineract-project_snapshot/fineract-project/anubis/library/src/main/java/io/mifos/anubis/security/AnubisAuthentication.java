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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Myrle Krantz
 */
class AnubisAuthentication implements Authentication {

  private boolean authenticated;

  private final String token;
  private final String userIdentifier;
  private final String forApplicationName;
  private final String sourceApplicationName;
  private final Set<ApplicationPermission> applicationPermissions;

  AnubisAuthentication(final String token,
                       final String userIdentifier,
                       final String forApplicationName,
                       final String sourceApplicationName,
                       final Set<ApplicationPermission> applicationPermissions) {
    authenticated = true;

    this.token = token;
    this.userIdentifier = userIdentifier;
    this.forApplicationName = forApplicationName;
    this.sourceApplicationName = sourceApplicationName;
    this.applicationPermissions = Collections.unmodifiableSet(new HashSet<>(applicationPermissions));
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return applicationPermissions;
  }

  @Override
  public Object getCredentials() {
    return token;
  }

  @Override
  public String getDetails() {
    return null;
  }

  @Override
  public AnubisPrincipal getPrincipal() {
    return new AnubisPrincipal(userIdentifier, forApplicationName, sourceApplicationName);
  }

  @Override
  public boolean isAuthenticated() {
    return this.authenticated;
  }

  @Override
  public void setAuthenticated(final boolean authenticated) throws IllegalArgumentException {
    this.authenticated = authenticated;
  }

  @Override
  public String getName() {
    return userIdentifier;
  }
}
