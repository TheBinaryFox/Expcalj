package com.thebinaryfox.expcalj.variables;

import java.math.BigDecimal;

import com.thebinaryfox.expcalj.IVariable;

/**
 * A variable that provides the value of the current Java timestamp.
 * 
 * @author The Binary Fox
 */
public class VarTimestamp implements IVariable {

	@Override
	public BigDecimal value() {
		return new BigDecimal(System.currentTimeMillis());
	}

}
