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
package io.mifos.permittedfeignclient.config;

import io.mifos.permittedfeignclient.service.ApplicationPermissionRequirementsService;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("WeakerAccess")
public class PermittedFeignClientBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

  @Override
  public void registerBeanDefinitions(
          final AnnotationMetadata importingClassMetadata,
          final BeanDefinitionRegistry registry) {

    final Object clients = importingClassMetadata.getAnnotationAttributes(
            EnablePermissionRequestingFeignClient.class.getTypeName()).get("feignClasses");

    final AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
            .genericBeanDefinition(ApplicationPermissionRequirementsService.class)
            .addConstructorArgValue(clients)
            .setScope(SCOPE_SINGLETON)
            .getBeanDefinition();

    registry.registerBeanDefinition("applicationPermissionRequirementsService", beanDefinition);
  }
}