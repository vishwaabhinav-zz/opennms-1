package org.opennms.features.jmxconfiggenerator.webui.ui;

import org.opennms.features.jmxconfiggenerator.webui.JmxConfigGeneratorApplication.UiStateMachine;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;

public class ButtonPanel extends Panel implements ModelChangeListener<UiState>, ClickListener {
	private final Button next;
	private final Button previous;
	private final Button help;
	private final UiStateMachine uiStateMachine;

	public ButtonPanel(UiStateMachine uiStateMachine) {
		this.uiStateMachine = uiStateMachine;
		next = UIHelper.createButton("next", IconProvider.BUTTON_NEXT, this);
		previous = UIHelper.createButton("previous", IconProvider.BUTTON_PREVIOUS, this);
		help = UIHelper.createButton("help", IconProvider.BUTTON_INFO, this);

		HorizontalLayout layout = new HorizontalLayout();
		setContent(layout);
		addComponent(previous);
		addComponent(help);
		addComponent(next);
	}

	@Override
	public void modelChanged(UiState newModel) {
		help.setVisible(false); // TODO enable/disable help dynamically
		previous.setVisible(newModel.hasPrevious());
		next.setVisible(newModel.hasNext());
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == next) uiStateMachine.doNext();
		if (event.getButton() == help) uiStateMachine.doHelp();
		if (event.getButton() == previous) uiStateMachine.doPrevious();
	}
}
