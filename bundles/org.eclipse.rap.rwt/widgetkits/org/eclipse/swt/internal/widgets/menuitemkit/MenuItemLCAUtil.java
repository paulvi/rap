/*******************************************************************************
 * Copyright (c) 2002, 2013 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.menuitemkit;

import static org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil.preserveListener;
import static org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil.renderListener;
import static org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil.renderProperty;

import org.eclipse.rap.rwt.internal.protocol.ClientMessageConst;
import org.eclipse.rap.rwt.internal.protocol.ClientObjectFactory;
import org.eclipse.rap.rwt.internal.protocol.IClientObject;
import org.eclipse.rap.rwt.internal.util.MnemonicUtil;
import org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

final class MenuItemLCAUtil {

  private static final String TYPE = "rwt.widgets.MenuItem";
  private static final String[] ALLOWED_STYLES = new String[] {
    "CHECK", "CASCADE", "PUSH", "RADIO", "SEPARATOR"
  };

  private static final String PROP_TEXT = "text";
  private static final String PROP_MNEMONIC_INDEX = "mnemonicIndex";
  private static final String PROP_IMAGE = "image";
  private static final String PROP_MENU = "menu";
  private static final String PROP_ENABLED = "enabled";
  private static final String PROP_SELECTION = "selection";
  private static final String PROP_SELECTION_LISTENER = "Selection";

  static void preserveValues( MenuItem item ) {
    WidgetLCAUtil.preserveCustomVariant( item );
    preserveProperty( item, PROP_TEXT, item.getText() );
    preserveProperty( item, PROP_IMAGE, item.getImage() );
    preserveProperty( item, PROP_MENU, item.getMenu() );
    preserveProperty( item, PROP_ENABLED, item.getEnabled() );
    preserveProperty( item, PROP_SELECTION, item.getSelection() );
    preserveListener( item, PROP_SELECTION_LISTENER, item.isListening( SWT.Selection ) );
    WidgetLCAUtil.preserveHelpListener( item );
  }

  static void renderInitialization( MenuItem item ) {
    IClientObject clientObject = ClientObjectFactory.getClientObject( item );
    clientObject.create( TYPE );
    Menu parent = item.getParent();
    clientObject.set( "parent", WidgetUtil.getId( parent ) );
    clientObject.set( "style", WidgetLCAUtil.getStyles( item, ALLOWED_STYLES ) );
    clientObject.set( "index", parent.indexOf( item ) );
  }

  static void renderChanges( MenuItem item ) {
    WidgetLCAUtil.renderCustomVariant( item );
    renderText( item );
    renderMnemonicIndex( item );
    renderProperty( item, PROP_IMAGE, item.getImage(), null );
    WidgetLCAUtil.renderMenu( item, item.getMenu() );
    renderProperty( item, PROP_ENABLED, item.getEnabled(), true );
    renderProperty( item, PROP_SELECTION, item.getSelection(), false );
    renderListener( item, PROP_SELECTION_LISTENER, item.isListening( SWT.Selection ), false );
    WidgetLCAUtil.renderListenHelp( item );
  }

  static void processArmEvent( MenuItem item ) {
    Menu menu = item.getParent();
    if( WidgetLCAUtil.wasEventSent( menu, ClientMessageConst.EVENT_SHOW ) ) {
      item.notifyListeners( SWT.Arm, new Event() );
    }
  }

  //////////////////
  // Helping methods

  private static void renderText( MenuItem item ) {
    String newValue = item.getText();
    if( WidgetLCAUtil.hasChanged( item, PROP_TEXT, newValue, "" ) ) {
      String text = MnemonicUtil.removeAmpersandControlCharacters( newValue );
      IClientObject clientObject = ClientObjectFactory.getClientObject( item );
      clientObject.set( PROP_TEXT, text );
    }
  }

  private static void renderMnemonicIndex( MenuItem item ) {
    String text = item.getText();
    if( WidgetLCAUtil.hasChanged( item, PROP_TEXT, text, "" ) ) {
      int mnemonicIndex = MnemonicUtil.findMnemonicCharacterIndex( text );
      if( mnemonicIndex != -1 ) {
        IClientObject clientObject = ClientObjectFactory.getClientObject( item );
        clientObject.set( PROP_MNEMONIC_INDEX, mnemonicIndex );
      }
    }
  }

}
