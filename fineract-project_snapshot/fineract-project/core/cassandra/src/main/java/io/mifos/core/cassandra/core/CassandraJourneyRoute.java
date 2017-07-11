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
package io.mifos.core.cassandra.core;

import org.springframework.util.Assert;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class CassandraJourneyRoute {

  private final String version;
  private final ArrayList<CassandraJourneyWaypoint> cassandraJourneyWaypoints;
  private CassandraJourneyRoute(final String version,
                                final ArrayList<CassandraJourneyWaypoint> cassandraJourneyWaypoints) {
    super();
    this.version = version;
    this.cassandraJourneyWaypoints = cassandraJourneyWaypoints;
  }

  public static Builder plan(final String version) {
    return new Builder(version);
  }

  public ArrayList<CassandraJourneyWaypoint> getCassandraJourneyWaypoints() {
    return this.cassandraJourneyWaypoints;
  }

  public String getVersion() {
    return this.version;
  }

  public Integer getHashValue() {
    return 31 * this.cassandraJourneyWaypoints.stream().mapToInt(CassandraJourneyWaypoint::getHashValue).sum();
  }

  public static class Builder {

    private final String version;
    private ArrayList<CassandraJourneyWaypoint> cassandraJourneyWaypoints;

    private Builder(final String version) {
      super();
      this.version = version;
    }

    public Builder addWaypoint(final String statement) {
      if (this.cassandraJourneyWaypoints == null) {
        this.cassandraJourneyWaypoints = new ArrayList<>();
      }
      this.cassandraJourneyWaypoints.add(new CassandraJourneyWaypoint(statement));
      return this;
    }

    public CassandraJourneyRoute build() {
      Assert.notNull(this.cassandraJourneyWaypoints);
      return new CassandraJourneyRoute(this.version, this.cassandraJourneyWaypoints);
    }
  }
}
