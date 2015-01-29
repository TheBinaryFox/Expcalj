package com.thebinaryfox.expcalj.operations;

import java.math.BigDecimal;

import com.thebinaryfox.expcalj.ExpressionEnvironment;
import com.thebinaryfox.expcalj.IOperation;
import com.thebinaryfox.expcalj.OperationOrder;

/**
 * The addition operation.
 * 
 * @author The Binary Fox
 */
@OperationOrder(1)
public class OpAddition implements IOperation {

	@Override
	public BigDecimal calculate(BigDecimal left, BigDecimal right, ExpressionEnvironment env) {
		return left.add(right, env.getMathContext());
	}

	@Override
	public String toString() {
		return "+";
	}

}
