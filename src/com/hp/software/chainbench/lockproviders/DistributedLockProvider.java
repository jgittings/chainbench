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

// DistributedLockProvider.java

package com.hp.software.chainbench.lockproviders;

import com.sun.lwuit.Container;

/**Note that not all lock providers return a java.util.concurrent.locks.Lock object, so these
methods do not use that class.
Lock providers are instantiated per-thread.*/
public interface DistributedLockProvider {
	
	public void doClusterWideInitialization();
	public void initialize(int serverIndex,int serverCount) throws Exception;

	public boolean tryLock(String lockName) throws Exception;

	public void unlock(String lockName) throws Exception;
	
	public Container getExtraOptionsPanel();
	
	public void onSlowdown(long slowdownMillis) throws Exception;
	
	public String getMiniServerClasspath(String classpathStub);
	public String getJavaProps(String propsStub,String[] p) throws Exception;
}
