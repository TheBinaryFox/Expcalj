package com.thebinaryfox.expcalj.operations;

import java.math.BigDecimal;

import com.thebinaryfox.expcalj.ExpressionEnvironment;
import com.thebinaryfox.expcalj.IOperation;
import com.thebinaryfox.expcalj.OperationOrder;

/**
 * The division operation.
 * 
 * @author The Binary Fox
 */
@OperationOrder(2)
public class OpDivision implements IOperation {

	@Override
	public BigDecimal calculate(BigDecimal left, BigDecimal right, ExpressionEnvironment env) {
		return left.divide(right, env.getMathContext());
	}

	@Override
	public String toString() {
		return "/";
	}

}
