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

rwt.remote.HandlerRegistry.add( "rwt.widgets.GridItem", {

  factory : function( properties ) {
    var result;
    rwt.remote.HandlerUtil.callWithTarget( properties.parent, function( parent ) {
      result = rwt.widgets.GridItem.createItem( parent, properties.index );
    } );
    return result;
  },

  destructor : function( item ) {
    var destroyItems = item.getUncachedChildren();
    for( var i = 0; i < destroyItems.length; i++ ) {
      destroyItems[ i ].dispose();
    }
    item.dispose();
  },

  getDestroyableChildren : function( widget ) {
    return widget.getCachedChildren();
  },

  properties : [
    "itemCount",
    "texts",
    "images",
    "background",
    "foreground",
    "font",
    "cellBackgrounds",
    "cellForegrounds",
    "cellFonts",
    "expanded",
    "checked",
    "grayed",
    "cellChecked",
    "cellGrayed",
    "cellCheckable",
    "customVariant",
    "height"
  ],

  propertyHandler : {
    "images" : function( widget, value ) {
      var images = [];
      for( var i = 0; i < value.length; i++ ) {
        if( value[ i ] === null ) {
          images[ i ] = null;
        } else {
          images[ i ] = value[ i ][ 0 ];
        }
      }
      widget.setImages( images );
    },
    "background" : function( widget, value ) {
      if( value === null ) {
        widget.setBackground( null );
      } else {
        widget.setBackground( rwt.util.Colors.rgbToRgbString( value ) );
      }
    },
    "foreground" : function( widget, value ) {
      if( value === null ) {
        widget.setForeground( null );
      } else {
        widget.setForeground( rwt.util.Colors.rgbToRgbString( value ) );
      }
    },
    "font" : function( widget, value ) {
      if( value === null ) {
        widget.setFont( null );
      } else {
        var font = rwt.html.Font.fromArray( value );
        widget.setFont( font );
      }
    },
    "cellBackgrounds" : function( widget, value ) {
      var backgrounds = [];
      for( var i = 0; i < value.length; i++ ) {
        if( value[ i ] === null ) {
          backgrounds[ i ] = null;
        } else {
          backgrounds[ i ] = rwt.util.Colors.rgbToRgbString( value[ i ] );
        }
      }
      widget.setCellBackgrounds( backgrounds );
    },
    "cellForegrounds" : function( widget, value ) {
      var foregrounds = [];
      for( var i = 0; i < value.length; i++ ) {
        if( value[ i ] === null ) {
          foregrounds[ i ] = null;
        } else {
          foregrounds[ i ] = rwt.util.Colors.rgbToRgbString( value[ i ] );
        }
      }
      widget.setCellForegrounds( foregrounds );
    },
    "cellFonts" : function( widget, value ) {
      var fonts = [];
      for( var i = 0; i < value.length; i++ ) {
        if( value[ i ] === null ) {
          fonts[ i ] = "";
        } else {
          var font = rwt.html.Font.fromArray( value[ i ] );
          fonts[ i ] = font.toCss();
        }
      }
      widget.setCellFonts( fonts );
    },
    "customVariant" : function( widget, value ) {
      widget.setVariant( value );
    }
  }

} );
