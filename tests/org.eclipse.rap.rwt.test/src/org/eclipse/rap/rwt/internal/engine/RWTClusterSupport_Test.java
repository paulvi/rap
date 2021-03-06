/*******************************************************************************
 * Copyright (c) 2011, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.engine;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.lifecycle.RequestCounter;
import org.eclipse.rap.rwt.internal.service.UISessionImpl;
import org.eclipse.rap.rwt.service.ApplicationContext;
import org.eclipse.rap.rwt.testfixture.TestRequest;
import org.eclipse.rap.rwt.testfixture.TestResponse;
import org.junit.Before;
import org.junit.Test;


public class RWTClusterSupport_Test {

  private static final String ATTR_UI_SESSION = UISessionImpl.class.getName();
  private static final String ATTR_REQUEST_COUNTER = RequestCounter.class.getName() + "#instance";
  private final static String ATTR_APPLICATION_CONTEXT = ApplicationContextImpl.class.getName()
                                                         + "#instance";
  private RWTClusterSupport rwtClusterSupport;
  private FilterChain chain;
  private TestRequest request;
  private TestResponse response;

  @Before
  public void setUp() {
    request = new TestRequest();
    response = new TestResponse();
    chain = mock( FilterChain.class );
    rwtClusterSupport = new RWTClusterSupport();
  }

  @Test
  public void testDoFilter_passesParametersToFilterChain() throws Exception {
    rwtClusterSupport.doFilter( request, response, chain );

    verify( chain ).doFilter( same( request ), same( response ) );
  }

  @Test
  public void testDoFilter_doesNotFailWithoutHttpSession() throws Exception {
    request.setSession( null );

    rwtClusterSupport.doFilter( request, response, chain );
  }

  @Test
  public void testDoFilter_attachesHttpSessionToUISession() throws Exception {
    UISessionImpl uiSession = new UISessionImpl( mock( HttpSession.class ) );
    HttpSession newHttpSession = mockHttpSession();
    request.setSession( newHttpSession );
    setUISession( newHttpSession, uiSession );

    rwtClusterSupport.doFilter( request, response, chain );

    assertSame( newHttpSession, uiSession.getHttpSession() );
  }

  @Test
  public void testDoFilter_attachesApplicationContextToUISession() throws Exception {
    ApplicationContext applicationContext = mock( ApplicationContextImpl.class );
    HttpSession httpSession = mockHttpSession( mockServletContext( applicationContext ) );
    request.setSession( httpSession );
    UISessionImpl uiSession = new UISessionImpl( httpSession );
    setUISession( httpSession, uiSession );

    rwtClusterSupport.doFilter( request, response, chain );

    assertSame( applicationContext, uiSession.getApplicationContext() );
  }

  @Test
  public void testDoFilter_marksUISessionAsChanged() throws Exception {
    HttpSession httpSession = mockHttpSession();
    request.setSession( httpSession );
    UISessionImpl uiSession = new UISessionImpl( httpSession );
    setUISession( httpSession, uiSession );

    rwtClusterSupport.doFilter( request, response, chain );

    verify( httpSession ).setAttribute( anyString(), same( uiSession ) );
  }

  @Test
  public void testDoFilter_marksRequestCounterAsChanged() throws Exception {
    HttpSession httpSession = mock( HttpSession.class );
    request.setSession( httpSession );

    rwtClusterSupport.doFilter( request, response, chain );

    verify( httpSession ).setAttribute( eq( ATTR_REQUEST_COUNTER ), any( RequestCounter.class ) );
  }

  private static HttpSession mockHttpSession() {
    return mockHttpSession( mock( ServletContext.class ) );
  }

  private static HttpSession mockHttpSession( ServletContext servletContext ) {
    HttpSession httpSession = mock( HttpSession.class );
    when( httpSession.getServletContext() ).thenReturn( servletContext );
    return httpSession;
  }

  private static ServletContext mockServletContext( ApplicationContext applicationContext ) {
    ServletContext servletContext = mock( ServletContext.class );
    when( servletContext.getAttribute( eq( ATTR_APPLICATION_CONTEXT ) ) ).thenReturn( applicationContext );
    return servletContext;
  }

  private static void setUISession( HttpSession httpSession, UISessionImpl uiSession ) {
    when( httpSession.getAttribute( eq( ATTR_UI_SESSION ) ) ).thenReturn( uiSession );
  }

}
