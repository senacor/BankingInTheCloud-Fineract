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

import io.mifos.core.test.servicestarter.aether.Booter;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import java.io.File;
import java.util.Collections;

/**
 * @author Myrle Krantz
 */
class ArtifactResolver {
  private final String artifactoryDirectory;

  ArtifactResolver(final String artifactoryDirectory)
  {
    this.artifactoryDirectory = artifactoryDirectory;
  }

  File getJarFile(final String project, final String group, final String name, final String version)
          throws ArtifactResolutionException {
    final RepositorySystem system = Booter.newRepositorySystem();
    final RepositorySystemSession session = Booter.newRepositorySystemSession(system, artifactoryDirectory);


    final ArtifactRequest artifactRequest = new ArtifactRequest();
    artifactRequest.setArtifact(new DefaultArtifact(group, name, "jar", version));

    final ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);

    return artifactResult.getArtifact().getFile();
  }
}