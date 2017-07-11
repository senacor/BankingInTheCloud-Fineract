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

@SuppressWarnings("WeakerAccess")
public class ReplicationStrategyResolver {

  private ReplicationStrategyResolver() {
    super();
  }

  public static String replicationStrategy(final String type, final String replicas) {
    final StringBuilder builder = new StringBuilder();
    if (type.equalsIgnoreCase("Simple")) {
      builder.append("{'class': 'SimpleStrategy', ");
      builder.append("'replication_factor': ");
      builder.append(replicas);
      builder.append("}");
      return builder.toString();
    } else if (type.equalsIgnoreCase("Network")) {
      builder.append("{'class': 'NetworkTopologyStrategy', ");

      final String[] splitReplicas = replicas.split(",");
      for (int i = 0; i < splitReplicas.length; i++) {
        final String[] replicaDataCenter = splitReplicas[i].split(":");
        builder.append("'").append(replicaDataCenter[0].trim()).append("': ");
        builder.append(replicaDataCenter[1].trim());
        if ((i + 1) < splitReplicas.length) {
          builder.append(", ");
        }
      }

      builder.append("}");
      return builder.toString();
    } else {
      throw new IllegalArgumentException("Unknown replication strategy: " + type);
    }
  }
}
