package bookstore;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.stage.Stage;
import javafx.scene.control.cell.*;
import javafx.scene.control.Alert.AlertType;
import java.io.IOException;

public class GUIController implements GUIHandler 
{
    private final Bookstore bookstore;
    private Stage primaryStage;
    private final BorderPane rootLayout;
    private Customer currentCustomer;
    private Owner currentOwner;
    private final ObservableList<Book> bookList = FXCollections.observableArrayList();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();

    private static boolean applicationRunning = false;
    
    public GUIController(Bookstore bookstore) 
    {
        this.bookstore = bookstore;
        this.rootLayout = new BorderPane();
        
        // initialize data
        bookList.setAll(bookstore.getBooks());
        customerList.setAll(bookstore.getCustomers());
        
        // prevent multiple windows
        rootLayout.sceneProperty().addListener((obs, oldScene, newScene) -> 
        {
            if (newScene != null) 
            {
                newScene.windowProperty().addListener((winObs, oldWin, newWin) -> 
                {
                    if (newWin != null && newWin != primaryStage) 
                    {
                        newWin.hide(); // block any secondary windows
                    }
                });
            }
        });
    }

    @Override
    public void setPrimaryStage(Stage primaryStage) 
    {
        if (this.primaryStage != null || applicationRunning) 
        {
            Platform.exit();
            return;
        }
        
        applicationRunning = true;
        this.primaryStage = primaryStage;
        primaryStage.setScene(new Scene(rootLayout, 800, 600));
        primaryStage.setOnCloseRequest(e -> 
        {
            bookstore.saveToFiles();
            applicationRunning = false;
        });
        displayLoginScreen();
    }

    private void showScreen(Node content) 
    {
        rootLayout.setCenter(content);
    }

    @Override
    public void displayLoginScreen() 
    {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);

        Label userLabel = new Label("Username:");
        TextField userInput = new TextField();
        userInput.setPromptText("username");

        Label passLabel = new Label("Password:");
        PasswordField passInput = new PasswordField();
        passInput.setPromptText("password");

        Button loginButton = new Button("Log In");
        loginButton.setOnAction(e -> handleLogin(userInput.getText(), passInput.getText()));

        grid.addRow(0, userLabel, userInput);
        grid.addRow(1, passLabel, passInput);
        grid.add(loginButton, 1, 2);

        showScreen(grid);
        primaryStage.setTitle("Bookstore Login");
    }

    private void handleLogin(String username, String password) 
    {
        if (bookstore.getOwner().login(username, password)) 
        {
            currentOwner = bookstore.getOwner();
            displayOwnerScreen();
        } 
        else 
        {
            Customer customer = bookstore.findCustomer(username);
            if (customer != null && customer.login(username, password)) 
            {
                currentCustomer = customer;
                displayCustomerScreen();
            } 
            else 
            {
                showAlert(AlertType.ERROR, "Login Failed", "Invalid username or password");
            }
        }
    }

    @Override
    public void displayOwnerScreen() 
    {
        BorderPane ownerScreen = new BorderPane();

        // menu Bar
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> displayLoginScreen());
        fileMenu.getItems().add(logoutItem);
        menuBar.getMenus().add(fileMenu);
        ownerScreen.setTop(menuBar);

        // TabPane for Books and Customers
        TabPane tabPane = new TabPane();
        
        // books Tab
        Tab booksTab = new Tab("Books");
        booksTab.setContent(createBooksPanel());
        booksTab.setClosable(false);
        
        // customers Tab
        Tab customersTab = new Tab("Customers");
        customersTab.setContent(createCustomersPanel());
        customersTab.setClosable(false);
        
        tabPane.getTabs().addAll(booksTab, customersTab);
        ownerScreen.setCenter(tabPane);

        showScreen(ownerScreen);
        primaryStage.setTitle("Owner Dashboard");
    }

    private VBox createBooksPanel() 
    {
        TableView<Book> bookTable = new TableView<>(bookList);
        
        TableColumn<Book, String> nameCol = new TableColumn<>("Book Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Book, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setCellFactory(tc -> new TableCell<Book, Double>() 
        {
            @Override
            protected void updateItem(Double price, boolean empty) 
            {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("$%.2f", price));
            }
        });
        
        bookTable.getColumns().addAll(nameCol, priceCol);

        // add Book controls
        HBox addPanel = new HBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Book name");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        Button addButton = new Button("Add Book");
        addButton.setOnAction(e -> 
        {
            try 
            {
                Book book = new Book(nameField.getText(), Double.parseDouble(priceField.getText()));
                bookstore.addBook(book);
                bookList.add(book);
                nameField.clear();
                priceField.clear();
            } 
            catch (NumberFormatException ex) 
            {
                showAlert(AlertType.ERROR, "Error", "Invalid price format");
            }
        });
        addPanel.getChildren().addAll(new Label("Name:"), nameField, 
                                    new Label("Price:"), priceField, addButton);
        addPanel.setAlignment(Pos.CENTER);

        // remove Book button
        Button removeButton = new Button("Remove Selected");
        removeButton.setOnAction(e -> 
        {
            Book selected = bookTable.getSelectionModel().getSelectedItem();
            if (selected != null) 
            {
                bookstore.removeBook(selected);
                bookList.remove(selected);
            }
        });

        VBox booksPanel = new VBox(10, bookTable, addPanel, removeButton);
        booksPanel.setPadding(new Insets(10));
        return booksPanel;
    }

    private VBox createCustomersPanel() 
    {
        TableView<Customer> customerTable = new TableView<>(customerList);
        
        TableColumn<Customer, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        
        TableColumn<Customer, String> passwordCol = new TableColumn<>("Password");
        passwordCol.setCellValueFactory(new PropertyValueFactory<>("password"));
        
        TableColumn<Customer, Integer> pointsCol = new TableColumn<>("Points");
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
        
        customerTable.getColumns().addAll(usernameCol, passwordCol, pointsCol);

        // Add Customer controls
        HBox addPanel = new HBox(10);
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        TextField passwordField = new TextField();
        passwordField.setPromptText("Password");
        Button addButton = new Button("Add Customer");
        addButton.setOnAction(e -> {
            Customer customer = new Customer(usernameField.getText(), passwordField.getText());
            bookstore.addCustomer(customer);
            customerList.add(customer);
            usernameField.clear();
            passwordField.clear();
        });
        addPanel.getChildren().addAll(new Label("Username:"), usernameField,
                                    new Label("Password:"), passwordField, addButton);
        addPanel.setAlignment(Pos.CENTER);

        // remove Customer button
        Button removeButton = new Button("Remove Selected");
        removeButton.setOnAction(e -> {
            Customer selected = customerTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                bookstore.removeCustomer(selected);
                customerList.remove(selected);
            }
        });

        VBox customersPanel = new VBox(10, customerTable, addPanel, removeButton);
        customersPanel.setPadding(new Insets(10));
        return customersPanel;
    }

    @Override
    public void displayCustomerScreen() {
        BorderPane customerScreen = new BorderPane();

        // welcome message
        Label welcomeLabel = new Label(String.format("Welcome %s. You have %d points. Your status is %s",
            currentCustomer.getUsername(),
            currentCustomer.getPoints(),
            currentCustomer.getStatus()));
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        customerScreen.setTop(welcomeLabel);

        // books table with checkboxes
        TableView<Book> bookTable = new TableView<>(bookList);
        
        TableColumn<Book, String> nameCol = new TableColumn<>("Book Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Book, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setCellFactory(tc -> new TableCell<Book, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("$%.2f", price));
            }
        });
        
        TableColumn<Book, Boolean> selectCol = new TableColumn<>("Select");
        selectCol.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectCol.setCellFactory(tc -> new CheckBoxTableCell<>());
        
        bookTable.getColumns().addAll(nameCol, priceCol, selectCol);

        // make rows clickable
        bookTable.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    Book book = row.getItem();
                    book.setSelected(!book.isSelected());
                }
            });
            return row;
        });

        // buy buttons
        HBox buttonBox = new HBox(10);
        Button buyButton = new Button("Buy");
        Button redeemButton = new Button("Redeem points and Buy");
        Button logoutButton = new Button("Logout");
        
        buyButton.setOnAction(e -> handlePurchase(false));
        redeemButton.setOnAction(e -> handlePurchase(true));
        logoutButton.setOnAction(e -> displayLoginScreen());
        
        buttonBox.getChildren().addAll(buyButton, redeemButton, logoutButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox centerPanel = new VBox(10, bookTable, buttonBox);
        centerPanel.setPadding(new Insets(10));
        customerScreen.setCenter(centerPanel);

        showScreen(customerScreen);
        primaryStage.setTitle("Customer Dashboard");
    }

    private void handlePurchase(boolean redeemPoints) {
        try {
            double originalCost = bookList.stream()
                .filter(Book::isSelected)
                .mapToDouble(Book::getPrice)
                .sum();
            
            if (originalCost <= 0) {
                showAlert(AlertType.ERROR, "Error", "No books selected");
                return;
            }

            double finalCost = originalCost;
            int pointsRedeemed = 0;
            
            if (redeemPoints) {
                pointsRedeemed = (int)Math.min(currentCustomer.getPoints(), originalCost * 100);
                finalCost = originalCost - (pointsRedeemed / 100.0);
                if (finalCost < 0) finalCost = 0;
            }
            
            currentCustomer.processPurchase(finalCost, pointsRedeemed);
            bookList.removeIf(Book::isSelected);
            bookstore.getBooks().removeIf(Book::isSelected);
            
            showPurchaseConfirmation(originalCost, finalCost, redeemPoints, pointsRedeemed);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Purchase Error", "An error occurred during purchase: " + e.getMessage());
        }
    }

    private void showPurchaseConfirmation(double originalCost, double finalCost, 
            boolean redeemed, int pointsRedeemed) 
    {
    	VBox confirmationScreen = new VBox(10);
    	confirmationScreen.setAlignment(Pos.CENTER);
    	confirmationScreen.setPadding(new Insets(20));

    	Label title = new Label("Purchase Complete");
    	title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    	// Build the confirmation message piece by piece
    	StringBuilder contentBuilder = new StringBuilder();
    	contentBuilder.append(String.format("Original Cost: $%.2f%n", originalCost));

    	if (redeemed) {
    		contentBuilder.append(String.format("Points Redeemed: %d%n", pointsRedeemed));
    		contentBuilder.append(String.format("Discount Applied: $%.2f%n", (originalCost - finalCost)));
    	}

    	contentBuilder.append(String.format("Final Cost: $%.2f%n%n", finalCost));
    	contentBuilder.append(String.format("Points Earned: %d%n", (int)(finalCost * 10)));
    	contentBuilder.append(String.format("New Points Balance: %d%n", currentCustomer.getPoints()));
    	contentBuilder.append(String.format("Status: %s", currentCustomer.getStatus()));

    	Label details = new Label(contentBuilder.toString());
    	details.setStyle("-fx-font-family: monospace;");

    	Button backButton = new Button("Back to Store");
    	backButton.setOnAction(e -> displayCustomerScreen());

    	confirmationScreen.getChildren().addAll(title, details, backButton);
    	showScreen(confirmationScreen);
    	primaryStage.setTitle("Purchase Complete");
    }

    private void showAlert(AlertType type, String title, String message) 
    {
        Alert alert = new Alert(type);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
