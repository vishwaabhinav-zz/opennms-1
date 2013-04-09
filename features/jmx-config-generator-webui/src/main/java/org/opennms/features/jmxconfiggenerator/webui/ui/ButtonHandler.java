package org.opennms.features.jmxconfiggenerator.webui.ui;


public interface ButtonHandler {
	public static enum ActionType {
		next, previous, save;
	}

	public boolean perform(ActionType actionType);
}
