package expcalj.cli;

import java.math.BigDecimal;

import com.thebinaryfox.expcalj.IOperation;
import com.thebinaryfox.expcalj.OperationOrder;

/**
 * An inequality operation.
 * 
 * @author The Binary Fox
 */
@OperationOrder(0)
public class TestOpNotEquals implements IOperation {

	static private final BigDecimal TRUE = new BigDecimal(1);
	static private final BigDecimal FALSE = new BigDecimal(0);

	@Override
	public BigDecimal calculate(BigDecimal left, BigDecimal right) {
		return (left.compareTo(right) == 0) ? FALSE : TRUE;
	}

	@Override
	public String toString() {
		return "!=";
	}

}
