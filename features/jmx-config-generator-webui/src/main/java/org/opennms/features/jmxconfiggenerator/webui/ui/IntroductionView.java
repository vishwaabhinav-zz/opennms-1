package org.opennms.features.jmxconfiggenerator.webui.ui;

import org.opennms.features.jmxconfiggenerator.webui.JmxConfigGeneratorApplication;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class IntroductionView extends Panel implements ClickListener {

	private final Button next;
	private JmxConfigGeneratorApplication app;
	
	public IntroductionView(JmxConfigGeneratorApplication app) {
		this.app = app;
		next = UIHelper.createButton("next",  IconProvider.BUTTON_NEXT, this);
		
		setSizeFull();
		setContent(new VerticalLayout());
		getContent().setSizeFull();
		
		
		addComponent(new Label(UIHelper.loadContentFromFile(getClass(), "/descriptions/IntroductionView.html"),
				Label.CONTENT_RAW));
		addComponent(next);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		app.updateView(UiState.ServiceConfigurationView);
	}
}
