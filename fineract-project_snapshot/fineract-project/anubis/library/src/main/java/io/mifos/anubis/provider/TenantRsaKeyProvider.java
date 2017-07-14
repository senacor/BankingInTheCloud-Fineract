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
package io.mifos.anubis.provider;

import io.mifos.anubis.api.v1.domain.Signature;
import io.mifos.anubis.config.TenantSignatureRepository;
import io.mifos.core.lang.security.RsaPublicKeyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Optional;

/**
 * @author Myrle Krantz
 */
@Component
public class TenantRsaKeyProvider {

  private final TenantSignatureRepository tenantSignatureRepository;

  @Autowired
  public TenantRsaKeyProvider(final TenantSignatureRepository tenantSignatureRepository)
  {
    this.tenantSignatureRepository = tenantSignatureRepository;
  }

  public PublicKey getPublicKey(final String keyTimestamp) throws InvalidKeyTimestampException {
    final Optional<Signature> tenantAuthorizationData =
        tenantSignatureRepository.getIdentityManagerSignature(keyTimestamp);

    return
        tenantAuthorizationData.map(x -> new RsaPublicKeyBuilder()
        .setPublicKeyMod(x.getPublicKeyMod())
        .setPublicKeyExp(x.getPublicKeyExp())
        .build()).orElseThrow(() -> new InvalidKeyTimestampException(keyTimestamp + " + not initialized."));
  }
}
