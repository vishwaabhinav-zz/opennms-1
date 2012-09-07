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

package org.opennms.features.jmxconfiggenerator.webui.ui;

import com.vaadin.ui.*;
import org.opennms.features.jmxconfiggenerator.webui.data.SelectableBeanItemContainer;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;

/**
 * A Test panel to get used to vaadin. Can be removed in future releases.
 *
 * @author m.v.rueden
 */
public class TestPanel extends VerticalLayout {

//	private static int count = 1;
	public TestPanel() {
		TabSheet tabSheet = new TabSheet();

		Table table = new Table();
		SelectableBeanItemContainer<Attrib> container = new SelectableBeanItemContainer<Attrib>(Attrib.class);
		for (int i = 0; i < 10; i++) {
			container.addItem(createTestAttribute(i + 1));
		}
		table.setContainerDataSource(container);
		table.setEditable(false);
		//		BeanItemContainer container = new BeanItemContainer<Person>(Person.class);
//		container.addItem(new Person("John", "Doe"));
//		container.addItem(new Person("Hugo", "Whatever"));
//		container.addItem(new Person("D", "E"));
//		table.setContainerDataSource(container);
//		table.setWidth(100, UNITS_PERCENTAGE);
//		table.setHeight(400, UNITS_PIXELS);

		tabSheet.addTab(table, "Attributes");
		tabSheet.addTab(new Label("2"), "Composites");
		addComponent(tabSheet);
	}

	public Attrib createTestAttribute(int no) {
		Attrib a = new Attrib();
		a.setAlias("Alias" + no);
		a.setMaxval("Maxval" + no);
		a.setMinval("Minval" + no);
		a.setName("Name" + no);
		a.setType("Type" + no);
		return a;
	}

	public class Person {

		private String nachname;
		private String vorname;
		private String email;

		public Person(String vorname, String nachname) {
			this.vorname = vorname;
			this.nachname = nachname;
			this.email = this.vorname + "." + this.nachname + "@gmail.com";
		}

		public String getEmail() {
			return email;
		}

		public String getNachname() {
			return nachname;
		}

		public String getVorname() {
			return vorname;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public void setNachname(String nachname) {
			this.nachname = nachname;
		}

		public void setVorname(String vorname) {
			this.vorname = vorname;
		}
	}
}
