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
package io.mifos.rhythm.service.internal.command.handler;

import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.command.annotation.CommandLogLevel;
import io.mifos.rhythm.api.v1.events.EventConstants;
import io.mifos.rhythm.service.internal.command.DeleteApplicationCommand;
import io.mifos.rhythm.service.internal.repository.ApplicationRepository;
import io.mifos.rhythm.service.internal.repository.BeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@Aggregate
public class ApplicationCommandHandler {
  private final ApplicationRepository applicationRepository;
  private final BeatRepository beatRepository;
  private final EventHelper eventHelper;

  @Autowired
  public ApplicationCommandHandler(
          final ApplicationRepository applicationRepository,
          final BeatRepository beatRepository,
          final EventHelper eventHelper) {
    super();
    this.applicationRepository = applicationRepository;
    this.beatRepository = beatRepository;
    this.eventHelper = eventHelper;
  }

  @CommandHandler(logStart = CommandLogLevel.INFO, logFinish = CommandLogLevel.INFO)
  @Transactional
  public void process(final DeleteApplicationCommand deleteApplicationCommand) {
    this.applicationRepository.deleteByTenantIdentifierAndApplicationIdentifier(deleteApplicationCommand.getTenantIdentifier(), deleteApplicationCommand.getApplicationIdentifier());
    this.beatRepository.deleteByTenantIdentifierAndApplicationIdentifier(deleteApplicationCommand.getTenantIdentifier(), deleteApplicationCommand.getApplicationIdentifier());
    eventHelper.sendEvent(EventConstants.DELETE_APPLICATION, deleteApplicationCommand.getTenantIdentifier(), deleteApplicationCommand.getApplicationIdentifier());
  }
}
