/*******************************************************************************
 * Copyright (c) 2011, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.internal.theme.JsonValue;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CallOperation;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.rap.rwt.testfixture.Message.DestroyOperation;
import org.eclipse.rap.rwt.testfixture.Message.ListenOperation;
import org.eclipse.rap.rwt.testfixture.Message.SetOperation;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class Message_Test {

  private ProtocolMessageWriter writer;

  @Before
  public void setUp() {
    Fixture.setUp();
    writer = new ProtocolMessageWriter();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testConstructWithNull() {
    try {
      new Message( null );
      fail();
    } catch( NullPointerException expected ) {
    }
  }

  @Test
  public void testConstructWithEmptyString() {
    try {
      new Message( "" );
      fail();
    } catch( IllegalArgumentException expected ) {
      assertTrue( expected.getMessage().contains( "Could not parse json" ) );
    }
  }

  @Test
  public void testConstructWithInvalidJson() {
    try {
      new Message( "{" );
      fail();
    } catch( IllegalArgumentException expected ) {
      assertTrue( expected.getMessage().contains( "Could not parse json" ) );
    }
  }

  @Test
  public void testConstructWithoutOperations() {
    try {
      new Message( "{ \"foo\": 23 }" );
      fail();
    } catch( IllegalArgumentException expected ) {
      assertTrue( expected.getMessage().contains( "Missing operations array" ) );
    }
  }

  @Test
  public void testConstructWithInvalidOperations() {
    try {
      new Message( "{ \"operations\": 23 }" );
      fail();
    } catch( IllegalArgumentException expected ) {
      assertTrue( expected.getMessage().contains( "Missing operations array" ) );
    }
  }

  @Test
  public void testGetOperationCountWhenEmpty() {
    assertEquals( 0, getMessage().getOperationCount() );
  }

  @Test
  public void testGetOperationCount() {
    writer.appendCall( "w1", "method1", null );
    writer.appendCall( "w2", "method2", null );

    assertEquals( 2, getMessage().getOperationCount() );
  }

  @Test
  public void testGetRequestCounter() {
    new Display();
    Fixture.fakeNewRequest();
    Fixture.executeLifeCycleFromServerThread();
    assertEquals( 1, Fixture.getProtocolMessage().getRequestCounter() );
  }

  @Test
  public void testGetOperation() {
    writer.appendCall( "w2", "method", null );

    assertNotNull( getMessage().getOperation( 0 ) );
  }

  @Test
  public void testGetCreateOperation() {
    writer.appendCreate( "w1", "type" );

    assertTrue( getMessage().getOperation( 0 ) instanceof CreateOperation );
  }

  @Test
  public void testGetCallOperation() {
    writer.appendCall( "w2", "method", null );

    assertTrue( getMessage().getOperation( 0 ) instanceof CallOperation );
  }

  @Test
  public void testGetSetOperation() {
    writer.appendSet( "w1", "key", true );

    assertTrue( getMessage().getOperation( 0 ) instanceof SetOperation );
  }

  @Test
  public void testGetListenOperation() {
    writer.appendListen( "w1", "event", true );

    assertTrue( getMessage().getOperation( 0 ) instanceof ListenOperation );
  }

  @Test
  public void testGetDestroyOperation() {
    writer.appendDestroy( "w1" );

    assertTrue( getMessage().getOperation( 0 ) instanceof DestroyOperation );
  }

  @Test
  public void testGetOperationWithUnknownType() {
    Message message = new Message( "{ \"operations\" : [ { \"action\" : \"foo\" } ] }" );
    try {
      message.getOperation( 0 );
      fail();
    } catch( IllegalStateException expected ) {
    }
  }

  @Test
  public void testGetOperationPosition() {
    writer.appendCreate( "w1", "type" );
    writer.appendCreate( "w2", "type" );

    Message message = getMessage();
    assertEquals( 0, message.getOperation( 0 ).getPosition() );
    assertEquals( 1, message.getOperation( 1 ).getPosition() );
  }

  @Test
  public void testCreateOperation() {
    writer.appendCreate( "w1", "type" );

    CreateOperation operation = ( CreateOperation )getMessage().getOperation( 0 );
    assertEquals( "w1", operation.getTarget() );
    assertEquals( "type", operation.getType() );
  }

  @Test
  public void testCallOperation() {
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put( "key1", "a" );
    properties.put( "key2", new Integer( 2 ) );
    writer.appendCall( "w2", "method", properties );

    CallOperation operation = ( CallOperation )getMessage().getOperation( 0 );
    assertEquals( "w2", operation.getTarget() );
    assertEquals( "method", operation.getMethodName() );
    assertEquals( "a", operation.getProperty( "key1" ) );
    assertEquals( new Integer( 2 ), operation.getProperty( "key2" ) );
  }

  @Test
  public void testSetOperation() {
    writer.appendSet( "w1", "key", true );
    writer.appendSet( "w1", "key2", "value" );

    SetOperation operation = ( SetOperation )getMessage().getOperation( 0 );
    assertEquals( "w1", operation.getTarget() );
    assertEquals( Boolean.TRUE, operation.getProperty( "key" ) );
    assertEquals( "value", operation.getProperty( "key2" ) );
  }

  @Test
  public void testListenOperation() {
    writer.appendListen( "w1", "event", true );
    writer.appendListen( "w1", "event2", false );

    ListenOperation operation = ( ListenOperation )getMessage().getOperation( 0 );
    assertTrue( operation.listensTo( "event" ) );
    assertFalse( operation.listensTo( "event2" ) );
  }

  @Test
  public void testOperationGetPropertyNames() {
    writer.appendSet( "w1", "key", true );
    SetOperation operation = ( SetOperation )getMessage().getOperation( 0 );

    assertEquals( 1, operation.getPropertyNames().size() );
    assertTrue( operation.getPropertyNames().contains( "key" ) );
  }

  @Test
  public void testOperationGetPropertyNamesWhenEmpty() {
    writer.appendCall( "w1", "foo", null );
    CallOperation operation = ( CallOperation )getMessage().getOperation( 0 );

    try {
      operation.getPropertyNames();
      fail();
    } catch( IllegalStateException expected ) {
    }
  }

  @Test
  public void testFindSetOperation() {
    writer.appendSet( "w1", "key", true );

    Message message = getMessage();

    SetOperation operation = message.findSetOperation( "w1", "key" );
    assertEquals( Boolean.TRUE, operation.getProperty( "key" ) );
  }

  @Test
  public void testFindSetOperationFailed() {
    writer.appendSet( "w1", "key1", true );

    Message message = getMessage();

    assertNull( message.findSetOperation( "w1", "key2" ) );
    assertNull( message.findSetOperation( "w2", "key1" ) );
  }

  @Test
  public void testFindSetProperty() {
    writer.appendSet( "w1", "key", true );

    Message message = getMessage();

    assertEquals( Boolean.TRUE, message.findSetProperty( "w1", "key" ) );
  }

  @Test
  public void testFindSetPropertyFailed() {
    writer.appendSet( "w1", "key1", true );

    Message message = getMessage();

    try {
      message.findSetProperty( "w1", "key2" );
      fail();
    } catch( IllegalStateException exception ) {
      //expected
    }
    try {
      message.findSetProperty( "w2", "key1" );
      fail();
    } catch( IllegalStateException exception ) {
      //expected
    }
  }

  @Test
  public void testFindListenOperation() {
    writer.appendListen( "w1", "key", true );

    Message message = getMessage();

    ListenOperation operation = message.findListenOperation( "w1", "key" );
    assertEquals( Boolean.TRUE, operation.getProperty( "key" ) );
  }

  @Test
  public void testFindListenOperationFailed() {
    writer.appendListen( "w1", "key1", true );

    Message message = getMessage();

    assertNull( message.findListenOperation( "w1", "key2" ) );
    assertNull( message.findListenOperation( "w2", "key1" ) );
  }

  @Test
  public void testFindListenProperty() {
    writer.appendListen( "w1", "key", true );

    Message message = getMessage();

    assertEquals( Boolean.TRUE, message.findListenProperty( "w1", "key" ) );
  }

  @Test
  public void testFindListenPropertyFailed() {
    writer.appendListen( "w1", "key1", true );

    Message message = getMessage();

    try {
      message.findListenProperty( "w1", "key2" );
      fail();
    } catch( IllegalStateException exception ) {
      //expected
    }
    try {
      message.findListenProperty( "w2", "key1" );
      fail();
    } catch( IllegalStateException exception ) {
      //expected
    }
  }

  @Test
  public void testFindCreateOperation() {
    writer.appendCreate( "w2", "myType" );
    writer.appendSet( "w2", "key", true );

    Message message = getMessage();

    CreateOperation operation = message.findCreateOperation( "w2" );
    assertEquals( "w2", operation.getTarget() );
    assertEquals( "myType", operation.getType() );
    assertEquals( Boolean.TRUE, operation.getProperty( "key" ) );
  }

  @Test
  public void testFindCreateFailed() {
    writer.appendCreate( "w2", "myType" );

    Message message = getMessage();

    assertNull( message.findCreateOperation( "w1" ) );
  }

  @Test
  public void testFindCreateProperty() {
    writer.appendCreate( "w2", "myType" );
    writer.appendSet( "w2", "key", true );

    Message message = getMessage();

    assertEquals( Boolean.TRUE, message.findCreateProperty( "w2", "key" ) );
  }

  @Test
  public void testFindCreatePropertyFailed() {
    writer.appendCreate( "w2", "myType" );
    writer.appendSet( "w2", "key1", true );

    Message message = getMessage();

    try {
      message.findCreateProperty( "w1", "key1" );
      fail();
    } catch( IllegalStateException exception ) {
      //expected
    }
    try {
      message.findCreateProperty( "w2", "key2" );
      fail();
    } catch( IllegalStateException exception ) {
      //expected
    }
  }

  @Test
  public void testFindCallOperation() {
    writer.appendCall( "w1", "method", null );

    Message message = getMessage();

    CallOperation operation = message.findCallOperation( "w1", "method" );
    assertEquals( "w1", operation.getTarget() );
    assertEquals( "method", operation.getMethodName() );
  }

  @Test
  public void testFindCallOperationFailed() {
    writer.appendCall( "w2", "method1", null );
    writer.appendCall( "w1", "method2", null );

    Message message = getMessage();

    assertNull( message.findCallOperation( "w1", "method1" ) );
  }

  @Test
  public void testOperationGetProperty() {
    writer.appendSet( "w1", "foo", 23 );
    SetOperation operation = ( SetOperation )getMessage().getOperation( 0 );

    assertEquals( Integer.valueOf( 23 ), operation.getProperty( "foo" ) );
  }

  @Test
  public void testOperationGetPropertyWithNonExistingValue() {
    writer.appendSet( "w1", "foo", 23 );

    SetOperation operation = ( SetOperation )getMessage().getOperation( 0 );
    try {
      operation.getProperty( "bar" );
      fail();
    } catch ( IllegalStateException expected ) {
    }
  }


  @Test
  public void testNonExistingOperation() {
    writer.appendSet( "w1", "key", true );

    try {
      getMessage().getOperation( 1 );
      fail();
    } catch ( IllegalStateException expected ) {
    }
  }

  @Test
  public void testGetError() {
    writer.appendHead( "error", JsonValue.valueOf( "test error" ) );

    assertEquals( "test error", getMessage().getError() );
  }

  @Test
  public void testGetNoError() {
    try {
      getMessage().getError();
      fail();
    } catch ( RuntimeException expected ) {
    }
  }

  @Test
  public void testGetErrorMessage() {
    writer.appendHead( "message", JsonValue.valueOf( "test message" ) );

    assertEquals( "test message", getMessage().getErrorMessage() );
  }

  @Test
  public void testGetNoErrorMessage() {
    try {
      getMessage().getErrorMessage();
      fail();
    } catch ( RuntimeException expected ) {
    }
  }

  private Message getMessage() {
    return new Message( writer.createMessage() );
  }

}
