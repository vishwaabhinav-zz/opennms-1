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
package org.opennms.features.jmxconfiggenerator.webui.ui.mbeans;

import org.opennms.features.jmxconfiggenerator.webui.JmxConfigGeneratorApplication;
import org.opennms.features.jmxconfiggenerator.webui.data.InternalModel;
import org.opennms.features.jmxconfiggenerator.webui.data.MetaMBeanItem;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;
import org.opennms.features.jmxconfiggenerator.webui.ui.UIHelper;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.NameEditForm.FormParameter;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractSplitPanel;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class MBeansView extends VerticalLayout implements ClickListener, ModelChangeListener, ViewStateChangedListener {

    private final MBeansController controller = new MBeansController();
    private final AbstractSplitPanel mainPanel;
    private final Layout mbeansContent;
    private final JmxConfigGeneratorApplication app;
    private final Button previous = new Button("back", (ClickListener) this);
    private final Button next = new Button("generate jmx config", (ClickListener) this);
//	private TextField serviceName;
    private final MBeansTree mbeansTree;
    private final MBeansContentTabSheet mbeansTabSheet;
    private final NameEditForm mbeansForm = new NameEditForm(controller, new FormParameter() {
        @Override
        public boolean hasFooter() {
            return true;
        }

        @Override
        public String getCaption() {
            return "MBeans details";
        }

        @Override
        public String getEditablePropertyName() {
            return MetaMBeanItem.NAME;
        }

        @Override
        public String getNonEditablePropertyName() {
            return MetaMBeanItem.OBJECTNAME;
        }

        @Override
        public Object[] getVisiblePropertieNames() {
            return new Object[]{MetaMBeanItem.SELECTED, MetaMBeanItem.OBJECTNAME, MetaMBeanItem.NAME};
        }

        @Override
        public EditControls.Callback getAdditionalCallback() {
            return new EditControls.Callback<Form>() {
                @Override
                public void callback(EditControls.ButtonType type, Form outer) {
                    if (type == EditControls.ButtonType.save) {
                        controller.updateMBeanIcon();
                        controller.updateMBean();
                    }
                }
            };
        }
    });
    // TODO MVR should we really save an instance of model here?
    private InternalModel model;

    public MBeansView(JmxConfigGeneratorApplication app) {
        this.app = app;
        setSizeFull();
        mbeansTabSheet = new MBeansContentTabSheet(controller);
        mbeansTree = new MBeansTree(controller);
        mbeansContent = initContentPanel(mbeansForm, mbeansTabSheet);
        mainPanel = initMainPanel(mbeansTree, mbeansContent);

        registerListener(controller);

        addComponent(mainPanel);
        addComponent(new UIHelper.LayoutCreator().setHorizontal().withComponents(previous, next).toLayout());
        setExpandRatio(mainPanel, 1);
    }

    @Override
    public void buttonClick(ClickEvent event) {
        if (event.getButton() == previous) {
            app.showConfigView(model);
        }
        if (event.getButton() == next) {
            app.generateJmxConfig(controller);
        }
    }

    private AbstractSplitPanel initMainPanel(Component first, Component second) {
        AbstractSplitPanel layout = new HorizontalSplitPanel();
        layout.setSizeFull();
        layout.setLocked(false);
        layout.setSplitPosition(20, UNITS_PERCENTAGE);
        layout.setFirstComponent(wrapToPanel(first));
        layout.setSecondComponent(second);
        return layout;
    }

    private Layout initContentPanel(NameEditForm form, MBeansContentTabSheet tabSheet) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(false);
        layout.addComponent(wrapToPanel(form));
        layout.addComponent(tabSheet);
//        layout.setExpandRatio(tabSheet, 1);
        return layout;
    }

    @Override
    public void modelChanged(Object newModel) {
        if (newModel instanceof InternalModel) {
            model = (InternalModel) newModel;
            controller.notifyObservers(InternalModel.class, newModel); //forward to all sub elements of this view
        }
    }

    private Panel wrapToPanel(Component component) {
        Panel panel = new Panel(component.getCaption());
        panel.setSizeFull();

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.setSizeFull();
        layout.addComponent(component);
        
        panel.setContent(layout);
        component.setCaption(null);
        return panel;
    }

    private void registerListener(MBeansController controller) {
        controller.registerListener(Item.class, mbeansForm);
        controller.registerListener(Mbean.class, mbeansTabSheet);
        controller.registerListener(InternalModel.class, mbeansTree);
        controller.registerListener(InternalModel.class, controller);
        controller.addView(mbeansForm);
        controller.addView(mbeansTabSheet);
        controller.addView(mbeansTree);
        controller.addView(this);
    }

    @Override
    public void viewStateChanged(ViewStateChangedEvent event) {
        //hide next, previous buttons if in edit mode
        previous.setEnabled(event.getNewState() != ViewState.Edit);
        next.setEnabled(event.getNewState() != ViewState.Edit);
    }
}
