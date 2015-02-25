# Expcalj Usage
Simple usage for the demo command line application and including the library in your code.

## Command Line
*Compiling Expcalj CLI:*
```
cd build
bash compile.sh
```

*Running Expcalj CLI:*
```
cd build
java -jar expcalj.jar --color
```

**Using Expcalj CLI:**
```
# Evaluating a simple expression.
Calc$ 1 + 1
Result: 2
```
```
# Evaluating a multi-step expression.
Calc$ 1 + 3 + 5 + 7
Result: 16
```
```
# Brackets
Calc$ 10 * (3 + 5)
Result: 80
```
```
# Variables
Calc$ x = 3
Defined "x" as "3".

Calc$ x
Result: 3

Calc$ ans ^ 2
Result: 9
```
```
# Functions
Calc$ x(y) = y * 2
Defined "x(y)" as "y*2".

Calc$ x(3)
Result: 6

Calc$ x(10)
Result: 20
```
```
# Negative Numbers
Calc$ -3
[Error]

Calc$ neg(3)
Result: -3
```
```
# Default Operators
x+y -- addition.
x-y -- substraction.
x*y -- multiplication.
x/y -- division.
x%y -- remainder.
x^y -- power (only works with whole numbers).
```
```
# Default Functions (Excluding BigDecimalMath)
round(x) -- round up/down.
floor(x) -- round down.
ceil(x) -- round up.
min(x,y) -- min.
max(x,y) -- max.
neg(x) -- negative of x.
abs(x) -- absolute (positive) value of x.
```
```
# Loop
# Syntax: loop [variable name:minimum:maximum(:step size)] -- [expression]
Calc$ :loop x:0:10 -- x + 1
0:          1
1:          2
2:          3
3:          4
4:          5
5:          6
6:          7
7:          8
8:          9
9:          10
10:         11
```

## Library
Calculate an expression with the default environment.
```
package example;
import com.thebinaryfox.expcalj.*;

class Main {
    static public void main(String[] args) {
        Expression expr = new Expression("1 + 1");
        System.out.println(expr.calculate());
    }
}
```

Calculate an expression with a new environment.
```
package example;
import com.thebinaryfox.expcalj.*;

class Main {
    static public void main(String[] args) {
        ExpressionEnvironment env = new ExpressionEnvironment();
        env.useDefault(); // Include default operators and functions.
        
        Expression expr = new Expression("1 + 1", env);
        System.out.println(expr.calculate());
    }
}
```

Custom operators.
```
package example;
import java.math.BigDecimal;
import com.thebinaryfox.expcalj.*;

class Main {
    static public void main(String[] args) {
        IOperation iop = new IOperation() {
            @Override
            public BigDecimal calculate(BigDecimal left, BigDecimal right, ExpressionEnvironment env) {
                return left.add(right.divide(new BigDecimal("2")));
            }
        };
        
        ExpressionEnvironment env = new ExpressionEnvironment();
        env.useDefault(); // Include default operators and functions.
        env.setOperation("+/", iop);
        
        Expression expr = new Expression("1 +/ 4", env);
        System.out.println(expr.calculate());
    }
}
```