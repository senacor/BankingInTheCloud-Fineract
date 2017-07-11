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

import io.jsonwebtoken.*;
import io.mifos.anubis.annotation.AcceptedTokenType;
import io.mifos.anubis.api.v1.TokenConstants;
import io.mifos.anubis.provider.InvalidKeyTimestampException;
import io.mifos.anubis.provider.SystemRsaKeyProvider;
import io.mifos.anubis.service.PermittableService;
import io.mifos.anubis.token.TokenType;
import io.mifos.core.api.util.ApiConstants;
import io.mifos.core.lang.TenantContextHolder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Set;

import static io.mifos.anubis.config.AnubisConstants.LOGGER_NAME;

/**
 * @author Myrle Krantz
 */
@Component
public class SystemAuthenticator {
  private final SystemRsaKeyProvider systemRsaKeyProvider;
  private final Set<ApplicationPermission> permissions;
  private final Logger logger;

  @Autowired
  public SystemAuthenticator(
          final SystemRsaKeyProvider systemRsaKeyProvider,
          final PermittableService permittableService,
          final @Qualifier(LOGGER_NAME) Logger logger) {
    this.systemRsaKeyProvider = systemRsaKeyProvider;
    this.permissions = permittableService.getPermittableEndpointsAsPermissions(AcceptedTokenType.SYSTEM);
    this.logger = logger;
  }

  @SuppressWarnings("WeakerAccess")
  public AnubisAuthentication authenticate(
      final String user,
      final String token,
      final String keyTimestamp) {
    if (!user.equals(ApiConstants.SYSTEM_SU))
      throw AmitAuthenticationException.invalidHeader();

    try {
      final JwtParser jwtParser = Jwts.parser()
          .setSigningKey(systemRsaKeyProvider.getPublicKey(keyTimestamp))
          .requireIssuer(TokenType.SYSTEM.getIssuer())
          .require(TokenConstants.JWT_SIGNATURE_TIMESTAMP_CLAIM, keyTimestamp);

      TenantContextHolder.identifier().ifPresent(jwtParser::requireSubject);

      //noinspection unchecked
      final Jwt<Header, Claims> result = jwtParser.parse(token);
      if (result.getBody() == null ||
              result.getBody().getAudience() == null) {
        logger.info("System token for user {}, with key timestamp {} failed to authenticate. Audience was not set.", user, keyTimestamp);
        throw AmitAuthenticationException.invalidToken();
      }

      logger.info("System token for user {}, with key timestamp {} authenticated successfully.", user, keyTimestamp);

      return new AnubisAuthentication(
              TokenConstants.PREFIX + token,
              user,
              result.getBody().getAudience(),
              TokenType.SYSTEM.getIssuer(),
              permissions);
    }
    catch (final JwtException e) {
      logger.debug("token = {}", token);
      logger.info("System token for user {}, with key timestamp {} failed to authenticate. Exception was {}", user, keyTimestamp, e);
      throw AmitAuthenticationException.invalidToken();
    } catch (final InvalidKeyTimestampException e) {
      logger.info("System token for user {}, with key timestamp {} failed to authenticate. Exception was {}", user, keyTimestamp, e);
      throw AmitAuthenticationException.invalidTokenKeyTimestamp("system", keyTimestamp);
    }
  }
}
