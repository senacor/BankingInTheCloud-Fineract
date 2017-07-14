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

import io.mifos.rhythm.service.ServiceConstants;
import io.mifos.rhythm.service.internal.repository.BeatEntity;
import io.mifos.rhythm.service.internal.repository.BeatRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Component
public class Drummer {
  private final IdentityPermittableGroupService identityPermittableGroupService;
  private final BeatPublisherService beatPublisherService;
  private final BeatRepository beatRepository;
  private final Logger logger;

  @Autowired
  public Drummer(
          final IdentityPermittableGroupService identityPermittableGroupService,
          final BeatPublisherService beatPublisherService,
          final BeatRepository beatRepository,
          @Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger) {
    this.identityPermittableGroupService = identityPermittableGroupService;
    this.beatPublisherService = beatPublisherService;
    this.beatRepository = beatRepository;
    this.logger = logger;
  }

  @Scheduled(initialDelayString = "${rhythm.beatCheckRate}", fixedRateString = "${rhythm.beatCheckRate}")
  @Transactional
  public synchronized void checkForBeatsNeeded() {
    //In it's current form this function cannot be run in multiple instances of the same service.  We need to get
    //locking on selected entries corrected here, before this will work.
    try {
      final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
      //Get beats from the last two hours in case restart/start happens close to hour begin.
      final Stream<BeatEntity> beats = beatRepository.findByNextBeatBefore(now);
      beats.forEach((beat) -> {
        final boolean applicationHasRequestForAccessPermission
                = identityPermittableGroupService.checkThatApplicationHasRequestForAccessPermission(
                beat.getTenantIdentifier(), beat.getApplicationIdentifier());
        if (!applicationHasRequestForAccessPermission) {
          logger.info("Not checking if beat {} needs publishing, because application access needed to publish is not available.", beat);
        }
        else {
          logger.info("Checking if beat {} needs publishing.", beat);
          final Optional<LocalDateTime> nextBeat = checkBeatForPublish(
                  now,
                  beat.getBeatIdentifier(),
                  beat.getTenantIdentifier(),
                  beat.getApplicationIdentifier(),
                  beat.getAlignmentHour(),
                  beat.getNextBeat());
          nextBeat.ifPresent(y -> {
            beat.setNextBeat(y);
            beatRepository.save(beat);
          });
        }
      });

    }
    catch (final InvalidDataAccessResourceUsageException e) {
      logger.info("InvalidDataAccessResourceUsageException in check for scheduled beats, probably " +
              "because initialize hasn't been called yet. {}", e);
    }
  }

  public Optional<LocalDateTime> checkBeatForPublish(
          final LocalDateTime now,
          final String beatIdentifier,
          final String tenantIdentifier,
          final String applicationIdentifier,
          final Integer alignmentHour,
          final LocalDateTime nextBeat) {
    return checkBeatForPublishHelper(now, alignmentHour, nextBeat,
            x -> beatPublisherService.publishBeat(beatIdentifier, tenantIdentifier, applicationIdentifier, x));
  }

  //Helper is separated from original function so that it can be unit-tested separately from publishBeat.
  static Optional<LocalDateTime> checkBeatForPublishHelper(
          final LocalDateTime now,
          final Integer alignmentHour,
          final LocalDateTime nextBeat,
          final Predicate<LocalDateTime> publishSucceeded) {
    final long numberOfBeatPublishesNeeded = getNumberOfBeatPublishesNeeded(now, nextBeat);
    if (numberOfBeatPublishesNeeded == 0)
      return Optional.empty();

    final Optional<LocalDateTime> firstFailedBeat = Stream.iterate(nextBeat,
            x -> incrementToAlignment(x, alignmentHour))
            .limit(numberOfBeatPublishesNeeded)
            .filter(x -> !publishSucceeded.test(x))
            .findFirst();

    if (firstFailedBeat.isPresent())
      return firstFailedBeat;
    else
      return Optional.of(incrementToAlignment(now, alignmentHour));
  }

  static long getNumberOfBeatPublishesNeeded(final LocalDateTime now, final @Nonnull LocalDateTime nextBeat) {
    if (nextBeat.isAfter(now))
      return 0;

    return Math.max(1, nextBeat.until(now, ChronoUnit.DAYS));
  }

  static LocalDateTime incrementToAlignment(final LocalDateTime toIncrement, final Integer alignmentHour)
  {
    return toIncrement.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(alignmentHour);
  }
}