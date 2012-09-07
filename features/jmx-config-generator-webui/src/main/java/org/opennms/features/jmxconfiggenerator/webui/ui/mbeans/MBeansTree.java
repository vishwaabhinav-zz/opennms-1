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

import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;
import org.opennms.features.jmxconfiggenerator.webui.data.InternalModel;
import org.opennms.features.jmxconfiggenerator.webui.data.MetaMBeanItem;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;

/**
 *
 * @author m.v.rueden
 */
class MBeansTree extends Tree implements ModelChangeListener<InternalModel>, ViewStateChangedListener, Action.Handler {

	private final MBeansController controller;
	private final MbeansHierarchicalContainer container;
	private final Action SELECT = new Action("select");
	private final Action DESELECT = new Action("deselect");
	private final Action[] ACTIONS = new Action[]{SELECT, DESELECT};

	protected MBeansTree(NameEditForm mbeansForm, MBeansContentTabSheet mbeansTabSheet, final MBeansController controller) {
		this.container = controller.getMBeansHierarchicalContainer();
		this.controller = controller;
		setCaption("MBeans");
		setContainerDataSource(container);
		setItemCaptionPropertyId(MetaMBeanItem.CAPTION);
		setItemIconPropertyId(MetaMBeanItem.ICON);
		setItemDescriptionGenerator(new ItemDescriptionGenerator() {
			@Override
			public String generateDescription(Component source, Object itemId, Object propertyId) {
				return getItem(itemId).getItemProperty(MetaMBeanItem.TOOLTIP).getValue().toString();
			}
		});
		setSelectable(true);
		setMultiSelect(false);
		setNullSelectionAllowed(true);
		setMultiselectMode(AbstractSelect.MultiSelectMode.SIMPLE);
		addListener(new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				controller.updateView(event);
			}
		});
		setSizeFull();
		setImmediate(true);
		addActionHandler(this);
	}

	private void expandTree() {
		for (Object itemId : getItemIds()) {
			expandItem(itemId);
			select(itemId);
		}
	}

	@Override
	public void modelChanged(InternalModel internalModel) {
		container.updateDataSource(internalModel);
		select(null);
		expandTree();
	}

	@Override
	public void viewStateChanged(ViewStateChangedEvent event) {
		switch (event.getNewState()) {
			case Edit:
				setEnabled(false);
				break;
			default:
				setEnabled(true);
				break;
		}
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		return ACTIONS;
	}
	
	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (action == SELECT) controller.handleSelect(container, target);
		if (action == DESELECT) controller.handleDeselect(container, target);
		fireValueChange(false);
	}
}
