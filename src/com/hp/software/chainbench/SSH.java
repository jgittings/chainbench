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

// SSH.java

package com.hp.software.chainbench;

import com.jcraft.jsch.*;
import java.io.*;
import java.util.*;

/**SSH helper class designed to be usable from BeanShell.*/
public class SSH implements UserInfo {

	private static TerminationListenerThread terminationReqListener;
	
	/**Implemented this way so we can establish sessions in their own terminal windows.*/
	public static void main(String[] args) {

		try {
			SSH ssh = new SSH();
			ssh.connect(args[0], args[1], args[2]);

			String terminationPort = args[3];
			terminationReqListener = new TerminationListenerThread(new Integer(terminationPort));
			terminationReqListener.start();
			
			String command = "";
			for (int i=4; i<args.length; i++) command += args[i] + " ";

			System.out.println("Executing: " + command);
			ssh.execIndefinitely(command);			
		}
		catch (Exception e) {
			e.printStackTrace();
			Globals.sleep(10000);
		}
	}
	
	private String password;
	private Session session;
	// private Channel channel;
	
	// UserInfo implementation
    public String getPassword(){ return password; }
    public String getPassphrase() {return null;}
    public boolean promptPassphrase(String message) {return false;}
    public void showMessage(String message){}
    // Really means "Trust all certificates?".
    public boolean promptYesNo(String str) {return false;}
    // Needs to be true, otherwise you get "com.jcraft.jsch.JSchException: Auth cancel"
    public boolean promptPassword(String message) {return true;}

	public void connect(String host,String user,String password) throws Exception {
		this.password = password;

		JSch jsch=new JSch();
		this.session=jsch.getSession(user, host, 22);
		session.setConfig("StrictHostKeyChecking", "no");  
		session.setUserInfo(this);
		 
		System.out.println("Calling connect");
		session.connect();
		System.out.println("Called connect");
	}

	public void disconnect() throws Exception {
		if (session!=null) session.disconnect();
		//if (channel!=null) channel.disconnect();
		session = null;
		//channel = null;
	}
	
	public ArrayList<String> getFile(String host,String user,String password,String filepath) throws Exception {

		ArrayList<String> fileContents = new ArrayList<String>();
		connect(host,user,password);
		
		Channel channel = session.openChannel("sftp");
		ChannelSftp sftp = (ChannelSftp) channel;
		sftp.connect();
		  
		InputStream is = sftp.get(filepath);
		BufferedReader buffered = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = buffered.readLine()) != null) {
			fileContents.add(line);
			/////System.out.println(line);
		}        	

		// if(checkAck(in)!=0) throw new Exception("checkAck error!");
		disconnect();
		return fileContents;
	}

	public void execIndefinitely(String command) throws Exception {
				
		Channel channel = session.openChannel("exec");
		ChannelExec exec = (ChannelExec)channel;
		exec.setCommand(command);
		InputStream is = exec.getInputStream();
		channel.connect();

		System.out.println("Waiting for command output...");
		
		BufferedReader buffered = new BufferedReader(new InputStreamReader(is));
		String line;
		while (1==1) {
			while ((line = buffered.readLine()) != null) {
				System.out.println(line);
			}
		}
	}

	private int checkAck(InputStream in) throws IOException {
		int b=in.read();
		// b may be 0 for success,
		//          1 for error,
		//          2 for fatal error,
		//          -1
		if(b==0) return b;
		if(b==-1) return b;
		
		if(b==1 || b==2) {
			StringBuffer sb=new StringBuffer();
			int c;
			do {
				c=in.read();
				sb.append((char)c);
			}
			while(c!='\n');
			if(b==1){ // error
				System.out.print(sb.toString());
			}
			if(b==2){ // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}
}
