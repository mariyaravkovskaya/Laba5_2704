package org.example;

import java.io.IOException;
import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import com.google.gson.Gson;


public class Main {

    private static final String URL = "jdbc:h2:mem:study "; //jdbc:h2:~/testdb
    private static final String USER = "sa";
    private static final String PASSWORD = "";

        public static void main(String[] args) {
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Создание таблицы music
            createMusicTable(connection);

            // Вставка начальных данных в таблицу music
            insertInitialMusicData(connection);
            System.out.println("-------------------Задание №1---------------------");
            // 1. Получение списка музыкальных композиций
            getAllMusic(connection);

            System.out.println("-------------------Задание №2---------------------");
            // 2. Получение композиций, в названиях которых отсутствуют буквы “m” и “t”
            getMusicWithoutMAndT(connection);

            System.out.println("-------------------Задание №3---------------------");
            // 3. Добавление своей любимой композиции
            addFavoriteSong(connection, "Shape of You");
            getAllMusic(connection);

            System.out.println("-------------------Задание №4---------------------");
            // 4. Создание таблиц для книг и посетителей
            createReadersAndBooksTables(connection);


            // 5. Добавление уникальных объектов из books.json
            addBooksFromJson(connection);
            System.out.println("-------------------Задание №5---------------------");
            // 6. Возврат отсортированного списка книг по году издания
            getSortedBooksByYear(connection);

            System.out.println("-------------------Задание №6---------------------");
            // 7. Вывод книг младше 2000 года
            getBooksYoungerThan2000(connection);

            System.out.println("-------------------Задание №7---------------------");
            // 8. Добавление информации о себе и своих любимых книгах
            addMyInfoAndFavoriteBooks(connection);

            System.out.println("-------------------Задание №8---------------------");
            // 9. Удаление созданных таблиц
            dropBooksAndVisitorsTables(connection);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createMusicTable(Connection connection) throws SQLException {
        String createMusicTable = "CREATE TABLE IF NOT EXISTS music (id INT PRIMARY KEY AUTO_INCREMENT, title VARCHAR(255))";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createMusicTable);
            System.out.println("Created table for music.");
        }
    }

    private static void insertInitialMusicData(Connection connection) throws SQLException {
        String insertMusicData = "INSERT INTO music (title) " +
                "SELECT * FROM (VALUES " +
                "('Bohemian Rhapsody'), " +
                "('Stairway to Heaven'), " +
                "('Imagine'), " +
                "('Sweet Child O Mine'), " +
                "('Hey Jude'), " +
                "('Hotel California'), " +
                "('Billie Jean'), " +
                "('Wonderwall'), " +
                "('Smells Like Teen Spirit'), " +
                "('Let It Be'), " +
                "('I Want It All'), " +
                "('November Rain'), " +
                "('Losing My Religion'), " +
                "('One'), " +
                "('With or Without You'), " +
                "('Sweet Caroline'), " +
                "('Yesterday'), " +
                "('Dont Stop Believin'), " +
                "('Crazy Train'), " +
                "('Always')) AS new_data(title) " +
                "WHERE NOT EXISTS (SELECT 1 FROM music)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(insertMusicData);
            System.out.println("Inserted initial music data.");
        }
    }


    private static void getAllMusic(Connection connection) throws SQLException {
        String query = "SELECT * FROM music";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                System.out.println("Music: " + rs.getString("title"));
            }
        }
    }

    private static void getMusicWithoutMAndT(Connection connection) throws SQLException {
        String query = "SELECT * FROM music WHERE LOWER(title) NOT LIKE '%m%' AND LOWER(title) NOT LIKE '%t%'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                System.out.println("Music without 'm' and 't': " + rs.getString("title"));
            }
        }
    }

    private static void addFavoriteSong(Connection connection, String title) throws SQLException {
        String query = "INSERT INTO music (title) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, title);
            pstmt.executeUpdate();
            System.out.println("Added favorite song: " + title);
        }
    }


private static void createReadersAndBooksTables(Connection connection) throws SQLException {
    String createReadersTable = "CREATE TABLE IF NOT EXISTS readers (" +
            "id INTEGER PRIMARY KEY AUTO_INCREMENT, " + // Исправлено
            "name VARCHAR(50) NOT NULL, " +
            "surname VARCHAR(100) NOT NULL, " +
            "subscribed BOOLEAN NOT NULL DEFAULT FALSE, " +
            "phone VARCHAR(15) NOT NULL" +
            ")";

    String createBooksTable = "CREATE TABLE IF NOT EXISTS books (" +
            "id INTEGER PRIMARY KEY AUTO_INCREMENT, " + // Исправлено
            "name VARCHAR(100) NOT NULL, " +
            "isbn VARCHAR(100) NOT NULL, " +
            "publishing_year INTEGER NOT NULL, " +
            "author VARCHAR(100) NOT NULL, " +
            "publisher VARCHAR(100) NOT NULL" +
            ")";

    try (Statement stmt = connection.createStatement()) {
        stmt.execute(createReadersTable);
        stmt.execute(createBooksTable);
        System.out.println("Created tables for readers and books.");
    }
}


private static void addBooksFromJson(Connection connection) {
    String jsonFilePath = "books.json"; // Путь к файлу

    try {
        String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
        JSONArray readersArray = new JSONArray(content);

        String readerQuery = "INSERT INTO readers (name, surname, phone, subscribed) VALUES (?, ?, ?, ?)";
        String bookQuery = "INSERT INTO books (name, author, publishing_year, isbn, publisher) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement readerPstmt = connection.prepareStatement(readerQuery);
             PreparedStatement bookPstmt = connection.prepareStatement(bookQuery)) {

            for (int i = 0; i < readersArray.length(); i++) {
                JSONObject reader = readersArray.getJSONObject(i);

                // Добавление читателя
                readerPstmt.setString(1, reader.getString("name"));
                readerPstmt.setString(2, reader.getString("surname"));
                readerPstmt.setString(3, reader.getString("phone"));
                readerPstmt.setBoolean(4, reader.getBoolean("subscribed"));
                readerPstmt.executeUpdate();

                // Добавление любимых книг
                JSONArray favoriteBooks = reader.getJSONArray("favoriteBooks");
                for (int j = 0; j < favoriteBooks.length(); j++) {
                    JSONObject book = favoriteBooks.getJSONObject(j);
                    // Проверка на существование книги перед вставкой
                    String checkBookQuery = "SELECT COUNT(*) FROM books WHERE isbn = ?";
                    try (PreparedStatement checkPstmt = connection.prepareStatement(checkBookQuery)) {
                        checkPstmt.setString(1, book.getString("isbn"));
                        ResultSet rs = checkPstmt.executeQuery();
                        if (rs.next() && rs.getInt(1) == 0) { // Если книги нет
                            bookPstmt.setString(1, book.getString("name"));
                            bookPstmt.setString(2, book.getString("author"));
                            bookPstmt.setInt(3, book.getInt("publishingYear"));
                            bookPstmt.setString(4, book.getString("isbn"));
                            bookPstmt.setString(5, book.getString("publisher"));
                            bookPstmt.executeUpdate();
                        }
                    }
                }
            }
            System.out.println("Added readers and their favorite books from JSON.");
        } catch (SQLException e) {
            System.err.println("Error while adding readers or books: " + e.getMessage());
        } catch (JSONException e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
    } catch (IOException e) {
        System.err.println("Error reading JSON file: " + e.getMessage());
    }
}


private static void getSortedBooksByYear(Connection connection) throws SQLException {
    String query = "SELECT * FROM books ORDER BY publishing_year";
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
        System.out.println("Books sorted by publishing year:");
        while (rs.next()) {
            System.out.printf("Name: %s, Author: %s, Year: %d, ISBN: %s, Publisher: %s%n",
                    rs.getString("name"),
                    rs.getString("author"),
                    rs.getInt("publishing_year"),
                    rs.getString("isbn"),
                    rs.getString("publisher"));
        }
    }
}

    private static void getBooksYoungerThan2000(Connection connection) throws SQLException {
        String query = "SELECT * FROM books WHERE publishing_year > 2000";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("Books published before year 2000:");
            while (rs.next()) {
                System.out.printf("Name: %s, Author: %s, Year: %d%n",
                        rs.getString("name"),
                        rs.getString("author"),
                        rs.getInt("publishing_year"));
            }
        }
    }

    private static void addMyInfoAndFavoriteBooks(Connection connection) throws SQLException {
        // Добавляем информацию о себе в таблицу readers
        String insertReader = "INSERT INTO readers (name, surname, subscribed, phone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertReader, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, "Mariya");
            pstmt.setString(2, "Ravkovskaya");
            pstmt.setBoolean(3, true);
            pstmt.setString(4, "113-456-7890");
            pstmt.executeUpdate();

            // Получаем id вставленного читателя
            int readerId = -1;
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    readerId = keys.getInt(1);
                }
            }

            System.out.println("Added my info to readers: id = " + readerId);
        }

        // Добавляем свои любимые книги в таблицу books
        // Предположим, вы любите 2 книги - добавим их
        String insertBook = "INSERT INTO books (name, isbn, publishing_year, author, publisher) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertBook)) {
            // Первая книга
            pstmt.setString(1, "The Lord of the Rings");
            pstmt.setString(2, "978-0261102385");
            pstmt.setInt(3, 1954);
            pstmt.setString(4, "J.R.R. Tolkien");
            pstmt.setString(5, "Allen & Unwin");
            pstmt.executeUpdate();

            // Вторая книга
            pstmt.setString(1, "1984");
            pstmt.setString(2, "978-0451524935");
            pstmt.setInt(3, 1949);
            pstmt.setString(4, "George Orwell");
            pstmt.setString(5, "Secker & Warburg");
            pstmt.executeUpdate();

            System.out.println("Added my favorite books.");
        }

        // Выводим информацию о себе и своих книгах (только что добавленных)

        System.out.println("My info in readers table:");
        String selectReader = "SELECT * FROM readers WHERE phone = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(selectReader)) {
            pstmt.setString(1, "113-456-7890");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("ID: %d, Name: %s, Surname: %s, Subscribed: %b, Phone: %s%n",
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("surname"),
                            rs.getBoolean("subscribed"),
                            rs.getString("phone"));
                }
            }
        }

        System.out.println("My favorite books:");
        String selectBooks = "SELECT * FROM books WHERE isbn IN (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(selectBooks)) {
            pstmt.setString(1, "978-0261102385");
            pstmt.setString(2, "978-0451524935");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("Name: %s, Author: %s, Year: %d, ISBN: %s, Publisher: %s%n",
                            rs.getString("name"),
                            rs.getString("author"),
                            rs.getInt("publishing_year"),
                            rs.getString("isbn"),
                            rs.getString("publisher"));
                }
            }
        }
    }

    private static void dropBooksAndVisitorsTables(Connection connection) throws SQLException {
        String dropBooksTable = "DROP TABLE IF EXISTS books";
        String dropReadersTable = "DROP TABLE IF EXISTS readers";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(dropBooksTable);
            stmt.execute(dropReadersTable);
            System.out.println("Dropped tables for music, books and readers.");
        }
    }
}