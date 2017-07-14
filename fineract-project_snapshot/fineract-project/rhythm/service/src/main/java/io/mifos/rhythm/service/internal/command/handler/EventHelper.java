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
package io.mifos.rhythm.service.internal.command.handler;

import com.google.gson.Gson;
import io.mifos.core.command.util.CommandConstants;
import io.mifos.core.lang.config.TenantHeaderFilter;
import io.mifos.rhythm.api.v1.events.EventConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("WeakerAccess")
@Component
public class EventHelper {
  private final Gson gson;
  private final JmsTemplate jmsTemplate;

  public EventHelper(final @Qualifier(CommandConstants.SERIALIZER) Gson gson, final JmsTemplate jmsTemplate) {
    this.gson = gson;
    this.jmsTemplate = jmsTemplate;
  }

  void sendEvent(final String eventName, final String tenantIdentifier, final Object payload) {
    this.jmsTemplate.convertAndSend(
            this.gson.toJson(payload),
            message -> {
              message.setStringProperty(
                      TenantHeaderFilter.TENANT_HEADER,
                      tenantIdentifier);
              message.setStringProperty(
                      EventConstants.SELECTOR_NAME,
                      eventName
              );
              return message;
            }
    );
  }
}
