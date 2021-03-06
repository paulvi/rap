/*******************************************************************************
* Copyright (c) 2011, 2013 EclipseSource and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    EclipseSource - initial API and implementation
*******************************************************************************/
package org.eclipse.rap.rwt.internal.protocol;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.internal.lifecycle.DisplayUtil;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CallOperation;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.rap.rwt.testfixture.Message.DestroyOperation;
import org.eclipse.rap.rwt.testfixture.Message.ListenOperation;
import org.eclipse.rap.rwt.testfixture.Message.SetOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ProtocolMessageWriter_Test {

  private ProtocolMessageWriter writer;
  private Shell shell;

  @Before
  public void setUp() {
    Fixture.setUp();
    Display display = new Display();
    shell = new Shell( display );
    writer = new ProtocolMessageWriter();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testHasNoOperations() {
    assertFalse( writer.hasOperations() );
  }

  @Test
  public void testHasOperationsAfterAppend() {
    writer.appendSet( "target", "foo", 23 );

    assertTrue( writer.hasOperations() );
  }

  @Test
  public void testEmptyMessage() throws JSONException {
    String messageString = writer.createMessage();
    JSONObject message = new JSONObject( messageString );
    JSONObject head = message.getJSONObject( "head" );
    assertEquals( 0, head.length() );
    JSONArray operations = message.getJSONArray( "operations" );
    assertEquals( 0, operations.length() );
  }

  @Test
  public void testMessageWithRequestCounter() {
    writer.appendHead( ProtocolConstants.REQUEST_COUNTER, 1 );

    assertEquals( 1, getMessage().getRequestCounter() );
  }

  @Test
  public void testWriteMessageTwice() {
    writer.createMessage();
    try {
      writer.createMessage();
      fail();
    } catch( IllegalStateException expected ) {
    }
  }

  @Test
  public void testAppendAfterCreate() {
    writer.createMessage();
    try {
      writer.appendDestroy( "target" );
      fail();
    } catch( IllegalStateException expected ) {
    }
  }

  @Test
  public void testMessageWithCall() {
    String shellId = WidgetUtil.getId( shell );
    String methodName = "methodName";
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put( "key1", "a" );
    properties.put( "key2", "b" );

    writer.appendCall( shellId, methodName, properties );

    CallOperation operation = (CallOperation)getMessage().getOperation( 0 );
    assertEquals( shellId, operation.getTarget() );
    assertEquals( methodName, operation.getMethodName() );
    assertEquals( "a", operation.getProperty( "key1" ) );
    assertEquals( "b", operation.getProperty( "key2" ) );
  }

  @Test
  public void testMessageWithTwoCallss() {
    String shellId = WidgetUtil.getId( shell );
    String methodName = "methodName";
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put( "key1", new Integer( 5 ) );
    properties.put( "key2", "b" );
    properties.put( "key3", Boolean.FALSE );

    writer.appendCall( shellId, methodName, null );
    writer.appendCall( shellId, methodName, properties );

    CallOperation operation = ( CallOperation )getMessage().getOperation( 1 );
    assertEquals( shellId, operation.getTarget() );
    assertEquals( methodName, operation.getMethodName() );
    assertEquals( new Integer( 5 ), operation.getProperty( "key1" ) );
    assertEquals( "b", operation.getProperty( "key2" ) );
    assertEquals( Boolean.FALSE, operation.getProperty( "key3" ) );
  }

  @Test
  public void testMessageWithCreate() {
    String displayId = DisplayUtil.getId( shell.getDisplay() );
    String shellId = WidgetUtil.getId( shell );
    String[] styles = new String[] { "TRIM", "FOO" };

    writer.appendCreate( shellId, "org.Text" );
    writer.appendSet( shellId, "parent", displayId );
    writer.appendSet( shellId, "style", styles );
    writer.appendSet( shellId, "key1", "a" );
    writer.appendSet( shellId, "key2", "b" );

    CreateOperation operation = ( CreateOperation )getMessage().getOperation( 0 );
    assertEquals( shellId, operation.getTarget() );
    assertEquals( displayId, operation.getParent() );
    assertEquals( "org.Text", operation.getType() );
    assertArrayEquals( styles, operation.getStyles() );
    assertEquals( "a", operation.getProperty( "key1" ) );
    assertEquals( "b", operation.getProperty( "key2" ) );
  }

  @Test
  public void testMessageWithMultipleOperations() {
    Button button = new Button( shell, SWT.PUSH );
    String shellId = WidgetUtil.getId( shell );
    String buttonId = WidgetUtil.getId( button );

    writer.appendCreate( shellId, "org.Text" );
    writer.appendCreate( buttonId, "org.Shell" );

    Message message = getMessage();
    assertTrue( message.getOperation( 0 ) instanceof CreateOperation );
    assertTrue( message.getOperation( 1 ) instanceof CreateOperation );
  }

  @Test
  public void testMessageWithIllegalParameterType() {
    Button wrongParameter = new Button( shell, SWT.PUSH );
    String shellId = WidgetUtil.getId( shell );

    try {
      writer.appendSet( shellId, "text", wrongParameter );
      fail();
    } catch ( IllegalArgumentException expected ) {
    }
  }

  @Test
  public void testMessageWithDestroy() {
    Button button = new Button( shell, SWT.PUSH );
    String buttonId = WidgetUtil.getId( button );

    writer.appendDestroy( buttonId );

    DestroyOperation operation = ( DestroyOperation )getMessage().getOperation( 0 );
    assertEquals( buttonId, operation.getTarget() );
  }

  @Test
  public void testMessageWithDestroyTwice() {
    Button button = new Button( shell, SWT.PUSH );
    String shellId = WidgetUtil.getId( shell );
    String buttonId = WidgetUtil.getId( button );

    writer.appendDestroy( buttonId );
    writer.appendDestroy( shellId );

    Message message = getMessage();
    assertTrue( message.getOperation( 0 ) instanceof DestroyOperation );
    assertTrue( message.getOperation( 1 ) instanceof DestroyOperation );
  }

  @Test
  public void testMessageWithListen() {
    Button button = new Button( shell, SWT.PUSH );
    String buttonId = WidgetUtil.getId( button );

    writer.appendListen( buttonId, "selection", false );
    writer.appendListen( buttonId, "focus", true );
    writer.appendListen( buttonId, "fake", true );

    ListenOperation operation = ( ListenOperation )getMessage().getOperation( 0 );
    assertEquals( buttonId, operation.getTarget() );
    assertFalse( operation.listensTo( "selection" ) );
    assertTrue( operation.listensTo( "focus" ) );
    assertTrue( operation.listensTo( "fake" ) );
  }

  @Test
  public void testMessageWithExecuteScript() {
    String script = "var c = 4; c++;";
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put( "content", script );

    writer.appendCall( "jsex", "execute", properties );

    CallOperation operation = ( CallOperation )getMessage().getOperation( 0 );
    assertEquals( "jsex", operation.getTarget() );
    assertEquals( script, operation.getProperty( "content" ) );
  }

  @Test
  public void testAppendSet_appendsSequentialPropertiesToSameOperation() {
    writer.appendSet( "id", "property-1", "value-1" );
    writer.appendSet( "id", "property-2", 23 );

    Message message = getMessage();
    assertEquals( 1, message.getOperationCount() );
    assertEquals( "value-1", message.getOperation( 0 ).getProperty( "property-1" ) );
    assertEquals( Integer.valueOf( 23 ), message.getOperation( 0 ).getProperty( "property-2" ) );
  }

  @Test
  public void testAppendSet_createsSeparateOperationsForDifferentTargets() {
    writer.appendSet( "id-1", "property", "value-1" );
    writer.appendSet( "id-2", "property", "value-2" );

    Message message = getMessage();
    assertEquals( 2, message.getOperationCount() );
    assertEquals( "id-1", message.getOperation( 0 ).getTarget() );
    assertEquals( "value-2", message.getOperation( 1 ).getProperty( "property" ) );
    assertEquals( "id-2", message.getOperation( 1 ).getTarget() );
    assertEquals( "value-2", message.getOperation( 1 ).getProperty( "property" ) );
  }

  @Test
  public void testAppendSet_overwritesDuplicatePropertyInSameOperation() {
    writer.appendSet( "id", "property", "value-1" );
    writer.appendSet( "id", "another-property", true );
    writer.appendSet( "id", "property", "value-2" );

    Message message = getMessage();
    assertEquals( 1, message.getOperationCount() );
    assertEquals( "value-2", message.getOperation( 0 ).getProperty( "property" ) );
  }

  @Test
  public void testAppendSet_createsNewOperationWhenInterruptedByAnotherOperation() {
    writer.appendSet( "id", "property", "value-1" );
    writer.appendCall( "id", "method", null );
    writer.appendSet( "id", "property", "value-2" );

    Message message = getMessage();
    assertEquals( 3, message.getOperationCount() );
    assertEquals( "value-1", message.getOperation( 0 ).getProperty( "property" ) );
    assertEquals( "value-2", message.getOperation( 2 ).getProperty( "property" ) );
  }

  @Test
  public void testAppendSet_createsNewOperationWhenInterruptedBySetForDifferentTarget() {
    writer.appendSet( "id-1", "property", "value-1" );
    writer.appendSet( "id-2", "property", "value-2" );
    writer.appendSet( "id-1", "property", "value-3" );

    Message message = getMessage();
    assertEquals( 3, message.getOperationCount() );
    assertEquals( "id-1", message.getOperation( 0 ).getTarget() );
    assertEquals( "value-1", message.getOperation( 0 ).getProperty( "property" ) );
    assertEquals( "id-2", message.getOperation( 1 ).getTarget() );
    assertEquals( "value-2", message.getOperation( 1 ).getProperty( "property" ) );
    assertEquals( "id-1", message.getOperation( 2 ).getTarget() );
    assertEquals( "value-3", message.getOperation( 2 ).getProperty( "property" ) );
  }

  @Test
  public void testMessageWithMixedOperations() {
    Button button = new Button( shell, SWT.PUSH );
    addShellCreate( shell );
    addShellListeners( shell );
    addButtonCreate( button );
    addButtonCall( button );
    String shellId = WidgetUtil.getId( shell );
    String buttonId = WidgetUtil.getId( button );

    Message message = getMessage();
    assertEquals( 4, message.getOperationCount() );

    CreateOperation shellCreateOperation = ( CreateOperation )message.getOperation( 0 );
    assertEquals( shellId, shellCreateOperation.getTarget() );
    assertEquals( 2, shellCreateOperation.getPropertyNames().size() );

    ListenOperation shellListenOperation = ( ListenOperation )message.getOperation( 1 );
    assertEquals( shellId, shellListenOperation.getTarget() );
    assertEquals( 2, shellListenOperation.getPropertyNames().size() );

    CreateOperation buttonCreateOperation = ( CreateOperation )message.getOperation( 2 );
    assertEquals( buttonId, buttonCreateOperation.getTarget() );
    assertEquals( 3, buttonCreateOperation.getPropertyNames().size() );

    CallOperation buttonCallOperation = ( CallOperation )message.getOperation( 3 );
    assertEquals( buttonId, buttonCallOperation.getTarget() );
    assertEquals( 1, buttonCallOperation.getPropertyNames().size() );
  }

  private void addShellCreate( Shell shell ) {
    String shellId = WidgetUtil.getId( shell );
    writer.appendCreate( shellId, "org.eclipse.swt.widgets.Shell" );
    writer.appendSet( shellId, "styles", new String[]{ "SHELL_TRIM" } );
    writer.appendSet( shellId, "foo", 23 );
  }

  private void addShellListeners( Shell shell ) {
    writer.appendListen( WidgetUtil.getId( shell ), "event1", true );
    writer.appendListen( WidgetUtil.getId( shell ), "event2", false );
  }

  private void addButtonCreate( Button button ) {
    String buttonId = WidgetUtil.getId( button );
    String shellId = WidgetUtil.getId( shell );
    writer.appendCreate( buttonId, "org.eclipse.swt.widgets.Button" );
    writer.appendSet( buttonId, "parent", shellId );
    writer.appendSet( buttonId, "styles", new String[] { "PUSH", "BORDER" } );
    writer.appendSet( buttonId, "text", "foo" );
  }

  private void addButtonCall( Button button ) {
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put( "key1", "a1" );
    writer.appendCall( WidgetUtil.getId( button ), "select", properties );
  }

  @Test
  public void testAppendsToExistingSetOperation() {
    String shellId = WidgetUtil.getId( shell );

    writer.appendSet( shellId, "key1", "value1" );
    writer.appendSet( shellId, "key2", "value2" );

    Message message = getMessage();
    SetOperation operation = ( SetOperation )message.getOperation( 0 );
    assertEquals( "value1", operation.getProperty( "key1" ) );
    assertEquals( "value2", operation.getProperty( "key2" ) );
  }

  @Test
  public void testAppendsToExistingCreateOperation() {
    String shellId = WidgetUtil.getId( shell );

    writer.appendCreate( shellId, "foo.Class" );
    writer.appendSet( shellId, "key1", "value1" );
    writer.appendSet( shellId, "key2", "value2" );

    Message message = getMessage();
    CreateOperation createOperation = ( CreateOperation )message.getOperation( 0 );
    assertEquals( "value1", createOperation.getProperty( "key1" ) );
    assertEquals( "value2", createOperation.getProperty( "key2" ) );
  }

  @Test
  public void testDoesNotAppendToOtherWidgetsOperation() {
    Button button = new Button( shell, SWT.PUSH );
    String shellId = WidgetUtil.getId( shell );
    String buttonId = WidgetUtil.getId( button );

    writer.appendSet( shellId, "key1", "value1" );
    writer.appendSet( buttonId, "key2", "value2" );

    Message message = getMessage();
    SetOperation firstOperation = ( SetOperation )message.getOperation( 0 );
    assertEquals( "value1", firstOperation.getProperty( "key1" ) );
    assertFalse( firstOperation.getPropertyNames().contains( "key2" ) );
  }

  @Test
  public void testStartsNewOperation() {
    Button button = new Button( shell, SWT.PUSH );
    String shellId = WidgetUtil.getId( shell );
    String buttonId = WidgetUtil.getId( button );

    writer.appendCreate( shellId, "foo.Class" );
    writer.appendCreate( buttonId, "org.eclipse.swt.widgets.Button" );
    writer.appendSet( buttonId, "parent", shellId );
    writer.appendSet( buttonId, "key1", "value1" );
    writer.appendSet( buttonId, "key2", "value2" );

    Message message = getMessage();
    CreateOperation createOperation = ( CreateOperation )message.getOperation( 1 );
    assertEquals( shellId, createOperation.getParent() );
    assertEquals( "value1", createOperation.getProperty( "key1" ) );
    assertEquals( "value2", createOperation.getProperty( "key2" ) );
  }

  @Test
  public void testAppendArrayParameter() throws JSONException {
    String shellId = WidgetUtil.getId( shell );
    Integer[] arrayParameter = new Integer[] { new Integer( 1 ), new Integer( 2 ) };

    writer.appendSet( shellId, "key", arrayParameter );

    SetOperation operation = ( SetOperation )getMessage().getOperation( 0 );
    assertEquals( 1, ( ( JSONArray )operation.getProperty( "key" ) ).getInt( 0 ) );
    assertEquals( 2, ( ( JSONArray )operation.getProperty( "key" ) ).getInt( 1 ) );
  }

  @Test
  public void testAppendEmptyArrayParameter() {
    String shellId = WidgetUtil.getId( shell );
    Object[] emptyArray = new Object[ 0 ];

    writer.appendSet( shellId, "key", emptyArray );

    SetOperation operation = ( SetOperation )getMessage().getOperation( 0 );
    assertEquals( 0, ( ( JSONArray )operation.getProperty( "key" ) ).length() );
  }

  @Test
  public void testAppendMixedArrayParameter() throws JSONException {
    String shellId = WidgetUtil.getId( shell );
    Object[] mixedArray = new Object[] { new Integer( 23 ), "Hello" };

    writer.appendSet( shellId, "key", mixedArray );

    SetOperation operation = ( SetOperation )getMessage().getOperation( 0 );
    assertEquals( 2, ( ( JSONArray )operation.getProperty( "key" ) ).length() );
    assertEquals( "Hello", ( ( JSONArray )operation.getProperty( "key" ) ).get( 1 ) );
  }

  private Message getMessage() {
    String message = writer.createMessage();
    return new Message( message );
  }

}
