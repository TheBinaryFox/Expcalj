package expcalj.cli;

import java.math.BigDecimal;

import com.thebinaryfox.expcalj.IVariable;

/**
 * A variable storing the last answer.
 * 
 * @author The Binary Fox
 */
public class VarAns implements IVariable {

	private BigDecimal ans;

	public VarAns() {
		ans = new BigDecimal(0);
	}

	public void set(BigDecimal val) {
		ans = val;
	}

	@Override
	public BigDecimal value() {
		return ans;
	}

}
