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

// SlowdownSecurityManager.java

package com.hp.software.chainbench;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

public class SlowdownSecurityManager extends SecurityManager {

	private static long sleepPerCall;
		
	public static void enable(long sleep) {
		sleepPerCall = sleep;
		System.setSecurityManager( new SlowdownSecurityManager() );		
	}
	
	private void sleep() {
		System.out.println("SlowdownSecurityManager.sleep");
		
		// Office network: 2000-3000ms seems to cause greatest disruption to Hazelcast (but doesnt stall the cluster).
		// From >2500ms the slow node starts to get removed from the cluster.
		//
		// Hazelcast, home network (192...): 1000 ms is optimum, above 1500ms node gets removed.

		try {
			Thread.sleep(sleepPerCall);
		}
		catch (Exception e) {System.err.println(e);}
	}
	
	@Override
	public void checkAccept(String host, int port) {
		sleep();
	}

	@Override
	public void checkAccess(Thread t) {
		sleep();
	}

	@Override
	public void checkAccess(ThreadGroup g) {
		sleep();
	}

	@Override
	public void checkAwtEventQueueAccess() {
		sleep();
	}

	@Override
	public void checkConnect(String host, int port, Object context) {
		sleep();
	}

	@Override
	public void checkConnect(String host, int port) {
		sleep();
	}

	@Override
	public void checkCreateClassLoader() {
		sleep();
	}

	@Override
	public void checkDelete(String file) {
		sleep();
	}

	@Override
	public void checkExec(String cmd) {
		sleep();
	}

	@Override
	public void checkExit(int status) {
		sleep();
	}

	@Override
	public void checkLink(String lib) {
		sleep();
	}

	@Override
	public void checkListen(int port) {
		sleep();
	}

	@Override
	public void checkMemberAccess(Class<?> clazz, int which) {
		sleep();
	}

	@Override
	public void checkMulticast(InetAddress maddr, byte ttl) {
		sleep();
	}

	@Override
	public void checkMulticast(InetAddress maddr) {
		sleep();
	}

	@Override
	public void checkPackageAccess(String pkg) {
		sleep();
	}

	@Override
	public void checkPackageDefinition(String pkg) {
		sleep();
	}

	@Override
	public void checkPermission(Permission perm, Object context) {
		sleep();
	}

	@Override
	public void checkPermission(Permission perm) {
		sleep();
	}

	@Override
	public void checkPrintJobAccess() {
		sleep();
	}

	@Override
	public void checkPropertiesAccess() {
		sleep();
	}

	@Override
	public void checkPropertyAccess(String key) {
		sleep();
	}

	@Override
	public void checkRead(FileDescriptor fd) {
		sleep();
	}

	@Override
	public void checkRead(String file, Object context) {
		sleep();
	}

	@Override
	public void checkRead(String file) {
		sleep();
	}

	@Override
	public void checkSecurityAccess(String target) {
		sleep();
	}

	@Override
	public void checkSetFactory() {
		sleep();
	}

	@Override
	public void checkSystemClipboardAccess() {
		sleep();
	}

	@Override
	public boolean checkTopLevelWindow(Object window) {
		sleep();
		return super.checkTopLevelWindow(window);
	}

	@Override
	public void checkWrite(FileDescriptor fd) {
		sleep();
	}

	@Override
	public void checkWrite(String file) {
		sleep();
	}

	@Override
	protected int classDepth(String name) {
		sleep();
		return super.classDepth(name);
	}

	@Override
	protected int classLoaderDepth() {
		sleep();
		return super.classLoaderDepth();
	}

	@Override
	protected ClassLoader currentClassLoader() {
		sleep();
		return super.currentClassLoader();
	}

	@Override
	protected Class<?> currentLoadedClass() {
		sleep();
		return super.currentLoadedClass();
	}

	@Override
	protected Class[] getClassContext() {
		sleep();
		return super.getClassContext();
	}

	@Override
	public boolean getInCheck() {
		sleep();
		return super.getInCheck();
	}

	@Override
	public Object getSecurityContext() {
		sleep();
		return super.getSecurityContext();
	}

	@Override
	public ThreadGroup getThreadGroup() {
		sleep();
		return super.getThreadGroup();
	}

	@Override
	protected boolean inClass(String name) {
		sleep();
		return super.inClass(name);
	}

	@Override
	protected boolean inClassLoader() {
		sleep();
		return super.inClassLoader();
	}

}
