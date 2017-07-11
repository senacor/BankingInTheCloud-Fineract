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
package io.mifos.rhythm;

import io.mifos.core.api.util.NotFoundException;
import io.mifos.rhythm.api.v1.domain.Beat;
import io.mifos.rhythm.api.v1.events.BeatEvent;
import io.mifos.rhythm.api.v1.events.EventConstants;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Myrle Krantz
 */
public class TestBeats extends AbstractRhythmTest {

  @Test
  public void shouldCreateBeat() throws InterruptedException {
    final String applicationIdentifier = "funnybusiness-v1";
    final Beat beat = createBeatForThisHour(applicationIdentifier, "bebopthedowop");

    final Beat createdBeat = this.testSubject.getBeat(applicationIdentifier, beat.getIdentifier());
    Assert.assertEquals(beat, createdBeat);

    final List<Beat> allEntities = this.testSubject.getAllBeatsForApplication(applicationIdentifier);
    Assert.assertTrue(allEntities.contains(beat));
  }

  @Test
  public void shouldDeleteBeat() throws InterruptedException {
    final String applicationIdentifier = "funnybusiness-v2";

    final Beat beat = createBeatForThisHour(applicationIdentifier, "bebopthedowop");

    testSubject.deleteBeat(applicationIdentifier, beat.getIdentifier());
    Assert.assertTrue(this.eventRecorder.wait(EventConstants.DELETE_BEAT, new BeatEvent(applicationIdentifier, beat.getIdentifier())));

    final List<Beat> allEntities = this.testSubject.getAllBeatsForApplication(applicationIdentifier);
    Assert.assertFalse(allEntities.contains(beat));

    try {
      this.testSubject.getBeat(applicationIdentifier, beat.getIdentifier());
      Assert.fail("NotFoundException should be thrown.");
    }
    catch (final NotFoundException ignored) { }
  }

  @Test
  public void shouldDeleteApplication() throws InterruptedException {
    final String applicationIdentifier = "funnybusiness-v3";
    createBeatForThisHour(applicationIdentifier, "bebopthedowop");

    this.testSubject.deleteApplication(applicationIdentifier);
    Assert.assertTrue(this.eventRecorder.wait(EventConstants.DELETE_APPLICATION, applicationIdentifier));

    final List<Beat> allEntities = this.testSubject.getAllBeatsForApplication(applicationIdentifier);
    Assert.assertTrue(allEntities.isEmpty());
  }

  @Test
  public void shouldRetryBeatPublishIfFirstAttemptFails() throws InterruptedException {
    final String tenantIdentifier = tenantDataStoreContext.getTenantName();
    final String applicationIdentifier = "funnybusiness-v4";
    final String beatId = "bebopthedowop";

    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

    final Beat beat = new Beat();
    beat.setIdentifier(beatId);
    beat.setAlignmentHour(now.getHour());

    final LocalDateTime expectedBeatTimestamp = getExpectedBeatTimestamp(now, beat.getAlignmentHour());

    Mockito.doReturn(Optional.of("boop")).when(beatPublisherServiceSpy).requestPermissionForBeats(Matchers.eq(tenantIdentifier), Matchers.eq(applicationIdentifier));
    Mockito.when(beatPublisherServiceSpy.publishBeat(beatId, tenantIdentifier, applicationIdentifier, expectedBeatTimestamp)).thenReturn(false, false, true);

    this.testSubject.createBeat(applicationIdentifier, beat);

    Assert.assertTrue(this.eventRecorder.wait(EventConstants.POST_BEAT, new BeatEvent(applicationIdentifier, beat.getIdentifier())));

    Mockito.verify(beatPublisherServiceSpy, Mockito.timeout(10_000).times(3)).publishBeat(beatId, tenantIdentifier, applicationIdentifier, expectedBeatTimestamp);
  }

  @Test
  public void twentyFourBeats() throws InterruptedException {
    final String applicationIdentifier = "funnybusiness-v5";
    final LocalDateTime today = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.DAYS);
    final List<Beat> beats = new ArrayList<>();
    for (int i = 0; i < 24; i ++) {
      final LocalDateTime expectedBeatTimestamp = today.plusHours(i);
      beats.add(createBeat(applicationIdentifier, "bebopthedowop" + i, i, expectedBeatTimestamp));
    }

    beats.forEach(x -> {
      final Beat createdBeat = this.testSubject.getBeat(applicationIdentifier, x.getIdentifier());
      Assert.assertEquals(x, createdBeat);
    });

    final List<Beat> allEntities = this.testSubject.getAllBeatsForApplication(applicationIdentifier);

    beats.forEach(x -> Assert.assertTrue(allEntities.contains(x)));
  }
}