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
package io.mifos.core.test.listener;

import com.google.gson.GsonBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * @author Myrle Krantz
 */
class EventRecorderBeanDefiner implements ImportBeanDefinitionRegistrar {

  @Override
  public void registerBeanDefinitions(
      final AnnotationMetadata importingClassMetadata,
      final BeanDefinitionRegistry registry) {

    final Object maxWait = importingClassMetadata.getAnnotationAttributes(
        EnableEventRecording.class.getTypeName()).get("maxWait");

    final AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
        .genericBeanDefinition(InMemoryEventRecorder.class)
        .addConstructorArgValue(maxWait)
        .addConstructorArgValue(new GsonBuilder().create())
        .addConstructorArgValue(LoggerFactory.getLogger("event-recorder-logger"))
        .setScope(SCOPE_SINGLETON)
        .getBeanDefinition();

    registry.registerBeanDefinition("eventRecorder", beanDefinition);
  }
}
