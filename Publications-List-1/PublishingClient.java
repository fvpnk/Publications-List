import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class PublishingClient {
    private static final String DB_URL = "jdbc:sqlite:publishing.db";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PublishingClient::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("База данных изданий");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);

        JButton fetchButton = new JButton("Посмотреть записи");
        JButton addButton = new JButton("Добавить запись");
        JButton deleteButton = new JButton("Удалить запись");
        JButton updateButton = new JButton("Изменить запись");
        JButton searchButton = new JButton("Искать по автору");
        JButton saveButton = new JButton("Сохранить как...");

        JPanel panel = new JPanel();
        panel.add(fetchButton);
        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(updateButton);
        panel.add(searchButton);
        panel.add(saveButton);

        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);

        fetchButton.addActionListener(e -> textArea.setText(fetchAllRecords()));
        addButton.addActionListener(e -> {
            System.out.println("Добавление новой записи...");
            addRecord();
        });
        deleteButton.addActionListener(e -> {
            System.out.println("Удаление записи...");
            deleteRecord();
        });
        updateButton.addActionListener(e -> {
            System.out.println("Изменение записи...");
            updateRecord();
        });
        searchButton.addActionListener(e -> {
            System.out.println("Поиск по автору...");
            textArea.setText(searchByAuthor());
        });
        saveButton.addActionListener(e -> {
            System.out.println("Сохранение в файл...");
            saveToFile(textArea.getText());
        });

        frame.setVisible(true);
    }

    private static String fetchAllRecords() {
        StringBuilder result = new StringBuilder();
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {

            while (rs.next()) {
                result.append(rs.getInt("id")).append(", ")
                        .append(rs.getString("title")).append(", ")
                        .append(rs.getString("author")).append(", ")
                        .append(rs.getString("genre")).append(", ")
                        .append(rs.getInt("year")).append(", ")
                        .append(rs.getString("publisher")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private static void addRecord() {
        String title = JOptionPane.showInputDialog("Введите название:");
        String author = JOptionPane.showInputDialog("Введите автора:");
        String genre = JOptionPane.showInputDialog("Введите жанр:");
        int year = Integer.parseInt(JOptionPane.showInputDialog("Введите год:"));
        String publisher = JOptionPane.showInputDialog("Введите издателя:");

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO books (title, author, genre, year, publisher) VALUES (?, ?, ?, ?, ?)")) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, genre);
            pstmt.setInt(4, year);
            pstmt.setString(5, publisher);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteRecord() {
        int id = Integer.parseInt(JOptionPane.showInputDialog("Введите ID записи для удаления:"));

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM books WHERE id = ?")) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();

            // Сбросить счетчик для AUTOINCREMENT 
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM sqlite_sequence WHERE name='books'");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateRecord() {
        int id = Integer.parseInt(JOptionPane.showInputDialog("Введите ID записи для изменения:"));
        String newTitle = JOptionPane.showInputDialog("Введите новое название:");

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement("UPDATE books SET title = ? WHERE id = ?")) {
            pstmt.setString(1, newTitle);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String searchByAuthor() {
        String author = JOptionPane.showInputDialog("Введите автора для поиска:");

        StringBuilder result = new StringBuilder();
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM books WHERE author = ?")) {
            pstmt.setString(1, author);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                result.append(rs.getInt("id")).append(", ")
                        .append(rs.getString("title")).append(", ")
                        .append(rs.getString("author")).append(", ")
                        .append(rs.getString("genre")).append(", ")
                        .append(rs.getInt("year")).append(", ")
                        .append(rs.getString("publisher")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private static void saveToFile(String data) {
        try (FileWriter writer = new FileWriter("output.txt")) {
            writer.write(data);
            JOptionPane.showMessageDialog(null, "Data saved to output.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
