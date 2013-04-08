/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 ******************************************************************************
 */
package org.opennms.features.jmxconfiggenerator.webui.ui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.velocity.runtime.parser.node.SetExecutor;
import org.opennms.features.jmxconfiggenerator.webui.JmxConfigGeneratorApplication;
import org.opennms.features.jmxconfiggenerator.webui.data.InternalModel;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;

import com.vaadin.Application;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * @author Markus von Rüden <mvr@opennms.com>
 */
public class ConfigResultView extends CustomComponent implements ModelChangeListener<InternalModel>, Button.ClickListener {

	private TabSheet tabSheet = new TabSheet();
	private Map<InternalModel.OutputKey, TabContent> tabContentMap = new HashMap<InternalModel.OutputKey, TabContent>();
	private Button previous = new Button("back", (Button.ClickListener) this);
	private Button download = new Button("download", (Button.ClickListener) this);

	private final JmxConfigGeneratorApplication app;

	// TODO MVR make layout definition more concrete... do not use fixed pixels...
	public ConfigResultView(JmxConfigGeneratorApplication app) {
		this.app = app;
		setSizeFull();

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.addComponent(tabSheet);
		mainLayout.addComponent(previous); // TODO set Height to a concrete value
//		layout.addComponent(download);

		tabSheet.setSizeFull();
		// TODO set tab name differently (e.g. SNMP Graph properties snippet)
		tabContentMap.put(InternalModel.OutputKey.JmxDataCollectionConfig, new TabContent("JmxDataCollectionConfig", getJmxDataCollectionConfigDescriptionText()));
		tabContentMap.put(InternalModel.OutputKey.SnmpGraphProperties, new TabContent("SnmpGraphProperties", getSnmpGraphDescriptionText()));
		// TODO add collectd.configuration.xml snippet
		// TODO add description to an externa html file :)

		// add all tabs
		for (TabContent eachContent : tabContentMap.values())
			tabSheet.addTab(eachContent, eachContent.getCaption());
		tabSheet.setSelectedTab(0); // select first component!
		
		mainLayout.setExpandRatio(tabSheet, 1);
		setCompositionRoot(mainLayout);
	}

	private String getSnmpGraphDescriptionText() {
		return "TODO enter description text here";
	}

	private String getJmxDataCollectionConfigDescriptionText() {
		return "TODO enter description text here";
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getSource() == previous) app.showMBeansView();
		if (event.getSource() == download) downloadConfigFile(event);
	}
	
	// TODO add download stuff here...
	private void downloadConfigFile(ClickEvent event) {
		final TabContent selectedTabContent = getSelectedTabContent();
		event.getButton().getWindow().open(new DownloadResource(selectedTabContent.getText(), selectedTabContent.getFileName(), getApplication()));
	}
	
	private TabContent getSelectedTabContent() {
		for (InternalModel.OutputKey eachOutputKey : tabContentMap.keySet()) {
			if (tabSheet.getSelectedTab().equals(tabContentMap.get(eachOutputKey))) {
				return tabContentMap.get(eachOutputKey);
			}
		}
		return null;
	}

	@Override
	public void modelChanged(InternalModel newValue) {
		if (newValue == null) return;
		for (Entry<InternalModel.OutputKey, String> eachEntry : newValue.getOutputMap().entrySet()) {
			if (tabContentMap.get(eachEntry.getKey()) != null) {
				tabContentMap.get(eachEntry.getKey()).setText(eachEntry.getValue());
			}
		}
	}

	private static class DownloadResource extends StreamResource {

		public DownloadResource(final String downloadString, final String filename, Application application) {
			super(new StreamSource() {
				@Override
				public InputStream getStream() {
					return new ByteArrayInputStream(downloadString.getBytes());
				}
			} , filename, application);
			setMIMEType("application/unknown");
		}
		
		public DownloadStream getStream() {
			DownloadStream ds = super.getStream();
			ds.setParameter("Content-Disposition", "attachment; filename=\"" +getFilename() + "\"");
			return ds;
		}		
	}
	
	// TODO MVR make layout definition more concrete... do not use fixed pixels...
	private static class TabContent extends Panel {

		private final TextArea text = new TextArea();

		private final Label label;

		private TabContent(String caption, String description) {
			setSizeFull();
			setContent(new HorizontalLayout());
			getContent().setSizeFull();
			setCaption(caption);
			text.setSizeFull();
			label = new Label(description);
			addComponent(text);
			addComponent(label);
		}

		// TODO use better file name
		public String getFileName() {
			return label.getCaption() + ".txt";
		}

		public void setText(String newText) {
			text.setValue(newText);
		}

		public String getText() {
			return text.getValue() == null ? "" : (String) text.getValue();
		}
	}
}
