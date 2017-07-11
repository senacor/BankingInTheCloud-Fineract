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
package accessanother.service;

import accessanother.service.apiforother.AnotherWithApplicationPermissions;
import io.mifos.anubis.config.EnableAnubis;
import io.mifos.core.lang.config.EnableServiceException;
import io.mifos.core.lang.config.EnableTenantContext;
import io.mifos.permittedfeignclient.config.EnablePermissionRequestingFeignClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author Myrle Krantz
 */
@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@EnableDiscoveryClient
@EnableTenantContext
@EnableAnubis(generateEmptyInitializeEndpoint = true)
@EnableFeignClients(basePackages = {"accessanother.service.apiforother"})
@RibbonClient(name = "accessanother-v1")
@EnableServiceException
@EnablePermissionRequestingFeignClient(feignClasses = {AnotherWithApplicationPermissions.class})
@ComponentScan({
        "accessanother.service"
})
public class AccessAnotherConfiguration {
}
