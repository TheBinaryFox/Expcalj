package com.thebinaryfox.expcalj.functions;

import java.math.BigDecimal;
import java.util.List;

import com.thebinaryfox.expcalj.ExpressionException;
import com.thebinaryfox.expcalj.IFunction;

/**
 * Mathematical rounding function.
 * 
 * @author The Binary Fox
 */
public class FuncAbsolute implements IFunction {

	@Override
	public BigDecimal run(List<BigDecimal> params) {
		if (params.size() != 1)
			throw new ExpressionException("requires exactly one parameter.");

		return params.get(0).abs();
	}

	@Override
	public String toString() {
		return "abs";
	}

}
