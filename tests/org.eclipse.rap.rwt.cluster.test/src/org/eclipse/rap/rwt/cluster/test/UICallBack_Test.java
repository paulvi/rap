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
package org.eclipse.rap.rwt.cluster.test;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

import org.eclipse.rap.rwt.cluster.test.entrypoints.UICallbackEntryPoint;
import org.eclipse.rap.rwt.cluster.testfixture.ClusterFixture;
import org.eclipse.rap.rwt.cluster.testfixture.client.RWTClient;
import org.eclipse.rap.rwt.cluster.testfixture.client.Response;
import org.eclipse.rap.rwt.cluster.testfixture.server.IServletEngine;
import org.eclipse.rap.rwt.cluster.testfixture.server.TomcatFactory;
import org.eclipse.rwt.internal.lifecycle.UICallBackManager;
import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.swt.widgets.Display;


@SuppressWarnings("restriction")
public class UICallBack_Test extends TestCase {

  private IServletEngine servletEngine;
  private RWTClient client;
  
  public void testUICallbackRequestResponse() throws Exception {
    servletEngine.start( UICallbackEntryPoint.class );
    client.sendStartupRequest();
    client.sendInitializationRequest();
    HttpSession session = ClusterFixture.getFirstSession( servletEngine );
    final Display display = ClusterFixture.getSessionDisplay( session );

    Thread thread = new Thread( new Runnable() {
      public void run() {
        sleep( 2000 );
        UICallBack.runNonUIThreadWithFakeContext( display, new Runnable() {
          public void run() {
            UICallBackManager uiCallBackManager = UICallBackManager.getInstance();
            uiCallBackManager.setRequestCheckInterval( 500 );
            uiCallBackManager.setHasRunnables( true );
            uiCallBackManager.releaseBlockedRequest();
          }
        } );
      }
    } );
    thread.setDaemon( true );
    thread.start();
    
    Response response = client.sendUICallBackRequest( 0 );
    thread.join();

    String expected = "org.eclipse.swt.Request.getInstance().send();";
    assertEquals( expected, response.getContentText().trim() );
  }

  public void testAbortConnectionDuringUICallbackRequest() throws Exception {
    servletEngine.start( UICallbackEntryPoint.class );
    client.sendStartupRequest();
    client.sendInitializationRequest();
    configureCallbackRequestCheckInterval( 400 );

    try {
      client.sendUICallBackRequest( 200 );
      fail();
    } catch( IOException expected ) {
      assertEquals( "Read timed out", expected.getMessage() );
    }
    
    sleep( 800 );
    
    UICallBackManager uiCallBackManager = getUICallBackManager();
    assertFalse( uiCallBackManager.isCallBackRequestBlocked() );
  }

  protected void setUp() throws Exception {
    ClusterFixture.setUp();
    servletEngine = new TomcatFactory().createServletEngine();
    client = new RWTClient( servletEngine );
  }

  protected void tearDown() throws Exception {
    servletEngine.stop();
    ClusterFixture.tearDown();
  }

  private UICallBackManager getUICallBackManager() {
    final UICallBackManager[] result = { null };
    HttpSession session = ClusterFixture.getFirstSession( servletEngine );
    Display display = ClusterFixture.getSessionDisplay( session );
    UICallBack.runNonUIThreadWithFakeContext( display, new Runnable() {
      public void run() {
        result[ 0 ] = UICallBackManager.getInstance();
      }
    } );
    return result[ 0 ];
  }

  private void sleep( int duration ) {
    try {
      Thread.sleep( duration );
    } catch( InterruptedException ie ) {
      throw new RuntimeException( ie );
    }
  }

  private void configureCallbackRequestCheckInterval( final int interval ) {
    HttpSession session = ClusterFixture.getFirstSession( servletEngine );
    Display display = ClusterFixture.getSessionDisplay( session );
    UICallBack.runNonUIThreadWithFakeContext( display, new Runnable() {
      public void run() {
        UICallBackManager.getInstance().setRequestCheckInterval( interval );
      }
    } );
  }
}