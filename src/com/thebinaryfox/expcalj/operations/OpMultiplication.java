package com.thebinaryfox.expcalj.operations;

import java.math.BigDecimal;

import com.thebinaryfox.expcalj.IOperation;
import com.thebinaryfox.expcalj.OperationOrder;

/**
 * The multiplication operation.
 * 
 * @author The Binary Fox
 */
@OperationOrder(2)
public class OpMultiplication implements IOperation {

	@Override
	public BigDecimal calculate(BigDecimal left, BigDecimal right) {
		return left.multiply(right);
	}
	
	@Override
	public String toString() {
		return "*";
	}

}
