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
package io.mifos.core.test.servicestarter;

import org.junit.rules.ExternalResource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class EurekaForTest extends ExternalResource {
  static final int PORT = 8761;
  static final String DEFAULT_ZONE = "http://localhost:" + PORT + "/eureka";
  private ConfigurableApplicationContext eurekaServer;

  @SuppressWarnings("WeakerAccess")
  @EnableEurekaServer
  @Configuration
  @EnableAutoConfiguration(exclude= {HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class})
  static class EurekaServer {
  }

  protected void before() throws Throwable {

    // start eureka server for test
    eurekaServer = SpringApplication.run(EurekaServer.class,
            "--server.port=" + PORT,
            "--security.basic.enabled=false",
            "--error.whitelabel.enabled=false",
            "--eureka.instance.leaseRenewalIntervalInSeconds=1", //Speed up registration for test purposes.
            "--eureka.server.responseCacheUpdateIntervalMs=500",
            "--eureka.instance.hostname=localhost",
            "--eureka.client.registerWithEureka=false",
            "--eureka.client.fetchRegistry=false",
            "--eureka.serviceUrl.defaultZone=" + DEFAULT_ZONE);
  }

  /**
   * Override to tear down your specific external resource.
   */
  protected void after() {
    eurekaServer.close();
  }
}
