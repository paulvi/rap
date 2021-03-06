/*******************************************************************************
 * Copyright (c) 2007, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
var appearances = {
// BEGIN TEMPLATE //

  "ctabfolder" : {
    style: function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.font = tv.getCssFont( "CTabItem", "font" );
      result.textColor = tv.getCssColor( "CTabItem", "color" );
      return result;
    }
  },

  "ctabfolder-body" : {
    style: function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.backgroundColor = tv.getCssColor( "CTabItem", "background-color" );
      var color = tv.getCssColor( "CTabFolder", "border-color" );
      var radii = tv.getCssBoxDimensions( "CTabFolder", "border-radius" );
      var borderWidth = states.rwt_BORDER ? 1 : 0;
      if( radii[ 0 ] > 0 || radii[ 1 ] > 0 || radii[ 2 ] > 0 || radii[ 3 ] > 0 ) {
        var borderRadii;
        if( states.barTop ) {
          borderRadii = [ radii[ 0 ], radii[ 1 ], 0, 0 ];
        } else {
          borderRadii = [ 0, 0, radii[ 2 ], radii[ 3 ] ];
        }
        result.border = new rwt.html.Border( borderWidth, "rounded", color, borderRadii );
      } else {
        result.border = new rwt.html.Border( borderWidth, "solid", color );
      }
      return result;
    }
  },

  "ctabfolder-frame" : {
    style: function( states ) {
      var result = {};
      if( !states.rwt_FLAT ) {
        // get the background color for selected items
        var statesWithSelected = { "selected": true };
        for( var property in states ) {
          statesWithSelected[ property ] = states[ property ];
        }
        var tv = new rwt.theme.ThemeValues( statesWithSelected );
        var color = tv.getCssColor( "CTabItem", "background-color" );
        result.border = new rwt.html.Border( 2, "solid", color );
      } else {
        result.border = "undefined";
      }
      result.backgroundColor = "undefined";
      return result;
    }
  },

  "ctabfolder-separator" : {
    style: function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      var color = tv.getCssColor( "CTabFolder", "border-color" );
      var border;
      if( states.barTop ) {
        border = new rwt.html.Border( [ 0, 0, 1, 0 ], "solid", color );
      } else {
        border = new rwt.html.Border( [ 1, 0, 0, 0 ], "solid", color );
      }
      result.border = border;
      return result;
    }
  },

  "ctab-item" : {
    style: function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.cursor = "default";
      var padding = tv.getCssBoxDimensions( "CTabItem", "padding" );
      result.paddingLeft = padding[ 3 ];
      result.paddingRight = padding[ 1 ];
      result.spacing = tv.getCssDimension( "CTabItem", "spacing" );
      result.font = tv.getCssFont( "CTabItem", "font" );
      result.textColor = tv.getCssColor( "CTabItem", "color" );
      result.textShadow = tv.getCssShadow( "CTabItem", "text-shadow" );
      var color = tv.getCssColor( "CTabFolder", "border-color" );
      // create a copy of the radii from theme
      var radii = tv.getCssBoxDimensions( "CTabFolder", "border-radius" ).slice( 0 );
      // cut off rounded corners at opposite side of tabs
      if( states.barTop ) {
        radii[ 2 ] = 0;
        radii[ 3 ] = 0;
      } else {
        radii[ 0 ] = 0;
        radii[ 1 ] = 0;
      }
      var rounded = radii[ 0 ] > 0 || radii[ 1 ] > 0 || radii[ 2 ] > 0 || radii[ 3 ] > 0;
      var borderWidths = [ 0, 0, 0, 0 ];
      if( !states.nextSelected ) {
        borderWidths[ 1 ] = 1;
      }
      if( states.selected ) {
        borderWidths[ 3 ] = 1;
        if( states.barTop ) {
          borderWidths[ 0 ] = 1;
        } else {
          borderWidths[ 2 ] = 1;
        }
      }
      if( states.firstItem && states.rwt_BORDER && !rounded ) {
        borderWidths[ 3 ] = 1;
      }
      if( rounded && states.selected ) {
        result.border = new rwt.html.Border( borderWidths, "rounded", color, radii );
        result.containerOverflow = false;
      } else {
        result.border = new rwt.html.Border( borderWidths, "solid", color );
      }
      if( states.selected ) {
        result.backgroundColor = tv.getCssColor( "CTabItem", "background-color" );
        result.backgroundImage = tv.getCssImage( "CTabItem", "background-image" );
        result.backgroundGradient = tv.getCssGradient( "CTabItem", "background-image" );
      } else {
        result.backgroundColor = "undefined";
        result.backgroundImage = null;
        result.backgroundGradient = null;
      }
      return result;
    }
  },

  "ctabfolder-button" : {
    include : "image",
    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      if( states.over ) {
        result.backgroundColor = "white";
        var color = tv.getCssColor( "CTabFolder", "border-color" );
        result.border = new rwt.html.Border( 1, "solid", color );
      } else {
        result.backgroundColor = "undefined";
        result.border = "undefined";
      }
      return result;
    }
  },

  "ctabfolder-drop-down-button" : {
    include : "ctabfolder-button",
    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.icon = tv.getCssImage( "CTabFolder-DropDownButton-Icon", "background-image" );
      return result;
    }
  },

  "ctab-close-button" : {
    include : "image",

    style : function( states ) {
      var path = rwt.remote.Server.RESOURCE_PATH + "widget/rap/ctabfolder/";
      return {
        source : path + ( states.over ? "close_hover.gif" : "close.gif" )
      };
    }
  }

// END TEMPLATE //
};
