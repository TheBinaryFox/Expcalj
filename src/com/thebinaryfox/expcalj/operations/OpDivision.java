package com.thebinaryfox.expcalj.operations;

import java.math.BigDecimal;
import java.math.MathContext;

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
	public BigDecimal calculate(BigDecimal left, BigDecimal right) {
		try {
			return left.divide(right);
		} catch (ArithmeticException ex) {
			return left.divide(right, MathContext.DECIMAL128);
		}
	}

	@Override
	public String toString() {
		return "/";
	}

}
