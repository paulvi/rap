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

rwt.protocol.AdapterRegistry.add( "rwt.JSExecutor", {

  factory : function( properties ) {
    return rwt.client.JSExecutor.getInstance();
  },

  destructor : rwt.util.Function.returnTrue,

  properties : [],

  propertyHandler : {},

  listeners : [],

  listenerHandler : {},

  methods : [
    "execute"
  ],

  methodHandler : {
    "execute" : function( object, args ) {
      object.execute( args.content );
    }
  }

} );
