
package bookstore;

/**
 *
 * @author terry
 */
public abstract class AbstractUser 
{
    protected String username;
    protected String password;

    public AbstractUser(String username, String password) 
    {
        this.username = username;
        this.password = password;
    }

    public boolean login(String username, String password) 
    {
        return this.username.equals(username) && this.password.equals(password);
    }

    public String getUsername() 
    {
        return username;
    }
    
    public String getPassword() 
    {
        return password;
    }
}
