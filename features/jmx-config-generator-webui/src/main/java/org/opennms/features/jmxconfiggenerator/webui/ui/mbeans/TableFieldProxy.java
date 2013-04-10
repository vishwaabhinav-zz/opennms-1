package org.opennms.features.jmxconfiggenerator.webui.ui.mbeans;

import java.util.Collection;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

public class TableFieldProxy extends HorizontalLayout implements Field {

	private TextField field;

	public TableFieldProxy(final TextField field) {
		this.field = field;
	}

	@Override
	public boolean isInvalidCommitted() {
		return this.field.isInvalidCommitted();
	}

	@Override
	public void setInvalidCommitted(final boolean isCommitted) {
		this.field.setInvalidCommitted(isCommitted);
	}

	@Override
	public void commit() throws SourceException, InvalidValueException {
		this.field.commit();
	}

	@Override
	public void discard() throws SourceException {
		this.field.discard();
	}

	@Override
	public boolean isWriteThrough() {
		return this.field.isWriteThrough();
	}

	@Override
	public void setWriteThrough(final boolean writeThrough) throws SourceException, InvalidValueException {
		this.field.setWriteThrough(writeThrough);
	}

	@Override
	public boolean isReadThrough() {
		return this.field.isReadThrough();
	}

	@Override
	public void setReadThrough(final boolean readThrough) throws SourceException {
		this.field.setReadThrough(readThrough);
	}

	@Override
	public boolean isModified() {
		return this.field.isModified();
	}

	@Override
	public void addValidator(final Validator validator) {
		this.field.addValidator(validator);
	}

	@Override
	public void removeValidator(final Validator validator) {
		this.field.removeValidator(validator);
	}

	@Override
	public Collection<Validator> getValidators() {
		return this.field.getValidators();
	}

	@Override
	public boolean isValid() {
		return this.field.isValid();
	}

	@Override
	public void validate() throws InvalidValueException {
		this.field.validate();
	}

	@Override
	public boolean isInvalidAllowed() {
		return this.field.isInvalidAllowed();
	}

	@Override
	public void setInvalidAllowed(final boolean invalidValueAllowed) throws UnsupportedOperationException {
		this.field.setInvalidAllowed(invalidValueAllowed);
	}

	@Override
	public Object getValue() {
		return this.field.getValue();
	}

	@Override
	public void setValue(final Object newValue) throws ReadOnlyException, ConversionException {
		this.field.setValue(newValue);
	}

	@Override
	public Class<?> getType() {
		return this.field.getType();
	}

	@Override
	public void addListener(final ValueChangeListener listener) {
		this.field.addListener(listener);
	}

	@Override
	public void removeListener(final ValueChangeListener listener) {
		this.field.removeListener(listener);
	}

	@Override
	public void valueChange(final com.vaadin.data.Property.ValueChangeEvent event) {
		this.field.valueChange(event);
	}

	@Override
	public void setPropertyDataSource(final Property newDataSource) {
		this.field.setPropertyDataSource(newDataSource);
	}

	@Override
	public Property getPropertyDataSource() {
		return this.field.getPropertyDataSource();
	}

	@Override
	public int getTabIndex() {
		return this.field.getTabIndex();
	}

	@Override
	public void setTabIndex(final int tabIndex) {
		this.field.setTabIndex(tabIndex);
	}

	@Override
	public boolean isRequired() {
		return this.field.isRequired();
	}

	@Override
	public void setRequired(final boolean required) {
		this.field.setRequired(required);
	}

	@Override
	public void setRequiredError(final String requiredMessage) {
		this.field.setRequiredError(requiredMessage);
	}

	@Override
	public String getRequiredError() {
		return this.field.getRequiredError();
	}

	@Override
	public void focus() {
		super.focus();
	}

	public TextField getField() {
		return this.field;
	}
}
