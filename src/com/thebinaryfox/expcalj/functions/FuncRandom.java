package com.thebinaryfox.expcalj.functions;

import java.math.BigDecimal;
import java.util.List;

import com.thebinaryfox.expcalj.ExpressionException;
import com.thebinaryfox.expcalj.IFunction;

/**
 * Random number generator.
 * 
 * @author The Binary Fox
 */
public class FuncRandom implements IFunction {

	@Override
	public BigDecimal run(List<BigDecimal> params) {
		if (params.size() == 0) {
			return generate(null, null);
		}
		
		if (params.size() == 1) {
			return generate(new BigDecimal(0), params.get(0));
		}
		
		if (params.size() == 2) {
			return generate(params.get(0), params.get(1));
		}
		
		throw new ExpressionException("requires between zero and two parameters.");
	}
	
	private BigDecimal generate(BigDecimal min, BigDecimal max) {
		// TODO
		return new BigDecimal(Math.random());
	}

	@Override
	public String toString() {
		return "random";
	}

}
