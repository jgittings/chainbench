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

// HazelcastLockProvider.java

package com.hp.software.chainbench.lockproviders;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.Member;
import com.sun.lwuit.Form;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import com.hp.software.chainbench.*;

public class HazelcastLockProvider implements DistributedLockProvider {

	private AtomicInteger memberCount = new AtomicInteger(0);	
	private Lock currentLock;

	public Form getExtraOptionsPanel() {return null;}
	public void onSlowdown(long slowdownMillis) {SlowDownThread.launch(slowdownMillis);}
	public String getMiniServerClasspath(String cpStub) {return cpStub;}

	public String getJavaProps(String propsStub,String[] p) {
		return propsStub +
			" -Dhazelcast.initial.min.cluster.size=" + Params.getServerCount(p); 

		// " -Dhazelcast.restart.on.max.idle=true " +			
		// " -Dhazelcast.max.no.heartbeat.seconds=15  " +			
		// " -Dhazelcast.in.thread.priority=10 " +
		// " -Dhazelcast.out.thread.priority=10 " +
		// " -Dhazelcast.service.thread.priority=10 " +
		
	}
	
	public void doClusterWideInitialization() {}
	public void initialize(int serverIndex,int serverCount) throws Exception {}
	
	public boolean tryLock(final String lockName) throws Exception {
		// traceMembership();
		
		Lock lock = Hazelcast.getLock(lockName);

		if (lock.tryLock()) {
			currentLock = lock;
			return true;
		}
		else {
			currentLock = null;
			return false;
		}
		
	}

	public void unlock(final String lockName) throws Exception {
		currentLock.unlock();		
		currentLock = null;		
	}
	
	private void traceMembership() {
		System.out.println("\n CURRENT HAZELCAST MEMBERSHIP:");
		Set<Member> members = Hazelcast.getCluster().getMembers();
		for (Member m : members) {
			System.out.println(m);
		}
		System.out.println();

		if (members.size()!=memberCount.get()) {
			if (members.size() < memberCount.get()) {
				Globals.trace("Membership count went down! \n" +
						"from " + memberCount.get() + " to " + members.size());
				// Globals.suspendVM();				
			}

			memberCount.set(members.size());
		}		
	}
}

