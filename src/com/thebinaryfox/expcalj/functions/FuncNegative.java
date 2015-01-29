package com.thebinaryfox.expcalj.functions;

import java.math.BigDecimal;
import java.util.List;

import com.thebinaryfox.expcalj.ExpressionEnvironment;
import com.thebinaryfox.expcalj.ExpressionException;
import com.thebinaryfox.expcalj.IFunction;

/**
 * Mathematical rounding function.
 * 
 * @author The Binary Fox
 */
public class FuncNegative implements IFunction {

	@Override
	public BigDecimal run(List<BigDecimal> params, ExpressionEnvironment env) {
		if (params.size() != 1)
			throw new ExpressionException("requires exactly one parameter.");

		return params.get(0).negate(env.getMathContext());
	}

	@Override
	public String toString() {
		return "neg()";
	}

}
