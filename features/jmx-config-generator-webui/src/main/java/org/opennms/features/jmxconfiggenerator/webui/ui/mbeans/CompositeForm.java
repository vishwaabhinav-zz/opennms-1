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

import com.vaadin.ui.Form;
import org.opennms.features.jmxconfiggenerator.webui.Config;

/**
 *
 * @author m.v.rueden
 */
public class CompositeForm extends Form implements ViewStateChangedListener {

	private final MBeansController controller;
	
	public CompositeForm(MBeansController controller) {
		this.controller = controller;
		setWidth(100, UNITS_PERCENTAGE);
		setHeight(Config.NAME_EDIT_FORM_HEIGHT, UNITS_PIXELS);
//		setFormFieldFactory(new FormFieldFactory() {
//			@Override
//			public Field createField(Item item, Object propertyId, Component uiContext) {
//				if (propertyId.toString().equals("selected")) {
//					CheckBox c = new CheckBox("selected");
//					return c;
//				}
//				if (propertyId.toString().equals())) {
//					final TextField tf = new TextField(MetaMBeanItem.OBJECTNAME) {
//						@Override
//						public void setReadOnly(boolean readOnly) {
//							super.setReadOnly(true); //never ever edit me
//						}
//					};
//					tf.setWidth(800, UNITS_PIXELS);
//					return tf;
//				}
//				if (propertyId.toString().equals(MetaMBeanItem.NAME)) {
//					TextField tf = new TextField(MetaMBeanItem.NAME);
//					tf.setWidth(400, UNITS_PIXELS);
//					tf.setValidationVisible(true);
//					tf.setRequired(true);
//					tf.setRequiredError("You must provide a MBeans name.");//TODO extract to "resource bundle"
//					tf.addValidator(nameValidator); //TODO extract to "resource bundle"
//					return tf;
//				}
//				return null;
//			}
//		});
		setReadOnly(true);
		setWriteThrough(false);
	}

	@Override
	public void viewStateChanged(ViewStateChangedEvent event) {
		switch(event.getNewState()) {
			case Init:
				setItemDataSource(null);
				break;
			case Edit:
				if (event.getSource() != this)
					setReadOnly(true);
				break;
		}
	}
}
