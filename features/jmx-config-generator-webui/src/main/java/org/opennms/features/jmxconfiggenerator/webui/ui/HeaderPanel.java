package org.opennms.features.jmxconfiggenerator.webui.ui;

import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;

import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

/**
 * This class represents the header panel of the jmx config ui tool. It simply
 * creates a label with the text according to the {@link UiState}s.
 * 
 * @author Markus von Rüden
 * @see #updateLabel(UiState)
 * 
 */
public class HeaderPanel extends Panel implements ModelChangeListener<UiState> {

	private Label label;

	public HeaderPanel() {
		label = new Label();
		label.setContentMode(Label.CONTENT_RAW);
		addComponent(label);
	}

	/**
	 * This method updates the label of the header panel. The header panel shows
	 * all UI steps e.g. (1. Step 1 / 2. Step 2 ... / Step n). For this purpose
	 * the enum {@link UiState} is used and each UiState which has a ui is
	 * printed with a number prefix. If one uiState from {@link UiState} matches
	 * the parameter <code>state</code> it is highlighted as a link. Updates the
	 * label.
	 * 
	 * @param state
	 *            The current ui State.
	 */
	private void updateLabel(UiState state) {
		final String selected = "<a href=\"#\">%d. %s</a>";
		final String notSelected = "%d. %s";

		String labelString = "";
		int i = 1;
		for (UiState eachState : UiState.values()) {
			if (!eachState.hasUi()) continue;
			String renderString = eachState.equals(state) ? selected : notSelected;
			if (!labelString.isEmpty()) labelString += " / ";
			labelString += String.format(renderString, i, eachState.getDescription());
			i++;
		}
		label.setValue(labelString.trim());
	}
	
	@Override
	/**
	 * Is invoked when the uiState changes and updates the label.
	 */
	public void modelChanged(UiState newModel) {
		updateLabel(newModel);
	}
}
