package com.thebinaryfox.expcalj.operations;

import java.math.BigDecimal;

import com.thebinaryfox.expcalj.ExpressionEnvironment;
import com.thebinaryfox.expcalj.IOperation;
import com.thebinaryfox.expcalj.OperationOrder;

/**
 * The exponent/power operation.
 * 
 * This is an extremely basic implementation, and it doesn't support decimals
 * (floor rounding). If necessary, please use or create an implementation for
 * BigDecimalMath. (http://arxiv.org/src/0908.3030v2/anc)
 * 
 * This is not included as a default in the ExpressionEnvironment.
 * 
 * @author The Binary Fox
 */
@OperationOrder(3)
public class OpExponent implements IOperation {

	@Override
	public BigDecimal calculate(BigDecimal left, BigDecimal right, ExpressionEnvironment env) {
		return left.pow(right.setScale(0, BigDecimal.ROUND_FLOOR).toBigInteger().intValue(), env.getMathContext());
	}

	@Override
	public String toString() {
		return "^";
	}

}
