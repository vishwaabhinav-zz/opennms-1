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
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opennms.features.jmxconfiggenerator.webui.data.InternalModel;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeNotifier;
import org.opennms.features.jmxconfiggenerator.webui.data.SelectableBeanItemContainer;
import org.opennms.features.jmxconfiggenerator.webui.data.StringRenderer;
import org.opennms.features.jmxconfiggenerator.webui.ui.ModelChangeRegistry;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompMember;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

/**
 * Controls the "MbeansView".
 *
 * @author m.v.rueden
 */
public class MBeansController implements ModelChangeNotifier, ViewStateChangedListener, ModelChangeListener<InternalModel>, NameProvider {

	/**
	 * Vaadin container for the MbeansTree
	 */
	private final MbeansHierarchicalContainer mbeansContainer = new MbeansHierarchicalContainer(this);
	/**
	 * Registry to notify underlying components on modelChange events
	 */
	private final ModelChangeRegistry registry = new ModelChangeRegistry();
	/**
	 * Collection to notify all view components if the ViewState changes. Any underlying component can invoke a
	 * viewStateChange
	 */
	private final Collection<ViewStateChangedListener> viewStateListener = new ArrayList<ViewStateChangedListener>();
	private final MBeansItemStrategyHandler itemStrategyHandler = new MBeansItemStrategyHandler();
	/**
	 * the Mbean which is currently selected in the MBeanTree
	 */
	private Mbean currentlySelected = null;
	/**
	 * The state in which the view is currently
	 */
	private ViewState currentState = ViewState.Init; //this would be default, but we set it nevertheless
	private AttributesContainerCache<Attrib, Mbean> attribContainerCache = new AttributesContainerCache<Attrib, Mbean>(Attrib.class, new AttributesContainerCache.AttributeCollector<Attrib, Mbean>() {
		@Override
		public List<Attrib> getAttributes(Mbean outer) {
			return outer.getAttrib();
		}
	});
		
	//TODO mvonrued -> this is not correct, because we do not want all members, we just want specific ones
	private AttributesContainerCache<CompAttrib, Mbean> compAttribContainerCache = new AttributesContainerCache<CompAttrib, Mbean>(CompAttrib.class, new AttributesContainerCache.AttributeCollector<CompAttrib, Mbean>() {
		@Override
		public List<CompAttrib> getAttributes(Mbean outer) {
			return outer.getCompAttrib();
		}
	});
	
	private AttributesContainerCache<CompMember, CompAttrib> compMemberContainerCache = new AttributesContainerCache<CompMember, CompAttrib>(CompMember.class, new AttributesContainerCache.AttributeCollector<CompMember, CompAttrib>() {
		@Override
		public List<CompMember> getAttributes(CompAttrib outer) {
			return outer.getCompMember();
		}
	});
	
	@Override
	public void registerListener(Class clazz, ModelChangeListener listener) {
		registry.registerListener(clazz, listener);
	}

	@Override
	public void notifyObservers(Class clazz, Object newModel) {
		registry.notifyObservers(clazz, newModel);
	}

	public MbeansHierarchicalContainer getMBeansHierarchicalContainer() {
		return mbeansContainer;
	}

	/**
	 * Updates the view when the selected MBean changes. At first each ModelChangeListener are told, that there is a new
	 * Mbean to take care of (in detail: change the view to list mbean details of new mbean). And of course set a new
	 * ViewState (e.g. a non Mbean was selected and now a Mbean is selected)
	 *
	 * @param event
	 */
	protected void updateView(ItemClickEvent event) {
		if (currentlySelected == event.getItemId()) return; //no change made
		currentlySelected = event.getItemId() instanceof Mbean ? (Mbean) event.getItemId() : null;
		registry.notifyObservers(Item.class, event.getItem());
		registry.notifyObservers(event.getItemId().getClass(), event.getItemId());
		setState(event.getItemId());
	}

	/**
	 * Gets the next ViewState of the view.
	 *
	 * @param itemId
	 * @return ViewState.Init if itemId is null, otherwise ViewState.LeafSelected on Mbean selection and NonLeafSelected
	 * on non-Mbean selection
	 */
	private ViewState getNextState(Object itemId) {
		if (itemId == null) return ViewState.Init;
		if (itemId instanceof Mbean) return ViewState.LeafSelected;
		if (!(itemId instanceof Mbean)) return ViewState.NonLeafSelected;
		return ViewState.Init;
	}

	private void setState(Object itemId) {
		ViewState nextState = getNextState(itemId);
		if (nextState == currentState) return; //nothing to do
		fireViewStateChanged(new ViewStateChangedEvent(currentState, nextState, this)); //tell the underlying views to handle the view state change :)
	}

	public void setItemProperties(Item item, Object itemId) {
		itemStrategyHandler.setItemProperties(item, itemId);
	}

	public StringRenderer getStringRenderer(Class<?> clazz) {
		return itemStrategyHandler.getStringRenderer(clazz);
	}

	@Override
	public void viewStateChanged(ViewStateChangedEvent event) {
		currentState = event.getNewState();
		if (event.getNewState() == ViewState.Init) {
			attribContainerCache.containerMap.clear();
			compAttribContainerCache.containerMap.clear();
			compMemberContainerCache.containerMap.clear();
		}
	}

	protected void addView(ViewStateChangedListener view) {
		viewStateListener.add(view);
	}

	@Override
	public void modelChanged(InternalModel newModel) {
		fireViewStateChanged(new ViewStateChangedEvent(currentState, ViewState.Init, this));
	}
	
	protected void fireViewStateChanged(ViewState newState, Object source) {
		fireViewStateChanged(new ViewStateChangedEvent(currentState, newState, source));
	}
	
	private void fireViewStateChanged(ViewStateChangedEvent event) {
		for (ViewStateChangedListener listener : viewStateListener)
			listener.viewStateChanged(event);
	}

	void handleDeselect(HierarchicalContainer container, Object itemId) {
		handleSelectDeselect(container, container.getItem(itemId), itemId, false);
	}

	void handleSelect(HierarchicalContainer container, Object itemId) {
		handleSelectDeselect(container, container.getItem(itemId), itemId, true);
	}

	public void handleSelectDeselect(HierarchicalContainer container, Item item, Object itemId, boolean select) {
		itemStrategyHandler.getStrategy(itemId.getClass()).handleSelectDeselect(item, itemId, select);
		if (!container.hasChildren(itemId)) return;
		for (Object childItemId : container.getChildren(itemId)) {
			handleSelectDeselect(container, container.getItem(childItemId), childItemId, select);
		}
	}

	public void updateMBeanIcon() {
		itemStrategyHandler.getStrategy(Mbean.class).updateIcon(mbeansContainer.getItem(currentlySelected));
	}

	public SelectableBeanItemContainer<Attrib> getAttributesContainer(Mbean bean) {
		return attribContainerCache.getContainer(bean);
	}

	public void clearAttributesCache() {
		attribContainerCache.containerMap.clear();
	}

	protected void updateMBean() {
		itemStrategyHandler.getStrategy(Mbean.class).updateModel(mbeansContainer.getItem(currentlySelected), currentlySelected);
	}

	public SelectableBeanItemContainer<CompMember> getCompMemberContainer(CompAttrib attrib) {
		return compMemberContainerCache.getContainer(attrib);
	}

	public SelectableBeanItemContainer<CompAttrib> getCompAttribContainer(Mbean mbean) {
		return compAttribContainerCache.getContainer(mbean);
	}
	
	@Override
	public Map<Object, String> getNames() {
		Map<Object, String> names = new HashMap<Object, String>();
		for (Mbean bean : mbeansContainer.getMBeans()) {
			for (Attrib att : bean.getAttrib()) {
				names.put(att, att.getAlias());
			}
			for (CompAttrib compAttrib : bean.getCompAttrib()) {
				for (CompMember compMember : compAttrib.getCompMember())
					names.put(compMember, compMember.getAlias());
			}
		}
		return names;
		
	}

	protected Mbean getSelectedMBean() {
		return currentlySelected;
	}
	
	public static interface Callback {
		Container getContainer();
	}
	
	/**
	 * The MBeanTree shows all available MBeans. Each Mbean has one or more attributes. Each attribute is selecatble.
	 * The MBean's attribute are shown in a table. The problem is, that we must store the "is selected" state of each
	 * AttributeItem. So we have two choices:<br/>
	 *
	 * 1. add ALL attributes to the container of the table and show only the one belonging to the selected Mbean.<br/>
	 *
	 * 2. only add selected MBean's attributes to the container and save the container for later use.<br/>
	 *
	 * I stick to 2. So this class simply maps a container to its Mbean.
	 *
	 * @param <IDTYPE>
	 * @param <OUTERTYPE>
	 * @author m.v.rueden@googlemail.com
	 */
	//TODO javadoc above does not fit to the current implementation
	public static class AttributesContainerCache<IDTYPE, OUTERTYPE> {

		private final Map<OUTERTYPE, SelectableBeanItemContainer<IDTYPE>> containerMap = new HashMap<OUTERTYPE, SelectableBeanItemContainer<IDTYPE>>();
		public static final SelectableBeanItemContainer NULL = new SelectableBeanItemContainer(Object.class);
		private final Class<? super IDTYPE> type;
		private final AttributeCollector<IDTYPE, OUTERTYPE> attribCollector;

		private AttributesContainerCache(Class<? super IDTYPE> type, AttributeCollector<IDTYPE, OUTERTYPE> attribCollector) {
			this.type = type;
			this.attribCollector = attribCollector;
		}

		/**
		 * Gets the container of the given bean. If there is no container a new one is created, otherwise the earlier
		 * used container is returned.
		 *
		 * @param bean
		 * @return
		 */
		public SelectableBeanItemContainer<IDTYPE> getContainer(OUTERTYPE bean) {
			if (bean == null) return NULL;
			if (containerMap.get(bean) != null) return containerMap.get(bean);
			containerMap.put(bean, new SelectableBeanItemContainer<IDTYPE>(type));
			initContainer(containerMap.get(bean), bean);
			return containerMap.get(bean);
		}

		private void initContainer(SelectableBeanItemContainer container, OUTERTYPE bean) {
			for (IDTYPE att : attribCollector.getAttributes(bean)) {
				container.addItem(att);
			}
		}

		public static interface AttributeCollector<T, X> {

			List<T> getAttributes(X outer);
		}
	}
}
