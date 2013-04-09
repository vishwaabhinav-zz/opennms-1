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
 * *****************************************************************************
 */
package org.opennms.features.jmxconfiggenerator.webui;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.xml.bind.JAXB;

import org.opennms.core.utils.LogUtils;
import org.opennms.features.jmxconfiggenerator.Starter;
import org.opennms.features.jmxconfiggenerator.graphs.GraphConfigGenerator;
import org.opennms.features.jmxconfiggenerator.graphs.JmxConfigReader;
import org.opennms.features.jmxconfiggenerator.graphs.Report;
import org.opennms.features.jmxconfiggenerator.jmxconfig.JmxDatacollectionConfiggenerator;
import org.opennms.features.jmxconfiggenerator.webui.data.ConfigModel;
import org.opennms.features.jmxconfiggenerator.webui.data.InternalModel;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;
import org.opennms.features.jmxconfiggenerator.webui.ui.ButtonHandler;
import org.opennms.features.jmxconfiggenerator.webui.ui.ButtonHandler.ActionType;
import org.opennms.features.jmxconfiggenerator.webui.ui.ButtonPanel;
import org.opennms.features.jmxconfiggenerator.webui.ui.ConfigForm;
import org.opennms.features.jmxconfiggenerator.webui.ui.ConfigResultView;
import org.opennms.features.jmxconfiggenerator.webui.ui.HeaderPanel;
import org.opennms.features.jmxconfiggenerator.webui.ui.IntroductionView;
import org.opennms.features.jmxconfiggenerator.webui.ui.ModelChangeRegistry;
import org.opennms.features.jmxconfiggenerator.webui.ui.ProgressWindow;
import org.opennms.features.jmxconfiggenerator.webui.ui.UIHelper;
import org.opennms.features.jmxconfiggenerator.webui.ui.UiState;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.MBeansController;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.MBeansView;
import org.opennms.xmlns.xsd.config.jmx_datacollection.JmxDatacollectionConfig;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class JmxConfigGeneratorApplication extends com.vaadin.Application implements ModelChangeListener<InternalModel> {

	private interface Action {
		public boolean perform(ActionType actionType, UiState uiState);
	}

	public class UiStateMachine implements ModelChangeListener<UiState> {		
		private class GuiAction implements Action {

			@Override
			public boolean perform(ActionType actionType, UiState uiState) {
				return ((ButtonHandler) getView(uiState)).perform(actionType);
			}

		}

		private Component createView(UiState uiState, JmxConfigGeneratorApplication app) {
			Component component = null;
			switch (uiState) {
				case IntroductionView:
					component = new IntroductionView();
					break;
				case ServiceConfigurationView:
					component = new ConfigForm(app);
					registerListener(InternalModel.class, (ModelChangeListener) component);
					break;
				case MbeansView:
					component = new MBeansView(app);
					registerListener(InternalModel.class, (ModelChangeListener) component);
					break;
				case ResultView:
					component = new ConfigResultView(app);
					registerListener(InternalModel.class, (ModelChangeListener) component);
					break;
			}
			return component;
		}

		private UiState currentUiState = UiState.IntroductionView;
		private Map<UiState, Action> actionMap = new HashMap<UiState, Action>();
		private Map<UiState, Component> viewCache = new HashMap<UiState, Component>();

		public UiStateMachine() {
			actionMap.put(UiState.IntroductionView, new GuiAction());
			actionMap.put(UiState.ServiceConfigurationView, new GuiAction() {
				@Override
				public boolean perform(ActionType actionType, UiState uiState) {
					boolean guiOk = super.perform(actionType, uiState);
					if (guiOk) {
						currentUiState = currentUiState.getNext(); // skip :)
						doNext(); // start mbeans stuff
						return false;
					}
					return guiOk;
				}
			});
			actionMap.put(UiState.MbeansView, new GuiAction());
			actionMap.put(UiState.ResultView, new GuiAction());
			actionMap.put(UiState.MbeansDetection, new Action() {

				@Override
				public boolean perform(ActionType actionType, UiState uiState) {
					if (actionType == ActionType.next) {
						showProgressWindow("Getting all available MBeans... This may take a while");
						model.setRawModel(detectMbeans());
						notifyObservers(InternalModel.class, model);
						getMainWindow().removeWindow(getProgressWindow());
						return true;
					}
					doPrevious();
					return false;
				}

				private void handleError(Exception ex) {
					Thread.currentThread().interrupt();
					// LogUtils.errorf(t, format, args)
					// TODO logging?
					getMainWindow().showNotification(
							"Connection error",
							"An error occured during connection jmx service. Please verify connection settings.<br/><br/>"
									+ ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
					getMainWindow().removeWindow(getProgressWindow());
				}

				private JmxDatacollectionConfig detectMbeans() {
					try {
						@SuppressWarnings("unchecked")
						ConfigModel config = ((BeanItem<ConfigModel>) ((ConfigForm) uiStateMachine
								.getView(UiState.ServiceConfigurationView)).getItemDataSource()).getBean();

						// TODO loading of the dictionary should not be done via
						// the Starter class and not in a static way!
						JmxDatacollectionConfiggenerator jmxConfigGenerator = new JmxDatacollectionConfiggenerator();
						JMXConnector connector = jmxConfigGenerator.getJmxConnector(config.getHost(), config.getPort(),
								config.getUser(), config.getPassword(), config.isSsl(), config.isJmxmp());

						JmxDatacollectionConfig generateJmxConfigModel = jmxConfigGenerator.generateJmxConfigModel(
								connector.getMBeanServerConnection(), model.getServiceName(),
								!config.isSkipDefaultVM(), config.isRunWritableMBeans(),
								Starter.loadInternalDictionary());
						connector.close();
						return generateJmxConfigModel;
					} catch (MalformedURLException ex) {
						handleError(ex);
					} catch (IOException ex) {
						handleError(ex);
					} catch (SecurityException ex) {
						handleError(ex);
					}
					return null;
				}

			});
			actionMap.put(UiState.ResultConfigGeneration, new Action() {

				@Override
				public boolean perform(ActionType actionType, UiState uiState) {
					if (actionType == ActionType.next) {
						// showProgressWindow("Getting all available MBeans... This may take a while");
						// new DetectMBeansWorkerThread().start();
						return false;
					}
					UiStateMachine.this.doPrevious();
					return true;
				}

			});
		}

		private Component getView(UiState uiState) {
			if (viewCache.get(uiState) == null) {
				Component component = createView(uiState, JmxConfigGeneratorApplication.this);
				if (component == null) return null; // no "real" view
				viewCache.put(uiState, component);
			}
			return viewCache.get(uiState);
		}

		public void doNext() {
			if (!currentUiState.hasNext()) return;
			if (!actionMap.get(currentUiState).perform(ActionType.next, currentUiState)) return;
			notifyObservers(UiState.class, currentUiState.getNext());
		}

		public void doHelp() {

		}

		public void doPrevious() {
			if (!currentUiState.hasPrevious()) return;
			if (!actionMap.get(currentUiState).perform(ActionType.previous, currentUiState)) return;
			notifyObservers(UiState.class, currentUiState.getPrevious());
		}

		@Override
		public void modelChanged(UiState newModel) {
			currentUiState = newModel;
			Component component = getView(newModel); 
			if (component == null) return; // no view available
			setContentPanelComponent(component);
		}
	}

	/**
	 * The Header panel which holds the steps which are necessary to complete
	 * the configuration for a new service to get collected.
	 * 
	 */
	private HeaderPanel headerPanel;

	/**
	 * The "content" panel which shows the view for the currently selected step
	 * of the configuration process.
	 */
	private Panel contentPanel;

	/**
	 * The button panel which shows the buttons for next and previous.
	 */
	private ButtonPanel buttonPanel;
	private ProgressWindow progressWindow;

	// TODO rename InternalModel to something more accurate, e.g.
	// JmxConfigUiModel or so...
	private InternalModel model = new InternalModel();
	private ModelChangeRegistry modelChangeRegistry = new ModelChangeRegistry();

	private UiStateMachine uiStateMachine = new UiStateMachine();

	@Override
	public void init() {
		setTheme(Config.STYLE_NAME);
		initHeaderPanel();
		initContentPanel();
		initButtonPanel();
		initMainWindow();

		registerListener(InternalModel.class, this);
		registerListener(UiState.class, uiStateMachine);

		notifyObservers(uiStateMachine.currentUiState.getClass(), uiStateMachine.currentUiState);
	}

	private void initButtonPanel() {
		buttonPanel = new ButtonPanel(uiStateMachine);
		registerListener(UiState.class, buttonPanel);
	}

	private void initHeaderPanel() {
		headerPanel = new HeaderPanel();
		registerListener(UiState.class, headerPanel);
	}

	// the Main panel holds all views such as Config view, mbeans view, etc.
	private void initContentPanel() {
		contentPanel = new Panel();
		contentPanel.setContent(new VerticalLayout());
		contentPanel.getContent().setSizeFull();
		contentPanel.setSizeFull();
	}

	/**
	 * Creates the main window and adds the header, main and button panels to
	 * it.
	 */
	private void initMainWindow() {
		setMainWindow(new Window("JmxConfigGenerator GUI Tool"));
		getMainWindow().setContent(new VerticalLayout());
		getMainWindow().getContent().setSizeFull();
		getMainWindow().setSizeFull();
		getMainWindow().addComponent(headerPanel);
		getMainWindow().addComponent(contentPanel);
		getMainWindow().addComponent(buttonPanel);
		// content Panel should use most of the space :)
		((VerticalLayout) getMainWindow().getContent()).setExpandRatio(contentPanel, 1);
	}

	private void setContentPanelComponent(Component c) {
		contentPanel.removeAllComponents();
		contentPanel.addComponent(c);
	}

	// public void generateJmxConfig(MBeansController mbeansController) {
	// showProgressWindow("Generating xml file. This may take a while...");
	// new CreateOutputWorkerThread(mbeansController).start();
	// }

	// public void findMBeans() {
	// showProgressWindow("Getting all available MBeans... This may take a while");
	// new DetectMBeansWorkerThread().start();
	// }

	private ProgressWindow getProgressWindow() {
		if (progressWindow == null) {
			progressWindow = new ProgressWindow();
		}
		return progressWindow;
	}

	// public void showConfigView(InternalModel internalModel) {
	// setMainComponent(getConfigView());
	// notifyObservers(InternalModel.class, internalModel);
	// }
	//
	// public void showMBeansView() {
	// setMainComponent(getMBeansView());
	// }
	//
	// public void showMBeansView(InternalModel newModel) {
	// setMainComponent(getMBeansView());
	// notifyObservers(InternalModel.class, newModel);
	// }

	public void showProgressWindow(String label) {
		getProgressWindow().setLabelText(label);
		getMainWindow().addWindow(getProgressWindow());
	}

	// public void showOutputView(InternalModel newModel) {
	// setMainComponent(getJmxConfigView());
	// notifyObservers(InternalModel.class, newModel);
	// }

	private void registerListener(Class<?> aClass, ModelChangeListener<?> listener) {
		modelChangeRegistry.registerListener(aClass, listener);
	}

	private void notifyObservers(Class<?> aClass, Object object) {
		modelChangeRegistry.notifyObservers(aClass, object);
	}

	// TODO MVR comment
	// TODO MVR Thread-Handling not this way...
	private class CreateOutputWorkerThread extends Thread {

		private final MBeansController mbeansController;

		private CreateOutputWorkerThread(MBeansController mbeansController) {
			this.mbeansController = mbeansController;
		}

		@Override
		public void run() {
			if (model == null) {
				return;
			}

			// get new collection config object depending on selection made in
			// MBeansView
			final JmxDatacollectionConfig newJmxDataCollectionConfig = model
					.getRawModelIncludeSelection(mbeansController);

			// create JmxDataCollectoinConfig.xml
			StringWriter newJmxDataCollectionConfigStringWriter = new StringWriter();
			JAXB.marshal(newJmxDataCollectionConfig, newJmxDataCollectionConfigStringWriter);

			// create CollectdConfigSnippet.xml
			StringWriter collectdConfigSnippetStringWriter = new StringWriter();
			// JAXB.marshal(model.getCollectdConfiguration(),
			// collectdConfigSnippetStringWriter); // TODO get it done

			// create snmp-graph.properties
			String snmpGraphProperties = "";
			try {
				GraphConfigGenerator graphConfigGenerator = new GraphConfigGenerator();
				Collection<Report> reports = new JmxConfigReader()
						.generateReportsByJmxDatacollectionConfig(newJmxDataCollectionConfig);
				snmpGraphProperties = graphConfigGenerator.generateSnmpGraph(reports);
			} catch (IOException ex) {
				snmpGraphProperties = ex.getMessage();
				LogUtils.errorf(this, ex, "SNMP Graph-Properties couldn't be created.");
			}

			// show changes in view
			model.setOutput(InternalModel.OutputKey.JmxDataCollectionConfig, newJmxDataCollectionConfigStringWriter
					.getBuffer().toString());
			model.setOutput(InternalModel.OutputKey.SnmpGraphProperties, snmpGraphProperties);
			model.setOutput(InternalModel.OutputKey.CollectdConfigSnippet, collectdConfigSnippetStringWriter
					.getBuffer().toString());
			// showOutputView(model);
			getMainWindow().removeWindow(getProgressWindow());
		}
	}

	@Override
	public void modelChanged(InternalModel newModel) {
		if (model != newModel) {
			model = newModel;
		}
	}
}
