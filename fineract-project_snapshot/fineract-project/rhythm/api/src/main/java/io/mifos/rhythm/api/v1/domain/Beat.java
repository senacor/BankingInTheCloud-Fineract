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
package io.mifos.rhythm.api.v1.domain;

import io.mifos.core.lang.validation.constraints.ValidIdentifier;
import org.hibernate.validator.constraints.Range;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Beat {
  @ValidIdentifier
  private String identifier;

  @Range(min = 0, max = 23)
  private Integer alignmentHour;

  public Beat() {
  }

  public Beat(String identifier, Integer alignmentHour) {
    this.identifier = identifier;
    this.alignmentHour = alignmentHour;
  }

  public Beat(String identifier) {
    this.identifier = identifier;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public Integer getAlignmentHour() {
    return alignmentHour;
  }

  public void setAlignmentHour(Integer alignmentHour) {
    this.alignmentHour = alignmentHour;
  }

  @SuppressWarnings("SimplifiableIfStatement")
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Beat beat = (Beat) o;

    if (identifier != null ? !identifier.equals(beat.identifier) : beat.identifier != null) return false;
    return alignmentHour != null ? alignmentHour.equals(beat.alignmentHour) : beat.alignmentHour == null;

  }

  @Override
  public int hashCode() {
    int result = identifier != null ? identifier.hashCode() : 0;
    result = 31 * result + (alignmentHour != null ? alignmentHour.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Beat{" +
            "identifier='" + identifier + '\'' +
            ", alignmentHour=" + alignmentHour +
            '}';
  }
}
