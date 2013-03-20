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

package org.opennms.features.jmxconfiggenerator.webui;

import org.opennms.features.jmxconfiggenerator.webui.ui.ProgressWindow;
import org.opennms.features.jmxconfiggenerator.webui.ui.TestWindow;
import org.opennms.features.jmxconfiggenerator.webui.ui.ConfigForm;
import org.opennms.features.jmxconfiggenerator.webui.ui.TestPanel;
import org.opennms.features.jmxconfiggenerator.webui.ui.ModelChangeRegistry;
import org.opennms.features.jmxconfiggenerator.webui.ui.JmxGeneratedConfigView;
import org.opennms.features.jmxconfiggenerator.webui.data.ConfigModel;
import org.opennms.features.jmxconfiggenerator.webui.data.InternalModel;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import javax.management.remote.JMXConnector;
import javax.xml.bind.JAXB;
import org.opennms.features.jmxconfiggenerator.Starter;
import org.opennms.features.jmxconfiggenerator.jmxconfig.JmxDatacollectionConfiggenerator;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.MBeansController;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.MBeansView;
import org.opennms.xmlns.xsd.config.jmx_datacollection.*;

@SuppressWarnings("serial")
public class JmxConfigGeneratorApplication extends com.vaadin.Application implements ModelChangeListener<InternalModel> {

	private VerticalLayout mainPanel = new VerticalLayout();
	private ConfigForm configView;
	private MBeansView mBeansView;
	private ProgressWindow progressWindow;
	private JmxGeneratedConfigView jmxConfigView;
	private InternalModel model;
//	private SchedulerFactory schedulerFactory;
	private ModelChangeRegistry modelChangeRegistry = new ModelChangeRegistry();

	@Override
	public void init() {
		buildMainLayout();
		setTheme(Config.STYLE_NAME);
//		schedulerFactory = getQuartzSchedulerFactory();
		registerListener(InternalModel.class, this);
		showConfigView();
	}

//	private SchedulerFactory getQuartzSchedulerFactory() {
//		ServletContext context = ((WebApplicationContext) getContext()).getHttpSession().getServletContext();
//		SchedulerFactory factory = (SchedulerFactory) context.getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
//		if (factory == null)
//			throw new IllegalArgumentException("Quartz is not initialized");
//		return factory;
//	}
	
	private void buildMainLayout() {
		setMainWindow(new Window("JmxConfigGenerator GUI Tool"));
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.addComponent(mainPanel);
		getMainWindow().setContent(layout);
	}

	private void setMainComponent(Component c) {
		mainPanel.removeAllComponents();
		mainPanel.addComponent(c);
	}

	private ConfigForm getConfigView() {
		if (configView == null) {
			configView = new ConfigForm(this);
		}
		return configView;
	}

	private MBeansView getMBeansView() {
		if (mBeansView == null) {
			mBeansView = new MBeansView(this);
			registerListener(InternalModel.class, mBeansView);
		}
		return mBeansView;
	}

	private JmxGeneratedConfigView getJmxConfigView() {
		if (jmxConfigView == null) {
			jmxConfigView = new JmxGeneratedConfigView(this);
			registerListener(String.class, jmxConfigView);
		}
		return jmxConfigView;
	}

	public void generateJmxConfig(MBeansController mbeansController) {
		showProgressWindow("Generating xml file. This may take a while...");
		new CreateXmlWorkerThread(mbeansController).start();
	}

	public void findMBeans() {
		showProgressWindow("Getting all available MBeans... This may take a while");
		new DetectMBeansWorkerThread().start();
	}

	private ProgressWindow getProgressWindow() {
		if (progressWindow == null)
			progressWindow = new ProgressWindow();
		return progressWindow;
	}

	public void showConfigView() {
		setMainComponent(getConfigView());
	}

	public void showMBeansView() {
		setMainComponent(getMBeansView());
	}

	public void showMBeansView(InternalModel newModel) {
		setMainComponent(getMBeansView());
		notifyObservers(InternalModel.class, newModel);
	}

	public void showProgressWindow(String label) {
		getProgressWindow().setLabelText(label);
		getMainWindow().addWindow(getProgressWindow());
	}

	public void showTestWindow() {
		getMainWindow().addWindow(new TestWindow(new TestPanel()));
	}

	public void showJmxConfigView(String jmxConfig) {
		setMainComponent(getJmxConfigView());
		notifyObservers(String.class, jmxConfig);
	}

	@Override
	public void modelChanged(InternalModel newModel) {
		if (model != newModel) model = newModel;
	}

	private void registerListener(Class<?> aClass, ModelChangeListener listener) {
		modelChangeRegistry.registerListener(aClass, listener);
	}

	private void notifyObservers(Class<?> aClass, Object object) {
		modelChangeRegistry.notifyObservers(aClass, object);
	}

	//TODO comment
	private class CreateXmlWorkerThread extends Thread {
		private final MBeansController mbeansController;

		private CreateXmlWorkerThread(MBeansController mbeansController) {
			this.mbeansController = mbeansController;
		}

		@Override
		public void run() {
			if (model == null) return;
			//create xml
			StringWriter writer = new StringWriter();
			JAXB.marshal(model.getRawModelIncludeSelection(mbeansController), writer);
			showJmxConfigView(writer.getBuffer().toString());
			getMainWindow().removeWindow(getProgressWindow());
		}
	}

	//TODO Threadhandling mit Quartz?
	private class DetectMBeansWorkerThread extends Thread {

		@Override
		public void run() {
			try {
				ConfigModel config = ((BeanItem<ConfigModel>) getConfigView().getItemDataSource()).getBean();

                                // TODO loading of the dictionary should not be done via the Starter class and not in a static way!
				JmxDatacollectionConfiggenerator jmxConfigGenerator = new JmxDatacollectionConfiggenerator();
				JMXConnector connector = jmxConfigGenerator.getJmxConnector(config.getHost(), config.getPort(), config.getUser(), config.getPassword(), config.isSsl(), config.isJmxmp());
				JmxDatacollectionConfig generateJmxConfigModel = jmxConfigGenerator.generateJmxConfigModel(connector.getMBeanServerConnection(), "anyservice", !config.isSkipDefaultVM(), config.isRunWritableMBeans(), Starter.loadInternalDictionary());
				connector.close();

				showMBeansView(new InternalModel().setRawModel(generateJmxConfigModel));
				getMainWindow().removeWindow(getProgressWindow());
			} catch (MalformedURLException ex) {
				handleError(ex);
			} catch (IOException ex) {
				handleError(ex);
			} catch (SecurityException ex) {
				handleError(ex);
			}
		}

		private void handleError(Exception ex) {
			//TODO logging?
			getMainWindow().showNotification("Connection error", "An error occured during connection jmx service. Please verify connection settings.<br/><br/>" + ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
			getMainWindow().removeWindow(getProgressWindow());
		}
	}
}
