// DATA MANIPULATION CONTROLLER
// DisplayQueryResultsController.java
// Controller for the DisplayQueryResults app

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.PatternSyntaxException;
import java.sql.PreparedStatement;

import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;

import java.awt.EventQueue;
import java.sql.* ;
import javax.swing.*;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import javax.swing.JOptionPane;
import javax.swing.JFrame;

public class DisplayQueryResultsController {
   @FXML private BorderPane borderPane;
   @FXML private TextArea queryTextArea;
   @FXML private TextField filterTextField;

   // database URL, username and password
   private static final String DATABASE_URL = "jdbc:derby:books";
   private static final String USERNAME = "deitel";
   private static final String PASSWORD = "deitel";

   // default query retrieves all data from Authors table
   private static final String DEFAULT_QUERY = "SELECT * FROM authors";

   // used for configuring JTable to display and sort data
   private ResultSetTableModel tableModel;
   private TableRowSorter<TableModel> sorter;

   private Connection connection;
   private PreparedStatement statement;


   @FXML
   private ComboBox<String> predefinedqueries;

   public void initialize() {
     predefinedqueries.getItems().addAll(
          "Display all authors",
          "Display specific author",
          "Add new author",
          "Edit an author",
          "Display all titles",
          "All titles ordered by year",
          "Display specific title",
          "Add new title",
          "Display all authorISBN",
          "Add new entry"
     );
      queryTextArea.setText(DEFAULT_QUERY);

      // create ResultSetTableModel and display database table
      try {
        connection = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
         // create TableModel for results of DEFAULT_QUERY
         statement = connection.prepareStatement(DEFAULT_QUERY,
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
         tableModel = new ResultSetTableModel(DATABASE_URL,
            USERNAME, PASSWORD, statement);

         // create JTable based on the tableModel
         JTable resultTable = new JTable(tableModel);

         // set up row sorting for JTable
         sorter = new TableRowSorter<TableModel>(tableModel);
         resultTable.setRowSorter(sorter);

         // configure SwingNode to display JTable, then add to borderPane
         SwingNode swingNode = new SwingNode();
         swingNode.setContent(new JScrollPane(resultTable));
         borderPane.setCenter(swingNode);
      }
      catch (SQLException sqlException) {
         displayAlert(AlertType.ERROR, "Database Error",
            sqlException.getMessage());
         tableModel.disconnectFromDatabase(); // close connection
         System.exit(1); // terminate application
      }
   }


   @FXML
   void querySelected(ActionEvent event){
     EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
          String firstname,lastname,authorid,isbn;
          try {
     switch (predefinedqueries.getValue()){

       case "Display all authors": //Create prepared statement that displays all authors
                                  statement = connection.prepareStatement("SELECT * FROM authors",
          ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                                  queryTextArea.setText("SELECT * FROM authors");
                                  tableModel.setQueryType("Query");
                                  tableModel.executeQuery(statement);
                                  break;

       case "Display specific author": //Create prepared statement that displays all titles of a specific author
                                  statement = connection.prepareStatement("SELECT lastName, firstName, title, titles.isbn, titles.copyright FROM authors INNER JOIN authorISBN ON authors.authorID=authorISBN.authorID INNER JOIN titles ON authorISBN.isbn=titles.isbn WHERE lastName = ? AND firstName = ?",
          ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                                  firstname = JOptionPane.showInputDialog("Enter author`s first name:");
                                  lastname = JOptionPane.showInputDialog("Enter author`s last name:");
                                  queryTextArea.setText("SELECT lastName, firstName, title, titles.isbn, titles.copyright FROM authors INNER JOIN authorISBN ON authors.authorID=authorISBN.authorID INNER JOIN titles ON authorISBN.isbn=titles.isbn WHERE lastName = '" + lastname + "' AND firstName = '" + firstname +"'");
                                  statement.setString(1, lastname);
                                  statement.setString(2, firstname);
                                  tableModel.setQueryType("Query");
                                  tableModel.executeQuery(statement);
                                  break;

      case "Add new author":  //Create prepared statement that adds new author
                              statement = connection.prepareStatement("INSERT INTO Authors (FirstName, LastName) VALUES (?, ?)",
         ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                              firstname = JOptionPane.showInputDialog("Enter author`s first name:");
                              lastname = JOptionPane.showInputDialog("Enter author`s last name:");
                              queryTextArea.setText("INSERT INTO Authors (FirstName, LastName) VALUES ('" + firstname + "', '" + lastname + "')");
                              statement.setString(1, firstname);
                              statement.setString(2, lastname);
                              tableModel.setQueryType("Update");
                              tableModel.executeQuery(statement);

                              //Create prepared statement that shows new author in authors list
                              statement = connection.prepareStatement("SELECT * FROM authors",
                                 ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                              queryTextArea.setText("SELECT * FROM authors");
                              tableModel.setQueryType("Query");
                              tableModel.executeQuery(statement);
                              break;

      case "Edit an author":  //Create prepared statement that edits an author
                              statement = connection.prepareStatement("UPDATE Authors SET LastName = ?, firstName = ? WHERE authorID = ?",
         ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                              authorid = JOptionPane.showInputDialog("Enter author`s ID:");
                              firstname = JOptionPane.showInputDialog("Enter author`s first name:");
                              lastname = JOptionPane.showInputDialog("Enter author`s last name:");
                              queryTextArea.setText("UPDATE Authors SET LastName = '" + lastname + "', firstName = '" + firstname + "' WHERE authorID = " + authorid);
                              statement.setString(1, lastname);
                              statement.setString(2, firstname);
                              statement.setString(3, authorid);
                              tableModel.setQueryType("Update");
                              tableModel.executeQuery(statement);

                              //Create prepared statement that shows author changes
                              statement = connection.prepareStatement("SELECT * FROM authors",
                                 ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                              queryTextArea.setText("SELECT * FROM authors");
                              tableModel.setQueryType("Query");
                              tableModel.executeQuery(statement);
                              break;

      case "Display all titles":  //Create prepared statement that displays all titles
                                  statement = connection.prepareStatement("SELECT * FROM titles",
         ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                                  queryTextArea.setText("SELECT * FROM titles");
                                  tableModel.setQueryType("Query");
                                  tableModel.executeQuery(statement);
                                  break;

      case "All titles ordered by year":  //Create prepared statement that displays all title ordered by year
                                  statement = connection.prepareStatement("SELECT title, copyright FROM titles ORDER BY copyright",
        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                                  queryTextArea.setText("SELECT title, copyright FROM titles ORDER BY copyright");
                                  tableModel.setQueryType("Query");
                                  tableModel.executeQuery(statement);
                                  break;

      case "Display specific title":  //Create prepared statement that displays all authors of a specific title
                                  statement = connection.prepareStatement("SELECT LastName, FirstName, Title FROM Authors INNER JOIN AuthorISBN ON Authors.AuthorID=AuthorISBN.AuthorID INNER JOIN Titles ON AuthorISBN.ISBN=Titles.ISBN WHERE title = ? ORDER BY lastName, firstName",
        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                                  String specifictitle = JOptionPane.showInputDialog("Enter a specific title:");
                                  queryTextArea.setText("SELECT LastName, FirstName, Title FROM Authors INNER JOIN AuthorISBN ON Authors.AuthorID=AuthorISBN.AuthorID INNER JOIN Titles ON AuthorISBN.ISBN=Titles.ISBN WHERE title='" + specifictitle + "' ORDER BY lastName, firstName");
                                  statement.setString(1, specifictitle);
                                  tableModel.setQueryType("Query");
                                  tableModel.executeQuery(statement);
                                  break;

      case "Add new title":   //Create prepared statement that adds new title
                              statement = connection.prepareStatement("INSERT INTO titles (isbn, title, editionNumber, copyright) VALUES (?, ?, ?, ?)",
         ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                              String newtitle = JOptionPane.showInputDialog("Enter new title:");
                              String edition = JOptionPane.showInputDialog("Enter edition number:");
                              String year = JOptionPane.showInputDialog("Enter year:");
                              isbn = JOptionPane.showInputDialog("Enter ISBN:");
                              authorid = JOptionPane.showInputDialog("Enter author`s ID:");
                              queryTextArea.setText("INSERT INTO titles (isbn, title, editionNumber, copyright) VALUES ('" + isbn + "', '" + newtitle + "', " + edition + ", '" + year + "')");
                              statement.setString(1, isbn);
                              statement.setString(2, newtitle);
                              statement.setString(3, edition);
                              statement.setString(4, year);
                              tableModel.setQueryType("Update");
                              tableModel.executeQuery(statement);

                              //Create prepared statement that links new title with author
                              statement = connection.prepareStatement("INSERT INTO authorISBN (authorID, isbn) VALUES (?, ?)",
                                 ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                              queryTextArea.setText("INSERT INTO authorISBN (authorID, isbn) VALUES (" + authorid + ", '" + isbn + "')");
                              statement.setString(1, authorid);
                              statement.setString(2, isbn);
                              tableModel.setQueryType("Update");
                              tableModel.executeQuery(statement);

                              //Create prepared statement that shows new title in titles list
                              statement = connection.prepareStatement("SELECT * FROM titles",
                                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                              queryTextArea.setText("SELECT * FROM titles");
                              tableModel.setQueryType("Query");
                              tableModel.executeQuery(statement);
                              break;

      case "Display all authorISBN":    //Create prepared statement that displays all books linked to each author
                                        statement = connection.prepareStatement("SELECT * FROM authorISBN",
         ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                                        queryTextArea.setText("SELECT * FROM authorISBN");
                                        tableModel.setQueryType("Query");
                                        tableModel.executeQuery(statement);
                                        break;

      case "Add new entry":   //Create prepared statement that adds new entry
                              statement = connection.prepareStatement("INSERT INTO authorISBN (authorID, isbn) VALUES (?, ?)",
         ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                              authorid = JOptionPane.showInputDialog("Enter author`s ID:");
                              isbn = JOptionPane.showInputDialog("Enter ISBN:");
                              queryTextArea.setText("INSERT INTO authorISBN (authorID, isbn) VALUES (" + authorid + ", '" + isbn + "')");
                              statement.setString(1, authorid);
                              statement.setString(2, isbn);
                              tableModel.setQueryType("Update");
                              tableModel.executeQuery(statement);

                              //Create prepared statement that shows all entries
                              statement = connection.prepareStatement("SELECT * FROM authorISBN",
                                 ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                              queryTextArea.setText("SELECT * FROM authorISBN");
                              tableModel.setQueryType("Query");
                              tableModel.executeQuery(statement);
                              break;
     }
   }
   catch (SQLException sqlException) {
   }
   }
  });

   }

   // query the database and display results in JTable
   @FXML
   void submitQueryButtonPressed(ActionEvent event) {


      // perform a new query
      try {
        String check = queryTextArea.getText();
        if(check.contains("SELECT")){
          tableModel.setQueryType("Query");
        }
        else{
          tableModel.setQueryType("Update");
        }
        statement = connection.prepareStatement(queryTextArea.getText(),
           ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
         tableModel.executeQuery(statement);
      }
      catch (SQLException sqlException) {
         displayAlert(AlertType.ERROR, "Database Error",
            sqlException.getMessage());

         // try to recover from invalid user query
         // by executing default query
         try {

            queryTextArea.setText(DEFAULT_QUERY);
            statement = connection.prepareStatement(DEFAULT_QUERY,
               ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
               tableModel.setQueryType("Query");
            tableModel.executeQuery(statement);
         }
         catch (SQLException sqlException2) {
            displayAlert(AlertType.ERROR, "Database Error",
               sqlException2.getMessage());
            tableModel.disconnectFromDatabase(); // close connection
            System.exit(1); // terminate application
         }
      }
   }

   // apply specified filter to results
   @FXML
   void applyFilterButtonPressed(ActionEvent event) {
      String text = filterTextField.getText();

      if (text.length() == 0) {
         sorter.setRowFilter(null);
      }
      else {
         try {
            sorter.setRowFilter(RowFilter.regexFilter(text));
         }
         catch (PatternSyntaxException pse) {
            displayAlert(AlertType.ERROR, "Regex Error",
               "Bad regex pattern");
         }
      }
   }

   // display an Alert dialog
   private void displayAlert(
      AlertType type, String title, String message) {
      Alert alert = new Alert(type);
      alert.setTitle(title);
      alert.setContentText(message);
      alert.showAndWait();
   }
}
