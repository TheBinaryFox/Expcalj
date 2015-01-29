package com.thebinaryfox.expcalj.functions;

import java.math.BigDecimal;
import java.util.List;

import org.nevec.rjm.BigDecimalMath;

import com.thebinaryfox.expcalj.ExpressionEnvironment;
import com.thebinaryfox.expcalj.ExpressionException;
import com.thebinaryfox.expcalj.IFunction;

/**
 * Square root implemented in BigDecimalMath.
 * 
 * @author The Binary Fox
 */
public class FuncSquareRoot implements IFunction {

	@Override
	public BigDecimal run(List<BigDecimal> params, ExpressionEnvironment env) {
		if (params.size() != 1)
			throw new ExpressionException("requires exactly one parameter.");

		return BigDecimalMath.sqrt(params.get(0), env.getMathContext());
	}

	@Override
	public String toString() {
		return "sqrt()";
	}

}
