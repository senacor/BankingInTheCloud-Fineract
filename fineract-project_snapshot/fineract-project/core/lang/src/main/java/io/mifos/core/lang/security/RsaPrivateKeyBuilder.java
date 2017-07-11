/*
 * Copyright 2017 The Mifos Initiative
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
package io.mifos.core.lang.security;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"WeakerAccess"})
public class RsaPrivateKeyBuilder {

  private BigInteger privateKeyMod;
  private BigInteger privateKeyExp;

  public RsaPrivateKeyBuilder setPrivateKeyMod(final BigInteger privateKeyMod) {
    this.privateKeyMod = privateKeyMod;
    return this;
  }

  public RsaPrivateKeyBuilder setPrivateKeyExp(final BigInteger privateKeyExp) {
    this.privateKeyExp = privateKeyExp;
    return this;
  }

  public PrivateKey build() {
    try {
      final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      final RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(privateKeyMod, privateKeyExp);
      return keyFactory.generatePrivate(rsaPrivateKeySpec);
    } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException("Could not read private RSA key pair!", e);
    }
  }
}
