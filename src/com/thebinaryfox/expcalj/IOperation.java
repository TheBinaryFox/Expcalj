package com.thebinaryfox.expcalj;

import java.math.BigDecimal;

/**
 * An interface for a mathematical operation.
 * 
 * @author The Binary Fox
 */
public interface IOperation {

	/**
	 * Calculate the result of the operation.
	 * 
	 * @param left
	 *            the left-hand variable.
	 * @param right
	 *            the right-hand variable.
	 * @return the calculated result.
	 */
	public BigDecimal calculate(BigDecimal left, BigDecimal right);

}
