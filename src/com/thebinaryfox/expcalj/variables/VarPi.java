package com.thebinaryfox.expcalj.variables;

import java.math.BigDecimal;

import com.thebinaryfox.expcalj.IVariable;

/**
 * A variable that holds the value of pi.
 * 
 * @author The Binary Fox
 */
public class VarPi implements IVariable {

	static private final BigDecimal PI = new BigDecimal("3.1415926535897932384626");

	@Override
	public BigDecimal value() {
		return PI;
	}

}
