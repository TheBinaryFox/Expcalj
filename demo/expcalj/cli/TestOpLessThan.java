package expcalj.cli;

import java.math.BigDecimal;

import com.thebinaryfox.expcalj.ExpressionEnvironment;
import com.thebinaryfox.expcalj.IOperation;
import com.thebinaryfox.expcalj.OperationOrder;

/**
 * A less-than operation.
 * 
 * @author The Binary Fox
 */
@OperationOrder(0)
public class TestOpLessThan implements IOperation {

	static private final BigDecimal TRUE = new BigDecimal(1);
	static private final BigDecimal FALSE = new BigDecimal(0);

	@Override
	public BigDecimal calculate(BigDecimal left, BigDecimal right, ExpressionEnvironment env) {
		return (left.compareTo(right) < 0) ? TRUE : FALSE;
	}

	@Override
	public String toString() {
		return "<";
	}

}
