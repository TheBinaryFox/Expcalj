package com.thebinaryfox.expcalj;

import java.math.BigDecimal;
import java.util.List;

/**
 * An interface for a mathematical function.
 * 
 * @author The Binary Fox
 */
public interface IFunction {

	/**
	 * Run the function.
	 * 
	 * @param params
	 *            the parameters of the function.
	 * @return the function.
	 */
	public BigDecimal run(List<BigDecimal> params);

}
