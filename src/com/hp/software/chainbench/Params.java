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

// Params.java

package com.hp.software.chainbench;

import java.util.*;

public class Params {

	private static int index = 0;

	private static final int TERMINATION_PORT_INDEX = index++;
	private static final int SERVER_INDEX = index++;
	private static final int SERVER_COUNT = index++;
	private static final int THREAD_COUNT = index++;
	private static final int JDBC_URL_INDEX = index++;
	private static final int JDBC_USER_INDEX = index++;
	private static final int JDBC_PW_INDEX = index++;
	private static final int MAX_CR_INDEX_INDEX = index++;
	private static final int HEAP_EXHAUST_NODES_LIST_INDEX = index++;
	private static final int HEAP_EXHAUST_INDEX = index++;
	private static final int FRAMEWORK_INDEX = index++;
	private static final int LOCKING_ON_INDEX = index++;
	private static final int STALL_NODES_LIST_INDEX = index++;
	private static final int STALL_AFTER_ITERATIONS_INDEX = index++;
	private static final int ITERATIONS_INDEX = index++;
	private static final int JMX_PORTS_BASE_INDEX = index++;
	
	private static final int SLOWDOWN_NODES_LIST_INDEX = index++;
	private static final int SLOWDOWN_AFTER_ITERATIONS_INDEX = index++;
	private static final int SLOWDOWN_AMOUNT_INDEX = index++;
	private static final int SLOWDOWN_MODE_INDEX = index++;

	public static String[] createArgsArray() {return new String[index];}

	public static String spaceSeparatedList(String[] args) {
		String list = "";
		for (String s : args) list += s + " ";
		return list;
	}
	
	public static int getTerminationPort(String[] p) {return getInt(p,TERMINATION_PORT_INDEX);}
	public static int getServerIndex(String[] p) {return getInt(p,SERVER_INDEX);}
	public static int getServerCount(String[] p) {return getInt(p,SERVER_COUNT);}
	public static int getThreadCount(String[] p) {return getInt(p,THREAD_COUNT);}
	public static String getJdbcUrl(String[] p) {return p[JDBC_URL_INDEX];}
	public static String getJdbcUser(String[] p) {return p[JDBC_USER_INDEX];}
	public static String getJdbcPassword(String[] p) {return p[JDBC_PW_INDEX];}
	public static String getFramework(String[] p) {return p[FRAMEWORK_INDEX];}
	
	public static ArrayList<Integer> getStallNodesList(String[] p) {
		return getIntegerList(p,STALL_NODES_LIST_INDEX);
	}

	public static int getStallAfterIterations(String[] p) {return getInt(p,STALL_AFTER_ITERATIONS_INDEX);}
	public static int getMaxContendedRscIndex(String[] p) {return getInt(p,MAX_CR_INDEX_INDEX);}

	public static ArrayList<Integer> getHeapExhaustNodesList(String[] p) {
		return getIntegerList(p,HEAP_EXHAUST_NODES_LIST_INDEX);		
	}

	public static double getHeapExhaustionPercent(String[] p) {
		return new Double(p[HEAP_EXHAUST_INDEX]);
	}

	public static boolean isLockingOn(String[] p) {
		return new Boolean(p[LOCKING_ON_INDEX]);
	}

	public static int getIterations(String[] p) {return getInt(p,ITERATIONS_INDEX);}
	public static Integer getJMXPortsBase(String[] p) {return getInt(p,JMX_PORTS_BASE_INDEX);}

	public static ArrayList<Integer> getSlowdownNodesList(String[] p) {
		return getIntegerList(p,SLOWDOWN_NODES_LIST_INDEX);
	}

	public static int getSlowdownAfterIterations(String[] p) {return getInt(p,SLOWDOWN_AFTER_ITERATIONS_INDEX);}
	public static String getSlowdownMode(String[] p) {return p[SLOWDOWN_MODE_INDEX];}
	public static int getSlowdownAmount(String[] p) {return getInt(p,SLOWDOWN_AMOUNT_INDEX);}

	////////////////////////////////////////////////////////////////////////////////////
	private static Integer getInt(String[] p,int argIndex) {
		if (p[argIndex].equals("null"))
			return null;
		else
			return new Integer(p[argIndex]);
	}

	private static ArrayList<Integer> getIntegerList(String[] p,int argIndex) {
		
		if (p[argIndex].equals("null")) return new ArrayList<Integer>();
		
		ArrayList<Integer> list = new ArrayList<Integer>();
		String[] split = p[argIndex].split(",");

		for (String sNode : split) {
			Integer nodeIndex = new Integer(sNode);
			list.add(nodeIndex);
		}
		return list;		
	}	
	
	//////////////////////////////////////////////////////////////////////////////////
	public static void setTerminationPort(String[] p,int i) {setInt(p,TERMINATION_PORT_INDEX,i);}
	public static void setServerIndex(String[] p,int i) {setInt(p,SERVER_INDEX,i);}
	public static void setServerCount(String[] p,int i) {setInt(p,SERVER_COUNT,i);}
	public static void setThreadCount(String[] p,int i) {setInt(p,THREAD_COUNT,i);}
	public static void setJdbcUrl(String[] p,String url) {p[JDBC_URL_INDEX] = url;}	
	public static void setJdbcUser(String[] p,String user) {p[JDBC_USER_INDEX] = user;}	
	public static void setJdbcPassword(String[] p,String pw) {p[JDBC_PW_INDEX] = pw;}
	public static void setFramework(String[] p,String f) {p[FRAMEWORK_INDEX] = f;}
	public static void setStallAfterIterations(String[] p,int i) {setInt(p,STALL_AFTER_ITERATIONS_INDEX,i);}
	public static void setMaxContendedRscIndex(String[] p,int i) {setInt(p,MAX_CR_INDEX_INDEX,i);}
	public static void setHeapExhaustionPercent(String[] p,double d) {setDouble(p,HEAP_EXHAUST_INDEX,d);}
	public static void setIterations(String[] p,int i) {setInt(p,ITERATIONS_INDEX,i);}
	public static void setJMXPortsBase(String[] p,Integer i) {setInt(p,JMX_PORTS_BASE_INDEX,i);}

	public static void setStallNodesList(String[] p,String list) {
		setString(p,STALL_NODES_LIST_INDEX,list);
	}

	public static void setHeapExhaustionNodesList(String[] p,String list) {
		setString(p,HEAP_EXHAUST_NODES_LIST_INDEX,list);
	}

	public static void setLockingOn(String[] p,boolean b) {
		p[LOCKING_ON_INDEX] = new Boolean(b).toString(); 
	}

	public static void setSlowdownNodesList(String[] p,String list) {
		setString(p,SLOWDOWN_NODES_LIST_INDEX,list);
	}

	public static void setSlowdownAfterIterations(String[] p,int i) {setInt(p,SLOWDOWN_AFTER_ITERATIONS_INDEX,i);}
	public static void setSlowdownMode(String[] p,String s) {p[SLOWDOWN_MODE_INDEX] = s;}
	public static void setSlowdownAmount(String[] p,int i) {setInt(p,SLOWDOWN_AMOUNT_INDEX,i);}

	/////////////////////////////////////////////////////////////////////////////
	private static void setInt(String[] p,int argIndex,Integer i) {
		if (i==null)
			p[argIndex] = "null"; 
		else
			p[argIndex] = i.toString(); 
	}
	
	private static void setDouble(String[] p,int argIndex,double d) {
		p[argIndex] = new Double(d).toString(); 
	}

	private static void setString(String[] p,int argIndex,String s) {
	    if (s!=null && s.length()!=0)
			p[argIndex] = s; 
	    else
			p[argIndex] = "null"; 
	}
}

