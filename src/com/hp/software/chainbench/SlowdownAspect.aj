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

// SlowdownAspect.aj

package com.hp.software.chainbench;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**Used to slow down mini servers by inserting sleeps in commonly called
methods via AspectJ.*/
public aspect SlowdownAspect issingleton() {

	private static final int REPORTING_INTERVAL = 1000;

	private static AtomicLong sleepPerCall = new AtomicLong(0);
	private static AtomicBoolean slowdownEnabled = new AtomicBoolean(false);
	private static ThreadLocal<Boolean> reentrant = new ThreadLocal<Boolean>();
	
	private static AtomicLong wastedTime = new AtomicLong(0);
	private static AtomicLong lastWasted = new AtomicLong(0);

	public static void enable(long sleep) {
		sleepPerCall.set(sleep);
		slowdownEnabled.set(true);
	}

	// Targetting java.lang.Object
	pointcut delayPointcut(): call(boolean equals(Object) );
	pointcut delayPointcut2(): call(int hashCode() );
	// pointcut delayPointcut3(): call(String toString() );
	
	// Targetting java.io.InputStream
	pointcut delayPointcut4(): call(int read() );
	pointcut delayPointcut5(): call(int read(byte[]) );

    before(): delayPointcut() {sleep();}
    before(): delayPointcut2() {sleep();}
    // before(): delayPointcut3() {sleep();}
    before(): delayPointcut4() {sleep();}
    before(): delayPointcut5() {sleep();}

    private static void sleep() {
    	if (slowdownEnabled.get()==false) return;
    	
    	if (reentrant.get()!=null && reentrant.get()==true) return;
    	reentrant.set(new Boolean(true));
    	    	
    	try {
    		Thread.sleep( sleepPerCall.get() );
    	} catch (Exception e) {}
    	
    	long wasted = wastedTime.addAndGet( sleepPerCall.get() );

    	synchronized(SlowdownAspect.class) {
	    	if (wasted-lastWasted.get()>REPORTING_INTERVAL) {
	    		System.out.println("Wasted total ms=" + wasted);
	    		lastWasted.set(wasted);
	    	}
    	}
    	
    	reentrant.set(new Boolean(false));
    }    
}
