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
package io.mifos.rhythm.service.internal.service;

import io.mifos.anubis.api.v1.domain.AllowedOperation;
import io.mifos.core.api.context.AutoUserContext;
import io.mifos.core.api.util.ApiFactory;
import io.mifos.core.lang.ApplicationName;
import io.mifos.core.lang.AutoTenantContext;
import io.mifos.core.lang.DateConverter;
import io.mifos.identity.api.v1.client.ApplicationPermissionAlreadyExistsException;
import io.mifos.identity.api.v1.domain.Permission;
import io.mifos.permittedfeignclient.service.ApplicationAccessTokenService;
import io.mifos.rhythm.service.config.RhythmProperties;
import io.mifos.rhythm.service.internal.identity.ApplicationPermissionRequestCreator;
import io.mifos.rhythm.spi.v1.PermittableGroupIds;
import io.mifos.rhythm.spi.v1.client.BeatListener;
import io.mifos.rhythm.spi.v1.domain.BeatPublish;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.mifos.rhythm.service.ServiceConstants.LOGGER_NAME;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("WeakerAccess")
@Service
public class BeatPublisherService {
  private final DiscoveryClient discoveryClient;
  private final ApplicationPermissionRequestCreator applicationPermissionRequestCreator;
  private final ApplicationAccessTokenService applicationAccessTokenService;
  private final ApplicationName rhythmApplicationName;
  private final ApiFactory apiFactory;
  private final RhythmProperties properties;
  private final Logger logger;

  @Autowired
  public BeatPublisherService(
          @SuppressWarnings("SpringJavaAutowiringInspection") final DiscoveryClient discoveryClient,
          @SuppressWarnings("SpringJavaAutowiringInspection") final ApplicationPermissionRequestCreator applicationPermissionRequestCreator,
          @SuppressWarnings("SpringJavaAutowiringInspection") final ApplicationAccessTokenService applicationAccessTokenService,
          final ApplicationName rhythmApplicationName,
          final ApiFactory apiFactory,
          final RhythmProperties properties,
          @Qualifier(LOGGER_NAME) final Logger logger) {
    this.discoveryClient = discoveryClient;
    this.applicationPermissionRequestCreator = applicationPermissionRequestCreator;
    this.applicationAccessTokenService = applicationAccessTokenService;
    this.rhythmApplicationName = rhythmApplicationName;
    this.apiFactory = apiFactory;
    this.properties = properties;
    this.logger = logger;
  }

  /**
   * Register a request to access an endpoint with identity.  This only creates the request.  The request must be
   * accepted by the user named in rhythm's configuration before beats can actually be sent.  This function makes
   * calls to identity, and therefore most be mocked in unit and component tests.
   *
   * @param tenantIdentifier The tenant identifier as provided via the tenant header when the beat was created.
   * @param applicationIdentifier The name of the application the beat should be sent to.
   *
   * @return true if the beat was published.  false if the beat was not published, or we just don't know.
   */
  @SuppressWarnings("WeakerAccess") //Access is public for mocking in component test.
  public Optional<String> requestPermissionForBeats(final String tenantIdentifier, final String applicationIdentifier) {
    try (final AutoTenantContext ignored = new AutoTenantContext(tenantIdentifier)) {
      try (final AutoUserContext ignored2 = new AutoUserContext(properties.getUser(), "")) {
        logger.info("Requesting permission to send beats to application '{}' under tenant '{}'.", applicationIdentifier, tenantIdentifier);

        final String consumerPermittableGroupIdentifier = PermittableGroupIds.forApplication(applicationIdentifier);
        final Permission publishBeatPermission = new Permission();
        publishBeatPermission.setAllowedOperations(Collections.singleton(AllowedOperation.CHANGE));
        publishBeatPermission.setPermittableEndpointGroupIdentifier(consumerPermittableGroupIdentifier);
        try {
          applicationPermissionRequestCreator.createApplicationPermission(rhythmApplicationName.toString(), publishBeatPermission);
        }
        catch (final ApplicationPermissionAlreadyExistsException e) {
          logger.debug("Failed to request permission for application {}, in tenant {} because the request already exists. {} was thrown.", applicationIdentifier, tenantIdentifier, e);
        }

        logger.debug("Successfully requested permission to send beats to application '{}' under tenant '{}'.", applicationIdentifier, tenantIdentifier);
        return Optional.of(consumerPermittableGroupIdentifier);
      }
    }
    catch (final Throwable e) {
      logger.warn("Failed to request permission for application {}, in tenant {}.", applicationIdentifier, tenantIdentifier, e);
      return Optional.empty();
    }
  }

  /**
   * Authenticate with identity and publish the beat to the application.  This function performs all the scheduled
   * interprocess communication in rhythm, and therefore most be mocked in unit and component tests.
   *
   * @param beatIdentifier The identifier of the beat as provided when the beat was created.
   * @param tenantIdentifier The tenant identifier as provided via the tenant header when the beat was created.
   * @param applicationIdentifier The name of the application the beat should be sent to.
   * @param timestamp The publication time for the beat.  If rhythm has been down for a while this could be in the past.
   *
   * @return true if the beat was published.  false if the beat was not published, or we just don't know.
   */
  @SuppressWarnings("WeakerAccess") //Access is public for mocking in component test.
  public boolean publishBeat(
          final String beatIdentifier,
          final String tenantIdentifier,
          final String applicationIdentifier,
          final LocalDateTime timestamp) {
    final BeatPublish beatPublish = new BeatPublish(beatIdentifier, DateConverter.toIsoString(timestamp));
    logger.info("Attempting publish {} with timestamp {} under user {}.", beatPublish, timestamp, properties.getUser());

    final List<ServiceInstance> applicationsByName = discoveryClient.getInstances(applicationIdentifier);
    if (applicationsByName.isEmpty())
      return false;

    final ServiceInstance beatListenerService = applicationsByName.get(0);
    final BeatListener beatListener = apiFactory.create(BeatListener.class, beatListenerService.getUri().toString());

    try (final AutoTenantContext ignored = new AutoTenantContext(tenantIdentifier)) {
      final String accessToken;
      try {
        accessToken = applicationAccessTokenService.getAccessToken(
                properties.getUser(), tenantIdentifier);
      }
      catch (final Exception e) {
        logger.warn("Unable to publish beat '{}' to application '{}' for tenant '{}', " +
                "because access token could not be acquired from identity. Exception was {}.",
                beatIdentifier, applicationIdentifier, tenantIdentifier, e);
        return false;
      }
      try (final AutoUserContext ignored2 = new AutoUserContext(properties.getUser(), accessToken)) {
        beatListener.publishBeat(beatPublish);
        return true;
      }
    }
    catch (final Throwable e) {
      logger.warn("Unable to publish beat '{}' to application '{}' for tenant '{}', " +
              "because exception was thrown in publish {}.", beatIdentifier, applicationIdentifier, tenantIdentifier, e);
      return false;
    }
  }
}