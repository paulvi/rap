/*******************************************************************************
 * Copyright (c) 2011 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.osgi.internal;

import java.util.*;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


class ServiceContainer<S> {

  private final Set<ServiceHolder<S>> services;
  private final BundleContext bundleContext;

  ServiceContainer( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
    this.services = new HashSet<ServiceHolder<S>>();
  }

  ServiceHolder<S> add( S service ) {
    return add( service, null );
  }

  ServiceHolder<S> add( ServiceReference<S> reference ) {
    return add( bundleContext.getService( reference ), reference );
  }

  void remove( S service ) {
    services.remove( find( service ) );
  }

  @SuppressWarnings( "unchecked" )
  ServiceHolder<S>[] getServices() {
    Set<ServiceHolder<S>> result = new HashSet<ServiceHolder<S>>();
    Iterator<ServiceHolder<S>> iterator = services.iterator();
    while( iterator.hasNext() ) {
      result.add( iterator.next() );
    }
    return result.toArray( new ServiceHolder[ result.size() ]);
  }

  ServiceHolder<S> find( S service ) {
    Finder<S> finder = new Finder<S>();
    return finder.findServiceHolder( service, services );
  }

  void clear() {
    services.clear();
  }

  int size() {
    return services.size();
  }

  private ServiceHolder<S> add( S service, ServiceReference<S> reference ) {
    ServiceHolder<S> result = find( service );
    if( notFound( result ) ) {
      result = new ServiceHolder<S>( service, reference );
      services.add( result );
    } else if( referenceIsMissing( reference, result ) ) {
      result.setServiceReference( reference );
    }
    return result;
  }

  private boolean notFound( ServiceHolder<S> result ) {
    return result == null;
  }

  private boolean referenceIsMissing( ServiceReference<S> reference, ServiceHolder<S> result ) {
    return reference != null && result.serviceReference == null;
  }

  static class ServiceHolder<S> {
  
    private ServiceReference<S> serviceReference;
    private final S service;
  
    private ServiceHolder( S service, ServiceReference<S> serviceReference ) {
      this.service = service;
      this.serviceReference = serviceReference;
    }
  
    S getService() {
      return service;
    }
  
    ServiceReference<S> getReference() {
      return serviceReference;
    }
  
    private void setServiceReference( ServiceReference<S> serviceReference ) {
      this.serviceReference = serviceReference;
    }
  }

  private static class Finder<S> {
  
    private ServiceHolder<S> findServiceHolder( S service, Set<ServiceHolder<S>> collection ) {
      Iterator<ServiceHolder<S>> iterator = collection.iterator();
      ServiceHolder<S> result = null;
      while( iterator.hasNext() ) {
        ServiceHolder<S> serviceHolder = iterator.next();
        S found = serviceHolder.getService();
        if( service.equals( found ) ) {
          result = serviceHolder;
        }
      }
      return result;
    }
  }
}
