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
package io.mifos.anubis.service;

import io.jsonwebtoken.lang.Assert;
import io.mifos.anubis.annotation.AcceptedTokenType;
import io.mifos.anubis.annotation.Permittable;
import io.mifos.anubis.api.v1.domain.AllowedOperation;
import io.mifos.anubis.api.v1.domain.PermittableEndpoint;
import io.mifos.anubis.security.ApplicationPermission;
import io.mifos.core.lang.ApplicationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMapping;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Myrle Krantz
 */
@Component
public class PermittableService {
  private final RequestMappingHandlerMapping requestMappingHandlerMapping;
  private final EndpointHandlerMapping endpointHandlerMapping;
  private final ApplicationName applicationName;

  @Autowired
  public PermittableService(final RequestMappingHandlerMapping requestMappingHandlerMapping,
                            final EndpointHandlerMapping endpointHandlerMapping,
                            final ApplicationName applicationName) {
    this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    this.endpointHandlerMapping = endpointHandlerMapping;
    this.applicationName = applicationName;
  }

  public Set<ApplicationPermission> getPermittableEndpointsAsPermissions(
      final AcceptedTokenType... acceptedTokenType) {
    final Set<PermittableEndpoint> permittableEndpoints
        = getPermittableEndpointsHelper(Arrays.asList(acceptedTokenType), false);

    return Collections.unmodifiableSet(
        permittableEndpoints.stream()
            .map(x -> new ApplicationPermission(x.getPath(), mapHttpMethod(x.getMethod()), x.isAcceptTokenIntendedForForeignApplication()))
            .collect(Collectors.toSet()));
  }

  private static AllowedOperation mapHttpMethod(final String httpMethod) {
    switch (httpMethod) {
      case "GET":
        return AllowedOperation.READ;
      case "HEAD":
        return AllowedOperation.READ;
      case "POST":
        return AllowedOperation.CHANGE;
      case "PUT":
        return AllowedOperation.CHANGE;
      case "DELETE":
        return AllowedOperation.DELETE;
      default:
        throw new IllegalArgumentException("Unsupported HTTP Method " + httpMethod);
    }
  }

  public Set<PermittableEndpoint> getPermittableEndpoints(final Collection<AcceptedTokenType> acceptedTokenTypes) {
    return getPermittableEndpointsHelper(acceptedTokenTypes, true);
  }

  private Set<PermittableEndpoint> getPermittableEndpointsHelper(
      final Collection<AcceptedTokenType> acceptedTokenTypes, boolean withAppName) {
    Assert.notEmpty(acceptedTokenTypes);

    final Set<PermittableEndpoint> permittableEndpoints = new LinkedHashSet<>();

    fillPermittableEndpointsFromHandlerMethods(acceptedTokenTypes, withAppName, this.requestMappingHandlerMapping.getHandlerMethods(), permittableEndpoints);
    fillPermittableEndpointsFromHandlerMethods(acceptedTokenTypes, withAppName, this.endpointHandlerMapping.getHandlerMethods(), permittableEndpoints);

    if (acceptedTokenTypes.contains(AcceptedTokenType.SYSTEM)) {
      final PermittableEndpoint permittableEndpoint = new PermittableEndpoint();
      if (withAppName)
        permittableEndpoint.setPath(applicationName + "/initialize");
      else
        permittableEndpoint.setPath("/initialize");

      permittableEndpoint.setMethod("POST");

      permittableEndpoint.setAcceptTokenIntendedForForeignApplication(false);

      permittableEndpoints.add(permittableEndpoint);
    }

    return permittableEndpoints;
  }

  private static class WhatINeedToBuildAPermittableEndpoint
  {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    final Permittable annotation;
    final Set<String> patterns;
    final Set<RequestMethod> methods;

    WhatINeedToBuildAPermittableEndpoint(final @Nonnull Permittable annotation,
                                         final @Nonnull Set<String> patterns,
                                         final @Nonnull Set<RequestMethod> methods) {
      this.annotation = annotation;
      this.patterns = patterns;
      this.methods = methods;
    }
  }

  private void fillPermittableEndpointsFromHandlerMethods(
      final Collection<AcceptedTokenType> acceptedTokenTypes,
      final boolean withAppName,
      final Map<RequestMappingInfo, HandlerMethod> handlerMethods,
      final @Nonnull Set<PermittableEndpoint> permittableEndpoints) {
    handlerMethods.entrySet()
        .stream().flatMap(PermittableService::whatINeedToBuildAPermittableEndpoint)
        .filter(whatINeedToBuildAPermittableEndpoint -> acceptedTokenTypes.contains(getAcceptedTokenType(whatINeedToBuildAPermittableEndpoint)))
        .forEachOrdered(whatINeedToBuildAPermittableEndpoint ->
             whatINeedToBuildAPermittableEndpoint.patterns.stream()
            .forEachOrdered(pattern -> whatINeedToBuildAPermittableEndpoint.methods
                .stream()
                .forEachOrdered(method -> {
                  final PermittableEndpoint permittableEndpoint = new PermittableEndpoint();
                  permittableEndpoint.setPath(getPath(
                          withAppName ? applicationName.toString() : "",
                          pattern,
                          whatINeedToBuildAPermittableEndpoint));
                  permittableEndpoint.setMethod(method.name());
                  permittableEndpoint.setGroupId(whatINeedToBuildAPermittableEndpoint.annotation.groupId());
                  permittableEndpoint.setAcceptTokenIntendedForForeignApplication(whatINeedToBuildAPermittableEndpoint.annotation.acceptTokenIntendedForForeignApplication());
                  permittableEndpoints.add(permittableEndpoint);
                })
            ));
  }

  static private AcceptedTokenType getAcceptedTokenType(final @Nonnull WhatINeedToBuildAPermittableEndpoint whatINeedToBuildAPermittableEndpoint) {
    return whatINeedToBuildAPermittableEndpoint.annotation.value();
  }

  static private String getPath(final @Nonnull String applicationName,
                                final @Nonnull String pattern,
                                final @Nonnull WhatINeedToBuildAPermittableEndpoint whatINeedToBuildAPermittableEndpoint) {
    final String programmerSpecifiedEndpoint = whatINeedToBuildAPermittableEndpoint.annotation.permittedEndpoint();
    if (!programmerSpecifiedEndpoint.isEmpty())
      return applicationName + programmerSpecifiedEndpoint;

    final StringBuilder ret = new StringBuilder(applicationName);

    PermissionSegmentMatcher.getServletPathSegmentMatchers(pattern).stream()   //parse the pattern into segments
            .map(x -> x.isParameterSegment() ? "*" : x.getPermissionSegment()) //replace the parameter segments with stars.
            .filter(x -> !x.isEmpty())                                         //remove any empty segments
            .forEachOrdered(x -> ret.append("/").append(x));                   //reassemble into a string.

    return ret.toString();
  }

  static private Stream<WhatINeedToBuildAPermittableEndpoint> whatINeedToBuildAPermittableEndpoint(
          final Map.Entry<RequestMappingInfo, HandlerMethod> handlerMethod) {
    final Set<Permittable> annotations = getPermittableAnnotations(handlerMethod);
    final Set<String> patterns = handlerMethod.getKey().getPatternsCondition().getPatterns();
    final Set<RequestMethod> methods = handlerMethod.getKey().getMethodsCondition().getMethods();
    return annotations.stream()
            .map(annotation -> new WhatINeedToBuildAPermittableEndpoint(annotation, patterns, methods));
  }

  static private Set<Permittable> getPermittableAnnotations(Map.Entry<RequestMappingInfo, HandlerMethod> handlerMethod) {
    final Method method = handlerMethod.getValue().getMethod();
    final Set<Permittable> ret = AnnotationUtils.getRepeatableAnnotations(method, Permittable.class);
    if (ret.isEmpty())
      return Collections.singleton(defaultPermittable());
    else
      return ret;
  }

  static private Permittable defaultPermittable() {
    return new Permittable() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return Permittable.class;
      }

      @Override
      public AcceptedTokenType value() {
        return AcceptedTokenType.SYSTEM;
      }

      @Override
      public String groupId() {
        return "";
      }

      @Override
      public String permittedEndpoint() {
        return "";
      }

      @Override
      public boolean acceptTokenIntendedForForeignApplication() {
        return false;
      }
    };
  }
}
