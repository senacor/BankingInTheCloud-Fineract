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
package io.mifos.anubis.filter;

import io.mifos.anubis.api.v1.RoleConstants;
import io.mifos.anubis.api.v1.TokenConstants;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.http.HttpServletRequest;

import java.util.Optional;

import static io.mifos.core.api.util.ApiConstants.AUTHORIZATION_HEADER;
import static io.mifos.core.api.util.ApiConstants.USER_HEADER;

/**
 * @author Myrle Krantz
 */
public class IsisAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

  public IsisAuthenticatedProcessingFilter(final AuthenticationManager authenticationManager) {
    setAuthenticationManager(authenticationManager);
    setCheckForPrincipalChanges(true);
  }

  @Override protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(USER_HEADER)).orElse(RoleConstants.GUEST_USER_IDENTIFIER);
  }

  @Override protected Object getPreAuthenticatedCredentials(final HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER)).orElse(TokenConstants.NO_AUTHENTICATION);
  }
}
