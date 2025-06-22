
package bookstore;

/**
 *
 * @author terry
 */
import javafx.stage.Stage;

public interface GUIHandler 
{
    void setPrimaryStage(Stage primaryStage);
    void displayLoginScreen();
    void displayOwnerScreen();
    void displayCustomerScreen();
}