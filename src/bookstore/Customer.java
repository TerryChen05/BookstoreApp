
package bookstore;

/**
 *
 * @author terry
 */
public class Customer extends AbstractUser 
{
    private int points;
    private CustomerState state;

    public Customer(String username, String password) 
    {
        super(username, password);
        this.points = 0; 
        this.state = new SilverState();
        updateState();
    }

    // adds points to the customer's total points
    public void addPoints(int pointsToAdd) 
    {
        this.points += pointsToAdd;
        updateState();
    }

    // handles points on redeem purchases
    public void processPurchase(double finalCost, int pointsRedeemed) 
    {
        this.points -= pointsRedeemed;
        this.points += (int)(finalCost * 10);
        updateState();
    }

    // checks for an update to customer state after every action
    private void updateState() 
    {
        if (points >= 1000) 
        {
            state = new GoldState();
        } 
        else 
        {
            state = new SilverState();
        }
    }

    public int getPoints() { return points; }
    public CustomerState getState() { return state; }
    public String getStatus() 
    { 
    	// if state is gold, returns gold, otherwise returns silver
        return state instanceof GoldState ? "Gold" : "Silver"; 
    }
}
