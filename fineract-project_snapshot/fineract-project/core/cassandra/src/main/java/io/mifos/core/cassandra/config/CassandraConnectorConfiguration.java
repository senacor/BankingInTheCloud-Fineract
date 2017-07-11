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
package io.mifos.core.cassandra.config;

import io.mifos.core.cassandra.core.CassandraJourneyFactory;
import io.mifos.core.cassandra.core.CassandraSessionProvider;
import io.mifos.core.cassandra.core.TenantAwareCassandraMapperProvider;
import io.mifos.core.cassandra.core.TenantAwareEntityTemplate;
import io.mifos.core.cassandra.util.CassandraConnectorConstants;
import io.mifos.core.lang.ApplicationName;
import io.mifos.core.lang.config.EnableApplicationName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@SuppressWarnings("WeakerAccess")
@Configuration
@ConditionalOnProperty(prefix = "cassandra", name = "enabled", matchIfMissing = true)
@EnableApplicationName
public class CassandraConnectorConfiguration {

  private final Environment env;

  private final ApplicationName applicationName;

  @Autowired
  public CassandraConnectorConfiguration(final ApplicationName applicationName, final Environment env) {
    super();
    this.applicationName = applicationName;
    this.env = env;
  }

  @Bean(name = CassandraConnectorConstants.LOGGER_NAME)
  public Logger loggerBean() {
    return LoggerFactory.getLogger(CassandraConnectorConstants.LOGGER_NAME);
  }

  @Bean
  public CassandraSessionProvider cassandraSessionProvider(@Qualifier(CassandraConnectorConstants.LOGGER_NAME) final Logger logger) {
    final CassandraSessionProvider cassandraSessionProvider = new CassandraSessionProvider(this.env, logger);
    cassandraSessionProvider.setAdminClusterName(
        this.env.getProperty(CassandraConnectorConstants.CLUSTER_NAME_PROP, CassandraConnectorConstants.CLUSTER_NAME_PROP_DEFAULT));
    cassandraSessionProvider.setAdminContactPoints(
        this.env.getProperty(CassandraConnectorConstants.CONTACT_POINTS_PROP, CassandraConnectorConstants.CONTACT_POINTS_PROP_DEFAULT));
    cassandraSessionProvider.setAdminKeyspace(
        this.env.getProperty(CassandraConnectorConstants.KEYSPACE_PROP, CassandraConnectorConstants.KEYSPACE_PROP_DEFAULT));

    cassandraSessionProvider.touchAdminSession();

    return cassandraSessionProvider;
  }

  @Bean
  public TenantAwareCassandraMapperProvider cassandraMapperProvider(@Qualifier(CassandraConnectorConstants.LOGGER_NAME) final Logger logger, final CassandraSessionProvider cassandraSessionProvider) {
    return new TenantAwareCassandraMapperProvider(this.env, logger, cassandraSessionProvider);
  }

  @Bean
  public TenantAwareEntityTemplate tenantAwareEntityTemplate(final CassandraSessionProvider cassandraSessionProvider,
                                                             final TenantAwareCassandraMapperProvider tenantAwareCassandraMapperProvider) {
    return new TenantAwareEntityTemplate(cassandraSessionProvider, tenantAwareCassandraMapperProvider);
  }

  @Bean
  public CassandraJourneyFactory cassandraJourneyFactory(@Qualifier(CassandraConnectorConstants.LOGGER_NAME) final Logger logger) {
    return new CassandraJourneyFactory(logger, this.applicationName);
  }
}
