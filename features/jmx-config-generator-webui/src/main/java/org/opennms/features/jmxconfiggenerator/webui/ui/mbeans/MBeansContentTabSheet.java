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

package org.opennms.features.jmxconfiggenerator.webui.ui.mbeans;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ReadOnlyStatusChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;
import org.opennms.features.jmxconfiggenerator.webui.ui.UIHelper;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.EditControls.ButtonHandler;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.EditControls.ButtonType;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.EditControls.FormButtonHandler;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.EditControls.TableButtonHandler;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.MBeansController.Callback;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

/**
 *
 * @author m.v.rueden
 */
public class MBeansContentTabSheet extends TabSheet implements ModelChangeListener<Mbean>, ViewStateChangedListener {

	private AttributesLayout attributesLayout;
	private CompositesLayout compositesLayout;
	private final MBeansController controller;
	private int selectedTabPosition;

	public MBeansContentTabSheet(final MBeansController controller) {
		this.controller = controller;
		attributesLayout = new AttributesLayout();
		compositesLayout = new CompositesLayout();
		addTab(attributesLayout, "Attributes");
		addTab(compositesLayout, "Composites");
		setSizeFull();
	}

	@Override
	public void modelChanged(Mbean newModel) {
		//forward
		attributesLayout.modelChanged(newModel);
		compositesLayout.modelChanged(newModel);
		disableCompositesTabIfNecessary(newModel);
	}

	@Override
	public void viewStateChanged(ViewStateChangedEvent event) {
		//just forward
		attributesLayout.viewStateChanged(event);
		compositesLayout.viewStateChanged(event);
		selectedTabPosition = UIHelper.enableTabs(this, event, selectedTabPosition);
		disableCompositesTabIfNecessary(controller.getSelectedMBean());
	}

	private void disableCompositesTabIfNecessary(Mbean newModel) {
		boolean alreadyDisabled = !getTab(compositesLayout).isEnabled();
		boolean shouldDisable = newModel == null || newModel.getCompAttrib().isEmpty();
		boolean disabled = shouldDisable || alreadyDisabled; //so we do not overwrite enable/disable while already disabled due to another component
		//disable composites tab, if there are no composites
		getTab(compositesLayout).setEnabled(!disabled);
		getTab(compositesLayout).setDescription(disabled ? "no composites available" : null);
	}

	private class CompositesLayout extends VerticalLayout implements ViewStateChangedListener, ModelChangeListener<Mbean> {

		private final TabSheet tabSheet = new TabSheet();
		private int selectedCompositesTabPosition;
		
		public CompositesLayout() {
			tabSheet.setWidth(100, UNITS_PERCENTAGE);
			tabSheet.setHeight(300, UNITS_PIXELS);
			addComponent(tabSheet);
			setSpacing(false);
			setMargin(false);
		}

		@Override
		public void viewStateChanged(ViewStateChangedEvent event) {
				selectedCompositesTabPosition = UIHelper.enableTabs(tabSheet, event, selectedCompositesTabPosition);
				if (tabSheet.getSelectedTab() == null) return;
				((CompositeTabLayout)tabSheet.getSelectedTab()).viewStateChanged((event)); //forwared
		}

		@Override
		public void modelChanged(Mbean newModel) {
			tabSheet.removeAllComponents();
			int no = 1;
			for (CompAttrib attrib : newModel.getCompAttrib()) {
				final String tabLabel = String.format("#%d %s", no++, attrib.getName());
				final CompositeTabLayout tabContent = new CompositeTabLayout(getCompositeForm(newModel, attrib), getCompAttribTable(newModel, attrib));
				tabSheet.addTab(tabContent, tabLabel);
			}
		}

		private NameEditForm getCompositeForm(final Mbean mbean, final CompAttrib compAttrib) {
			NameEditForm form = new NameEditForm(controller, new NameEditForm.FormParameter() {
				@Override public boolean hasFooter() { return false; }
				@Override public String getCaption() { return null; }
				@Override public String getEditablePropertyName() { return "name"; }
				@Override public String getNonEditablePropertyName() { return "alias"; }
				@Override public Object[] getVisiblePropertieNames() { return new Object[]{"selected", getNonEditablePropertyName(), getEditablePropertyName()};}
				@Override public EditControls.Callback getAdditionalCallback() { return null; }
			});
			Item item = controller.getCompAttribContainer(mbean).getItem(compAttrib);
			form.setItemDataSource(item);
			return form;
		}

		private Table getCompAttribTable(final Mbean mbean, final CompAttrib attrib) {
			AttributesTable memberTable = new AttributesTable(controller, new Callback() {
				@Override
				public Container getContainer() {
					return controller.getCompMemberContainer(attrib);
				}
			});
			memberTable.modelChanged(mbean);
			return memberTable;
		}

		private class CompositeTabLayout extends VerticalLayout implements Property.ReadOnlyStatusChangeNotifier, EditControls.Callback, ViewStateChangedListener {

			private final NameEditForm compositeForm;
			private final Table compositeTable;
			private final FormButtonHandler formButtonHandler;
			private final TableButtonHandler tableButtonHandler;
			//TODO mvonrued -> rename in EditControls 
			//TODO mvonrued (optional) rename footer in editControls (but I don't like to do it... because it is ... *grr*)
			private final EditControls footer;

			private CompositeTabLayout(NameEditForm compositeForm, Table compositeTable) {
				this.compositeForm = compositeForm;
				this.compositeTable = compositeTable;
				formButtonHandler = new FormButtonHandler(compositeForm);
				tableButtonHandler = new TableButtonHandler(compositeTable);
				footer = new EditControls(this, new ButtonHandler<CompositeTabLayout>() {
					@Override
					public void handleSave() {
						formButtonHandler.handleSave();
						tableButtonHandler.handleSave();
					}

					@Override
					public void handleCancel() {
						formButtonHandler.handleCancel();
						tableButtonHandler.handleCancel();
					}

					@Override
					public void handleEdit() {
						formButtonHandler.handleEdit();
						tableButtonHandler.handleEdit();
					}

					@Override
					public CompositeTabLayout getOuter() {
						return CompositeTabLayout.this;
					}					
				});
				addComponent(footer);
				addComponent(compositeForm);
				addComponent(compositeTable);
				setSpacing(false);
				setReadOnly(true);
				addFooterHooks(footer);
			}

			@Override
			public void addListener(ReadOnlyStatusChangeListener listener) {
				compositeForm.addListener(listener);
				compositeTable.addListener(listener);
			}

			@Override
			public void removeListener(ReadOnlyStatusChangeListener listener) {
				compositeForm.removeListener(listener);
				compositeTable.removeListener(listener);
			}

			@Override
			public void setReadOnly(boolean readOnly) {
				super.setReadOnly(readOnly);
				compositeForm.setReadOnly(readOnly);
				compositeTable.setReadOnly(readOnly);

			}

			@Override
			public void callback(ButtonType type, Component outer) {
				if (type == ButtonType.save) controller.fireViewStateChanged(ViewState.LeafSelected, outer);
				if (type == ButtonType.edit) controller.fireViewStateChanged(ViewState.Edit, outer);
				if (type == ButtonType.cancel) controller.fireViewStateChanged(ViewState.LeafSelected, outer);
			}
			
			//TODO mvonrued -> todo :)
			private void addFooterHooks(final EditControls footer) {
				footer.addSaveHook(this);
				footer.addCancelHook(this);
				footer.addEditHook(this);
			}

			@Override
			public void viewStateChanged(ViewStateChangedEvent event) {
				switch(event.getNewState()) {
					case Init:
					case LeafSelected:
						setEnabled(true);
						footer.setVisible(true);
						break;
					case NonLeafSelected:
					case Edit:
						setEnabled(event.getSource() == this);
						footer.setVisible(event.getSource() == this);
						break;
				}
			}
		}
	}

	private class AttributesLayout extends VerticalLayout implements ViewStateChangedListener, EditControls.Callback<Table> {

		private final AttributesTable attributesTable;
		private final EditControls footer;

		private AttributesLayout() {
			attributesTable = new AttributesTable(controller, new Callback() {
				@Override
				public Container getContainer() {
					return controller.getAttributesContainer(controller.getSelectedMBean());
				}
			});
			footer = new EditControls(this.attributesTable);
			addComponent(footer);
			addComponent(attributesTable);
			setSpacing(false);
			setMargin(false);
			addFooterHooks();
		}

		@Override
		public void callback(ButtonType type, Table outer) {
			if (type == ButtonType.save) controller.fireViewStateChanged(ViewState.LeafSelected, outer);
			if (type == ButtonType.cancel) controller.fireViewStateChanged(ViewState.LeafSelected, outer);
			if (type == ButtonType.edit) controller.fireViewStateChanged(ViewState.Edit, outer);
		}
		
		private void addFooterHooks() {
			footer.addSaveHook(this);
			footer.addCancelHook(this);
			footer.addEditHook(this);
		}

		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			footer.setVisible(enabled);
		}

		@Override
		public void viewStateChanged(ViewStateChangedEvent event) {
			switch (event.getNewState()) {
				case Init:
				case NonLeafSelected:
				case Edit:
					footer.setVisible(event.getSource() == attributesTable);
					break;
				case LeafSelected:
					footer.setVisible(true);
					break;
			}
			attributesTable.viewStateChanged(event);//forward
		}

		private void modelChanged(Mbean newModel) {
			attributesTable.modelChanged(newModel); //forward
		}
	}
}
