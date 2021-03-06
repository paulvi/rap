/*******************************************************************************
 * Copyright (c) 2012, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.client;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.rap.rwt.internal.remote.ConnectionImpl;
import org.eclipse.rap.rwt.remote.RemoteObject;


public class UrlLauncherImpl implements UrlLauncher {

  private static final String TYPE = "rwt.client.UrlLauncher";
  private static final String OPEN_URL = "openURL";
  private final RemoteObject remoteObject
    = ( ( ConnectionImpl )RWT.getUISession().getConnection() ).createServiceObject( TYPE );

  public void openURL( String url ) {
    Map< String, Object > properties = new HashMap< String, Object >();
    properties.put( "url", url );
    remoteObject.call( OPEN_URL, properties );
  }

}
