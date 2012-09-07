/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.jmxconfiggenerator.webui.ui;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.jmxconfiggenerator.webui.JmxConfigGeneratorApplication;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;

/**
 *
 * @author m.v.rueden
 */
public class JmxGeneratedConfigView extends VerticalLayout implements ModelChangeListener<String>, Button.ClickListener {

	private TextArea result = new TextArea();
	private Button previous = new Button("back", (Button.ClickListener) this);
	private final JmxConfigGeneratorApplication app;

	public JmxGeneratedConfigView(JmxConfigGeneratorApplication app) {
		this.app = app;
		setCaption("Result of generated JMX configuration");
		result.setWidth(100, UNITS_PERCENTAGE);
		result.setHeight(600, UNITS_PIXELS);
		addComponent(result);
		addComponent(previous);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getSource() == previous) app.showMBeansView();
	}

	@Override
	public void modelChanged(String newValue) {
		if (newValue == null) return;
		result.setValue(newValue);
	}
}
