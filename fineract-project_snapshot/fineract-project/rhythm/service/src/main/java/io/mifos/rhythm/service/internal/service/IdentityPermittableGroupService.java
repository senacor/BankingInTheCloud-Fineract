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
package io.mifos.rhythm.service.internal.service;

import io.mifos.rhythm.service.internal.repository.ApplicationEntity;
import io.mifos.rhythm.service.internal.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Myrle Krantz
 */
@Service
public class IdentityPermittableGroupService {
  private final ApplicationRepository applicationRepository;
  private final BeatPublisherService beatPublisherService;

  @Autowired
  public IdentityPermittableGroupService(
          final ApplicationRepository applicationRepository,
          final BeatPublisherService beatPublisherService) {
    this.applicationRepository = applicationRepository;
    this.beatPublisherService = beatPublisherService;
  }

  public synchronized boolean checkThatApplicationHasRequestForAccessPermission(
          final String tenantIdentifier,
          final String applicationIdentifier) {
    try {
      return checkThatApplicationHasRequestForAccessPermissionHelper(tenantIdentifier, applicationIdentifier);
    }
    catch (final DataIntegrityViolationException e) {
      return false;
    }
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean checkThatApplicationHasRequestForAccessPermissionHelper(String tenantIdentifier, String applicationIdentifier) {
    final Optional<ApplicationEntity> findApplication = applicationRepository.findByTenantIdentifierAndApplicationIdentifier(
            tenantIdentifier,
            applicationIdentifier);
    if (findApplication.isPresent())
      return true;
    else {
      final Optional<String> ret = beatPublisherService.requestPermissionForBeats(tenantIdentifier, applicationIdentifier);

      ret.ifPresent(x -> {
        final ApplicationEntity saveApplication = new ApplicationEntity();
        saveApplication.setTenantIdentifier(tenantIdentifier);
        saveApplication.setApplicationIdentifier(applicationIdentifier);
        saveApplication.setConsumerPermittableGroupIdentifier(x);
        applicationRepository.save(saveApplication);
      });

      return ret.isPresent();
    }
  }
}
