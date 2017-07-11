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
package io.mifos.anubis.token;

import java.util.Optional;

public enum TokenType {
  SYSTEM("system"), TENANT("tenant"), ;

  private final String issuer;

  TokenType(final String issuer) {
    this.issuer = issuer;
  }

  public static Optional<TokenType> valueOfIssuer(final String issuer)
  {
    if (issuer.equals(SYSTEM.issuer))
      return Optional.of(SYSTEM);
    else if (issuer.equals(TENANT.issuer))
      return Optional.of(TENANT);
    else
      return Optional.empty();
  }

  public String getIssuer() {
    return issuer;
  }
}
