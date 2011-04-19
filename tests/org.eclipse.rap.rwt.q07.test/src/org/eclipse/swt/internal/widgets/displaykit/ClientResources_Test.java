/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.displaykit;

import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

import org.eclipse.rwt.Fixture;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.resources.IResourceManager;


public class ClientResources_Test extends TestCase {

  private ClientResources clientResources;
  private IResourceManager resourceManager;

  public void testRegisterResources() throws Exception {
    clientResources.registerResources();
    assertTrue( resourceManager.isRegistered( "client.js" ) );
    assertFalse( resourceManager.isRegistered( "qx/lang/Core.js" ) );
  }

  public void testRegisterResourcesDebug() throws Exception {
    System.setProperty( "org.eclipse.rwt.clientLibraryVariant", "DEBUG" );
    clientResources.registerResources();
    assertFalse( resourceManager.isRegistered( "client.js" ) );
    assertTrue( resourceManager.isRegistered( "qx/lang/Core.js" ) );
  }
  
  public void testRegisterResourcesWithCustomContextLoader() {
    URLClassLoader contextLoader = new URLClassLoader( new URL[ 0 ] );
    resourceManager.setContextLoader( contextLoader );
    clientResources.registerResources();
    assertSame( contextLoader, resourceManager.getContextLoader() );
  }

  protected void setUp() throws Exception {
    Fixture.setUp();
    resourceManager = RWT.getResourceManager();
    clientResources = new ClientResources( resourceManager );
  }

  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }
}