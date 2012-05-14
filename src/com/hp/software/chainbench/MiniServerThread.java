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

// MiniServerThread.java

package com.hp.software.chainbench;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import com.hp.software.chainbench.lockproviders.*;

public class MiniServerThread extends Thread {

	private static AtomicInteger threadExitCount = new AtomicInteger(0);
	public static int getThreadExitCount() {return threadExitCount.get();}
	
	private int serverIndex;
	private int threadIndex;
	
	private double heapfillFraction = 0.0;
	private ArrayList<Integer> heapExhaustionNodes;
	private int iterations;

	private boolean chewHeap = false;

	private DistributedLockProvider lockProvider;
	private ContendedRowDao contendedRowDao;
	private ServerStatsDao serverStatsDao;

	public MiniServerThread(int serverIndex,int threadIndex,
		double heapfillFraction,ArrayList<Integer> heapExhaustionNodes,
		String frameworkArg,boolean lockingOn,
		int iterations) throws Exception {

		this.serverIndex = serverIndex;
		this.threadIndex = threadIndex;
		this.heapfillFraction = heapfillFraction;
		this.heapExhaustionNodes = heapExhaustionNodes;
		this.iterations = iterations;

		for (Integer i : this.heapExhaustionNodes) {
			if (i==serverIndex) this.chewHeap = true;
		}

		initializeLockProvider(Globals.FRAMEWORK,Globals.SERVER_COUNT);

		System.out.println("Creating ContendedRow...");
		contendedRowDao = new ContendedRowDao(serverIndex,threadIndex,frameworkArg,lockingOn,lockProvider);
		serverStatsDao = new ServerStatsDao();

		Connection toInitializeIt = Globals.getConnection();
		System.out.println("Created connection: " + toInitializeIt);
	}

	private void initializeLockProvider(String framework,int serverCount) throws Exception {

		lockProvider = Globals.instantiateLockProvider(framework);

		try {
			lockProvider.initialize(serverIndex,serverCount);
		}
		catch (Throwable t) {
			Globals.onException(t);
		}
	}
	
	public void run() {
		
		for (int i=0; i < iterations; i++) {
			try {
				boolean lockGranted = contendedRowDao.updateRandomRow();
				serverStatsDao.updateRecord(serverIndex,threadIndex,lockGranted,(i+1));
				
				if (chewHeap) Globals.chewHeap(heapfillFraction);				
			}
			catch (Exception e) {
				Globals.onException(e);
			}			
		}
		
		System.out.println("MiniServerThread exiting for the " +
			threadExitCount.incrementAndGet() + "th time");
	}
		
}

