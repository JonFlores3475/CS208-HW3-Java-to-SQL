package cs208;
import java.sql.*;
import java.util.Scanner;

import org.sqlite.SQLiteConfig;

import java.sql.Date;


/**
 * The Database class contains helper functions to
 * create a connection and
 * test your connection
 * with the SQLite database.
 */
public class Database
{
    private final String sqliteFileName;

    public Database(String sqliteFileName)
    {
        this.sqliteFileName = sqliteFileName;
    }

    /**
     * Creates a connection to the SQLite database file specified in the {@link #Database(String) constructor}
     *
     * @return a connection to the database, which can be used to execute SQL statements against in the database
     * @throws SQLException if we cannot connect to the database (e.g., missing driver)
     */
    public Connection getDatabaseConnection() throws SQLException
    {
        // NOTE:
        // 'jdbc' is the protocol or API for connecting from a Java application to a database (SQLite, PostgreSQL, etc.)
        // 'sqlite' is the format of the database (for PostgreSQL, we would use the 'postgresql' format)
        String databaseConnectionURL = "jdbc:sqlite:" + sqliteFileName;
        System.out.println("databaseConnectionURL = " + databaseConnectionURL);

        Connection connection;
        try
        {
            SQLiteConfig sqLiteConfig = new SQLiteConfig();
            // Enables enforcement of foreign keys constraints in the SQLite database every time we start the application
            sqLiteConfig.enforceForeignKeys(true);

            connection = DriverManager.getConnection(databaseConnectionURL, sqLiteConfig.toProperties());
            return connection;
        }
        catch (SQLException sqlException)
        {
            System.err.println("SQLException was thrown while trying to connect using the '" + databaseConnectionURL + "' connection URL");
            System.err.println(sqlException.getMessage());
            throw sqlException;
        }
    }

    /**
     * Tests the connection to the database by running a simple SQL SELECT statement
     * to return the driver version used to connect to the database
     * NOTE:
     * See below the {@link #testConnectionSimplifiedVersion() testConnectionSimplifiedVersion()} method
     * for a simplified version of this method that uses less boilerplate.
     */
    public void testConnection()
    {
        // this SELECT statement will retrieve one row with one column containing the
        // version of the driver used to connect to the SQLite database
        String sql = "SELECT sqlite_version();";

        Connection connection = null;
        Statement sqlStatement = null;
        ResultSet resultSet = null;

        try
        {
            connection = getDatabaseConnection();
            sqlStatement = connection.createStatement();

            resultSet = sqlStatement.executeQuery(sql);

            // get to the first (and only) returned record (row)
            resultSet.next();

            // get the results of the first column in the row which contains the driver version
            String driverVersionToConnectToTheDatabase = resultSet.getString(1);
            System.out.println("Connection to Database Successful!");
            System.out.println("Driver version used to connect to the database: " + driverVersionToConnectToTheDatabase);
        }
        catch (SQLException sqlException)
        {
            System.err.println("SQLException: failed to query the database");
            System.err.println(sqlException.getMessage());
        }
        finally
        {
            // Q: Do I need to write this complex boilerplate code every time?
            // No, this is just for illustration purposes.
            // See below the testConnectionSimplifiedVersion() method
            // for a simpler alternative that does not require this finally block
            try
            {
                if (resultSet != null)
                {
                    resultSet.close();
                }
            }
            catch (SQLException sqlException)
            {
                System.err.println("SQLException: failed to close the resultSet");
                System.err.println(sqlException.getMessage());
            }

            try
            {
                if (sqlStatement != null)
                {
                    sqlStatement.close();
                }
            }
            catch (SQLException sqlException)
            {
                System.err.println("SQLException: failed to close the sqlStatement");
                System.err.println(sqlException.getMessage());
            }

            try
            {
                if (connection != null)
                {
                    connection.close();
                }
            }
            catch (SQLException sqlException)
            {
                System.err.println("SQLException: failed to close the connection");
                System.err.println(sqlException.getMessage());
            }
        }
    }

    /**
     * Tests the connection to the database by running a simple SQL SELECT statement
     * to return the driver version used to connect to the database
     * <p>
     * NOTE:
     * This method is the simplified version of {@link #testConnection() testConnection()}.
     * This method uses the
     * {@link <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources</a>}
     * statement to automatically close the {@code connection}, {@code sqlStatement} and {@code resultSet} objects
     * in case an error occurs, thus eliminating the code from the {@code finally} block
     */
    public void testConnectionSimplifiedVersion()
    {
        // this SELECT statement will retrieve one row with one column containing the
        // version of the driver used to connect to the SQLite database
        String sql = "SELECT sqlite_version();";

        try
        (
            Connection connection = getDatabaseConnection();
            Statement sqlStatement = connection.createStatement();
            ResultSet resultSet = sqlStatement.executeQuery(sql);
        )
        {
            // get to the first (and only) returned record (row)
            resultSet.next();

            // get the results of the first column in the row which contains the driver version
            String driverVersionToConnectToTheDatabase = resultSet.getString(1);
            System.out.println("Connection to Database Successful!");
            System.out.println("Driver version used to connect to the database: " + driverVersionToConnectToTheDatabase);
        }
        catch (SQLException sqlException)
        {
            System.err.println("SQLException: failed to query the database");
            System.err.println(sqlException.getMessage());
        }
    }

    public void listAllClasses()
    {
        String sql =
                "SELECT id, code, title, description, max_students\n" +
                "FROM classes;";

        try
        (
                Connection connection = getDatabaseConnection();
                Statement sqlStatement = connection.createStatement();
                ResultSet resultSet = sqlStatement.executeQuery(sql);
        )
        {
            //print table header
            printTableHeader(new String[]{"id", "code", "title", "description", "max_students"});

            // resultSet.next() either
            // advances to the next returned record (row)
            // or
            // returns false if there are no more records
            while (resultSet.next())
            {
                // extract the values from the current row
                int id = resultSet.getInt("id");
                String code = resultSet.getString("code");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                int maxStudents = resultSet.getInt("max_students");

                // print the results of the current row
                System.out.printf("| %d | %s | %s | %s | %d |%n", id, code, title, description, maxStudents);
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the classes table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }
    }

    public void addNewClass(Class newClass)
    {
        String sql =
                "INSERT INTO classes (code, title, description, max_students)\n" +
                "VALUES (?, ?, ?, ?);";

        try
        (
            Connection connection = getDatabaseConnection();
            PreparedStatement sqlStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        )
        {
            sqlStatement.setString(1, newClass.getCode());
            sqlStatement.setString(2, newClass.getTitle());
            sqlStatement.setString(3, newClass.getDescription());
            sqlStatement.setInt(4, newClass.getMaxStudents());

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);

            if (numberOfRowsAffected > 0)
            {
                ResultSet resultSet = sqlStatement.getGeneratedKeys();

                while (resultSet.next())
                {
                    // "last_insert_rowid()" is the column name that contains the id of the last inserted row
                    // alternatively, we could have used resultSet.getInt(1); to get the id of the first column returned
                    int generatedIdForTheNewlyInsertedClass = resultSet.getInt("last_insert_rowid()");
                    System.out.println("SUCCESSFULLY inserted a new class with id = " + generatedIdForTheNewlyInsertedClass);

                    // this can be useful if we need to make additional processing on the newClass object
                    newClass.setId(generatedIdForTheNewlyInsertedClass);
                }

                resultSet.close();
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to insert into the classes table");
            System.out.println(sqlException.getMessage());
        }
    }

    public void updateExistingClassInformation(Class classToUpdate)
    {
        String sql =
                "UPDATE classes\n" +
                "SET code = ?, title = ?, description = ?, max_students = ?\n" +
                "WHERE id = ?;";

        try
        (
            Connection connection = getDatabaseConnection();
            PreparedStatement sqlStatement = connection.prepareStatement(sql);
        )
        {
            sqlStatement.setString(1, classToUpdate.getCode());
            sqlStatement.setString(2, classToUpdate.getTitle());
            sqlStatement.setString(3, classToUpdate.getDescription());
            sqlStatement.setInt(4, classToUpdate.getMaxStudents());
            sqlStatement.setInt(5, classToUpdate.getId());

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);

            if (numberOfRowsAffected > 0)
            {
                System.out.println("SUCCESSFULLY updated the class with id = " + classToUpdate.getId());
            }
            else
            {
                System.out.println("!!! WARNING: failed to update the class with id = " + classToUpdate.getId());
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to update the class with id = " + classToUpdate.getId());
            System.out.println(sqlException.getMessage());
        }
    }

    public void deleteExistingClass(int idOfClassToDelete)
    {
        String sql =
                "DELETE FROM classes\n" +
                "WHERE id = ?;";

        try
        (
            Connection connection = getDatabaseConnection();
            PreparedStatement sqlStatement = connection.prepareStatement(sql);
        )
        {
            sqlStatement.setInt(1, idOfClassToDelete);

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);

            if (numberOfRowsAffected > 0)
            {
                System.out.println("SUCCESSFULLY deleted the class with id = " + idOfClassToDelete);
            }
            else
            {
                System.out.println("!!! WARNING: failed to delete the class with id = " + idOfClassToDelete);
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to delete the class with id = " + idOfClassToDelete);
            System.out.println(sqlException.getMessage());
        }
    }

    public void listAllStudents()
    {
        String sql =
                "SELECT id, first_name, last_name, birth_date\n" +
                "FROM students;";

        try
        (
            Connection connection = getDatabaseConnection();
            Statement sqlStatement = connection.createStatement();
            ResultSet resultSet = sqlStatement.executeQuery(sql);
        )
        {
            printTableHeader(new String[]{"id", "first_name", "last_name", "birth_date"});

            while (resultSet.next())
            {
                int id = resultSet.getInt("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");

                // the resultSet.getDate() does not work in this case, so we're using the getString() method instead
                String birthDate = resultSet.getString("birth_date");

                // TODO: add your code here
                System.out.printf("| %d | %s | %s | %s |%n", id, firstName, lastName, birthDate);
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the students table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }
    }

    public void addNewStudent(Student newStudent)
    {
        // 💡 HINT: in a prepared statement
        // to set the date parameter in the format "YYYY-MM-DD", use the code:
        // sqlStatement.setString(columnIndexTBD, newStudent.getBirthDate().toString());
        //
        // to set the date parameter in the unix format (i.e., milliseconds since 1970), use this code:
        // sqlStatement.setDate(columnIndexTBD, newStudent.getBirthDate());

        // TODO: add your code here
        String sql =
                "INSERT INTO students (id, first_name,last_name,birth_date)\n" +
                        "VALUES (?, ?, ?, ?);";
        String sql1 =
                "SELECT max(students.id)\n" +
                        "FROM students;\n";

        try
                (
                        Connection connection = getDatabaseConnection();
                        PreparedStatement sqlStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        Statement res = connection.createStatement();
                        ResultSet resultSet = res.executeQuery(sql1);
                )
        {
            int id = resultSet.getInt(1)+1;
            newStudent.setId(id);
            sqlStatement.setInt(1, newStudent.getId());
            sqlStatement.setString(2, newStudent.getFirstName());
            sqlStatement.setString(3, newStudent.getLastName());
            sqlStatement.setString(4, newStudent.getBirthDate().toString());

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to insert into the classes table");
            System.out.println(sqlException.getMessage());
        }
    }
    public void UpdateExistingStudentInformation(int studentID){
        int studentIDretry = 0;
        int newid = 0;
        String newname = null;
        boolean working = false;
        boolean uniqueid = false;
        int choice;
        boolean shouldexit = false;
        Scanner inputScanner = new Scanner(System.in);
        String sql =
                "SELECT *\n" +
                        "FROM students\n" +
                        "WHERE students.id";
        try {
            Connection connection = getDatabaseConnection();
            Statement res = connection.createStatement();
            ResultSet ResultSet0 = res.executeQuery(sql + "=" + studentID + ";");
            if (!ResultSet0.next()) {
                System.out.println("Invalid ID number, try entering a valid student ID here: ");
                studentIDretry = inputScanner.nextInt();
                UpdateExistingStudentInformation(studentIDretry);
            }
                while (!shouldexit) {
                    System.out.println("What would you like to alter?");
                    System.out.println(" 0 - Student ID (note: must be unique within the table).");
                    System.out.println(" 1 - Student First Name");
                    System.out.println(" 2 - Student Last Name");
                    System.out.println(" 3 - Student Date of Birth");

                        try {
                            choice = Integer.parseInt(inputScanner.nextLine());
                        } catch (Exception e) {
                            System.out.println("Invalid choice, expected an integer value. Please enter a number such as 0, 1, 2, or 3.");
                            continue;
                        }
                        shouldexit = true;
                        switch (choice) {
                            case 0:
                                while (!uniqueid) {
                                    try {
                                        System.out.println("Enter a new, unique integer student ID.");
                                        newid = inputScanner.nextInt();
                                        String sql1 = "SELECT students.id\n" +
                                                "FROM students\n" +
                                                "WHERE students.id";
                                        Statement sqlStatement = connection.createStatement();
                                        ResultSet resultSet2 = sqlStatement.executeQuery(sql1 + "=" + newid);
                                        if (!resultSet2.next()) {
                                            uniqueid = true;
                                            String sql2 = "UPDATE students\n" +
                                                    "SET id = ?\n" +
                                                    "WHERE id = ?";
                                            PreparedStatement sqlStatement2 = connection.prepareStatement(sql2);
                                            sqlStatement2.setInt(1,newid);
                                            sqlStatement2.setInt(2,studentID);
                                            sqlStatement2.execute();
                                            break;
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Invalid input, please try again.");
                                    }
                                }
                            case 1:
                                while(!working){
                                    try{
                                        System.out.println("Please enter the student's new first name: ");
                                        newname = inputScanner.next();
                                        String sql3 = "UPDATE students\n" +
                                                "SET first_name = ?\n" +
                                                "WHERE id = ?";
                                        PreparedStatement sqlStatement2 = connection.prepareStatement(sql3);
                                        sqlStatement2.setString(1,newname);
                                        sqlStatement2.setInt(2,studentID);
                                        sqlStatement2.execute();
                                        working = true;
                                    }
                                    catch(Exception e){
                                        e.getMessage();
                                        e.printStackTrace();
                                        System.out.println("Invalid input, please try again.");
                                        System.exit(1);
                                    }
                                }
                        }
                    }
            }
            catch (SQLException sqlException)
            {
                System.out.println("!!! SQLException: failed to alter Students table");
                System.out.println(sqlException.getMessage());
            }
        }

    public void listAllRegisteredStudents()
    {
        String sql =
                "SELECT students.id, students.first_name || ' ' || students.last_name AS student_full_name, classes.code, classes.title\n" +
                "FROM students\n" +
                "INNER JOIN registered_students ON students.id = registered_students.student_id\n" +
                "INNER JOIN classes ON classes.id = registered_students.class_id\n" +
                "ORDER BY students.last_name, students.first_name, classes.code;";

        try
        (
            Connection connection = getDatabaseConnection();
            Statement sqlStatement = connection.createStatement();
            ResultSet resultSet = sqlStatement.executeQuery(sql);
        )
        {
            printTableHeader(new String[]{"students.id", "student_full_name", "classes.code", "classes.title"});

            while (resultSet.next())
            {
                int id = resultSet.getInt("id");
                String studentFullName = resultSet.getString("student_full_name");
                String code = resultSet.getString("code");
                String title = resultSet.getString("title");

                System.out.printf("| %d | %s | %s | %s |%n", id, studentFullName, code, title);
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the registered_students table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }
    }

    private void printTableHeader(String[] listOfColumnNames)
    {
        System.out.print("| ");
        for (String columnName : listOfColumnNames)
        {
            System.out.print(columnName + " | ");
        }
        System.out.println();
        System.out.println(Utils.characterRepeat('-', 80));
    }
}
