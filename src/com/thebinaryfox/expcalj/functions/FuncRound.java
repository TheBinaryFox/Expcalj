package com.thebinaryfox.expcalj.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.thebinaryfox.expcalj.ExpressionEnvironment;
import com.thebinaryfox.expcalj.ExpressionException;
import com.thebinaryfox.expcalj.IFunction;

/**
 * Mathematical rounding function.
 * 
 * @author The Binary Fox
 */
public class FuncRound implements IFunction {

	@Override
	public BigDecimal run(List<BigDecimal> params, ExpressionEnvironment env) {
		if (params.size() != 1)
			throw new ExpressionException("requires exactly one parameter.");

		return params.get(0).setScale(0, RoundingMode.HALF_UP);
	}

	@Override
	public String toString() {
		return "ceil()";
	}

}
