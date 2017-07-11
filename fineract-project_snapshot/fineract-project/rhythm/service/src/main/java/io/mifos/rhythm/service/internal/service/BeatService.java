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

import io.mifos.rhythm.api.v1.domain.Beat;
import io.mifos.rhythm.service.internal.mapper.BeatMapper;
import io.mifos.rhythm.service.internal.repository.BeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Myrle Krantz
 */
@Service
public class BeatService {

  private final BeatRepository beatRepository;

  @Autowired
  public BeatService(final BeatRepository beatRepository) {
    super();
    this.beatRepository = beatRepository;
  }

  public List<Beat> findAllEntities(final String tenantIdentifier, final String applicationIdentifier) {
    return BeatMapper.map(this.beatRepository.findByTenantIdentifierAndApplicationIdentifier(tenantIdentifier, applicationIdentifier));
  }

  public Optional<Beat> findByIdentifier(final String tenantIdentifier, final String applicationIdentifier, final String identifier) {
    return this.beatRepository.findByTenantIdentifierAndApplicationIdentifierAndBeatIdentifier(tenantIdentifier, applicationIdentifier, identifier).map(BeatMapper::map);
  }
}
