// TableTest3.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.demo;

import eu.reservoir.monitoring.core.table.*;
import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.TypeException;
import java.util.*;
import java.io.IOException;

/**
 * A test of the Table type.
 */
public class TableTest3 {
    public static void main(String[] args) throws TableException, TypeException {
	// allocate a table
	Table table1 = new DefaultTable();

	// define the header
	TableHeader header = new DefaultTableHeader().
	    add("name", ProbeAttributeType.STRING).
	    add("type", ProbeAttributeType.STRING);

	table1.defineTable(header);

	// add a row of values
	TableRow r0 = new DefaultTableRow().
	    add(new DefaultTableValue("stuart")).
	    add(new DefaultTableValue("person"));

	table1.addRow(r0);

	table1.addRow(new DefaultTableRow().add("hello").add("world"));
	table1.addRow(new DefaultTableRow().add("one").add("two"));
	table1.addRow(new DefaultTableRow().add("___alpha").add("___beta"));


	System.out.println(table1.getColumnDefinitions());
	System.out.println(table1.getRowCount());


	System.out.println(table1);

        // delete row by TableRow

        System.out.println("Row 0 == r0? " + r0.equals(table1.getRow(0)));

        table1.deleteRow(r0);
	System.out.println(table1);

        // delete row by position
        table1.deleteRow(1);
	System.out.println(table1);
    }
}
