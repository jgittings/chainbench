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

// ProcessControl.java

package com.hp.software.chainbench;

import java.io.File;
import java.net.Socket;
import java.util.*;
import com.hp.software.chainbench.lockproviders.*;

public class ProcessControl {

	protected ArrayList<Process> localProcesses = new ArrayList<Process>();
	
	private int iActiveServers = 0;
	private ArrayList<ServerInfo> logins = new ArrayList<ServerInfo>();	
	private ArrayList<ServerInfo> servers = new ArrayList<ServerInfo>();	

	public ProcessControl() {}
	
	public void spawn(int processes,int maxHeap,String frameworkCode,
		ArrayList<ServerInfo> logins,String[] args) {
				
		try {
			for (int i=0; i<processes; i++) {
				int termPort = 44445+i;
				Params.setTerminationPort(args,termPort);
				spawnInner(processes,i,maxHeap,frameworkCode,logins,args,termPort);				
			}
						
			iActiveServers = processes;
			this.logins = logins;
		}
		catch (Exception e) {
			Globals.onException(e);
		}		
	}

	private void spawnInner(int processes,int serverIndex,int maxHeap,
		String frameworkCode,
		ArrayList<ServerInfo> logins,String[] args,
		int terminationPort)
	throws Exception {

		String ownLocation = new File(".").getAbsolutePath();
		System.out.println("ownLocation=" + ownLocation);

		DistributedLockProvider provider =
			Globals.instantiateLockProvider(frameworkCode);

		String localClasspath;
		
		if (System.getProperty("java.class.path").contains("Chainbench.jar"))
			localClasspath = ownLocation + "/Chainbench.jar";
		else {
			final String CP_STUB =
				".;./bin;./lib/*;./lib/ojdbc6.jar;./lib/aspectjrt.jar";

			localClasspath = provider.getMiniServerClasspath(CP_STUB);			
		}

		int pathEnvVarIndex = -1;
		
		// Need to copy env vars across or get a Windows networking error
		Map<String, String> variables = System.getenv();  
		String[] envp = new String[variables.size()];
		int i=0;
	    for (Map.Entry<String, String> entry : variables.entrySet())  
	    {  
	       String name = entry.getKey();	       
	       String value = entry.getValue();  

	       if (name.toLowerCase().equals("path")) {
	    	   pathEnvVarIndex = i;
	    	   System.out.println("PATH AT INDEX" + i);
	    	   System.out.println("PATH=" + value);
	       }

	       envp[i++] = name + "=" + value;	       
	    }  

	    Params.setServerIndex(args,serverIndex);

	    String flattened = Params.spaceSeparatedList(args);	    
	    System.out.println("Flattened=" + flattened);
	    
	    String debugFlags =
	    	(serverIndex==0) ?
	    	" -debug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1234 " : "";

	    String cmdStub;
	    String miniserverClasspath;
	    String extraProps = "";
	    	    
	    if ( serverIndex!=0 && logins.size()!=0) {
	    	int remoteBoxIndex = serverIndex%(logins.size());
	    	ServerInfo login = logins.get(remoteBoxIndex);
			cmdStub = "cmd /K start java -cp " + localClasspath +
				" com.hp.software.chainbench.SSH " +
				login.getHost() + " " + login.getUsername() + " " + login.getPassword() + " " +
				terminationPort + " " + " java ";
			
			miniserverClasspath = "./Chainbench.jar";
			
			// Speeds up JDBC cxns on Linux -
			// see http://stackoverflow.com/questions/5503063/oracle-getconnection-slow
			extraProps = "-Djava.security.egd=file:/dev/./urandom";

			ServerInfo remote = (ServerInfo)login.clone();		
			remote.setTerminationPort(terminationPort);
			servers.add(remote);
	    }
	    else {
	    	miniserverClasspath = localClasspath;
			cmdStub = "cmd /K start java ";
			
			ServerInfo local = new ServerInfo("localhost",null,null,terminationPort);
			servers.add(local);
	    }
	    
	    String loadTimeWeaver = "";
	    if (Params.getSlowdownMode(args).equals(Constants.SLOWDOWN_ASPECT)) {
	    	loadTimeWeaver = " -javaagent:./lib/aspectjweaver.jar ";
	    }
	    else if (Params.getSlowdownMode(args).equals(Constants.SLOWDOWN_REVISER)) {
	    	loadTimeWeaver =
	    		" -javaagent:./lib/gluonj.jar=debug:com.hp.software.chainbench.SlowdownReviser ";
	    }

	    String propsStub = 
			// " -XX:+HeapDumpOnOutOfMemoryError  " +
			" -Xms" + maxHeap + "m" +
			" -Xmx" + maxHeap + "m" +
			loadTimeWeaver +
			debugFlags +
			extraProps;

		String props = provider.getJavaProps(propsStub,args);

	    String cmd =
			cmdStub + " " +
			props + " " +
			" -cp " + miniserverClasspath +
			" com.hp.software.chainbench.MiniServer " + flattened;			
	    System.out.println(cmd);			
			
		/* " -Dgemfire.remove-unresponsive-client=true " +
		" -Dgemfire.enable-network-partition-detection=true " +		
		" -Dgemfire.departure-correlation-window=30 " + 
		" -Dgemfire.conserve-sockets=false " + 
		" -Dgemfire.ack-severe-alert-threshold=5  " + */
		//
		/* " -Dhazelcast.initial.min.cluster.size=" + processes + 
		// " -Dhazelcast.restart.on.max.idle=true " +			
		// " -Dhazelcast.max.no.heartbeat.seconds=15  " +			
		// " -Dhazelcast.in.thread.priority=10 " +
		// " -Dhazelcast.out.thread.priority=10 " +
		// " -Dhazelcast.service.thread.priority=10 " + */
		//
		/* " -Djgroups.bind_addr=" + bindAddress + "  " +
		" -Djgroups.diagnostics_addr=" + bindAddress + "  " +
		" -Dsm.ini.groupname=testGroupName   " + */

		File dir = new File(ownLocation);
		Process p = Runtime.getRuntime().exec(cmd,envp,dir);
		
		localProcesses.add(p);		
	}

	public void stopAll() {
		try {
			stopAllInner();
		}
		catch (Throwable t) {
			Globals.onException(t);
		}
	}

	private void stopAllInner() throws Exception {
		
		for (ServerInfo server : servers) {
			Socket s = new Socket(server.getHost(),server.getTerminationPort());
			s.getOutputStream();
			s.getInputStream();

			// If the MiniServer is remote, terminate the local SSH window for it too
			if (server.getHost().equals("localhost")==false) {
				s = new Socket("localhost",server.getTerminationPort());
				s.getOutputStream();
				s.getInputStream();				
			}			
		}
						
		Runtime.getRuntime().halt(0);
	}
}
