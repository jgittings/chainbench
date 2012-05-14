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

// ContendedRow.java

package com.hp.software.chainbench;

import java.lang.management.ManagementFactory;
// import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.sql.*;
import java.util.*;
import com.hp.software.chainbench.lockproviders.*;

public class ContendedRowDao {

	private int serverIndex;
	private int threadIndex;
	private boolean lockingOn;
	private DistributedLockProvider lockProvider;

	private int iterations = 0;

	private com.sun.management.OperatingSystemMXBean osMXBean;
	
	// private ThreadMXBean threadMXBean;
	private Random gen = new Random(System.currentTimeMillis());
	
	public ContendedRowDao(int serverIndex,int threadIndex,String framework,boolean lockingOn,
		DistributedLockProvider lockProvider)
	throws Exception {
		
		this.serverIndex = serverIndex;
		this.threadIndex = threadIndex;
		this.lockingOn = lockingOn;
		this.lockProvider = lockProvider;		
		
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		threadMXBean.setThreadCpuTimeEnabled(true);		

		Globals.trace("cpu time supported? " + threadMXBean.isThreadCpuTimeSupported());
		Globals.trace("cpu time enabled? " + threadMXBean.isThreadCpuTimeEnabled());

		if (threadMXBean.isThreadCpuTimeSupported()==false) {
			Globals.suspendOtherThreads();
			Globals.trace("CANT GET THREAD CPU TIME");
			Globals.suspendVM();
		}
		
		osMXBean =
			(com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
		
	}
	
	public boolean updateRandomRow() throws Exception {
		iterations++;				
		
		int rowIndex = (int)(gen.nextDouble() * (double)(Globals.MAX_CONTENDED_RSC_INDEX+1));
		Globals.trace("Selected index " + rowIndex +
			" from MAX_CONTENDED_RSC_INDEX=" + Globals.MAX_CONTENDED_RSC_INDEX);
		
		final String lockName = Constants.LOCK_NAME_STUB + rowIndex;
		final String contendedRscId = Globals.makeContendedResourceId(rowIndex);
		
		long startClock = System.nanoTime();
		long startCPU = osMXBean.getProcessCpuTime();

		boolean available = (lockingOn ? lockProvider.tryLock(lockName) : true);
		
		long lockWaitNanosClock = System.nanoTime() - startClock;

		long lockWaitNanosCPU = osMXBean.getProcessCpuTime() - startCPU;
		
		if (!available) {
			Globals.trace("\n Lock: " + lockName + " not available! \n");
			return false;
		}

		Globals.trace("Waited " + lockWaitNanosClock + " actual nanos for lock " + lockName);
		Globals.trace("Waited " + lockWaitNanosCPU + " CPU nanos for lock " + lockName);
		
		double lockWaitMillisClock = ((double)lockWaitNanosClock)/1000000.00;
		Globals.trace("Clock time=" + lockWaitMillisClock + " millis");

		double lockWaitMillisCPU = ((double)lockWaitNanosCPU)/1000000.00;
		Globals.trace("CPU time=" + lockWaitMillisCPU + " millis");

		String sql;
		WaitStats physicalWait = new WaitStats();
		WaitStats cpuWait = new WaitStats();
		
		try {

			if (iterations > 10) {				
				if (  (Globals.STALL_NODES_LIST.size()!=0) &&
					(iterations >= Globals.STALL_AFTER_ITERATIONS)) {
					
					for (Integer i : Globals.STALL_NODES_LIST) {
						if (i==serverIndex) stallThisServer(lockName);
					}
				}

				if (  (Globals.SLOWDOWN_NODES_LIST.size()!=0) &&
					(iterations >= Globals.SLOWDOWN_AFTER_ITERATIONS)) {
					
					for (Integer i : Globals.SLOWDOWN_NODES_LIST) {
						if (i==serverIndex) {
							Globals.trace("Triggering slowdown at iterations=" + iterations);
							lockProvider.onSlowdown(Globals.SLOWDOWN_AMOUNT);
						}
					}
				}

				Connection cxn = Globals.getConnection();
				Statement s = cxn.createStatement();
	
				sql = "select updateCount,totalLockWaitClock,worstLockWaitClock," +
					"totalLockWaitCPU,worstLockWaitCPU from " +
					Constants.STATS_TABLE +
					" where " + Constants.STATS_TABLE_ID_FIELD + "='" + contendedRscId + "'";
				Globals.trace("Executing: " + sql);
				ResultSet rs = s.executeQuery(sql);
				Globals.trace("Executed: " + sql);
				rs.next();
				Globals.trace("rs.next");
				int updateCount = rs.getInt("updateCount");
				
				updateCount += 1;
				physicalWait.load(updateCount, rs, "Clock", lockWaitMillisClock);
				cpuWait.load(updateCount, rs, "CPU", lockWaitMillisCPU);

				sql = "update " + Constants.STATS_TABLE + " set (" +
					"timeField,lastUpdatedByServer,lastUpdatedByThread,updateCount," +
						"lastLockWaitClock,totalLockWaitClock,meanLockWaitClock,worstLockWaitClock," +
						"lastLockWaitCPU,totalLockWaitCPU,meanLockWaitCPU,worstLockWaitCPU" +
						")=" +
					"(select sysdate," +
					serverIndex + "," +
					threadIndex + "," +
					updateCount + "," +
					//
					lockWaitMillisClock + "," +
					physicalWait.getTotalLockWait() + "," +
					physicalWait.getMeanLockWait() + "," +
					physicalWait.getWorstLockWait() + "," +
					//
					lockWaitMillisCPU + "," +
					cpuWait.getTotalLockWait() + "," +
					cpuWait.getMeanLockWait() + "," +
					cpuWait.getWorstLockWait() +
					//
					" from dual) where " +
					Constants.STATS_TABLE_ID_FIELD + "='" + contendedRscId + "' " +
					"and updateCount=" + (updateCount-1);
				int rows = s.executeUpdate(sql);
				if (rows==0) {
					Globals.trace("ERROR!  LOCKING FAILURE DETECTED WITH FRAMEWORK " + Globals.FRAMEWORK);
					Globals.suspendVM();
					/// while (1==1) Globals.sleep(1000);
				}
				
				s.close();
			}
		} catch (Exception e) {
			Globals.onException(e);
		} finally {
		    if (lockingOn) lockProvider.unlock(lockName);
		}

		Globals.traceHeapFill();
		return true;
	}
	
	private void stallThisServer(String holdingLockName) {
		Globals.trace("STALLING! HOLDING LOCK " + holdingLockName);
		Globals.suspendVM();
	}	
	
	private long getTotalThreadCPUTime() {
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		threadMXBean.setThreadCpuTimeEnabled(true);		
		long[] ids = threadMXBean.getAllThreadIds();
		long sum = 0;
		for (long id: ids) {
			long contrib = threadMXBean.getThreadCpuTime(id);
			if (contrib!=-1) sum += contrib;
		}
		return sum;
	}
	
	/* private long getTotalThreadUserTime() {
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		threadMXBean.setThreadCpuTimeEnabled(true);		
		long[] ids = threadMXBean.getAllThreadIds();
		long sum = 0;
		for (long id: ids) {
			sum += threadMXBean.getThreadUserTime(id);
		}
		return sum;
	} */
}

