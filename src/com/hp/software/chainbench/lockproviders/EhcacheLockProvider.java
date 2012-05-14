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

// EhcacheLockProvider.java

package com.hp.software.chainbench.lockproviders;

import java.io.File;

import com.sun.lwuit.Form;

import net.sf.ehcache.*;
import net.sf.ehcache.constructs.locking.*;
import com.hp.software.chainbench.*;

public class EhcacheLockProvider implements DistributedLockProvider {

	public static final String CACHE_NAME = "testCache";
	private ExplicitLockingCache lockingCache;

	public EhcacheLockProvider() {}

	public Form getExtraOptionsPanel() {return null;}
	public void onSlowdown(long slowdownMillis) {SlowDownThread.launch(slowdownMillis);}

	public String getMiniServerClasspath(String cpStub) {
		return cpStub + ";./lib/ehcache/*";
	}
	
	public String getJavaProps(String propsStub,String[] p) {return propsStub;}
	
	private ExplicitLockingCache getLockingCache() {
		String ownLocation = new File(".").getAbsolutePath();
		System.out.println("ownLocation=" + ownLocation);
		
		System.out.println("Creating singletonManager via ehcache.xml");
		CacheManager singletonManager = CacheManager.create("./src/ehcache.xml");
		System.out.println("Created singletonManager via ehcache.xml");

		System.out.println("Getting ref to " + CACHE_NAME);
		Cache test = singletonManager.getCache(CACHE_NAME);
		System.out.println("Got ref to " + CACHE_NAME);
		
		ExplicitLockingCache wrapper = new ExplicitLockingCache(test);
		System.out.println("Got locking wrapper for " + CACHE_NAME +
			"=" + wrapper);

		return wrapper;
	}

	public void doClusterWideInitialization() {
				
		lockingCache = this.getLockingCache();

		/* for (int i=0; i<100; i++) {
			Element element = new Element(Globals.LOCK_STUB, "valueIrrelevant");
			lockingCache.put(element);
		} */
	}
	
	public void initialize(int serverIndex,int serverCount) throws Exception {
		lockingCache = this.getLockingCache();
		
		/* String testKey = Globals.LOCK_STUB + "0";
		Object testRetrieval = lockingCache.get(testKey);
		System.out.println("Test retrieval==" + testRetrieval);
		if (testRetrieval==null) {
			System.out.println("ERROR! cache not populated");
		}
		else {
			System.out.println("Cache already populated");
		} */		
	}
	
	public boolean tryLock(final String lockName) throws Exception {
		return lockingCache.tryWriteLockOnKey(lockName,5);
	}

	public void unlock(final String lockName) throws Exception {
		lockingCache.releaseWriteLockOnKey(lockName);
	}
}

