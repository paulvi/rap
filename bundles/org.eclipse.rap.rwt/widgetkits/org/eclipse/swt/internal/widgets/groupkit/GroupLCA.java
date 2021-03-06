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
package org.eclipse.swt.internal.widgets.groupkit;

import static org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil.preserveProperty;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.protocol.ClientObjectFactory;
import org.eclipse.rap.rwt.internal.protocol.IClientObject;
import org.eclipse.rap.rwt.internal.util.MnemonicUtil;
import org.eclipse.rap.rwt.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Widget;


public class GroupLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.Group";
  private static final String[] ALLOWED_STYLES = new String[] {
    "SHADOW_ETCHED_IN",
    "SHADOW_ETCHED_OUT",
    "SHADOW_IN",
    "SHADOW_OUT",
    "SHADOW_NONE",
    "NO_RADIO_GROUP",
    "BORDER"
  };

  private static final String PROP_TEXT = "text";
  private static final String PROP_MNEMONIC_INDEX = "mnemonicIndex";

  @Override
  public void preserveValues( Widget widget ) {
    Group group = ( Group )widget;
    ControlLCAUtil.preserveValues( group );
    WidgetLCAUtil.preserveCustomVariant( group );
    preserveProperty( group, PROP_TEXT, group.getText() );
  }

  public void readData( Widget widget ) {
    ControlLCAUtil.processEvents( ( Group )widget );
    ControlLCAUtil.processKeyEvents( ( Group )widget );
    ControlLCAUtil.processMenuDetect( ( Group )widget );
    WidgetLCAUtil.processHelp( widget );
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    Group group = ( Group )widget;
    IClientObject clientObject = ClientObjectFactory.getClientObject( group );
    clientObject.create( TYPE );
    clientObject.set( "parent", WidgetUtil.getId( group.getParent() ) );
    clientObject.set( "style", WidgetLCAUtil.getStyles( group, ALLOWED_STYLES ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    Group group = ( Group )widget;
    ControlLCAUtil.renderChanges( group );
    WidgetLCAUtil.renderCustomVariant( group );
    renderText( group );
    renderMnemonicIndex( group );
  }

  //////////////////
  // Helping methods

  private static void renderText( Group group ) {
    String newValue = group.getText();
    if( WidgetLCAUtil.hasChanged( group, PROP_TEXT, newValue, "" ) ) {
      String text = MnemonicUtil.removeAmpersandControlCharacters( newValue );
      IClientObject clientObject = ClientObjectFactory.getClientObject( group );
      clientObject.set( PROP_TEXT, text );
    }
  }

  private static void renderMnemonicIndex( Group group ) {
    String text = group.getText();
    if( WidgetLCAUtil.hasChanged( group, PROP_TEXT, text, "" ) ) {
      int mnemonicIndex = MnemonicUtil.findMnemonicCharacterIndex( text );
      if( mnemonicIndex != -1 ) {
        IClientObject clientObject = ClientObjectFactory.getClientObject( group );
        clientObject.set( PROP_MNEMONIC_INDEX, mnemonicIndex );
      }
    }
  }

}
