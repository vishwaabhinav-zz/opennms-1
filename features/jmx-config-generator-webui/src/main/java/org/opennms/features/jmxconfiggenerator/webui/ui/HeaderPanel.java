package org.opennms.features.jmxconfiggenerator.webui.ui;

import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;

import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

public class HeaderPanel extends Panel implements ModelChangeListener<UiState> {
	
	private Label label;

	public HeaderPanel() {
		label = new Label();
		label.setContentMode(Label.CONTENT_RAW);
		addComponent(label);
	}

	@Override
	public void modelChanged(UiState newModel) {
		updateLabel(newModel);
	}

	/**
	 * Updates the label.
	 * @param state The new ui State.
	 */
	// TODO do a more specific java doc comment
	private void updateLabel(UiState state) {
		final String selected = "<a href=\"#\">%d. %s</a>";
		final String notSelected = "%d. %s";
		
		String labelString = "";
		for (int i=0; i<UiState.values().length; i++) {
			UiState eachState = UiState.values()[i];
			if (!eachState.hasUi()) continue;
			String renderString = eachState.equals(state) ? selected : notSelected;
			if (!labelString.isEmpty()) labelString += " / ";
			labelString += String.format(renderString, (i+1), eachState.getDescription());
		}
		label.setValue(labelString.trim());
	}
}
