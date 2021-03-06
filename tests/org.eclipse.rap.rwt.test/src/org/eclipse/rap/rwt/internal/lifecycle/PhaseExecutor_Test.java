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
package org.eclipse.rap.rwt.internal.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.internal.LoggingPhaseListener;
import org.eclipse.rap.rwt.testfixture.internal.LoggingPhaseListener.PhaseEventInfo;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class PhaseExecutor_Test {

  private PhaseListenerManager phaseListenerManager;
  private LifeCycle lifeCycle;

  @Before
  public void setUp() {
    Fixture.setUp();
    lifeCycle = mock( LifeCycle.class );
    phaseListenerManager = new PhaseListenerManager( lifeCycle );
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testExecute() throws IOException {
    List<IPhase> executionLog = new LinkedList<IPhase>();
    IPhase[] phases = new IPhase[] {
      new TestPhase( executionLog, PhaseId.PREPARE_UI_ROOT, PhaseId.RENDER ),
      new TestPhase( executionLog, PhaseId.RENDER, null )
    };
    PhaseExecutor phaseExecutor = new TestPhaseExecutor( phaseListenerManager, phases );
    phaseExecutor.execute( PhaseId.PREPARE_UI_ROOT );
    assertEquals( 2, executionLog.size() );
    assertSame( phases[ 0 ], executionLog.get( 0 ) );
    assertSame( phases[ 1 ], executionLog.get( 1 ) );
  }

  @Test
  public void testExecuteNotifiesPhaseListener() throws IOException {
    LoggingPhaseListener phaseListener = new LoggingPhaseListener( PhaseId.ANY );
    phaseListenerManager.addPhaseListener( phaseListener );
    IPhase[] phases = new IPhase[] {
      new TestPhase( new LinkedList<IPhase>(), PhaseId.PREPARE_UI_ROOT, null ),
    };
    PhaseExecutor phaseExecutor = new TestPhaseExecutor( phaseListenerManager, phases );
    phaseExecutor.execute( PhaseId.PREPARE_UI_ROOT );
    PhaseEventInfo[] loggedEvents = phaseListener.getLoggedEvents();
    assertEquals( 2, loggedEvents.length );
    PhaseEventInfo beforePrepareUIRoot = loggedEvents[ 0 ];
    assertTrue( beforePrepareUIRoot.before );
    assertEquals( PhaseId.PREPARE_UI_ROOT, beforePrepareUIRoot.phaseId );
    assertSame( lifeCycle, beforePrepareUIRoot.source );
    PhaseEventInfo afterPrepareUIRoot = loggedEvents[ 1 ];
    assertFalse( afterPrepareUIRoot.before );
    assertEquals( PhaseId.PREPARE_UI_ROOT, afterPrepareUIRoot.phaseId );
    assertSame( lifeCycle, afterPrepareUIRoot.source );
  }

  private static class TestPhaseExecutor extends PhaseExecutor {

    private TestPhaseExecutor( PhaseListenerManager phaseListenerManager, IPhase[] phases ) {
      super( phaseListenerManager, phases );
    }

    @Override
    Display getDisplay() {
      return null;
    }
  }

  private static class TestPhase implements IPhase {

    private final List<IPhase> executionLog;
    private final PhaseId phaseId;
    private final PhaseId nextPhaseId;

    TestPhase( List<IPhase> executionLog, PhaseId phaseId, PhaseId nextPhaseId ) {
      this.executionLog = executionLog;
      this.phaseId = phaseId;
      this.nextPhaseId = nextPhaseId;
    }

    public PhaseId getPhaseId() {
      return phaseId;
    }

    public PhaseId execute(Display display) throws IOException {
      executionLog.add( this );
      return nextPhaseId;
    }
  }

}
