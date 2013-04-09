package org.opennms.features.jmxconfiggenerator.webui.ui;

import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class IntroductionView extends Panel implements ButtonHandler {

	public IntroductionView() {
		setSizeFull();
		setContent(new VerticalLayout());
		getContent().setSizeFull();
		addComponent(new Label(UIHelper.loadContentFromFile(getClass(), "/descriptions/IntroductionView.html"),
				Label.CONTENT_RAW));
	}

	@Override
	public boolean perform(ActionType actionType) {
		return true;
	}

	
}
