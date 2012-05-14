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

// ServerListPanel.java

package com.hp.software.chainbench;

import java.util.ArrayList;

import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.table.TableLayout;

public class ServerListPanel extends Container implements ActionListener {

	private final static String ADDRESS_PROMPT = "Server address";
	private final static String USERNAME_PROMPT = "Username";
	private final static String PASSWORD_PROMPT = "Password";
	
	private ArrayList<ServerPanel> serverPanels = new ArrayList<ServerPanel>();
	private ArrayList<Button> deleteButtons = new ArrayList<Button>();

	private Button addServerButton = new Button("Add server");

	public ServerListPanel() {
		init(0);
		addServerButton.addActionListener(this);
	}

	public void init(int serverListIdx) {
		
        TableLayout layout = new TableLayout(20,2);
        setLayout(layout);
		
		this.removeAll();
		serverPanels.clear();
		deleteButtons.clear();

		TableLayout.Constraint constraint = leftColConstraint(0);
		addComponent(constraint,addServerButton);
		addServerButton.addActionListener(this);

		Label prompt = new Label(ADDRESS_PROMPT + " / " + USERNAME_PROMPT + " / " + PASSWORD_PROMPT);
		constraint = rightColConstraint(0);
		addComponent(constraint,prompt);
	}

	private void onDeleteButtonClicked(ActionEvent e) {
		for (int i = 0; i < deleteButtons.size(); i++) {
			if (e.getSource() == deleteButtons.get(i)) {

				TableLayout layout = (TableLayout)getLayout();
				layout.removeLayoutComponent(deleteButtons.get(i));
				layout.removeLayoutComponent(serverPanels.get(i));
				
				deleteButtons.remove(i);
				serverPanels.remove(i);
				revalidate();
			}
		}
	}

	private void onAddServer() {
		
		ServerPanel svrPanel = new ServerPanel();
		serverPanels.add(svrPanel);

		int gridy = serverPanels.size();

		Button delete = new Button("x");
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDeleteButtonClicked(e);
			}
		});

		TableLayout.Constraint constraint = leftColConstraint(gridy);
		deleteButtons.add(delete);
		addComponent(constraint,delete);

		constraint = rightColConstraint(gridy);
		addComponent(constraint,svrPanel);

		revalidate();
	}

	public ArrayList<ServerInfo> getLogins() throws Exception {
		ArrayList<ServerInfo> logins = new ArrayList<ServerInfo>();
		
		for (ServerPanel panel : serverPanels) {
			ServerInfo si = new ServerInfo(
				panel.getAddress(),panel.getUsername(),panel.getPassword());
			logins.add(si);
		}
		
		return logins;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==this.addServerButton) {
			onAddServer();
		}
	}

	private TableLayout.Constraint leftColConstraint(int y) {
		TableLayout layout = (TableLayout)getLayout();
		TableLayout.Constraint constraint = layout.createConstraint(y,0);
		constraint.setWidthPercentage(10);
		return constraint;
	}

	private TableLayout.Constraint rightColConstraint(int y) {
		TableLayout layout = (TableLayout)getLayout();
		TableLayout.Constraint constraint = layout.createConstraint(y,1);
		return constraint;
	}

}
