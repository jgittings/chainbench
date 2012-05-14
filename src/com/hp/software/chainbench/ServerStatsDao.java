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

// ServerStatsDao.java

package com.hp.software.chainbench;

import java.sql.*;

public class ServerStatsDao {

	protected String TABLE = "SERVER_STATS";
			
	public void recreateTable(int serverCount,int threadCount) throws Exception {
		
		Connection cxn = Globals.getConnection();
		Statement s = cxn.createStatement();
		
		try {
			Globals.trace("Dropping " + TABLE);
			s.execute("drop table " + TABLE);
			Globals.trace("Dropped " + TABLE + " ok");
		}
		catch (Exception e) {
			System.err.println("Failed to drop table " + TABLE + ": " + e);
		}
		
		String ddl =
			 "CREATE TABLE " + TABLE + " (" + 
				"server_index NUMBER, " +
				"thread_index NUMBER, " +
				"iterations NUMBER, " +
				"locks_granted NUMBER, " +
				"locks_refused NUMBER, " +
				"granted_fraction NUMBER " +
				")";
		
		System.out.println(ddl);		
		s.execute(ddl);
		Globals.trace("Created table " + TABLE + " ok");

		for (int svr=0; svr<serverCount; svr++) {
			for (int t=0; t<threadCount; t++) {
				String sql = "insert into " + TABLE + " values(" + svr + "," + t + ", 0, 0, 0, 0)";
				s.execute(sql);								
			}
		}
		
		s.close();
	}

	public void updateRecord(int server,int thread,boolean lockGranted,int iterations) {
		try {
			Connection cxn = Globals.getConnection();
			Statement s = cxn.createStatement();

			String updateField = lockGranted ? "locks_granted" : "locks_refused";
			String whereClause = " where server_index=" + server + " and thread_index=" + thread;

			String sql = "select locks_granted,locks_refused from " + TABLE + whereClause;
			Globals.trace("Executing: " + sql);
			ResultSet rs = s.executeQuery(sql);
			Globals.trace("Executed: " + sql);
			rs.next();
			int locksGranted = rs.getInt("locks_granted");
			int locksRefused = rs.getInt("locks_refused");
	
			if (lockGranted)
				locksGranted++;
			else
				locksRefused++;
			
			double denom = (double)(locksGranted+locksRefused);
			double grantedFraction = ((double)locksGranted) / denom;			
			
			sql = "update " + TABLE + " set (iterations,locks_granted,locks_refused,granted_fraction)=" +
				"(select " + iterations + "," + locksGranted + "," + locksRefused + "," + grantedFraction +
				" from dual) " + 
				whereClause;
			Globals.trace(sql);
			
			int rows = s.executeUpdate(sql);
			if (rows==0) {
				Globals.trace("ERROR! FAILED TO UPDATE SERVER_STATS FOR SERVER=" + server +
					", THREAD=" + thread);
				Globals.suspendVM();
			}
			
			s.close();
		}
		catch (Exception e) {
			Globals.onException(e);
		}
	}	
}

