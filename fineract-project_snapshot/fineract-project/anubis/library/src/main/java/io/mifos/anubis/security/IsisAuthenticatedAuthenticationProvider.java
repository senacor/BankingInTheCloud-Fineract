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
import io.mifos.anubis.api.v1.TokenConstants;
import io.mifos.anubis.provider.InvalidKeyTimestampException;
import io.mifos.anubis.provider.SystemRsaKeyProvider;
import io.mifos.anubis.provider.TenantRsaKeyProvider;
import io.mifos.anubis.token.TokenType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.security.Key;
import java.util.Optional;


/**
 * @author Myrle Krantz
 */
@Component
public class IsisAuthenticatedAuthenticationProvider implements AuthenticationProvider {
  private final SystemRsaKeyProvider systemRsaKeyProvider;
  private final TenantRsaKeyProvider tenantRsaKeyProvider;
  private final SystemAuthenticator systemAuthenticator;
  private final TenantAuthenticator tenantAuthenticator;
  private final GuestAuthenticator guestAuthenticator;

  @Autowired
  public IsisAuthenticatedAuthenticationProvider(
      final SystemRsaKeyProvider systemRsaKeyProvider,
      final TenantRsaKeyProvider tenantRsaKeyProvider,
      final SystemAuthenticator systemAuthenticator,
      final TenantAuthenticator tenantAuthenticator,
      final GuestAuthenticator guestAuthenticator) {
    this.systemRsaKeyProvider = systemRsaKeyProvider;
    this.tenantRsaKeyProvider = tenantRsaKeyProvider;
    this.systemAuthenticator = systemAuthenticator;
    this.tenantAuthenticator = tenantAuthenticator;
    this.guestAuthenticator = guestAuthenticator;
  }

  @Override public boolean supports(final Class<?> clazz) {
    return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(clazz);
  }

  @Override public Authentication authenticate(Authentication authentication)
      throws AuthenticationException {
    if (!PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication.getClass()))
    {
      throw AmitAuthenticationException.internalError(
          "authentication called with unexpected authentication object.");
    }

    final PreAuthenticatedAuthenticationToken preAuthentication = (PreAuthenticatedAuthenticationToken) authentication;

    final String user = (String) preAuthentication.getPrincipal();
    Assert.hasText(user, "user cannot be empty.  This should have been assured in preauthentication");

    return convert(user, (String)preAuthentication.getCredentials());
  }

  private Authentication convert(final @Nonnull String user, final String authenticationHeader) {
    final Optional<String> token = getJwtTokenString(authenticationHeader);
    return token.map(x -> {
      final TokenInfo tokenInfo = getTokenInfo(x);

      switch (tokenInfo.getType()) {
        case TENANT:
          return tenantAuthenticator.authenticate(user, x, tokenInfo.getKeyTimestamp());
        case SYSTEM:
          return systemAuthenticator.authenticate(user, x, tokenInfo.getKeyTimestamp());
        default:
          throw AmitAuthenticationException.invalidTokenIssuer(tokenInfo.getType().getIssuer());
      }
    }).orElseGet(() -> guestAuthenticator.authenticate(user));
  }

  private Optional<String> getJwtTokenString(final String authenticationHeader) {
    if ((authenticationHeader == null) || authenticationHeader.equals(
        TokenConstants.NO_AUTHENTICATION)){
      return Optional.empty();
    }

    if (!authenticationHeader.startsWith(TokenConstants.PREFIX)) {
      throw AmitAuthenticationException.invalidHeader();
    }
    return Optional.of(authenticationHeader.substring(TokenConstants.PREFIX.length()).trim());
  }

  @Nonnull private TokenInfo getTokenInfo(final String token)
  {
    try {
      @SuppressWarnings("unchecked")
      final Jwt<Header, Claims> jwt = Jwts.parser().setSigningKeyResolver(new SigningKeyResolver() {
        @Override public Key resolveSigningKey(final JwsHeader header, final Claims claims) {
          final TokenType tokenType = getTokenTypeFromClaims(claims);
          final String keyTimestamp = getKeyTimestampFromClaims(claims);

          try {
            switch (tokenType) {
              case TENANT:
                return tenantRsaKeyProvider.getPublicKey(keyTimestamp);
              case SYSTEM:
                return systemRsaKeyProvider.getPublicKey(keyTimestamp);
              default:
                throw AmitAuthenticationException.invalidTokenIssuer(tokenType.getIssuer());
            }
          }
          catch (final IllegalArgumentException e)
          {
            throw AmitAuthenticationException.missingTenant();
          }
          catch (final InvalidKeyTimestampException e)
          {
            throw AmitAuthenticationException.invalidTokenKeyTimestamp(tokenType.getIssuer(), keyTimestamp);
          }
        }

        @Override public Key resolveSigningKey(final JwsHeader header, final String plaintext) {
          return null;
        }
      }).parse(token);

      final String alg = jwt.getHeader().get("alg").toString();
      final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.forName(alg);
      if (!signatureAlgorithm.isRsa()) {
        throw AmitAuthenticationException.invalidTokenAlgorithm(alg);
      }

      final String keyTimestamp = getKeyTimestampFromClaims(jwt.getBody());
      final TokenType tokenType = getTokenTypeFromClaims(jwt.getBody());

      return new TokenInfo(tokenType, keyTimestamp);
    }
    catch (final JwtException e)
    {
      throw AmitAuthenticationException.invalidToken();
    }
  }

  private @Nonnull String getKeyTimestampFromClaims(final Claims claims) {
    return claims.get(TokenConstants.JWT_SIGNATURE_TIMESTAMP_CLAIM, String.class);
  }

  private @Nonnull TokenType getTokenTypeFromClaims(final Claims claims) {
    final String issuer = claims.getIssuer();
    final Optional<TokenType> tokenType = TokenType.valueOfIssuer(issuer);
    if (!tokenType.isPresent())
      throw AmitAuthenticationException.invalidTokenIssuer(issuer);
    return tokenType.get();
  }
}
