
package bookstore;
import javafx.beans.property.*;

/**
 *
 * @author terry
 */
public class Book 
{
	// property class variables
	/*
	 *  used properties instead of regular variables because
	 *  they have change notifications and automatic UI updates
	 */
    private final StringProperty name;
    private final DoubleProperty price;
    private final BooleanProperty selected;

    public Book(String name, double price) 
    {
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.selected = new SimpleBooleanProperty(false); // not selected by default
    }

    // name property methods
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    // price property methods
    public double getPrice() { return price.get(); }
    public void setPrice(double price) { this.price.set(price); }
    public DoubleProperty priceProperty() { return price; }

    // selected property methods
    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean selected) { this.selected.set(selected); }
    public BooleanProperty selectedProperty() { return selected; }
}
