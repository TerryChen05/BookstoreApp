
package bookstore;

/**
 *
 * @author terry
 */
public class GoldState implements CustomerState
{
	@Override
    public double getDiscountRate() { return 0.10; }
}

