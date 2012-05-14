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

// GemFireLockProvider.java

package com.hp.software.chainbench.lockproviders;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.distributed.*;
import com.sun.lwuit.Form;
import com.hp.software.chainbench.*;

public class GemFireLockProvider implements DistributedLockProvider {

	private static Cache cache;
	private static DistributedLockService dLS;

	public GemFireLockProvider() {}
	
	public void doClusterWideInitialization() {}	
	public Form getExtraOptionsPanel() {return null;}
	public void onSlowdown(long slowdownMillis) {SlowDownThread.launch(slowdownMillis);}
	public String getMiniServerClasspath(String cpStub) {return cpStub;}

	public String getJavaProps(String propsStub,String[] p) {
		return propsStub +		
			" -Dgemfire.remove-unresponsive-client=true " +
			" -Dgemfire.enable-network-partition-detection=true " +		
			" -Dgemfire.departure-correlation-window=30 " + 
			" -Dgemfire.conserve-sockets=false " + 
			" -Dgemfire.ack-severe-alert-threshold=5 ";
	}
	
	public void initialize(int serverIndex,int serverCount) throws Exception {
		synchronized(GemFireLockProvider.class) {
			if (cache==null) {
				cache = new CacheFactory()
	            	.set("name", "DistributedLocking")
	            	.set("cache-xml-file", "GemFireDistributedLocking.xml")
	            	.create();
	
				// Create a distributed named lock service
				dLS = DistributedLockService.create("distLockService", cache.getDistributedSystem());			
			}
		}
	}
	
	public boolean tryLock(String lockName) throws Exception {

		boolean success = dLS.lock(lockName,0,60000);
		return success;
	}

	public void unlock(String lockName) throws Exception {
		
		dLS.unlock(lockName);
	}
}

