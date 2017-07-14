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
package io.mifos.anubis.config;

import io.mifos.anubis.controller.EmptyInitializeResourcesRestController;
import io.mifos.anubis.controller.SignatureCreatorRestController;
import io.mifos.anubis.controller.SignatureRestController;
import io.mifos.anubis.controller.PermittableRestController;
import io.mifos.anubis.provider.SystemRsaKeyProvider;
import io.mifos.anubis.provider.TenantRsaKeyProvider;
import io.mifos.anubis.repository.TenantAuthorizationDataRepository;
import io.mifos.anubis.security.GuestAuthenticator;
import io.mifos.anubis.security.IsisAuthenticatedAuthenticationProvider;
import io.mifos.anubis.security.SystemAuthenticator;
import io.mifos.anubis.security.TenantAuthenticator;
import io.mifos.anubis.service.PermittableService;
import io.mifos.anubis.token.SystemAccessTokenSerializer;
import io.mifos.anubis.token.TenantAccessTokenSerializer;
import io.mifos.anubis.token.TenantRefreshTokenSerializer;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Myrle Krantz
 */
class AnubisImportSelector implements ImportSelector {
  AnubisImportSelector() { }

  @Override public String[] selectImports(AnnotationMetadata importingClassMetadata) {
    final Set<Class> classesToImport = new HashSet<>();
    classesToImport.add(TenantRsaKeyProvider.class);
    classesToImport.add(SystemRsaKeyProvider.class);

    classesToImport.add(SystemAccessTokenSerializer.class);
    classesToImport.add(TenantAccessTokenSerializer.class);
    classesToImport.add(TenantRefreshTokenSerializer.class);

    classesToImport.add(IsisAuthenticatedAuthenticationProvider.class);
    classesToImport.add(TenantAuthenticator.class);
    classesToImport.add(SystemAuthenticator.class);
    classesToImport.add(GuestAuthenticator.class);

    classesToImport.add(PermittableRestController.class);
    classesToImport.add(PermittableService.class);

    final boolean provideSignatureRestController = (boolean)importingClassMetadata
            .getAnnotationAttributes(EnableAnubis.class.getTypeName())
            .get("provideSignatureRestController");
    final boolean provideSignatureStorage = (boolean) importingClassMetadata
            .getAnnotationAttributes(EnableAnubis.class.getTypeName())
            .get("provideSignatureStorage");
    final boolean generateEmptyInitializeEndpoint = (boolean)importingClassMetadata
            .getAnnotationAttributes(EnableAnubis.class.getTypeName())
            .get("generateEmptyInitializeEndpoint");

    if (provideSignatureRestController) {
      classesToImport.add(SignatureRestController.class);

      if (provideSignatureStorage)
        classesToImport.add(SignatureCreatorRestController.class);
    }

    if (provideSignatureStorage)
      classesToImport.add(TenantAuthorizationDataRepository.class);

    if (generateEmptyInitializeEndpoint)
      classesToImport.add(EmptyInitializeResourcesRestController.class);


    return classesToImport.stream().map(Class::getCanonicalName).toArray(String[]::new);
  }
}
