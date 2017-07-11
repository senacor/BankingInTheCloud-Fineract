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
package io.mifos.anubis.service;

import io.mifos.anubis.security.AnubisPrincipal;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Myrle Krantz
 */
public class PermissionSegmentMatcher {
  final private String permissionSegment;

  private PermissionSegmentMatcher(final String permissionSegment) {
    this.permissionSegment = permissionSegment;
  }

  public boolean isStarSegment() {
    return permissionSegment.equals("*");
  }

  private boolean isUserIdentifierSegment() {
    return permissionSegment.equals("{useridentifier}");
  }

  private boolean isApplicationIdentifierSegment() {
    return permissionSegment.equals("{applicationidentifier}");
  }

  boolean isParameterSegment() {
    return permissionSegment.startsWith("{") && permissionSegment.endsWith("}");
  }

  public String getPermissionSegment() { return permissionSegment; }

  public boolean matches(
          final String requestSegment,
          final AnubisPrincipal principal,
          boolean acceptTokenIntendedForForeignApplication,
          boolean isSu) {
    if (isStarSegment())
      return true;
    else if (isUserIdentifierSegment())
      return requestSegment.equals(principal.getUser());
    else if (acceptTokenIntendedForForeignApplication && isApplicationIdentifierSegment())
      return requestSegment.equals(principal.getForApplicationName());
    else if (!acceptTokenIntendedForForeignApplication && isApplicationIdentifierSegment())
      return requestSegment.equals(principal.getSourceApplicationName());
    else if (isParameterSegment())
      return isSu;
    else
      return permissionSegment.equals(requestSegment);
  }

  static public List<PermissionSegmentMatcher> getServletPathSegmentMatchers(final @Nonnull String servletPath) {
    return Arrays.stream(servletPath.split("/"))
            .map(PermissionSegmentMatcher::new)
            .collect(Collectors.toList());
  }
}
