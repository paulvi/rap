/*******************************************************************************
 * Copyright (c) 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.rap.rwt.client.Client;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.internal.service.UISessionImpl;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.TestSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ClientSelector_Test {

  private ClientSelector clientSelector;
  private UISession uiSession;

  @Before
  public void setUp() {
    Fixture.setUp();
    uiSession = new UISessionImpl( new TestSession() );
    clientSelector = new ClientSelector();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testNoClientSelectedByDefault() {
    assertNull( clientSelector.getSelectedClient( uiSession ) );
  }

  @Test
  public void testSelectsMatchingClient() {
    Client client = mock( Client.class );
    clientSelector.addClientProvider( mockClientProvider( true, client ) );

    clientSelector.selectClient( mockRequest(), uiSession );

    assertSame( client, clientSelector.getSelectedClient( uiSession ) );
  }

  @Test
  public void testSelectFailsWithoutClientProviders() {
    try {
      clientSelector.selectClient( mockRequest(), uiSession );
      fail();
    } catch( IllegalStateException exception ) {
      assertEquals( "No client provider found for request", exception.getMessage() );
    }
  }

  @Test
  public void testSelectFailsWithoutMatchingClientProviders() {
    clientSelector.addClientProvider( mockClientProvider( false, null ) );

    try {
      clientSelector.selectClient( mockRequest(), uiSession );
      fail();
    } catch( Exception exception ) {
      assertEquals( "No client provider found for request", exception.getMessage() );
    }
  }

  @Test
  public void testSelectsFirstMatchingClient() {
    Client expected = mock( Client.class );
    clientSelector.addClientProvider( mockClientProvider( false, null ) );
    clientSelector.addClientProvider( mockClientProvider( true, expected ) );
    clientSelector.addClientProvider( mockClientProvider( true, mock( Client.class ) ) );

    clientSelector.selectClient( mockRequest(), uiSession );

    assertSame( expected, clientSelector.getSelectedClient( uiSession ) );
  }

  @Test
  public void testActivateInstallsWebClientProvider() {
    clientSelector.activate();

    clientSelector.selectClient( mockRequest(), uiSession );

    assertTrue( clientSelector.getSelectedClient( uiSession ) instanceof WebClient );
  }

  @Test
  public void testWebClientDoesNotOverrideOthers() {
    Client client = mock( Client.class );
    clientSelector.addClientProvider( mockClientProvider( true, client ) );
    clientSelector.activate();

    clientSelector.selectClient( mockRequest(), uiSession );

    assertSame( client, clientSelector.getSelectedClient( uiSession ) );
  }

  @Test
  public void testFallbackToWebClient() {
    clientSelector.addClientProvider( mockClientProvider( false, null ) );
    clientSelector.activate();

    clientSelector.selectClient( mockRequest(), uiSession );

    assertTrue( clientSelector.getSelectedClient( uiSession ) instanceof WebClient );
  }

  @Test
  public void testCannotActivateTwice() {
    clientSelector.activate();
    try {
      clientSelector.activate();
      fail();
    } catch( IllegalStateException exception ) {
      assertEquals( "ClientSelector already activated", exception.getMessage() );
    }
  }

  @Test
  public void testCannotAddClientProviderAfterActivation() {
    clientSelector.activate();
    try {
      clientSelector.addClientProvider( mock( ClientProvider.class ) );
      fail();
    } catch( IllegalStateException exception ) {
      assertEquals( "ClientSelector already activated", exception.getMessage() );
    }
  }

  private static HttpServletRequest mockRequest() {
    return mock( HttpServletRequest.class );
  }

  private static ClientProvider mockClientProvider( boolean accept, Client client ) {
    ClientProvider provider = mock( ClientProvider.class );
    HttpServletRequest anyReq = any( HttpServletRequest.class );
    when( Boolean.valueOf( provider.accept( anyReq ) ) ).thenReturn( Boolean.valueOf( accept ) );
    when( provider.getClient() ).thenReturn( client );
    return provider;
  }

}
