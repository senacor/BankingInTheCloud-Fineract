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
package io.mifos.anubis.api.v1.domain;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Objects;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Signature {
  @NotNull
  private BigInteger publicKeyMod;

  @NotNull
  private BigInteger publicKeyExp;

  public Signature() {
  }

  public Signature(final BigInteger publicKeyMod, final BigInteger publicKeyExp) {
    this.publicKeyMod = publicKeyMod;
    this.publicKeyExp = publicKeyExp;
  }

  public BigInteger getPublicKeyMod() {
    return publicKeyMod;
  }

  public void setPublicKeyMod(BigInteger publicKeyMod) {
    this.publicKeyMod = publicKeyMod;
  }

  public BigInteger getPublicKeyExp() {
    return publicKeyExp;
  }

  public void setPublicKeyExp(BigInteger publicKeyExp) {
    this.publicKeyExp = publicKeyExp;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Signature))
      return false;
    Signature signature = (Signature) o;
    return Objects.equals(publicKeyMod, signature.publicKeyMod) && Objects
        .equals(publicKeyExp, signature.publicKeyExp);
  }

  @Override public int hashCode() {
    return Objects.hash(publicKeyMod, publicKeyExp);
  }

  @Override public String toString() {
    return "Signature{" +
        "publicKeyMod=" + publicKeyMod +
        ", publicKeyExp=" + publicKeyExp +
        '}';
  }
}
