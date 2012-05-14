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

// ChainbenchLWUIT.java

package com.hp.software.chainbench;

import com.sun.lwuit.*;
import com.sun.lwuit.events.*;
import com.sun.lwuit.plaf.*;
import com.sun.lwuit.table.TableLayout;
import com.sun.lwuit.util.*;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import javax.swing.JOptionPane;

import com.hp.software.chainbench.lockproviders.*;

public class ChainbenchLWUIT  implements ActionListener {

	private static final String COMMA = ",";
	private Random gen = new Random(System.currentTimeMillis());

	private ComboBox frameworkCombo = new ComboBox(
		new String[] {
			Constants.HAZELCAST2,
			Constants.INFINISPAN,
			Constants.GEMFIRE,
			Constants.ZOOKEEPER,
			Constants.EHCACHE2});

    private DistributedLockProvider lockProvider;
    private Container providerOptionsContainer;

	public static final String[] slowModes = {
		"None",
		Constants.SLOWDOWN_REVISER,
		Constants.SLOW_SECURITY_MANAGER,
		Constants.SLOWDOWN_ASPECT,
		Constants.SLOW_MATHS};
	private ComboBox slowdownModeCombo = new ComboBox(slowModes);	

	private class AlphanumTextField extends TextField {
		public AlphanumTextField(String s) {
			super(s);
			setConstraint(TextField.ANY);
			setInputModeOrder(new String[] {"123abc"});
		}
	}

	private class NumericTextField extends TextField {
		public NumericTextField(String s) {
			super(s);
			setConstraint(TextField.NUMERIC);
			setInputModeOrder(new String[] {"123"});
		}
	}

	private class DecimalTextField extends TextField {
		public DecimalTextField(String s) {
			super(s);
			setConstraint(TextField.DECIMAL);
			setInputModeOrder(new String[] {"123"});
		}
	}

	private TextField resultsFileField = new TextField("/resultsFile.txt");	
	private TextField frameworkListField = new TextField("TC,HC");	

	private TextField jdbcUrlField
		= new TextField("jdbc:oracle:thin:@localhost:1521:SCHEMANAME");
	
	private TextField usernameField = new TextField("DBUSERNAME");	
	private TextField pwdField = new TextField("dbpassword");	
	private TextField serverCountField = new AlphanumTextField("1-9,2");	
	private TextField threadCountField = new AlphanumTextField("1"); // 1-6,5");	

	private ServerListPanel serverListPanel;	
	
	private TextField contendedRscCountField = new NumericTextField("3");	
	private TextField heapSizeField = new NumericTextField("30");	
	private TextField heapExhaustionNodesField = new NumericTextField("");	
	private TextField heapExhaustionField = new DecimalTextField("0.0");	
	private CheckBox lockingEnabledCheckBox = new CheckBox();	

	private TextField iterationsField = new NumericTextField("2000");

	// Comma separated list
	private TextField stallNodesListField = new NumericTextField("");	

	// Iterations after which to stall on specified nodes
	private TextField stallAfterIterationsField = new NumericTextField("75");	

	// Comma separated list
	private TextField slowNodesListField = new NumericTextField("");	

	// Iterations after which to trigger slow-running on specified nodes
	private TextField slowAfterIterationsField = new NumericTextField("75");	

	// Numeric param controlling slowdown, meaning dependent on slowdown mode:
	// SlowdownSecurityManager: sleep per call
	// SlowdownAspect: sleep per call
	// SlowdownHeavyMathsThread: number of threads
	private TextField slowdownAmountField = new NumericTextField("50");	

	private Button testButton = new Button("Test");	
	private Button stopButton = new Button("Stop");	
	private TextArea resultsArea = new TextArea();	

	private ProcessControl processControl;

    public ChainbenchLWUIT(ProcessControl processControl) {
    	this.processControl = processControl;
    	
    	Globals.resultsArea = resultsArea;
    	
    	this.serverListPanel = new ServerListPanel();
    	
    	Display.getInstance().setDefaultVirtualKeyboard(null);    	
    }
    
    public void startApp() {
        try {
            Resources rscs = Resources.open("/TipsterTheme.res");
            Hashtable theme = rscs.getTheme(rscs.getThemeResourceNames()[0]);
            changeThemeFonts(theme);
            UIManager.getInstance().setThemeProps(theme);

            Display.getInstance().callSerially(new Runnable() {
                public void run() {
                	try {
                		doLayout();
                	}
                	catch (Throwable t) {
                        Dialog.show("Exception", t.getMessage(), "OK", null);
                	}
                }
            });
            
        } catch (Throwable e) {
            e.printStackTrace();
            Dialog.show("Exception", e.getMessage(), "OK", null);
        }
    }

    /**Set the fonts programatically.*/
    private void changeThemeFonts(Hashtable theme) {
        Font fDefault = Font.createSystemFont(Font.FACE_PROPORTIONAL,
            	Font.STYLE_PLAIN,Font.SIZE_SMALL);
        Font.setDefaultFont(fDefault);
        
        Enumeration e = theme.keys();
        while(e.hasMoreElements()) {
            String current = (String)e.nextElement();
            if (current.contains("font")) {
            	theme.put(current,fDefault);
            }
        }
    }
    
    private void doLayout() throws Exception {
        Font.setBitmapFontEnabled(true);

        Form main;
        main = new Form(Constants.PRODUCT_NAME);
        TableLayout layout = new TableLayout(10,2);
        main.setLayout(layout);
        main.setDropTarget(true);

        main.getContentPane().setDraggable(false);

        TableLayout.Constraint constraint = layout.createConstraint();
        main.addComponent(constraint,testButton);
        
        constraint = layout.createConstraint();
        main.addComponent(constraint,stopButton);

        addPromptAndComponent(main,"Results file",resultsFileField);
        
        addPromptAndComponent(main,"Locking provider",frameworkCombo);
        addPromptAndComponent(main,"Locking provider list",frameworkListField);
        
        frameworkCombo.addSelectionListener( new SelectionListener() {
			@Override
			public void selectionChanged(int arg0, int arg1) {
				try {
					setProviderOptionsPanel();
				}
				catch (Exception e) {System.err.println(e);}
			}        	
        } );

        constraint = layout.createConstraint();
        constraint.setHorizontalSpan(2);
        providerOptionsContainer = new Container();
    	main.addComponent(constraint,providerOptionsContainer);
    	setProviderOptionsPanel();
        
        addPromptAndComponent(main,"JDBC URL",jdbcUrlField);
        addPromptAndComponent(main,"Username",usernameField);
        addPromptAndComponent(main,"Password",pwdField);
        addPromptAndComponent(main,"No of servers (eg 1-5,2)",serverCountField);
        addPromptAndComponent(main,"Server threads (eg 1-30,5)",threadCountField);
        
        constraint = layout.createConstraint();
        constraint.setHorizontalSpan(2);
        main.addComponent(constraint,serverListPanel);
        
        addPromptAndComponent(main,"Contended resources",contendedRscCountField);
        addPromptAndComponent(main,"Max heap size (MB)",heapSizeField);
        addPromptAndComponent(main,"Heap exhaustion nodes",heapExhaustionNodesField);
        addPromptAndComponent(main,"Heap exhaustion (% full)",heapExhaustionField);
        addPromptAndComponent(main,"Locking enabled",lockingEnabledCheckBox);

        addPromptAndComponent(main,"Nodes to stall",stallNodesListField);
        addPromptAndComponent(main,"Iterations to stall after",stallAfterIterationsField);

        addPromptAndComponent(main,"Nodes to slow down",slowNodesListField);
        addPromptAndComponent(main,"Iterations to trigger slowdown after",slowAfterIterationsField);
        addPromptAndComponent(main,"Slowdown method",slowdownModeCombo);
        addPromptAndComponent(main,"Slowdown amount (method dependent)",slowdownAmountField);

        addPromptAndComponent(main,"Iterations",iterationsField);

        constraint = layout.createConstraint();
        constraint.setHorizontalSpan(2);
        constraint.setVerticalSpan(5);
        main.addComponent(constraint,resultsArea);
        resultsArea.setEditable(false);
        
        testButton.addActionListener(this);
        stopButton.addActionListener(this);
        lockingEnabledCheckBox.setSelected(true);
        
        main.show();
    }

	private void setProviderOptionsPanel() throws Exception {
		providerOptionsContainer.removeAll();
		
        String fwkCode =
        	Globals.getFrameworkCode((String)frameworkCombo.getSelectedItem());
        lockProvider = Globals.instantiateLockProvider(fwkCode);
        Container providerOptions = lockProvider.getExtraOptionsPanel();
        if (providerOptions!=null) {
        	providerOptionsContainer.addComponent(providerOptions);
        }		
	}

	private void addPromptAndComponent(Form main,String prompt,Component tf) {

		TableLayout layout = (TableLayout)main.getLayout();
		TableLayout.Constraint constraint = layout.createConstraint();
		constraint.setWidthPercentage(40);
		
		main.addComponent(constraint,new Label(prompt));
		main.addComponent(tf);
	}	
        
    public void actionPerformed(ActionEvent e) {
		if (e.getSource()==testButton) {
			try {
				onTest();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		else {
			processControl.stopAll();
		}
    }
    
    private void onTest() throws Exception {
		int crCount = new Integer(contendedRscCountField.getText());
		double heapFillPercent = new Double(heapExhaustionField.getText());
		int stallAfterIterations = new Integer(stallAfterIterationsField.getText());
		int slowdownAfterIterations = new Integer(slowAfterIterationsField.getText());
		int slowdownAmount = new Integer(slowdownAmountField.getText());
		int iterations = new Integer(iterationsField.getText());

		Globals.JDBC_URL = this.jdbcUrlField.getText();
		Globals.JDBC_USER = this.usernameField.getText();
		Globals.JDBC_PASSWORD = this.pwdField.getText();
		Globals.MAX_CONTENDED_RSC_INDEX = crCount - 1;		

		if (this.frameworkCombo.getSelectedItem().equals(Constants.EHCACHE2)) {
			new EhcacheLockProvider().doClusterWideInitialization();
		}

	    String[] args = Params.createArgsArray();
	    Params.setJdbcUrl(args,Globals.JDBC_URL);
	    Params.setJdbcUser(args,Globals.JDBC_USER);
	    Params.setJdbcPassword(args,Globals.JDBC_PASSWORD);
	    Params.setMaxContendedRscIndex(args,crCount-1);
	    Params.setHeapExhaustionPercent(args,heapFillPercent);
	    Params.setHeapExhaustionNodesList(args,heapExhaustionNodesField.getText());
	    Params.setLockingOn(args,lockingEnabledCheckBox.isSelected());
	    Params.setIterations(args,iterations);
    	Params.setStallNodesList(args,stallNodesListField.getText());
    	Params.setStallAfterIterations(args,stallAfterIterations);

    	Params.setSlowdownNodesList(args,slowNodesListField.getText());
    	Params.setSlowdownAfterIterations(args,slowdownAfterIterations);
    	Params.setSlowdownAmount(args,slowdownAmount);
	    Params.setSlowdownMode(args,(String)slowdownModeCombo.getSelectedItem());

	    String resultsFilePath = resultsFileField.getText();
	    if (resultsFilePath.length()==0) {
			int serverCount = new Integer(serverCountField.getText());
			int threadCount = new Integer(threadCountField.getText());
			String framework = (String)frameworkCombo.getSelectedItem();
			String fwkCode = Globals.getFrameworkCode(framework);
	    	onSingleTest(args,serverCount,threadCount,fwkCode);
	    }
	    else
	    	onMultipleTests(args,iterations,resultsFilePath,crCount);
    }

    private void onSingleTest(String[] args,int serverCount,int threadCount,
    	String fwkCode) throws Exception {

	    Params.setServerCount(args,serverCount);
	    Params.setThreadCount(args,threadCount);

		int maxHeapMB = new Integer(heapSizeField.getText());
    	ArrayList<ServerInfo> logins = this.serverListPanel.getLogins();

	    Params.setFramework(args,fwkCode);

		Chainbench.createLockStatsTable();
		new ServerStatsDao().recreateTable(serverCount,threadCount);

		processControl.spawn(serverCount,maxHeapMB,fwkCode,logins,args);		
    }

    private void onMultipleTests(String[] args,int iterations,
    	String resultsFilePath,int crCount) throws Exception {

    	FileOutputStream fos = new FileOutputStream(resultsFilePath);
        Writer outw = new OutputStreamWriter(fos);
		outw.write("servers,threads,crs,meanMeanLockWait \n");

    	String fwkCodeList = this.frameworkListField.getText();
    	String[] frameworkCodes = null;
    	if (fwkCodeList.length()!=0) {
    		frameworkCodes = fwkCodeList.toUpperCase().split(",");
    	}
    	else {
    		frameworkCodes = new String[1];
    		frameworkCodes[0] =
    			Globals.getFrameworkCode( (String)frameworkCombo.getSelectedItem() );
    	}
    	
    	RangeAndStep serverRange = processRangeField(serverCountField);
    	RangeAndStep threadRange = processRangeField(threadCountField);

		Connection cxn = Globals.getConnection();
		Statement stmt = cxn.createStatement();

		if (frameworkCodes.length==1) {
	    	for (int s=serverRange.low; s<=serverRange.high; s+=serverRange.step) {
	        	for (int t=threadRange.low; t<=threadRange.high; t+=threadRange.step) {
	        	    doRunAndRecordResults(args,s,t,frameworkCodes[0],
	        	    	stmt,iterations,outw,crCount);
	        	}
	    	}
		}
		else {
			double fServerValueCount = ((double)(serverRange.high - serverRange.low))
				/ (double)serverRange.step;
			int serverValueCount = ((int)fServerValueCount) + 1;

			double fThreadValueCount = (double)(threadRange.high - threadRange.low)
				/ (double)threadRange.step;
			int threadValueCount = ((int)fThreadValueCount) + 1;
			
			while (1==1) {
				int fwkIndex = gen.nextInt(frameworkCodes.length);
				System.out.println("Framework index=" + fwkIndex);
				String fwkCode = frameworkCodes[fwkIndex];
				System.out.println("Framework code=" + fwkCode);
				
				int serverIndex = serverRange.low +
					(gen.nextInt(serverValueCount) * serverRange.step);
				
				int threadIndex = threadRange.low +
					(gen.nextInt(threadValueCount) * threadRange.step);

        	    doRunAndRecordResults(args,serverIndex,threadIndex,fwkCode,
	        	    	stmt,iterations,outw,crCount);
			}
		}

    	outw.close();    	
    	stmt.close();
    	cxn.close();
    	
		JOptionPane.showMessageDialog(null,
			"All test runs finished at " + new java.util.Date());
    }
    
    private void doRunAndRecordResults(String[] args,int serverCount,
    	int threadCount,String framework,
    	Statement stmt,int iterations,Writer resultsWriter,int crCount)
    	throws Exception {

    	final String QUERY =
    		"select count(*) from SERVER_STATS where iterations=" + iterations;

		System.out.println("\n Running test with framework=" + framework +
				" and " + serverCount + " servers and " +
    			threadCount + " threads");
		onSingleTest(args,serverCount,threadCount,framework);
		
		int count = 0;
		final int totalThreads = serverCount*threadCount;
		double meanMeanLockWait = 0.0;
		
		while (count < totalThreads) {
			ResultSet rs = stmt.executeQuery(QUERY);				
			rs.next();
			count = rs.getInt(1);
			rs.close();
			System.out.println(new java.util.Date() + " " + framework +
				" Completed threads=" + count +
				", total threads=" + totalThreads);

			// Use the last value from when all servers were still running
			if (count==0) {
        		rs = stmt.executeQuery("select avg(meanlockwaitclock) from lock_stats");
        		rs.next();
        		meanMeanLockWait = rs.getDouble(1);
        		rs.close();	    				
			}
			
			Globals.sleep(10000);
		}        		
		
		resultsWriter.write("" + framework + COMMA + serverCount + COMMA + threadCount +
			COMMA + crCount + COMMA + meanMeanLockWait + "\n");
		resultsWriter.flush();    	
    }
    
    private class RangeAndStep {    	
    	public RangeAndStep(int singleValue) {
    		low = singleValue;
    		high = singleValue;
    		step = 1;
    	}

    	public RangeAndStep(int low,int high,int step) {
    		this.low = low;
    		this.high = high;
    		this.step = step;
    	}

    	public int low;
    	public int high;
    	public int step;
    }
    
    private RangeAndStep processRangeField(TextField rangeField) {

    	String range = rangeField.getText();
    	if (range.contains("-")==false) {
    		int low = new Integer(range);
    		return new RangeAndStep(low);
    	}

    	if (range.contains(",")==false) {
    		String[] parts = range.split("-");
    		int low = new Integer(parts[0]);
    		int high = new Integer(parts[1]);
    		return new RangeAndStep(low,high,1);
    	}

		String[] parts = range.split("-|,");
		int low = new Integer(parts[0]);
		int high = new Integer(parts[1]);
		int step = new Integer(parts[2]);
		return new RangeAndStep(low,high,step);
    }
}

