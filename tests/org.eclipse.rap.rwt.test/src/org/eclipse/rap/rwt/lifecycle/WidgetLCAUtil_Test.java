/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.lifecycle;

import static org.mockito.Mockito.mock;

import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.graphics.Graphics;
import org.eclipse.rap.rwt.internal.protocol.ProtocolTestUtil;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.internal.widgets.ControlUtil;
import org.eclipse.swt.internal.widgets.IControlAdapter;
import org.eclipse.swt.internal.widgets.IWidgetGraphicsAdapter;
import org.eclipse.swt.internal.widgets.Props;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class WidgetLCAUtil_Test extends TestCase {

  private Display display;
  private Shell shell;
  private Control widget;

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    Fixture.fakeResponseWriter();
    display = new Display();
    shell = new Shell( display , SWT.NONE );
    widget = new Button( shell, SWT.PUSH );
  }

  @Override
  protected void tearDown() throws Exception {
    display.dispose();
    Fixture.tearDown();
  }

  public void testHasChanged() {
    Text text = new Text( shell, SWT.NONE );
    // test initial behaviour, text is same as default value -> no 'change'
    text.setText( "" );
    boolean hasChanged;
    hasChanged = WidgetLCAUtil.hasChanged( text, Props.TEXT, text.getText(), "" );
    assertEquals( false, hasChanged );
    // test initial behaviour, text is different as default value -> 'change'
    text.setText( "other value" );
    hasChanged = WidgetLCAUtil.hasChanged( text, Props.TEXT, text.getText(), "" );
    assertEquals( true, hasChanged );
    // test subsequent behaviour (when already initialized)
    Fixture.markInitialized( display );
    Fixture.markInitialized( text );
    Fixture.clearPreserved();
    Fixture.preserveWidgets();
    hasChanged = WidgetLCAUtil.hasChanged( text, Props.TEXT, text.getText(), "" );
    assertEquals( false, hasChanged );
    Fixture.clearPreserved();
    Fixture.preserveWidgets();
    text.setText( "whatsoevervaluehasbeensetduringrequest" );
    hasChanged = WidgetLCAUtil.hasChanged( text, Props.TEXT, text.getText(), "" );
    assertEquals( true, hasChanged );
  }

  public void testHasChangedWidthArrays() {
    List list = new List( shell, SWT.MULTI );

    boolean hasChanged;
    hasChanged = WidgetLCAUtil.hasChanged( list, "items", new String[] { "a" } );
    assertEquals( true, hasChanged );

    list.setItems( new String[] { "a" } );
    Fixture.markInitialized( display );
    Fixture.preserveWidgets();
    hasChanged = WidgetLCAUtil.hasChanged( list, "items", new String[] { "a" } );
    assertEquals( false, hasChanged );

    list.setItems( new String[] { "a" } );
    Fixture.preserveWidgets();
    hasChanged = WidgetLCAUtil.hasChanged( list, "items", new String[] { "b" } );
    assertEquals( true, hasChanged );

    list.setItems( new String[] { "a" } );
    Fixture.preserveWidgets();
    hasChanged
      = WidgetLCAUtil.hasChanged( list, "items", new String[] { "a", "b" } );
    assertEquals( true, hasChanged );

    list.setItems( new String[] { "a" } );
    Fixture.preserveWidgets();
    hasChanged = WidgetLCAUtil.hasChanged( list, "items", null );
    assertEquals( true, hasChanged );

    list.setItems( new String[] { "a", "b", "c" } );
    list.setSelection( new int[] { 0, 1, 2 } );
    Fixture.preserveWidgets();
    hasChanged = WidgetLCAUtil.hasChanged( list, "selectionIndices", new int[] { 0, 1, 4 } );
    assertEquals( true, hasChanged );

    list.setItems( new String[] { "a", "b", "c" } );
    list.setSelection( new int[] { 0, 1, 2 } );
    Fixture.preserveWidgets();
    hasChanged = WidgetLCAUtil.hasChanged( list, "selectionIndices", new int[] { 0, 1, 2 } );
    assertEquals( false, hasChanged );
  }

  public void testEquals() {
    assertTrue( WidgetLCAUtil.equals( null, null ) );
    assertFalse( WidgetLCAUtil.equals( null, "1" ) );
    assertFalse( WidgetLCAUtil.equals( "1", null ) );
    assertFalse( WidgetLCAUtil.equals( "1", "2" ) );
    assertTrue( WidgetLCAUtil.equals( "1", "1" ) );
    assertTrue( WidgetLCAUtil.equals( new String[] { "1" },
                                   new String[] { "1" } ) );
    assertTrue( WidgetLCAUtil.equals( new int[] { 1 },
                                   new int[] { 1 } ) );
    assertTrue( WidgetLCAUtil.equals( new boolean[] { true },
                                   new boolean[] { true } ) );
    assertTrue( WidgetLCAUtil.equals( new long[] { 232 },
                                   new long[] { 232 } ) );
    assertTrue( WidgetLCAUtil.equals( new float[] { 232 },
                                   new float[] { 232 } ) );
    assertTrue( WidgetLCAUtil.equals( new double[] { 345 },
                                   new double[] { 345 } ) );
    assertTrue( WidgetLCAUtil.equals( new Date[] { new Date( 1 ) },
                                   new Date[] { new Date( 1 ) } ) );
    assertFalse( WidgetLCAUtil.equals( new double[] { 345 },
                                    new float[] { 345 } ) );
    assertFalse( WidgetLCAUtil.equals( new int[] { 345 },
                                    new float[] { 345 } ) );
    assertFalse( WidgetLCAUtil.equals( new int[] { 345 },
                                    new long[] { 345 } ) );
    assertFalse( WidgetLCAUtil.equals( new Date[] { new Date( 3 ) }, null ) );
  }

  public void testParseFontName() {
    // IE doesn't like quoted font names (or whatever qooxdoo makes out of them)
    String systemFontName
      = "\"Segoe UI\", Corbel, Calibri, Tahoma, \"Lucida Sans Unicode\", "
      + "sans-serif";
    String[] fontNames = ProtocolUtil.parseFontName( systemFontName );
    assertEquals( 6, fontNames.length );
    assertEquals( "Segoe UI", fontNames[ 0 ] );
    assertEquals( "Corbel", fontNames[ 1 ] );
    assertEquals( "Calibri", fontNames[ 2 ] );
    assertEquals( "Tahoma", fontNames[ 3 ] );
    assertEquals( "Lucida Sans Unicode", fontNames[ 4 ] );
    assertEquals( "sans-serif", fontNames[ 5 ] );

    // Empty font names don't cause trouble (at least for the browsers
    // currently tested - therefore don't make extra effort to eliminate them
    fontNames = ProtocolUtil.parseFontName( "a, , b" );
    assertEquals( 3, fontNames.length );
    assertEquals( "a", fontNames[ 0 ] );
    assertEquals( "", fontNames[ 1 ] );
    assertEquals( "b", fontNames[ 2 ] );
  }

  //////////////////////////////////////////////
  // Tests for new render methods using protocol

  public void testRenderIntialBackgroundNull() {
    WidgetLCAUtil.renderBackground( widget, null );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( widget, "background" ) );
  }

  public void testRenderBackground() throws JSONException {
    WidgetLCAUtil.renderBackground( widget, new Color( display, 0, 16, 255 ) );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( widget, "background" );
    assertTrue( ProtocolTestUtil.jsonEquals( "[0,16,255,255]", actual ) );
  }

  public void testRenderBackgroundNull() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    widget.setBackground( new Color( display, 0, 16, 255 ) );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderBackground( widget, null, false );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JSONObject.NULL, message.findSetProperty( widget, "background" ) );
  }

  public void testRenderBackgroundUnchanged() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    widget.setBackground( new Color( display, 0, 16, 255 ) );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderBackground( widget, new Color( display, 0, 16, 255 ) );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( widget, "background" ) );
  }

  public void testRenderIntialBackgroundTransparent() throws JSONException {
    WidgetLCAUtil.renderBackground( widget, null, true );

    Message message = Fixture.getProtocolMessage();

    JSONArray actual = ( JSONArray )message.findSetProperty( widget, "background" );
    assertTrue( ProtocolTestUtil.jsonEquals( "[0,0,0,0]", actual ) );
  }

  public void testRenderBackgroundTransparencyUnchanged() {
    widget = new Button( shell, SWT.CHECK );
    shell.setBackgroundMode( SWT.INHERIT_DEFAULT );
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    IControlAdapter controlAdapter = ControlUtil.getControlAdapter( widget );
    assertTrue( controlAdapter.getBackgroundTransparency() );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderBackground( widget, null, true );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( widget, "background" ) );
  }

  public void testRenderBackgroundNoMoreTransparent() throws JSONException {
    widget = new Button( shell, SWT.CHECK );
    shell.setBackgroundMode( SWT.INHERIT_DEFAULT );
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    IControlAdapter controlAdapter = ControlUtil.getControlAdapter( widget );
    assertTrue( controlAdapter.getBackgroundTransparency() );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderBackground( widget, new Color( display, 0, 16, 255 ), false );

    Message message = Fixture.getProtocolMessage();

    JSONArray actual = ( JSONArray )message.findSetProperty( widget, "background" );
    assertTrue( ProtocolTestUtil.jsonEquals( "[0,16,255,255]", actual ) );
  }

  public void testRenderBackgroundReset() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    widget.setBackground( new Color( display, 0, 16, 255 ) );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderBackground( widget, null );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JSONObject.NULL, message.findSetProperty( widget, "background" ) );
  }

  public void testRenderIntialForeground() {
    ControlLCAUtil.renderForeground( widget );

    Message message = Fixture.getProtocolMessage();

    assertNull( message.findSetOperation( widget, "foreground" ) );
  }

  public void testRenderForeground() throws JSONException {
    widget.setForeground( new Color( display, 0, 16, 255 ) );
    ControlLCAUtil.renderForeground( widget );

    Message message = Fixture.getProtocolMessage();


    JSONArray actual = ( JSONArray )message.findSetProperty( widget, "foreground" );
    assertTrue( ProtocolTestUtil.jsonEquals( "[0,16,255,255]", actual ) );
  }

  public void testRenderForegroundUnchanged() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    widget.setForeground( new Color( display, 0, 16, 255 ) );

    Fixture.preserveWidgets();
    ControlLCAUtil.renderForeground( widget );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( widget, "foreground" ) );
  }

  public void testRenderInitialCustomVariant() {
    WidgetLCAUtil.renderCustomVariant( widget );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( widget, "customVariant" ) );
  }

  public void testRenderCustomVariant() {
    widget.setData( RWT.CUSTOM_VARIANT, "my_variant" );
    WidgetLCAUtil.renderCustomVariant( widget );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "variant_my_variant", message.findSetProperty( widget, "customVariant" ) );
  }

  public void testRenderCustomVariantUnchanged() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    widget.setData( RWT.CUSTOM_VARIANT, "my_variant" );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderCustomVariant( widget );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( widget, "customVariant" ) );
  }

  public void testRenderInitialListenHelp() {
    WidgetLCAUtil.renderListenHelp( widget );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( widget, "Help" ) );
  }

  public void testRenderListenHelp() {
    widget.addHelpListener( mock( HelpListener.class ) );
    WidgetLCAUtil.renderListenHelp( widget );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findListenProperty( widget, "Help" ) );
  }

  public void testRenderListenHelpUnchanged() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    widget.addHelpListener( mock( HelpListener.class ) );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderListenHelp( widget );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( widget, "Help" ) );
  }

  public void testRenderListenHelpRemoved() {
    HelpListener listener = mock( HelpListener.class );
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    widget.addHelpListener( listener );
    Fixture.preserveWidgets();

    widget.removeHelpListener( listener );
    WidgetLCAUtil.renderListenHelp( widget );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findListenProperty( widget, "Help" ) );
  }

  public void testRenderBackgroundGradient() throws JSONException {
    Control control = new Composite( shell, SWT.NONE );
    Object adapter = control.getAdapter( IWidgetGraphicsAdapter.class );
    IWidgetGraphicsAdapter gfxAdapter = ( IWidgetGraphicsAdapter )adapter;
    Color[] gradientColors = new Color[] {
      Graphics.getColor( 0, 255, 0 ),
      Graphics.getColor( 0, 0, 255 )
    };
    int[] percents = new int[] { 0, 100 };

    gfxAdapter.setBackgroundGradient( gradientColors, percents, true );
    WidgetLCAUtil.renderBackgroundGradient( control );

    Message message = Fixture.getProtocolMessage();
    JSONArray gradient = ( JSONArray )message.findSetProperty( control, "backgroundGradient" );
    JSONArray colors = ( JSONArray )gradient.get( 0 );
    JSONArray stops = ( JSONArray )gradient.get( 1 );
    assertTrue( ProtocolTestUtil.jsonEquals( "[0,255,0,255]", colors.getJSONArray( 0 ) ) );
    assertTrue( ProtocolTestUtil.jsonEquals( "[0,0,255,255]", colors.getJSONArray( 1 ) ) );
    assertEquals( new Integer( 0 ), stops.get( 0 ) );
    assertEquals( new Integer( 100 ), stops.get( 1 ) );
    assertEquals( Boolean.TRUE, gradient.get( 2 ) );
  }

  public void testRenderBackgroundGradientHorizontal() throws JSONException {
    Control control = new Composite( shell, SWT.NONE );
    Object adapter = control.getAdapter( IWidgetGraphicsAdapter.class );
    IWidgetGraphicsAdapter gfxAdapter = ( IWidgetGraphicsAdapter )adapter;
    Color[] gradientColors = new Color[] {
      Graphics.getColor( 0, 255, 0 ),
      Graphics.getColor( 0, 0, 255 )
    };
    int[] percents = new int[] { 0, 100 };

    gfxAdapter.setBackgroundGradient( gradientColors, percents, false );
    WidgetLCAUtil.renderBackgroundGradient( control );

    Message message = Fixture.getProtocolMessage();
    JSONArray gradient = ( JSONArray )message.findSetProperty( control, "backgroundGradient" );
    JSONArray colors = ( JSONArray )gradient.get( 0 );
    JSONArray stops = ( JSONArray )gradient.get( 1 );
    assertTrue( ProtocolTestUtil.jsonEquals( "[0,255,0,255]", colors.getJSONArray( 0 ) ) );
    assertTrue( ProtocolTestUtil.jsonEquals( "[0,0,255,255]", colors.getJSONArray( 1 ) ) );
    assertEquals( new Integer( 0 ), stops.get( 0 ) );
    assertEquals( new Integer( 100 ), stops.get( 1 ) );
    assertEquals( Boolean.FALSE, gradient.get( 2 ) );
  }

  public void testRenderBackgroundGradientUnchanged() {
    Control control = new Composite( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( control );
    Object adapter = control.getAdapter( IWidgetGraphicsAdapter.class );
    IWidgetGraphicsAdapter gfxAdapter = ( IWidgetGraphicsAdapter )adapter;
    Color[] gradientColors = new Color[] {
      Graphics.getColor( 0, 255, 0 ),
      Graphics.getColor( 0, 0, 255 )
    };
    int[] percents = new int[] { 0, 100 };

    gfxAdapter.setBackgroundGradient( gradientColors, percents, true );
    WidgetLCAUtil.preserveBackgroundGradient( control );
    WidgetLCAUtil.renderBackgroundGradient( control );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( control, "backgroundGradient" ) );
  }

  public void testResetBackgroundGradient() {
    Control control = new Composite( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( control );
    Object adapter = control.getAdapter( IWidgetGraphicsAdapter.class );
    IWidgetGraphicsAdapter gfxAdapter = ( IWidgetGraphicsAdapter )adapter;
    Color[] gradientColors = new Color[] {
      Graphics.getColor( 0, 255, 0 ),
      Graphics.getColor( 0, 0, 255 )
    };
    int[] percents = new int[] { 0, 100 };
    gfxAdapter.setBackgroundGradient( gradientColors, percents, true );
    WidgetLCAUtil.preserveBackgroundGradient( control );

    gfxAdapter.setBackgroundGradient( null, null, true );
    WidgetLCAUtil.renderBackgroundGradient( control );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JSONObject.NULL, message.findSetProperty( control, "backgroundGradient" ) );
  }

  public void testRenderRoundedBorder() throws JSONException {
    Widget widget = new Composite( shell, SWT.NONE );
    Object adapter = widget.getAdapter( IWidgetGraphicsAdapter.class );
    IWidgetGraphicsAdapter graphicsAdapter = ( IWidgetGraphicsAdapter )adapter;
    Color color = Graphics.getColor( 0, 255, 0 );

    graphicsAdapter.setRoundedBorder( 2, color, 5, 6, 7, 8 );
    WidgetLCAUtil.renderRoundedBorder( widget );

    Message message = Fixture.getProtocolMessage();
    JSONArray border = ( JSONArray )message.findSetProperty( widget, "roundedBorder" );
    assertEquals( 6, border.length() );
    assertEquals( 2, border.getInt( 0 ) );
    assertTrue( ProtocolTestUtil.jsonEquals( "[0,255,0,255]", border.getJSONArray( 1 ) ) );
    assertEquals( 5, border.getInt( 2 ) );
    assertEquals( 6, border.getInt( 3 ) );
    assertEquals( 7, border.getInt( 4 ) );
    assertEquals( 8, border.getInt( 5 ) );
  }

  public void testRenderRoundedBorderUnchanged() {
    Widget widget = new Composite( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    Object adapter = widget.getAdapter( IWidgetGraphicsAdapter.class );
    IWidgetGraphicsAdapter graphicsAdapter = ( IWidgetGraphicsAdapter )adapter;
    Color color = Graphics.getColor( 0, 255, 0 );
    graphicsAdapter.setRoundedBorder( 2, color, 5, 6, 7, 8 );

    WidgetLCAUtil.preserveRoundedBorder( widget );
    WidgetLCAUtil.renderRoundedBorder( widget );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( widget, "roundedBorder" ) );
  }

  public void testResetRoundedBorder() {
    Widget widget = new Composite( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    Object adapter = widget.getAdapter( IWidgetGraphicsAdapter.class );
    IWidgetGraphicsAdapter graphicsAdapter = ( IWidgetGraphicsAdapter )adapter;
    Color color = Graphics.getColor( 0, 255, 0 );
    graphicsAdapter.setRoundedBorder( 2, color, 5, 6, 7, 8 );
    WidgetLCAUtil.preserveRoundedBorder( widget );

    graphicsAdapter.setRoundedBorder( 0, null, 0, 0, 0, 0 );
    WidgetLCAUtil.renderRoundedBorder( widget );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JSONObject.NULL, message.findSetProperty( widget, "roundedBorder" ) );
  }

  public void testRenderInitialMenu() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );

    WidgetLCAUtil.renderMenu( widget, widget.getMenu() );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 0, message.getOperationCount() );
  }

  public void testRenderMenu() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    Menu menu = new Menu( widget );
    widget.setMenu( menu );

    WidgetLCAUtil.renderMenu( widget, widget.getMenu() );

    Message message = Fixture.getProtocolMessage();
    assertEquals( WidgetUtil.getId( menu ), message.findSetProperty( widget, "menu" ) );
  }

  public void testRenderMenuReset() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    Menu menu = new Menu( widget );
    widget.setMenu( menu );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderMenu( widget, null );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JSONObject.NULL, message.findSetProperty( widget, "menu" ) );
  }

}
