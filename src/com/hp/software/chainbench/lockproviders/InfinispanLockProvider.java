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

// InfinispanLockProvider.java

package com.hp.software.chainbench.lockproviders;

import javax.transaction.TransactionManager;

import org.infinispan.*;
import org.infinispan.config.Configuration;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.config.Configuration.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.*;

import com.sun.lwuit.Form;
import com.hp.software.chainbench.*;

public class InfinispanLockProvider implements DistributedLockProvider {

	private static Cache<Object,Object> cache;
	private TransactionManager currentTxn;
	
	public Form getExtraOptionsPanel() {return null;}
	public void onSlowdown(long slowdownMillis) {SlowDownThread.launch(slowdownMillis);}

	public String getMiniServerClasspath(String cpStub) {
		return cpStub + ";./lib/infinispan/*";
	}

	public String getJavaProps(String propsStub,String[] p) {return propsStub;}
	
	public void doClusterWideInitialization() {}
	
	public void initialize(int serverIndex,int serverCount) throws Exception {
		synchronized(InfinispanLockProvider.class) {
			if (cache==null) {
				/* Configuration config = new Configuration();
		        config.setCacheMode(CacheMode.DIST_SYNC);
		        config.setL1CacheEnabled(true);
		        config.setL1Lifespan(60000);                             
		        config.setEagerLockSingleNode(true);       
		        cache = new DefaultCacheManager(GlobalConfiguration.getClusteredDefault()).getCache(); */
				
				/* Configuration config = new Configuration();
				config.setTransactionManagerLookupClass("org.infinispan.transaction.lookup.JBossStandaloneJTAManagerLookup");
				GlobalConfiguration gc = config.getGlobalConfiguration().getClusteredDefault();
		        cache = new DefaultCacheManager(gc).getCache(); */
		        

				cache = new DefaultCacheManager("infinispan.xml").getCache("xml-configured-cache");
				// cache = new DefaultCacheManager().getCache();		
			}
		}
	}
	
	public boolean tryLock(String lockName) throws Exception {

		TransactionManager tm = cache.getAdvancedCache().getTransactionManager();
		System.out.println("Advanced cache=" + cache.getAdvancedCache());
		System.out.println("tm=" + tm);
		 
		tm.begin();

		boolean success = cache.getAdvancedCache().lock(lockName);
         
	     if (success) {
	    	 currentTxn = tm;
	    	 return true;
	     }
	     else {
	    	 // tm.rollback();
	    	 currentTxn = null;
	    	 return false;
	     }
	}

	public void unlock(String lockName) throws Exception {
		currentTxn.commit();
		currentTxn = null;
	}
}

