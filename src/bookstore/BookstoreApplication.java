
package bookstore;

/**
 *
 * @author terry
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.ServerSocket;
import static javafx.application.Application.launch;

public class BookstoreApplication extends Application 
{
    private static final int SINGLE_INSTANCE_LOCK_PORT = 48239; // arbitrary unused port
    private static ServerSocket instanceLockSocket;

    @Override
    public void start(Stage primaryStage) 
    {
    	// first tries file-based locking
    	if (!SingleInstanceGuard.lockInstance()) 
    	{
            System.err.println("Application already running!");
            Platform.exit();
            return;
        }
        
    	try 
        {
            // try to acquire instance lock
            instanceLockSocket = new ServerSocket(SINGLE_INSTANCE_LOCK_PORT);
            
            // initialize application components
            Bookstore bookstore = Bookstore.getInstance();
            GUIController controller = new GUIController(bookstore);
            
            // set up primary stage
            controller.setPrimaryStage(primaryStage);
            primaryStage.show();
            
        } 
        catch (IOException e) 
        {
            // if port is already in use, another instance is running
            System.err.println("Application already running!");
            Platform.exit();
        }
    }

    @Override
    public void stop() 
    {
    	// clean up method when application is shutting down
    	SingleInstanceGuard.releaseInstance();
    }

    // main method launching the JavaFX application
    public static void main(String[] args) 
    {
        launch(args);
    }
}

