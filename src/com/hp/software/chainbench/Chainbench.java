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

// Chainbench.java

package com.hp.software.chainbench;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.sun.lwuit.Display;
import com.sun.lwuit.awtport.AwtImpl;

public class Chainbench {

	private ProcessControl processControl;
			
	public static void main(String[] args) {
		new Chainbench();		
	}

	public Chainbench() {
		try {
			System.out.println(System.getProperty("java.library.path"));
			
			processControl = new ProcessControl();
			initLWUIT();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void createLockStatsTable() throws Exception {

		Connection cxn = Globals.getConnection();
		Globals.trace("cxn=" + cxn);
		Statement s = cxn.createStatement();

		String ddlDrop = "drop table " + Constants.STATS_TABLE;
		try {
			s.execute(ddlDrop);
			Globals.trace("Dropped ok: " + Constants.STATS_TABLE);
		}
		catch (Exception e) {
			System.err.println("Failed to drop table: " + e);
		}

		String ddl =
			 "CREATE TABLE " + Constants.STATS_TABLE + " (" + 
				"contended_rsc_id VARCHAR2(100), " + 
				"timeField DATE, " + 
				"lastUpdatedByServer NUMBER, " +
				"lastUpdatedByThread NUMBER, " +
				"updateCount NUMBER, " +
				//
				"lastLockWaitClock NUMBER, " +
				"totalLockWaitClock NUMBER," +
				"meanLockWaitClock NUMBER," +
				"worstLockWaitClock NUMBER," +
				//
				"lastLockWaitCPU NUMBER, " +
				"totalLockWaitCPU NUMBER," +
				"meanLockWaitCPU NUMBER," +
				"worstLockWaitCPU NUMBER" +
				")";
		
		s.execute(ddl);
		Globals.trace("Created table ok");
		
		for (int i=0; i<=Globals.MAX_CONTENDED_RSC_INDEX; i++) {
			final String contendedResource = "cr" + i;
			String sql = "insert into " + Constants.STATS_TABLE + " values('" +
				contendedResource + "',sysdate,0,0,0,0,0,0,0,0,0,0,0)";
			s.execute(sql);				
		}
		
		s.close();
	}

    public void initLWUIT() {
        Frame f = new Frame(Constants.PRODUCT_NAME);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Display.getInstance().exitApplication();
            }
        });
        
        f.setLayout(new java.awt.BorderLayout());
        f.setSize(900,1000);
        AwtImpl.setUseNativeInput(false);
        Display.init(f);
        f.validate();
        f.setLocationByPlatform(true);
        f.setVisible(true);

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ChainbenchLWUIT(processControl).startApp();
            }
        });
    }	
}
