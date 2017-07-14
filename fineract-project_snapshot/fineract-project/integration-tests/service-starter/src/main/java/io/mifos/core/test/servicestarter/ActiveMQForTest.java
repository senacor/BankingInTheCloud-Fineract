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

import io.mifos.core.test.listener.EnableEventRecording;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.junit.rules.ExternalResource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import java.lang.annotation.*;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("WeakerAccess")
public class ActiveMQForTest extends ExternalResource {
  static final int PORT = 61616;
  static final String BIND_ADDRESS = "tcp://localhost:" + PORT;

  @Configuration
  @EnableAutoConfiguration(exclude= {HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class})
  @EnableEventRecording(maxWait = 45000L)
  public static class ActiveMQListenConfiguration {
    @Bean
    public PooledConnectionFactory jmsFactory() {
      final PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
      final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
      activeMQConnectionFactory.setBrokerURL(BIND_ADDRESS);
      pooledConnectionFactory.setConnectionFactory(activeMQConnectionFactory);

      return pooledConnectionFactory;
    }

    @Bean
    public JmsListenerContainerFactory jmsListenerContainerFactory(final PooledConnectionFactory jmsFactory) {
      final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
      factory.setPubSubDomain(true);
      factory.setConnectionFactory(jmsFactory);
      factory.setConcurrency(CONCURRENCY);

      return factory;
    }
  }


  @SuppressWarnings("unused")
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  @Inherited
  @Import({ActiveMQListenConfiguration.class})
  public @interface EnableActiveMQListen {

  }

  private static final String CONCURRENCY = "3-10";

  private BrokerService broker;

  @Override
  protected void before() throws Throwable {
    broker = new  BrokerService();
    broker.addConnector(BIND_ADDRESS);
    broker.setPersistent(false);
    broker.start();
  }

  @Override
  protected void after() {
    try {
      broker.stop();
    } catch (final Exception e) {
      System.out.println("ActiveMQ threw an exception when stopping.");
      e.printStackTrace();
    }
  }
}
