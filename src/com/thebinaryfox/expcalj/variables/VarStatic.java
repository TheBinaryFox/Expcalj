package com.thebinaryfox.expcalj.variables;

import java.math.BigDecimal;

import com.thebinaryfox.expcalj.IVariable;

/**
 * A variable that holds a static value.
 * 
 * @author The Binary Fox
 */
public class VarStatic implements IVariable {

	private BigDecimal value;
	
	/**
	 * Create a new static variable.
	 * @param value the value of the variable.
	 */
	public VarStatic(BigDecimal value) {
		if (value == null)
			throw new IllegalArgumentException("The variable value cannot be null!");
		
		this.value = value;
	}

	@Override
	public BigDecimal value() {
		return value;
	}

}
