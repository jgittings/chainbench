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

// TrackingSocketFactory.java

package com.hp.software.chainbench;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.SocketFactory;

import org.jgroups.blocks.RpcDispatcher;

public class TrackingSocketFactory extends SocketFactory {

	private class SocketImpl extends java.net.SocketImpl {
		private SocketImpl si;
		
		public SocketImpl(SocketImpl realImpl) {
			si = realImpl;
		}
		
		@Override
		protected void accept(java.net.SocketImpl s) throws IOException {si.accept(s);}

		@Override
		protected int available() throws IOException {return si.available();}

		@Override
		protected void bind(InetAddress host, int port) throws IOException {si.bind(host, port);}

		@Override
		protected void close() throws IOException {si.close();}

		@Override
		protected void connect(String host, int port) throws IOException {si.connect(host, port);}

		@Override
		protected void connect(InetAddress address, int port) throws IOException {
			si.connect(address, port);
		}

		@Override
		protected void connect(SocketAddress address, int timeout) throws IOException {
			si.connect(address, timeout);
		}

		@Override
		protected void create(boolean stream) throws IOException {si.create(stream);}

		@Override
		protected InputStream getInputStream() throws IOException {return si.getInputStream();}

		@Override
		protected OutputStream getOutputStream() throws IOException {return si.getOutputStream();}

		@Override
		protected void listen(int backlog) throws IOException {si.listen(backlog);}

		@Override
		protected void sendUrgentData(int data) throws IOException {si.sendUrgentData(data);}

		@Override
		public Object getOption(int optID) throws SocketException {
			return si.getOption(optID);
		}

		@Override
		public void setOption(int optID, Object value) throws SocketException {
			si.setOption(optID, value);
		}
		
	}
	
	private SocketFactory realFactory;
	private int socketsCreated;
	
	private synchronized void increaseSocketsCreated() {
		socketsCreated++;
		System.out.println("SOCKETS CREATED=" + socketsCreated);
		Globals.sleep(2000);
		
		Runtime.getRuntime().halt(-1);
	} 
	
	public TrackingSocketFactory() throws Exception {

		this.realFactory = SocketFactory.getDefault();
		System.out.println("Default SocketFactory=" + realFactory);
		Globals.sleep(5000);
			
		Field f = SocketFactory.class.getDeclaredField("theFactory");
		f.setAccessible(true);
		f.set(realFactory, this);
		
		Object sanity = f.get(realFactory);
		System.out.println("sanity=" +  sanity);
		Globals.sleep(5000);
						
		socketsCreated = 0;
	}

	@Override
	public Socket createSocket(String arg0, int arg1) throws IOException,UnknownHostException {
		increaseSocketsCreated();
		return realFactory.createSocket(arg0, arg1);
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
		increaseSocketsCreated();
		return realFactory.createSocket(arg0, arg1);
	}

	@Override
	public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
			throws IOException, UnknownHostException {
		increaseSocketsCreated();
		return realFactory.createSocket(arg0, arg1, arg2, arg3);
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,int arg3) throws IOException {
		increaseSocketsCreated();
		return realFactory.createSocket(arg0, arg1, arg2, arg3);
	}
	
}
