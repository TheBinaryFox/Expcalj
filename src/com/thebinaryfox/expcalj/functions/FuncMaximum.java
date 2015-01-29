package com.thebinaryfox.expcalj.functions;

import java.math.BigDecimal;
import java.util.List;

import com.thebinaryfox.expcalj.ExpressionEnvironment;
import com.thebinaryfox.expcalj.ExpressionException;
import com.thebinaryfox.expcalj.IFunction;

/**
 * Mathematical maximum function.
 * 
 * @author The Binary Fox
 */
public class FuncMaximum implements IFunction {

	@Override
	public BigDecimal run(List<BigDecimal> params, ExpressionEnvironment env) {
		if (params.size() != 2)
			throw new ExpressionException("requires exactly two parameters.");

		return params.get(0).max(params.get(1));
	}

	@Override
	public String toString() {
		return "max()";
	}

}
