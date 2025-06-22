
package bookstore;

/**
 *
 * @author terry
 */
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Bookstore 
{
    private static Bookstore instance;
    private List<Book> books;
    private List<Customer> customers;
    private Owner owner;

    private Bookstore() 
    {
        books = new ArrayList<>();
        customers = new ArrayList<>();
        owner = new Owner("admin", "admin");
        loadFromFiles();
    }

    public static Bookstore getInstance() 
    {
        if (instance == null) 
        {
            instance = new Bookstore();
        }
        return instance;
    }

    // book methods
    public void addBook(Book book) { books.add(book); }
    public void removeBook(Book book) { books.remove(book); }
    public List<Book> getBooks() { return books; }

    // user methods
    public void addCustomer(Customer customer) { customers.add(customer); }
    public void removeCustomer(Customer customer) { customers.remove(customer); }
    public List<Customer> getCustomers() { return customers; }
    public Owner getOwner() { return owner; }

    public Customer findCustomer(String username) 
    {
        return customers.stream()
                      .filter(c -> c.getUsername().equals(username))
                      .findFirst()
                      .orElse(null);
    }

    // saves data to 2 text files in CSV format
    public void saveToFiles() 
    {
        try (PrintWriter bookWriter = new PrintWriter("books.txt");
             PrintWriter customerWriter = new PrintWriter("customers.txt")) 
        {
            // save books
            for (Book book : books) 
            {
                bookWriter.println(book.getName() + "," + book.getPrice());
            }
            
            // save customers
            for (Customer customer : customers) 
            {
                customerWriter.println(customer.getUsername() + "," + 
                                     customer.getPassword() + "," + 
                                     customer.getPoints());
            }
        } 
        catch (IOException e) 
        {
            System.err.println("Error saving files: " + e.getMessage());
        }
    }

    // loads data from 2 text files
    private void loadFromFiles() 
    {
        try (BufferedReader bookReader = new BufferedReader(new FileReader("books.txt"));
             BufferedReader customerReader = new BufferedReader(new FileReader("customers.txt"))) 
        {  
            // load books
            String line;
            while ((line = bookReader.readLine()) != null) 
            {
                String[] parts = line.split(",");
                if (parts.length == 2) 
                {
                    books.add(new Book(parts[0], Double.parseDouble(parts[1])));
                }
            }
            
            // load customers
            while ((line = customerReader.readLine()) != null) 
            {
                String[] parts = line.split(",");
                if (parts.length == 3) 
                {
                    Customer customer = new Customer(parts[0], parts[1]);
                    customer.addPoints(Integer.parseInt(parts[2]));
                    customers.add(customer);
                }
            }
        } 
        catch (IOException e) 
        {
            // files don't exist yet 
        }
    }
}
