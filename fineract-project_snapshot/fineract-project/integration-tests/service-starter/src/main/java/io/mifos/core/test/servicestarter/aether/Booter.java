/*******************************************************************************
 * Copyright (c) 2010, 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package io.mifos.core.test.servicestarter.aether;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.LocalRepository;


public class Booter {

  public static RepositorySystem newRepositorySystem()
  {
    return ManualRepositorySystemFactory.newRepositorySystem();
    // return org.eclipse.aether.examples.guice.GuiceRepositorySystemFactory.newRepositorySystem();
    // return org.eclipse.aether.examples.sisu.SisuRepositorySystemFactory.newRepositorySystem();
    // return org.eclipse.aether.examples.plexus.PlexusRepositorySystemFactory.newRepositorySystem();
  }


  public static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system, String artifactoryDirectory)
  {
    DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

    LocalRepository localRepo = new LocalRepository(artifactoryDirectory);
    session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );

    session.setTransferListener( new ConsoleTransferListener() );
    session.setRepositoryListener( new ConsoleRepositoryListener() );

    // uncomment to generate dirty trees
    // session.setDependencyGraphTransformer( null );

    return session;
  }
}
