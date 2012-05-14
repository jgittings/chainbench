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

// ServerInfo.java

package com.hp.software.chainbench;

public class ServerInfo {

	private String host;
	private String username;
	private String password;
	private int terminationPort;	

	public ServerInfo(String h,String u,String p) {
		host = h;
		username = u;
		password = p;
	}

	public ServerInfo(String h,String u,String p,Integer tp) {
		host = h;
		username = u;
		password = p;
		terminationPort = tp;
	}

	public Object clone() {
		return new ServerInfo(host,username,password,terminationPort);
	}
	
	public String getHost() {return host;}
	public String getUsername() {return username;}
	public String getPassword() {return password;}
	
	public void setTerminationPort(int i) {terminationPort = i;}
	public int getTerminationPort() {return terminationPort;}
}
