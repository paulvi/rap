/*******************************************************************************
 * Copyright (c) 2010, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme;

import java.util.Arrays;


public class QxAnimation implements QxType {

  public Animation[] animations;

  public QxAnimation() {
    animations = new Animation[ 0 ];
  }

  public void addAnimation( String name, int duration, String timingFunction ) {
    Animation animation = new Animation( name, duration, timingFunction );
    Animation[] newAnimations = new Animation[ animations.length + 1 ];
    System.arraycopy( animations, 0, newAnimations, 0, animations.length );
    newAnimations[ animations.length ] = animation;
    animations = newAnimations;
  }

  public String toDefaultString() {
    StringBuilder result = new StringBuilder();
    for( int i = 0; i < animations.length; i++ ) {
      if( result.length() != 0 ) {
        result.append( ", " );
      }
      result.append( animations[ i ].name );
      result.append( " " );
      result.append( animations[ i ].duration );
      result.append( "ms " );
      result.append( animations[ i ].timingFunction );
    }
    return result.toString();
  }

  @Override
  public String toString() {
    return "QxAnimation{ " + toDefaultString() + " }";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode( animations );
    return result;
  }

  @Override
  public boolean equals( Object obj ) {
    if( this == obj ) {
      return true;
    }
    if( obj == null ) {
      return false;
    }
    if( getClass() != obj.getClass() ) {
      return false;
    }
    QxAnimation other = ( QxAnimation )obj;
    if( !Arrays.equals( animations, other.animations ) ) {
      return false;
    }
    return true;
  }

  public static String toCamelCaseString( String string ) {
    StringBuilder result = new StringBuilder();
    boolean toUpperCase = false;
    for( int i = 0; i < string.length(); i++ ) {
      char ch = string.charAt( i );
      if( ch == '-' ) {
        toUpperCase = true;
      } else if( toUpperCase ) {
        result.append( Character.toUpperCase( ch ) );
        toUpperCase = false;
      } else {
        result.append( ch );
      }
    }
    return result.toString();
  }

  public static final class Animation {

    private static final String[] PREDEFINED_NAMES = new String[] {
      "hoverIn",
      "hoverOut",
      "fadeIn",
      "fadeOut",
      "slideIn",
      "slideOut",
      "flyInLeft",
      "flyInTop",
      "flyInRight",
      "flyInBottom",
      "flyOutLeft",
      "flyOutTop",
      "flyOutRight",
      "flyOutBottom"
    };

    private static final String[] PREDEFINED_TIMING_FUNCTIONS = new String[] {
      "ease",
      "linear",
      "ease-in",
      "ease-out",
      "ease-in-out"
    };

    public final String name;
    public final int duration;
    public final String timingFunction;

    private Animation( String name, int duration, String timingFunction ) {
      checkName( name );
      checkTimingFunction( timingFunction );
      this.name = name;
      this.duration = duration;
      this.timingFunction = timingFunction;
    }

    private void checkName( String name ) {
      boolean result = false;
      if( name == null ) {
        throw new NullPointerException( "null argument" );
      }
      for( int i = 0; i < PREDEFINED_NAMES.length && !result; i++ ) {
        if( name.equals( PREDEFINED_NAMES[ i ] ) ) {
          result = true;
        }
      }
      if( !result ) {
        String msg = "Invalid value for animation name: " + name;
        throw new IllegalArgumentException( msg );
      }
    }

    private void checkTimingFunction( String timingFunction ) {
      boolean result = false;
      if( timingFunction == null ) {
        throw new NullPointerException( "null argument" );
      }
      for( int i = 0; i < PREDEFINED_TIMING_FUNCTIONS.length && !result; i++ ) {
        if( timingFunction.equals( PREDEFINED_TIMING_FUNCTIONS[ i ] ) ) {
          result = true;
        }
      }
      if( !result ) {
        String msg = "Invalid value for animation timing function: "
                     + timingFunction;
        throw new IllegalArgumentException( msg );
      }
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + duration;
      result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
      result = prime * result + ( ( timingFunction == null ) ? 0 : timingFunction.hashCode() );
      return result;
    }

    @Override
    public boolean equals( Object obj ) {
      if( this == obj ) {
        return true;
      }
      if( obj == null ) {
        return false;
      }
      if( getClass() != obj.getClass() ) {
        return false;
      }
      Animation other = ( Animation )obj;
      if( duration != other.duration ) {
        return false;
      }
      if( name == null ) {
        if( other.name != null ) {
          return false;
        }
      } else if( !name.equals( other.name ) ) {
        return false;
      }
      if( timingFunction == null ) {
        if( other.timingFunction != null ) {
          return false;
        }
      } else if( !timingFunction.equals( other.timingFunction ) ) {
        return false;
      }
      return true;
    }

  }

}
