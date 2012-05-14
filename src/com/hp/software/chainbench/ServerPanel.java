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

// ServerPanel.java

package com.hp.software.chainbench;

import com.sun.lwuit.*;
import com.sun.lwuit.table.TableLayout;

public class ServerPanel extends Container {

	private TextField addressTF = new TextField();
	private TextField usernameTF = new TextField();
	private TextField passwordTF = new TextField();

	public void setHost(String host) {addressTF.setText(host);}
	public void setUsername(String user) {usernameTF.setText(user);}

	public ServerPanel() {
		
        TableLayout layout = new TableLayout(10,3);
        setLayout(layout);

        addComponent(addressTF);
        addComponent(usernameTF);
        addComponent(passwordTF);

		addressTF.setText("192.168.2.2");
		usernameTF.setText("username");
		passwordTF.setText("password");
	}
	
	public String getAddress() {return addressTF.getText();}
	public String getUsername() {return usernameTF.getText();}
	public String getPassword() {return passwordTF.getText();}
}
