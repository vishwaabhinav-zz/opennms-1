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

package org.opennms.features.jmxconfiggenerator.webui.data;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.List;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.MBeansController;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.MBeansController.AttributesContainerCache;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.MbeansHierarchicalContainer;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompMember;
import org.opennms.xmlns.xsd.config.jmx_datacollection.JmxDatacollectionConfig;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

/**
 * This class wraps the
 * <code>JmxDatacollectionConfig</code> and provides some methods to make life a little easier. So in the future we may
 * support multiple data soruces (and therefore multiple JmxDatacollectionConfigs), and so on. Or we find out, that we
 * do not need this class, then we will remove it.
 *
 * @author m.v.rueden
 */
public class InternalModel {

	private String serviceName;
	private JmxDatacollectionConfig rawModel;

	/**
	 * Set the real model and get the data we need out of it
	 */
	public InternalModel setRawModel(JmxDatacollectionConfig rawModel) {
		if (!isValid(rawModel)) throw new IllegalArgumentException("Model is not valid.");
		this.rawModel = rawModel;
		this.serviceName = rawModel.getJmxCollection().get(0).getName();
		return this;
	}

	/**
	 * Checks if the given
	 * <code>rawModel</code> is not null and does have Mbeans (count can be 0, but not NULL).
	 *
	 * @param rawModel
	 * @return true if valid, false otherwise
	 */
	private boolean isValid(JmxDatacollectionConfig rawModel) {
		return !(rawModel.getJmxCollection().isEmpty()
				|| rawModel.getJmxCollection().get(0) == null
				|| rawModel.getJmxCollection().get(0).getMbeans() == null);
	}

	public JmxDatacollectionConfig getRawModel() {
		return rawModel;
	}

	/**
	 * The whole point was to select/deselect Mbeans/Attribs/CompMembers/CompAttribs. In this method we simply create a
	 * JmxDatacollectionConfig considering the choices we made in the gui. To do this, we simply clone the original
	 * <code>JmxDatacollectionConfig</code> loaded at the beginning. After that we remove all
	 * MBeans/Attribs/CompMembers/CompAttribs and add them manually with the changes made in the gui.
	 *
	 * @param controller the MBeansController of the MbeansView (is needed to determine the changes made in gui)
	 * @return
	 */
	//TODO mvonrued -> I guess we do not need this clone-stuff at all ^^ and it is too complicated for such a simple
	//task 
	public JmxDatacollectionConfig getRawModelIncludeSelection(MBeansController controller) {
		/**
		 * At First we clone the original collection. This is done, because if we make any modifications (e.g. deleting
		 * not selected elements) the data isn't available in the GUI, too. To avoid reloading the data from server, we
		 * just clone it.
		 */
		JmxDatacollectionConfig clone = JmxCollectionCloner.clone(getRawModel());
		/**
		 * At second we remove all MBeans from original data and get only selected once.
		 */
		List<Mbean> exportBeans = clone.getJmxCollection().get(0).getMbeans().getMbean();
		exportBeans.clear();
		Iterable<Mbean> selectedMbeans = getSelectedMbeans(controller.getMBeansHierarchicalContainer());
		for (Mbean mbean : selectedMbeans) {
			/**
			 * At 3.1. we remove all Attributes from Mbean, because we only want selected ones.
			 */
			Mbean exportBean = JmxCollectionCloner.clone(mbean);
			exportBean.getAttrib().clear(); //we only want selected ones :)
			for (Attrib att : getSelectedAttributes(mbean, controller.getAttributesContainer(mbean)))
				exportBean.getAttrib().add(JmxCollectionCloner.clone(att));
			if (!exportBean.getAttrib().isEmpty()) exportBeans.add(exportBean); //no attributes selected, don't add bean
			
			/* At 3.2. we remove all CompAttribs and CompMembers from MBean, because we only want selected ones :) */
			exportBean.getCompAttrib().clear();
			for (CompAttrib compAtt : getSelectedCompAttributes(mbean, controller.getCompAttribContainer(mbean))) {
				CompAttrib cloneCompAtt = JmxCollectionCloner.clone(compAtt);
				cloneCompAtt.getCompMember().clear();
				for (CompMember compMember : getSelectedCompMembers(compAtt, controller.getCompMemberContainer(compAtt)))
					cloneCompAtt.getCompMember().add(JmxCollectionCloner.clone(compMember));
				if (!cloneCompAtt.getCompMember().isEmpty()) exportBean.getCompAttrib().add(cloneCompAtt);
			}
		}
		//Last but not least, we need to update the service name
		clone.getJmxCollection().get(0).setName(serviceName);
		return clone;
	}

	/**
	 * @param container
	 * @return all Mbeans which are selected
	 */
	private Iterable<Mbean> getSelectedMbeans(final MbeansHierarchicalContainer container) {
		return Iterables.filter(container.getMBeans(), new Predicate<Mbean>() {
			@Override
			public boolean apply(final Mbean bean) {
				return (Boolean) container.getItem(bean).getItemProperty(MetaMBeanItem.SELECTED).getValue();
			}
		});
	}

	/**
	 *
	 * @param mbean
	 * @param attributesContainer
	 * @return all Attributes which are selected.
	 */
	private Iterable<Attrib> getSelectedAttributes(final Mbean mbean, final SelectableBeanItemContainer<Attrib> attributesContainer) {
		if (AttributesContainerCache.NULL == attributesContainer) return mbean.getAttrib(); //no change made, return all
		return Iterables.filter(mbean.getAttrib(), new Predicate<Attrib>() {
			@Override
			public boolean apply(Attrib attrib) {
				return attributesContainer.getItem(attrib).isSelected();
			}
		});
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * 
	 * @param mbean
	 * @param compAttribContainer
	 * @return all CompAttrib elements which are selected
	 */
	private Iterable<CompAttrib> getSelectedCompAttributes(final Mbean mbean, final SelectableBeanItemContainer<CompAttrib> compAttribContainer) {
		if (AttributesContainerCache.NULL == compAttribContainer) return mbean.getCompAttrib();
		return Iterables.filter(mbean.getCompAttrib(), new Predicate<CompAttrib>() {
			@Override
			public boolean apply(CompAttrib compAtt) {
				return compAttribContainer.getItem(compAtt).isSelected();
			}
		});
	}

	/**
	 * 
	 * @param compAtt
	 * @param compMemberContainer
	 * @return  all <code>CompMember</code>s which are selected.
	 */
	private Iterable<CompMember> getSelectedCompMembers(final CompAttrib compAtt, final SelectableBeanItemContainer<CompMember> compMemberContainer) {
		if (AttributesContainerCache.NULL == compMemberContainer) return compAtt.getCompMember();
		return Iterables.filter(compAtt.getCompMember(), new Predicate<CompMember>() {
			@Override
			public boolean apply(CompMember compMember) {
				return compMemberContainer.getItem(compMember).isSelected();
			}
		});
	}
}
