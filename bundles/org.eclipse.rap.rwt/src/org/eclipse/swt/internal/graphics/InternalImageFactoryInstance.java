/*******************************************************************************
 * Copyright (c) 2010, 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 *    Frank Appel - replaced singletons and static fields (Bug 337787)
 ******************************************************************************/
package org.eclipse.swt.internal.graphics;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rwt.internal.resources.ResourceManager;
import org.eclipse.rwt.resources.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.*;


public class InternalImageFactoryInstance {
  private final Map cache;
  private final Object cacheLock;
  
  private InternalImageFactoryInstance() {
    cache = new HashMap();
    cacheLock = new Object();
  }

  // TODO [rst] If we do not rely on the fact that there is only one
  //            InternalImage instance, we could loose synchronization as in
  //            ImageDataFactory.
  InternalImage findInternalImage( final String fileName ) {
    InternalImage result;
    synchronized( cacheLock ) {
      result = ( InternalImage )cache.get( fileName );
      if( result == null ) {
        result = createInternalImage( fileName );
        cache.put( fileName, result );
      }
    }
    return result;
  }

  InternalImage findInternalImage( final InputStream stream ) {
    InternalImage result;
    BufferedInputStream bufferedStream = new BufferedInputStream( stream );
    ImageData imageData = readImageData( bufferedStream );
    String path = createGeneratedImagePath( imageData );
    synchronized( cacheLock ) {
      result = ( InternalImage )cache.get( path );
      if( result == null ) {
        result = createInternalImage( path, bufferedStream, imageData );
        cache.put( path, result );
      }
    }
    return result;
  }

  InternalImage findInternalImage( final ImageData imageData ) {
    InternalImage result;
    String path = createGeneratedImagePath( imageData );
    synchronized( cacheLock ) {
      result = ( InternalImage )cache.get( path );
      if( result == null ) {
        InputStream stream = createInputStream( imageData );
        result = createInternalImage( path, stream, imageData );
        cache.put( path, result );
      }
    }
    return result;
  }

  InternalImage findInternalImage( final String key,
                                   final InputStream inputStream )
  {
    InternalImage result;
    synchronized( cacheLock ) {
      result = ( InternalImage )cache.get( key );
      if( result == null ) {
        BufferedInputStream bufferedStream = new BufferedInputStream( inputStream );
        ImageData imageData = readImageData( bufferedStream );
        String path = createGeneratedImagePath( imageData );
        result = createInternalImage( path, bufferedStream, imageData );
        cache.put( key, result );
      }
    }
    return result;
  }

  private InternalImage createInternalImage( final String fileName ) {
    InternalImage result;
    try {
      FileInputStream stream = new FileInputStream( fileName );
      try {
        result = createInternalImage( stream );
      } finally {
        stream.close();
      }
    } catch( IOException e ) {
      throw new SWTException( SWT.ERROR_IO, e.getMessage() );
    }
    return result;
  }

  private InternalImage createInternalImage( final InputStream stream ) {
    BufferedInputStream bufferedStream = new BufferedInputStream( stream );
    ImageData imageData = readImageData( bufferedStream );
    String path = createGeneratedImagePath( imageData );
    return createInternalImage( path, bufferedStream, imageData );
  }

  private InternalImage createInternalImage( final String path,
                                             final InputStream stream,
                                             final ImageData imageData )
  {
    registerResource( path, stream );
    return new InternalImage( path, imageData.width, imageData.height );
  }

  void registerResource( final String path, final InputStream stream ) {
    IResourceManager manager = ResourceManager.getInstance();
    manager.register( path, stream );
  }

  ImageData readImageData( final BufferedInputStream stream )
    throws SWTException
  {
    ////////////////////////////////////////////////////////////////////////////
    // TODO: [fappel] Image size calculation and resource registration both
    //                read the input stream. Because of this I use a workaround
    //                with a BufferedInputStream. Resetting it after reading the
    //                image size enables the ResourceManager to reuse it for
    //                registration. Note that the order is crucial here, since
    //                the ResourceManager seems to close the stream (shrug).
    //                It would be nice to find a solution without reading the
    //                stream twice.
    stream.mark( Integer.MAX_VALUE );
    ImageData result = new ImageData( stream );
    try {
      stream.reset();
    } catch( final IOException shouldNotHappen ) {
      String msg = "Could not reset input stream after reading image";
      throw new RuntimeException( msg, shouldNotHappen );
    }
    return result;
  }

  InputStream createInputStream( final ImageData imageData ) {
    ImageLoader imageLoader = new ImageLoader();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    imageLoader.data = new ImageData[] { imageData };
    imageLoader.save( outputStream, getOutputFormat( imageData ) );
    byte[] bytes = outputStream.toByteArray();
    return new ByteArrayInputStream( bytes );
  }

  private static int getOutputFormat( ImageData imageData ) {
    int result = imageData.type;
    if( imageData.type == SWT.IMAGE_UNDEFINED ) {
      result = SWT.IMAGE_PNG;
    }
    return result;
  }

  private static String createGeneratedImagePath( ImageData data ) {
    int hashCode = getHashCode( data );
    return "generated/" + Integer.toHexString( hashCode );
  }

  private static int getHashCode( ImageData imageData ) {
    int result;
    if( imageData.data  == null ) {
      result = 0;
    } else {
      result = 1;
      for( int i = 0; i < imageData.data.length; i++ ) {
        result = 31 * result + imageData.data[ i ];
      }
    }
    if( imageData.palette != null  ) {
      if( imageData.palette.isDirect ) {
        result = result * 29 + imageData.palette.redMask;
        result = result * 29 + imageData.palette.greenMask;
        result = result * 29 + imageData.palette.blueMask;
      } else {
        RGB[] rgb = imageData.palette.getRGBs();
        for( int i = 0; i < rgb.length; i++ ) {
          result = result * 37 + rgb[ i ].red;
          result = result * 37 + rgb[ i ].green;
          result = result * 37 + rgb[ i ].blue;
        }
      }
    }
    result = result * 41 + imageData.alpha;
    result = result * 41 + imageData.transparentPixel;
    result = result * 41 + imageData.type;
    result = result * 41 + imageData.bytesPerLine;
    result = result * 41 + imageData.scanlinePad;
    result = result * 41 + imageData.maskPad;
    return result;
  }

  void clear() {
    synchronized( cacheLock ) {
      cache.clear();
    }
  }
}