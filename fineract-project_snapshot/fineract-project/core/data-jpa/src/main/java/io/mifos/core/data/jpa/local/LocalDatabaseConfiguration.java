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
package io.mifos.core.data.jpa.local;

import io.mifos.core.data.jpa.core.DatabaseConnectorConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;
import java.util.HashMap;

@Profile("local")
@Configuration
@EnableConfigurationProperties({
    LocalDatabaseProperties.class
})
@EnableJpaRepositories(
    basePackages = {
        "io.mifos.core.data.jpa.local.repository"
    }
)
public class LocalDatabaseConfiguration {

  public LocalDatabaseConfiguration() {
    super();
  }

  @Bean
  public DataSource dataSource(@Qualifier(DatabaseConnectorConstants.LOGGER_NAME) final Logger logger,
                               final LocalDatabaseProperties localDatabaseProperties) {
    final LocalRoutingDataSource localRoutingDataSource =
        new LocalRoutingDataSource(logger, localDatabaseProperties);
    localRoutingDataSource.setTargetDataSources(new HashMap<>());
    localRoutingDataSource.setLenientFallback(false);
    return localRoutingDataSource;
  }
}
