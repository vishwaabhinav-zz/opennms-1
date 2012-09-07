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

import com.vaadin.data.Property;
import com.vaadin.data.Property.ReadOnlyStatusChangeListener;
import com.vaadin.data.Property.ReadOnlyStatusChangeNotifier;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opennms.features.jmxconfiggenerator.webui.ui.IconProvider;

/**
 * This class represents a "control panel", providing three buttons: edit, cancel, save. At the
 * beginning only edit button is visible. After clicking edit-Button, cancel and save are visible. Pressing save
 * indicates a commit(), cancel a discard() on the outer component (e.g. a table).
 *
 * @param <T> any Component. It usually is the "outer" component, which needs a "EditControls" to toggle between
 * readOnly and edit mode.
 * @author m.v.rueden
 */
class EditControls<T extends Component> extends HorizontalLayout implements ReadOnlyStatusChangeListener, Button.ClickListener {

	/**
	 * "cancel" button. Visibility public, so we can add some additional behaviour, if we want to.
	 */
	public final Button cancel;
	/**
	 * "save" button. Visibility public, so we can add some additional behaviour, if we want to.
	 */
	public final Button save;
	/**
	 * "edit" button. Visibility public, so we can add some additional behaviour, if we want to.
	 */
	public final Button edit;
	/**
	 * If a component uses the EditControls, we may want to do something after clicking one of the buttons.
	 */
	private final Map<Button, List<Callback>> hooks = new HashMap<Button, List<Callback>>();
	/**
	 * Default edit handler. It encapsulates what happens on a button click.
	 *
	 * @see ButtonHandler
	 * @see #FORM_BUTTON_HANDLER
	 * @see #TABLE_EDITING
	 */
	private ButtonHandler<T> buttonHandler; 

	protected EditControls(final Form outerForm) {
		this(outerForm, new FormButtonHandler(outerForm));
	}
	
	protected EditControls(final Table outerTable) {
		this(outerTable, new TableButtonHandler(outerTable));
	}
	
	protected EditControls(ReadOnlyStatusChangeNotifier callback, ButtonHandler<T> buttonHandler) {
		this.buttonHandler = buttonHandler;
		//we need to do this, otherwise we don't notice when to hide/show buttons
		callback.addListener((ReadOnlyStatusChangeListener) this);
		save = createButton("save", IconProvider.BUTTON_SAVE);
		cancel = createButton("cancel", IconProvider.BUTTON_CANCEL);
		edit = createButton("edit", IconProvider.BUTTON_EDIT);
		addComponent(edit);
		addComponent(save);
		addComponent(cancel);
		initFooterButtonActions();
		initHooks();
		setSpacing(true);
		setStyleName("editlayout");
	}
	
	/**
	 * Adds an empty list to the buttons.
	 */
	private void initHooks() {
		hooks.put(cancel, new ArrayList<Callback>());
		hooks.put(save, new ArrayList<Callback>());
		hooks.put(edit, new ArrayList<Callback>());
	}

	private void initFooterButtonActions() {
		edit.addListener((Button.ClickListener) this);
		save.addListener((Button.ClickListener) this);
		cancel.addListener((Button.ClickListener) this);
	}

	/**
	 * Update the visibility of the buttons, depending on readOnly. Be aware, that readOnly does not mean, the outer
	 * component has set readOnly to true. It just means, that the outer component is in read only mode or in editing
	 * mode. The handling of the editing and readOnly mode depends on the outer component.
	 *
	 * @param readOnly if true => edit is visible, if false cancel and save are visible.
	 */
	private void updateVisibility(boolean readOnly) {
		edit.setVisible(readOnly);
		cancel.setVisible(!readOnly);
		save.setVisible(!readOnly);
	}

	/**
	 * If any button is clicked, we toggle from read to write mode and vice versa. In edition we execute any registerd
	 * hooks afterwards.
	 *
	 * @param event
	 */
	@Override
	public void buttonClick(Button.ClickEvent event) {
		Button source = event.getButton();
		if (source == save)	buttonHandler.handleSave();
		if (source == cancel) buttonHandler.handleCancel();
		if (source == edit)	buttonHandler.handleEdit();
		executeHooks(event);
	}

	/**
	 * You can add a
	 * <code>Button.ClickListener</code> to the layout to do some stuff AFTER handling the toggle between read and write
	 * mode. <br/><br/> <b>Be aware that
	 * <code>button</code> should be one of:</b> {@link #save}, {@link #edit}, {@link #cancel}.
	 *
	 * @param button {@link #save}, {@link #edit}, {@link #cancel}
	 * @param listener
	 */
	private void addHook(ButtonType button, Callback callback) {
		Button b = getButton(button);
		if (hooks.get(b) == null) return;
		hooks.get(b).add(callback);
	}
	
	public void addSaveHook(Callback callback) {
		addHook(ButtonType.save, callback);
	}
	
	public void addEditHook(Callback callback) {
		addHook(ButtonType.edit, callback);
	}
	
	public void addCancelHook(Callback callback) {
		addHook(ButtonType.cancel, callback);
	}

	@Override
	public void readOnlyStatusChange(Property.ReadOnlyStatusChangeEvent event) {
		updateVisibility(event.getProperty().isReadOnly());
	}

	private void executeHooks(final Button.ClickEvent event) {
		if (hooks.get(event.getButton()) == null) return; //nothing to do //nothing to do
		for (Callback callback : hooks.get(event.getButton())) {
			callback.callback(getButtonType(event.getButton()), buttonHandler.getOuter());
		}
	}

	private Button getButton(ButtonType button) {
		switch (button) {
			default:
			case edit : return edit;
			case cancel: return cancel;
			case save: return save;
		}
	}

	private ButtonType getButtonType(Button button) {
		if (button == cancel) return ButtonType.cancel;
		if (button == save) return ButtonType.save;
		if (button == edit) return ButtonType.edit;
		return ButtonType.cancel;
	}

	private Button createButton(final String buttonDescription, final String iconName) {
		Button button = new Button();
		button.setCaption(buttonDescription);
		button.setIcon(IconProvider.getIcon(iconName));
		return button;
	}
	
	//TODO mvonrued -> kommentieren
	public static interface Callback<T extends Component> {
		void callback(ButtonType type, T outer);
	}
	
	//TODO mvonrued- > kommentieren
	public static enum ButtonType {
		edit, cancel, save;
	}

	/**
	 * Sets a new
	 * <code>editHandler</code>.
	 *
	 * @param editHandler
	 * @return
	 * @see ButtonHandler
	 */
	protected EditControls changeButtonHandler(ButtonHandler editHandler) {
		this.buttonHandler = editHandler;
		return this;
	}

	/**
	 * Abstraction of how the outer component toggles between read and write mode and vice versa.
	 *
	 * @param <T> Type of the outer component.
	 * @author m.v.rueden@googlemail.com
	 */
	public static interface ButtonHandler<T extends Component> {

		//TODO mvonrued
		void handleSave();

		//TODO mvonrued
		void handleCancel();

		//TODO mvonrued
		void handleEdit();
		
		//TODO mvonrued
		T getOuter();
	}

	//TODO mvonrued
	public static abstract class AbstractButtonHandler<T extends Component> implements ButtonHandler<T> {

		private final T outer;

		public AbstractButtonHandler(T outer) {
			this.outer = outer;
		}

		@Override
		public T getOuter() {
			return outer;
		}
	}

	/**
	 * An
	 * <code>EditHandler</code> of a vaadin Form. Switching from read to write mode is handled by setting the read only
	 * flag. <ul> <li>form.setReadOnly(true)</li> -> read mode <li>form.setReadOnly(false)</li> -> write mode </ul>
	 *
	 * @author m.v.rueden@googlemail.com
	 */
	public static class FormButtonHandler<T extends AbstractField> extends AbstractButtonHandler<T> {

		public FormButtonHandler(T outer) {
			super(outer);
		}
		
		@Override
		public void handleSave() {
			if (!getOuter().isValid()) return;
			getOuter().commit();
			setEditAllowed(false);
		}

		@Override
		public void handleCancel() {
			getOuter().discard();
			setEditAllowed(false);
		}

		@Override
		public void handleEdit() {
			setEditAllowed(true);
		}

		/**
		 *
		 * @param component the outer component, which toggles from read to write mode (or vice versa).
		 * @param editAllowed true: <code>component</code> is in edit mode, false: <code>component</code> is in read
		 * mode
		 */
		protected void setEditAllowed(boolean editAllowed) {
			getOuter().setReadOnly(!editAllowed);
		}
	}
	
	/**
	 * An
	 * <code>EditHandler</codE> for a vaadin Table. Switching from read to write mode is handled by setting the editable
	 * flag. Setting read only flag does not change from non-editable to editable.
	 *
	 * @author m.v.rueden@googlemail.com
	 */
	public static class TableButtonHandler<T extends Table> extends FormButtonHandler<T> {
		
		public TableButtonHandler(T t) {
			super(t);
		}
		
		@Override
		protected void setEditAllowed(boolean editAllowed) {
			getOuter().setReadOnly(!editAllowed); //to be consistent, we set readOnly
			getOuter().setEditable(editAllowed);
		}
	};
}
