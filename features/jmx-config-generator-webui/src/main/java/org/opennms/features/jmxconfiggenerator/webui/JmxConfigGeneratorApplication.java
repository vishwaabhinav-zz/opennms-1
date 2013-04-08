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
import org.opennms.features.jmxconfiggenerator.webui.ui.ConfigForm;
import org.opennms.features.jmxconfiggenerator.webui.ui.ConfigResultView;
import org.opennms.features.jmxconfiggenerator.webui.ui.ModelChangeRegistry;
import org.opennms.features.jmxconfiggenerator.webui.ui.ProgressWindow;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.MBeansController;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.MBeansView;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.xmlns.xsd.config.jmx_datacollection.JmxDatacollectionConfig;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class JmxConfigGeneratorApplication extends com.vaadin.Application implements ModelChangeListener<InternalModel> {

    private Panel mainPanel;
    private ConfigForm configView;
    private MBeansView mBeansView;
    private ProgressWindow progressWindow;
    private ConfigResultView jmxConfigView;
    private InternalModel model;
    private ModelChangeRegistry modelChangeRegistry = new ModelChangeRegistry();

    @Override
    public void init() {
        buildMainLayout();
        setTheme(Config.STYLE_NAME);
        registerListener(InternalModel.class, this);
        initInternalModel();
        showConfigView(model);
    }

    private void initInternalModel() {
        model = new InternalModel();
        model.setCollectdConfig(CollectdConfigFactory.getInstance().getCollectdConfig());
    }

    private void buildMainLayout() {
    	// the Main panel holds all views such as Config view, mbeans view, etc.
    	// TODO maybe we can get rid of the main panel?
    	mainPanel = new Panel();
    	mainPanel.setContent(new VerticalLayout());
    	mainPanel.getContent().setSizeFull();
    	mainPanel.setSizeFull();
    	
    	// create a window and add the main panel
        setMainWindow(new Window("JmxConfigGenerator GUI Tool"));
        getMainWindow().setContent(new VerticalLayout());
        getMainWindow().getContent().setSizeFull();
        getMainWindow().setSizeFull();
        getMainWindow().addComponent(mainPanel);
        
    }

    private void setMainComponent(Component c) {
        mainPanel.removeAllComponents();
        mainPanel.addComponent(c);
    }

    private ConfigForm getConfigView() {
        if (configView == null) {
            configView = new ConfigForm(this);
            registerListener(InternalModel.class, configView);
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

    private ConfigResultView getJmxConfigView() {
        if (jmxConfigView == null) {
            jmxConfigView = new ConfigResultView(this);
            registerListener(InternalModel.class, jmxConfigView);
        }
        return jmxConfigView;
    }

    public void generateJmxConfig(MBeansController mbeansController) {
        showProgressWindow("Generating xml file. This may take a while...");
        new CreateOutputWorkerThread(mbeansController).start();
    }

    public void findMBeans() {
        showProgressWindow("Getting all available MBeans... This may take a while");
        new DetectMBeansWorkerThread().start();
    }

    private ProgressWindow getProgressWindow() {
        if (progressWindow == null) {
            progressWindow = new ProgressWindow();
        }
        return progressWindow;
    }

    public void showConfigView(InternalModel internalModel) {
        setMainComponent(getConfigView());
        notifyObservers(InternalModel.class, internalModel);
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

    public void showOutputView(InternalModel newModel) {
        setMainComponent(getJmxConfigView());
        notifyObservers(InternalModel.class, newModel);
    }

    @Override
    public void modelChanged(InternalModel newModel) {
        if (model != newModel) {
            model = newModel;
        }
    }

    private void registerListener(Class<?> aClass, ModelChangeListener listener) {
        modelChangeRegistry.registerListener(aClass, listener);
    }

    private void notifyObservers(Class<?> aClass, Object object) {
        modelChangeRegistry.notifyObservers(aClass, object);
    }

    //TODO MVR comment
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

            // get new collection config object depending on selection made in MBeansView
            final JmxDatacollectionConfig newJmxDataCollectionConfig = model.getRawModelIncludeSelection(mbeansController);

            // create JmxDataCollectoinConfig.xml
            StringWriter newJmxDataCollectionConfigStringWriter = new StringWriter();
            JAXB.marshal(newJmxDataCollectionConfig, newJmxDataCollectionConfigStringWriter);

            // create snmp-graph.properties      
            String snmpGraphProperties = "";
            try {
                GraphConfigGenerator graphConfigGenerator = new GraphConfigGenerator();
                Collection<Report> reports = new JmxConfigReader().generateReportsByJmxDatacollectionConfig(newJmxDataCollectionConfig);
                snmpGraphProperties = graphConfigGenerator.generateSnmpGraph(reports);
            } catch (IOException ex) {
                snmpGraphProperties = ex.getMessage();
                LogUtils.errorf(this, ex, "SNMP Graph-Properties couldn't be created.");
            }

            // show changes in view
            model.setOutput(InternalModel.OutputKey.JmxDataCollectionConfig, newJmxDataCollectionConfigStringWriter.getBuffer().toString());
            model.setOutput(InternalModel.OutputKey.SnmpGraphProperties, snmpGraphProperties);
            showOutputView(model);
            getMainWindow().removeWindow(getProgressWindow());
        }
    }

    // TODO MVR Thread-Handling not this way...
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

                showMBeansView(model.setRawModel(generateJmxConfigModel));
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
