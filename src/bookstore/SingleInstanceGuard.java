
package bookstore;

/**
 *
 * @author terry
 */
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

public class SingleInstanceGuard 
{
    private static FileLock lock;
    private static FileChannel channel;

    public static boolean lockInstance() 
    {
        try 
        {
            File file = new File(System.getProperty("user.home"), "bookstoreapp.lock");
            channel = FileChannel.open(file.toPath(), 
                StandardOpenOption.CREATE, 
                StandardOpenOption.WRITE);
            lock = channel.tryLock();
            return lock != null;
        } 
        catch (IOException e) 
        {
            return false;
        }
    }

    public static void releaseInstance() 
    {
        try 
        {
            if (lock != null) lock.release();
            if (channel != null) channel.close();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
}