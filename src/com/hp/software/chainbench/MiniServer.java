/***************************************************************************
(C) Copyright 2012 Hewlett-Packard Company, LP

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations
under the License
***************************************************************************/

// MiniServer.java

package com.hp.software.chainbench;

import java.util.*;

public class MiniServer {

	private static TerminationListenerThread terminationReqListener;
	
	private ArrayList<Thread> threads = new ArrayList<Thread>();
	private int serverIndex;
	private double heapfillFraction = 0.0;
	
	public static void main(String[] args) {

		try {
			///////////  TO DO -- MAKE THIS A UI PARAM  //////////////
			// /dev/null on Windows
			// PrintStream ns = new PrintStream(new FileOutputStream("NUL:"));
			// System.setOut(ns);
			
			final int TERMINATION_PORT = Params.getTerminationPort(args);
			Globals.trace("Termination request port=" + TERMINATION_PORT);

			terminationReqListener = new TerminationListenerThread(TERMINATION_PORT);
			terminationReqListener.start();
			Globals.trace("Started termination request listener thread");
			    
			// Swap in socket factory wrappers which let us track creation
			// TrackingSocketFactory tsf = new TrackingSocketFactory();
			
			final int THREAD_COUNT = Params.getThreadCount(args);
			MiniServer ms = new MiniServer(args);
			
			while (MiniServerThread.getThreadExitCount() < THREAD_COUNT) {
				Globals.sleep(5000);
			}
			
			Globals.trace("All threads done, exiting server...");
			Globals.sleep(1000);
		}
		catch (Throwable t)
		{
			Globals.onException(t);
		}

		// Seems necessary to get the process to quit
		Runtime.getRuntime().halt(0);
	}

	public MiniServer(String[] args) {
		try {
			MiniServerInner(args);
		}
		catch (Throwable t) {
			Globals.onException(t);
		}
	}

	private void MiniServerInner(String[] args) throws Exception {

		serverIndex = Params.getServerIndex(args);
		heapfillFraction = Params.getHeapExhaustionPercent(args) / 100.00;
		ArrayList<Integer> heapExhaustionNodes = Params.getHeapExhaustNodesList(args);
		int iterations = Params.getIterations(args);

		Globals.SERVER_INDEX = Params.getServerIndex(args);
		Globals.JDBC_URL = Params.getJdbcUrl(args);
		Globals.JDBC_USER = Params.getJdbcUser(args);
		Globals.JDBC_PASSWORD = Params.getJdbcPassword(args);
		Globals.MAX_CONTENDED_RSC_INDEX = Params.getMaxContendedRscIndex(args);
		Globals.STALL_NODES_LIST = Params.getStallNodesList(args);
		Globals.STALL_AFTER_ITERATIONS = Params.getStallAfterIterations(args);
		
		Globals.SLOWDOWN_NODES_LIST = Params.getSlowdownNodesList(args);
		Globals.SLOWDOWN_AFTER_ITERATIONS = Params.getSlowdownAfterIterations(args);
		Globals.SLOWDOWN_AMOUNT = Params.getSlowdownAmount(args);
		Globals.SLOWDOWN_MODE = Params.getSlowdownMode(args);
		
		Globals.THREAD_COUNT = Params.getThreadCount(args);
		Globals.FRAMEWORK = Params.getFramework(args);
		Globals.JMX_PORTS_BASE = Params.getJMXPortsBase(args);
		
		if (Globals.FRAMEWORK.equals(Globals.getFrameworkCode(Constants.INFINISPAN))) {
			try {
				Class forceLoading = com.arjuna.ats.jta.logging.jtaLogger.class;
				System.out.println("Loaded jtaLogger class ok");
				Globals.sleep(10000);
			}
			catch (Throwable t) {
				Globals.onException(t);
			}
		}
			
		System.out.println("Creating server threads...");
		
		for (int threadIndex=0; threadIndex<Globals.THREAD_COUNT; threadIndex++) {
			MiniServerThread thread = new MiniServerThread(serverIndex,threadIndex,
				heapfillFraction,heapExhaustionNodes,
				Params.getFramework(args),Params.isLockingOn(args),iterations);
			threads.add(thread);
		}

		System.out.println("Starting server threads...");

		for (Thread t : threads) {
			t.start();
		}				
	}
}

