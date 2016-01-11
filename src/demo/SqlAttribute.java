/*******************************************************************************
* Copyright (c) 2015-2016 Alexey Penkin.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Alexey Penkin - initial API and implementation
*******************************************************************************/

package demo;

import java.sql.Types;

import xmlaserver.Olap.Attribute;


public class SqlAttribute extends Attribute {
	
	int sqlType;
	
	public SqlAttribute(String name, int sqlType) {
		super(name);
		this.sqlType = sqlType;
	}
	
	public int getSqlType() {
		return sqlType;
	}
	
	public String getWhereClause(String value) throws Exception {
		// https://docs.oracle.com/javase/6/docs/api/java/sql/Types.html
		// https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=4&cad=rja&uact=8&ved=0ahUKEwiT9tHNl5TKAhUVCY4KHYO4DSAQFggzMAM&url=http%3A%2F%2Fwww.cs.mun.ca%2F~michael%2Fjava%2Fjdk1.1-beta2-docs%2Fguide%2Fjdbc%2Fmapping.doc.html&usg=AFQjCNHWTQYHQIwC3PdJEm8CyZnaD5uLEw&sig2=95uOhhhu3cYU6AdXonpbCw&bvm=bv.110151844,d.c2E
		
		if (value == null || value.equals("#null")) 
			return getName() + " IS NULL";
		
		switch (sqlType) {
		// Integer SQL Data Types
		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.INTEGER:
		case Types.BIGINT:
			return getName() + "=" + value;

		// Decimal SQL Data Types
		case Types.DECIMAL:
		case Types.NUMERIC:
		case Types.REAL:
		case Types.FLOAT:
		case Types.DOUBLE:
			return getName() + "=" + value;
			
		// String SQL Data Types
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			return getName() + "='" + value + "'";
			
		// Date SQL Data Types
		case Types.DATE:
		case Types.TIME:
		case Types.TIMESTAMP:
			return getName() + "='" + value + "'";
		}
		
		throw new Exception("getWhereClause does not support SQL data type " + sqlType + " yet for SqlAttribute " + getName());
	}	
}
