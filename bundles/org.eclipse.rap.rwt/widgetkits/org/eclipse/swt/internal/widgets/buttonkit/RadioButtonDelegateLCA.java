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
package org.eclipse.swt.internal.widgets.buttonkit;

import java.io.IOException;

import org.eclipse.rap.rwt.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil;
import org.eclipse.swt.internal.events.EventLCAUtil;
import org.eclipse.swt.widgets.Button;


final class RadioButtonDelegateLCA extends ButtonDelegateLCA {

  @Override
  void preserveValues( Button button ) {
    ButtonLCAUtil.preserveValues( button );
  }

  @Override
  void readData( Button button ) {
    // [if] The selection event is based on the request "selection" parameter
    // and not on the selection event, because it is not possible to fire the
    // same event (Id) from javascript for two widgets (selected and unselected
    // radio button) at the same time.
    ButtonLCAUtil.readSelection( button );
    EventLCAUtil.processRadioSelection( button, button.getSelection() ); // order is relevant
    ControlLCAUtil.processEvents( button );
    ControlLCAUtil.processKeyEvents( button );
    ControlLCAUtil.processMenuDetect( button );
    WidgetLCAUtil.processHelp( button );
  }

  @Override
  void renderInitialization( Button button ) throws IOException {
    ButtonLCAUtil.renderInitialization( button );
  }

  @Override
  void renderChanges( Button button ) throws IOException {
    ButtonLCAUtil.renderChanges( button );
  }

}
