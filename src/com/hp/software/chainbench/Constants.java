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

// Constants.java

package com.hp.software.chainbench;

public class Constants {

	public static String PRODUCT_NAME = "Chainbench";

	public static final String SLOWDOWN_REVISER = "SlowdownReviser";
	public static final String SLOWDOWN_ASPECT = "SlowdownAspect";
	public static final String SLOW_SECURITY_MANAGER = "SlowdownSecurityManager";
	public static final String SLOW_MATHS = "Slowdown_maths_thread";

	public static String STATS_TABLE = "LOCK_STATS";
	public static String STATS_TABLE_ID_FIELD = "contended_rsc_id";
	public static String LOCK_NAME_STUB = "ROW_LOCK_";	

	public static final String HAZELCAST2 = "HC: Hazelcast 2";	
	public static final String EHCACHE2 = "TC: Terracotta/Ehcache 2.0";
	public static final String ZOOKEEPER = "ZK: Zookeeper 3.3.4 recipes";
	public static final String GEMFIRE = "GF: GemFire 6.6.1";
	public static final String INFINISPAN = "IF: Infinispan 5.1.2";
}

