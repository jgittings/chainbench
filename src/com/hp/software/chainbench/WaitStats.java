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

// WaitStats

package com.hp.software.chainbench;

import java.sql.*;

public class WaitStats {

	private double totalLockWait;
	private double worstLockWait;
	private double meanLockWait;
	
	public void load(int updateCount,ResultSet rs,String fieldSuffix,double lockWaitMillis)
	throws Exception {

		totalLockWait = rs.getDouble("totalLockWait" + fieldSuffix);
		worstLockWait = rs.getDouble("worstLockWait" + fieldSuffix);
		
		if (lockWaitMillis > worstLockWait) worstLockWait = lockWaitMillis;

		totalLockWait += lockWaitMillis;
		meanLockWait = totalLockWait / ((double)updateCount);
	}
	
	public double getTotalLockWait() {return totalLockWait;}
	public double getWorstLockWait() {return worstLockWait;}
	public double getMeanLockWait() {return meanLockWait;}
}

