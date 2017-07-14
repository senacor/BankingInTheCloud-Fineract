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
package io.mifos.rhythm.spi.v1.events;

import java.util.Objects;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BeatPublishEvent {
  String applicationIdentifier;
  String beatIdentifier;
  String forTime;

  public BeatPublishEvent() {
  }

  public BeatPublishEvent(String applicationIdentifier, String beatIdentifier, String forTime) {
    this.applicationIdentifier = applicationIdentifier;
    this.beatIdentifier = beatIdentifier;
    this.forTime = forTime;
  }

  public String getApplicationIdentifier() {
    return applicationIdentifier;
  }

  public void setApplicationIdentifier(String applicationIdentifier) {
    this.applicationIdentifier = applicationIdentifier;
  }

  public String getBeatIdentifier() {
    return beatIdentifier;
  }

  public void setBeatIdentifier(String beatIdentifier) {
    this.beatIdentifier = beatIdentifier;
  }

  public String getForTime() {
    return forTime;
  }

  public void setForTime(String forTime) {
    this.forTime = forTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BeatPublishEvent that = (BeatPublishEvent) o;
    return Objects.equals(applicationIdentifier, that.applicationIdentifier) &&
            Objects.equals(beatIdentifier, that.beatIdentifier) &&
            Objects.equals(forTime, that.forTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationIdentifier, beatIdentifier, forTime);
  }

  @Override
  public String toString() {
    return "BeatPublishEvent{" +
            "applicationIdentifier='" + applicationIdentifier + '\'' +
            ", beatIdentifier='" + beatIdentifier + '\'' +
            ", forTime='" + forTime + '\'' +
            '}';
  }
}
