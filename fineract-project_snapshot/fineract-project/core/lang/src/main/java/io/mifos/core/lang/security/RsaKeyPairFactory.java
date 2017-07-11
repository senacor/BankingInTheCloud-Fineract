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

import io.mifos.core.lang.DateConverter;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Clock;
import java.time.LocalDateTime;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class RsaKeyPairFactory {

  private RsaKeyPairFactory() {
  }

  public static KeyPairHolder createKeyPair() {
    try {
      final KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048);
      final KeyPair keyPair = keyPairGenerator.genKeyPair();

      final RSAPublicKeySpec rsaPublicKeySpec =
          keyFactory.getKeySpec(keyPair.getPublic(), RSAPublicKeySpec.class);
      final RSAPrivateKeySpec rsaPrivateKeySpec =
          keyFactory.getKeySpec(keyPair.getPrivate(), RSAPrivateKeySpec.class);

      final String keyTimestamp = createKeyTimestampNow();
      final RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(rsaPublicKeySpec);
      final RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(rsaPrivateKeySpec);

      return new KeyPairHolder(keyTimestamp, publicKey, privateKey);
    } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException("RSA problem.");
    }
  }

  private static String createKeyTimestampNow() {
    final String timestamp = DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC()));
    final int index = timestamp.indexOf(".");
    final String timestampWithoutNanos;
    if (index > 0) {
      timestampWithoutNanos = timestamp.substring(0, index);
    }
    else {
      timestampWithoutNanos = timestamp;
    }
    return timestampWithoutNanos.replace(':', '_');
  }

  public static class KeyPairHolder {
    private final String timestamp;
    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    public KeyPairHolder(final String timestamp, final RSAPublicKey publicKey, final RSAPrivateKey privateKey) {
      super();
      this.timestamp = timestamp;
      this.publicKey = publicKey;
      this.privateKey = privateKey;
    }


    public RSAPublicKey publicKey() {
      return publicKey;
    }

    public RSAPrivateKey privateKey() {
      return privateKey;
    }

    public String getTimestamp() {
      return timestamp;
    }

    public BigInteger getPublicKeyMod() {
      return publicKey.getModulus();
    }

    public BigInteger getPublicKeyExp() {
      return publicKey.getPublicExponent();
    }

    public BigInteger getPrivateKeyMod() {
      return privateKey.getModulus();
    }

    public BigInteger getPrivateKeyExp() {
      return privateKey.getPrivateExponent();
    }
  }
}