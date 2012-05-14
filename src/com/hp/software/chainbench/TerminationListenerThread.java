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

// TerminationListenerThread.java

package com.hp.software.chainbench;

import java.net.ServerSocket;

public class TerminationListenerThread extends Thread {

	private ServerSocket terminationSocket;
	
	public TerminationListenerThread(int terminationPort) {
		try {
			terminationSocket = new ServerSocket(terminationPort);
		}
		catch (Throwable t)
		{
			Globals.onException(t);
		}
	}
	
	public void run() {
		try {
			Globals.trace("In termination listener thread: about to wait for request");
			
			// Listen for the termination request on the termination socket.
			terminationSocket.accept();

			Globals.trace("In termination listener thread: request came in! Halting VM...");
			Globals.sleep(1000);

			// The call came in.
			Runtime.getRuntime().halt(0);
		}
		catch (Exception e) {
			System.err.println("Unable to listen for termination request");
			Globals.sleep(5000);
			Globals.onException(e);
		}
	}
}