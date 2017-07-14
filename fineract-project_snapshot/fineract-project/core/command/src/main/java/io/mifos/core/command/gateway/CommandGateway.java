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
package io.mifos.core.command.gateway;

import io.mifos.core.command.domain.CommandCallback;
import io.mifos.core.command.domain.CommandProcessingException;
import io.mifos.core.command.internal.CommandBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommandGateway {

  private final CommandBus commandBus;

  @Autowired
  public CommandGateway(final CommandBus commandBus) {
    super();
    this.commandBus = commandBus;
  }

  public <C> void process(final C command) {
    this.commandBus.dispatch(command);
  }

  public <C, T> CommandCallback<T> process(final C command, Class<T> clazz) throws CommandProcessingException {
    return new CommandCallback<>(this.commandBus.dispatch(command, clazz));
  }
}
