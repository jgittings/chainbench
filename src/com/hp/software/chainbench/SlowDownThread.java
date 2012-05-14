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

// SlowDownThread.java

package com.hp.software.chainbench;

public class SlowDownThread extends Thread {
	
	private static final long MAX_RUNTIME = 60000;
	private static SlowDownThread instance;
	private static String slowdownThreadNameFilter;
	
	public static synchronized void launch(long slowdownAmount) {
	
		if (Globals.SLOWDOWN_MODE.equals(Constants.SLOWDOWN_REVISER)) {
			SlowdownReviser.enable(slowdownAmount);
		}
		else if (Globals.SLOWDOWN_MODE.equals(Constants.SLOWDOWN_ASPECT)) {
			SlowdownAspect.enable(slowdownAmount);
		}
		else if (Globals.SLOWDOWN_MODE.equals(Constants.SLOW_SECURITY_MANAGER)) {
			SlowdownSecurityManager.enable(slowdownAmount);
		}
		else if (Globals.SLOWDOWN_MODE.equals(Constants.SLOW_MATHS)) {
			if (instance==null) {
				for (int i=0; i<slowdownAmount; i++) {
					slowdownThreadNameFilter = ""; 
					instance = new SlowDownThread();
					instance.start();
				}
			}			
		}
		else {
			Globals.trace("ERROR! Unknown slowdown mode param=" + Globals.SLOWDOWN_MODE);
			Globals.suspendVM();
		}		
	}
	
	private SlowDownThread() {
				
		Globals.trace("\n");
		Globals.trace("Launching SlowDownThread....");
		Globals.trace("\n");
		
		/* this.setPriority(priority);
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
		for (Thread t : threadArray) {
			if (!(t instanceof SlowDownThread)) t.setPriority(MIN_PRIORITY);
		} */
	}
	
	public void run() {
		runHeavyComputeStyle();
	}

	private void runHeavyComputeStyle() {
		// Only run for X minutes, to make it easier to get rid of the balky node when testing
		long start = System.currentTimeMillis();
		long lastTrace = System.currentTimeMillis();
		
		while (System.currentTimeMillis()-start < MAX_RUNTIME) {
			if (System.currentTimeMillis() > (lastTrace+1000)) {
				Globals.trace("In heavy compute slowdown thread");
				lastTrace = System.currentTimeMillis();
			}
			
			double d = Math.sqrt(200.00);
			double d2 = Math.sqrt(d);
			d += d2;
		}				
	}
	
	private void runSuspendOthersStyle() {
		// Only run for X minutes, to make it easier to get rid of the balky node when testing
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis()-start < MAX_RUNTIME) {
			
			Globals.suspendMatchingThreads(slowdownThreadNameFilter);
			System.out.println("Suspended db lock provider threads");
			Globals.sleep(200);
			Globals.resumeMatchingThreads(slowdownThreadNameFilter);			
			System.out.println("Resumed db lock provider threads");
			Globals.sleep(10);
		}				
	}
}

