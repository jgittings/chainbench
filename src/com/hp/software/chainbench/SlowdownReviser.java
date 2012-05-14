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

// SlowdownReviser.java

package com.hp.software.chainbench;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javassist.gluonj.Reviser;

// NB. The hitpoints dont get hit if we do this to java.lang.Object.
// Also note that if GluonJ's classloader suffers an exception at any point,
// subsequently loaded classes don't get revised.
@Reviser
public class SlowdownReviser extends java.lang.Thread {

	private static final int REPORTING_INTERVAL = 1000;

	private static AtomicLong sleepPerCall = new AtomicLong(0);
	private static AtomicBoolean slowdownEnabled = new AtomicBoolean(false);
	private static ThreadLocal<Boolean> reentrant = new ThreadLocal<Boolean>();
	
	private static AtomicLong wastedTime = new AtomicLong(0);
	private static AtomicLong lastWasted = new AtomicLong(0);

	public static void enable(long sleep) {
		
		System.out.println("In SlowdownReviser.enable");
		
		sleepPerCall.set(sleep);
		slowdownEnabled.set(true);
	}
	
	public boolean equals(Object obj) {
		System.out.println("@@@@@@@@@@@@@@@@@ In SlowdownReviser.equals");
		sleep();
		return super.equals(obj);
	}

	public int hashCode() {
		System.out.println("@@@@@@@@@@@@@@@@@ In SlowdownReviser.hashCode");
		sleep();
		return super.hashCode();
	}

	public String toString() {
		System.out.println("@@@@@@@@@@@@@@@@@ In SlowdownReviser.toString");
		sleep();
		return super.toString();
	}
	
    private static void sleep() {
    	if (slowdownEnabled.get()==false) return;
    	
    	if (reentrant.get()!=null && reentrant.get()==true) return;
    	reentrant.set(new Boolean(true));
    	    	
    	try {
    		Thread.sleep( sleepPerCall.get() );
    	} catch (Exception e) {}
    	
    	long wasted = wastedTime.addAndGet( sleepPerCall.get() );

    	synchronized(SlowdownReviser.class) {
	    	if (wasted-lastWasted.get()>REPORTING_INTERVAL) {
	    		System.out.println("Wasted total ms=" + wasted);
	    		lastWasted.set(wasted);
	    	}
    	}
    	
    	reentrant.set(new Boolean(false));
    }    
	
}

