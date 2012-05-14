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

// Globals.java

package com.hp.software.chainbench;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.lwuit.TextArea;
import com.hp.software.chainbench.lockproviders.*;

public class Globals {

	private static ThreadLocal<Connection> connection = new ThreadLocal<Connection>();
	
	private static final String COLON = ":";
	private static final String SPACES = "  ";
	
	public static int SERVER_INDEX;
	public static String JDBC_URL;
	public static String JDBC_USER;
	public static String JDBC_PASSWORD;
	public static int SERVER_COUNT;
	public static int THREAD_COUNT;
	public static int MAX_CONTENDED_RSC_INDEX;
	public static ArrayList<Integer> STALL_NODES_LIST = new ArrayList<Integer>();
	public static int STALL_AFTER_ITERATIONS;

	public static String FRAMEWORK;
	public static Integer JMX_PORTS_BASE;
	
	public static ArrayList<Integer> SLOWDOWN_NODES_LIST = new ArrayList<Integer>();
	public static int SLOWDOWN_AFTER_ITERATIONS;
	public static int SLOWDOWN_AMOUNT;
	public static String SLOWDOWN_MODE;

	public static TextArea resultsArea;
	
	public static String makeContendedResourceId(int index) {
		return "cr" + index;
	}
	
	private static class Kilobyte {
		private byte[] kilobyte = new byte[1024];
	}
	
	private static ArrayList heapChewer = new ArrayList();
	private static Random gen = new Random(System.currentTimeMillis());
	
	public static Connection getConnection() {
		try {
			if (connection.get()==null) {
				System.out.println("Connecting to " + JDBC_USER + "/" + JDBC_PASSWORD + " on " + JDBC_URL);
				Connection cxn = DriverManager.getConnection(JDBC_URL,JDBC_USER,JDBC_PASSWORD);
				System.out.println("Got JDBC connection");
				connection.set(cxn);
			}
			
			return connection.get();
		}
		catch (Throwable e) {
			System.out.println(e);
			Globals.onException(e);
			return null;
		}
	}
	
	public static void traceHeapFill() {
		Runtime rt = Runtime.getRuntime();
		System.out.println("Heap free/total/max=" + rt.freeMemory() + "/" +
				rt.totalMemory() + "/" + rt.maxMemory());
		double fill = (double)(rt.maxMemory()-rt.freeMemory()) / (double)(rt.maxMemory());
		System.out.println("Heap fill fraction ((max-free)/max) = " + fill);
	}
	
	// Fills the heap up to the fraction specified in heapfill.
	public static void chewHeap(double heapfill) {

		if (heapfill==0.0) return;
		
		System.out.println("Chewing heap with target heapfill=" + heapfill);
		
		while (calcFill() < heapfill) {
			if (heapChewer.size() % 1000 ==0) {
				System.out.println("Fill=" + calcFill() + ", target heapfill=" + heapfill);
				try {Thread.sleep(50);} catch (Exception e) {}
			}

			heapChewer.add(new Kilobyte());			
		};		
		
		traceHeapFill();
	}

	private static double calcFill() {
		Runtime rt = Runtime.getRuntime();
		return (double)(rt.maxMemory()-rt.freeMemory()) / (double)(rt.maxMemory());
	}
	
	public static void trace(String s) {
		final Calendar cal = Calendar.getInstance();
		
		// Actually worth using StringBuilder here.
		StringBuilder sb = new StringBuilder();
		sb.append(Thread.currentThread().toString());
		sb.append(SPACES);
		sb.append(cal.get(Calendar.MINUTE));
		sb.append(COLON);
		sb.append(cal.get(Calendar.SECOND));
		sb.append(COLON);
		sb.append(cal.get(Calendar.MILLISECOND));
		sb.append(SPACES);
		sb.append(s);
		
		System.out.println(sb.toString());

		if (resultsArea!=null) {
			String existing = resultsArea.getText();
			resultsArea.setText(existing + s + "\n");
		}
	}	

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		}
		catch (Exception e) {}
	}
	
	public static String getCurrentIP() {
		try {
			return InetAddress.getLocalHost().toString();
		}
		catch (UnknownHostException e) {
			Globals.onException(e);
			return "error!";
		}		
	}

	/**Suspend all threads except current one.*/
	public static void suspendOtherThreads() {
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
		for (Thread t : threadArray) {
			if (!t.equals(Thread.currentThread())) t.suspend();
		}
	}

	/**Resume all threads except current one.*/
	public static void resumeOtherThreads() {
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
		for (Thread t : threadArray) {
			if (!t.equals(Thread.currentThread())) t.resume();
		}
	}

	/**Suspend all threads whose name matches filter.*/
	public static void suspendMatchingThreads(String nameFilter) {
		final String LOWER_FILTER = nameFilter.toLowerCase();
		int suspendCount = 0;
		
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
		for (Thread t : threadArray) {
			if (t.getName().toLowerCase().matches(LOWER_FILTER)) {
System.out.println(t.getName());
System.out.println(t.getContextClassLoader());
				t.suspend();
				suspendCount++;
			}
		}

		System.out.println("Suspended " + suspendCount + " matching threads");
		
Globals.suspendVM();		
	}

	/**Resume all threads whose name matches filter.*/
	public static void resumeMatchingThreads(String nameFilter) {
		final String LOWER_FILTER = nameFilter.toLowerCase();
		int resumeCount = 0;
		
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
		for (Thread t : threadArray) {
			if (t.getName().toLowerCase().matches(LOWER_FILTER)) {
				t.resume();
				resumeCount++;
			}
		}

		System.out.println("Resumed " + resumeCount + " matching threads");
	}

	/**Suspend all threads, causing VM to effectively hang.*/
	public static void suspendVM() {
		suspendOtherThreads();		
		trace("Suspended all threads except current... now suspending current thread");
		Thread.currentThread().suspend();
		trace("This line should never be reached");
	}
	
	public static void onException(Throwable e) {
		// Suspend other threads first else exception trace will be mixed in with their output
		Globals.suspendOtherThreads();

		System.err.println("\n");
		System.err.println("Suspending VM due to exception: \n");
		e.printStackTrace();
		Thread.currentThread().suspend();
	}

	public static DistributedLockProvider instantiateLockProvider(String fwCode) 
	throws Exception {
		if (fwCode.equals(getFrameworkCode(Constants.EHCACHE2))) {
			Globals.trace("Using EhcacheLockProvider");
			return new EhcacheLockProvider();		
		}
		else if (fwCode.equals(getFrameworkCode(Constants.HAZELCAST2))) {
			Globals.trace("Using HazelcastLockProvider");
			return new HazelcastLockProvider();
		}
		//else if (fwCode.equals(getFrameworkCode(Constants.ZOOKEEPER))) {
		//	Globals.trace("Using Zookeeper");
		//	return new ZookeeperLockProvider();
		// }
		else if (fwCode.equals(getFrameworkCode(Constants.GEMFIRE))) {
			Globals.trace("Using Gemfire");
			return new GemFireLockProvider();
		}
		else if (fwCode.equals(getFrameworkCode(Constants.INFINISPAN))) {
			Globals.trace("Using Infinispan");
			return new InfinispanLockProvider();
		}
		else {
			throw new Exception("INVALID LOCK PROVIDER PARAM: " + fwCode);
		}
	}
	
	public static String getFrameworkCode(String framework) {
		int colon = framework.indexOf(':');
		return framework.substring(0,colon);
	}
}

