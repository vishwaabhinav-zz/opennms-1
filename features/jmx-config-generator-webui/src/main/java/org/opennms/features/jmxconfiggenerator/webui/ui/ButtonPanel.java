package org.opennms.features.jmxconfiggenerator.webui.ui;

import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;

public class ButtonPanel extends HorizontalLayout implements ModelChangeListener<UiState> {
	private final Button next;
	private final Button previous;

	public ButtonPanel(ClickListener listener) {
		next = UIHelper.createButton("next", IconProvider.BUTTON_NEXT, listener);
		previous = UIHelper.createButton("previous", IconProvider.BUTTON_PREVIOUS, listener);
//		help = UIHelper.createButton("help", IconProvider.BUTTON_INFO, this);

		addComponent(previous);
//		addComponent(help);
		addComponent(next);
	}

	public Button getNext() {
		return next;
	}
	
	public Button getPrevious() {
		return previous;
	}
	
	@Override
	public void modelChanged(UiState newModel) {
//		help.setVisible(false); // TODO enable/disable help dynamically
		previous.setVisible(newModel.hasPrevious());
		next.setVisible(newModel.hasNext());
	}
}
