package cs208;
import java.sql.*;
import java.util.Locale;
import java.util.Scanner;

import org.sqlite.SQLiteConfig;

import javax.print.attribute.standard.DateTimeAtCreation;
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
    public Class oldClassInfo;

    public Database(String sqliteFileName) {
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

    public void listAllClasses() {
        String sql =
                "SELECT id, code, title, description, max_students\n" +
                        "FROM classes;";

        try
                (
                        Connection connection = getDatabaseConnection();
                        Statement sqlStatement = connection.createStatement();
                        ResultSet resultSet = sqlStatement.executeQuery(sql);
                ) {
            //print table header
            printTableHeader(new String[]{"id", "code", "title", "description", "max_students"});

            // resultSet.next() either
            // advances to the next returned record (row)
            // or
            // returns false if there are no more records
            while (resultSet.next()) {
                // extract the values from the current row
                int id = resultSet.getInt("id");
                String code = resultSet.getString("code");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                int maxStudents = resultSet.getInt("max_students");

                // print the results of the current row
                System.out.printf("| %d | %s | %s | %s | %d |%n", id, code, title, description, maxStudents);
            }
        } catch (SQLException sqlException) {
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
    public Class getOldClassInfo(int classID){
        String retVal = "SELECT *\n" +
                "FROM classes\n" +
                "WHERE id = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement ret = connection.prepareStatement(retVal);
            ret.setInt(1, classID);
            ResultSet resultSet = ret.executeQuery();
            oldClassInfo = new Class(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getInt(5));
            connection.close();

        }
        catch(SQLException sqlException){
            System.out.println("!!! SQLException: failed to update the class with id = " + classID);
            System.out.println(sqlException.getMessage());
        }
        return oldClassInfo;
    }

    public void updateExistingClassInformation(Class classToUpdate)
    {
        String sql =
                "UPDATE classes\n" +
                "SET code = ?, title = ?, description = ?, max_students = ?\n" +
                "WHERE id = ?;";

        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement sqlStatement = connection.prepareStatement(sql);
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
        Date birthdate = null;
        boolean working = false;
        boolean uniqueid = false;
        int choice;
        boolean shouldexit = false;
        Scanner inputScannersub = new Scanner(System.in);
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
                studentIDretry = inputScannersub.nextInt();
                connection.close();
                UpdateExistingStudentInformation(studentIDretry);
            }
                while (!shouldexit) {
                    System.out.println("What would you like to alter?");
                    System.out.println(" 0 - Student ID (note: must be unique within the table).");
                    System.out.println(" 1 - Student First Name");
                    System.out.println(" 2 - Student Last Name");
                    System.out.println(" 3 - Student Date of Birth");

                        try {
                            choice = Integer.parseInt(inputScannersub.nextLine());
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
                                        newid = inputScannersub.nextInt();
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
                                        }
                                        connection.close();
                                    } catch (Exception e) {
                                        System.out.println("Invalid input, please try again.");
                                        System.out.println(e.getMessage());
                                    }
                                }
                                break;
                            case 1:
                                while(!working){
                                    try{
                                        System.out.println("Please enter the student's new first name: ");
                                        newname = inputScannersub.next();
                                        String sql3 = "UPDATE students\n" +
                                                "SET first_name = ?\n" +
                                                "WHERE id = ?";
                                        PreparedStatement sqlStatement2 = connection.prepareStatement(sql3);
                                        sqlStatement2.setString(1,newname);
                                        sqlStatement2.setInt(2,studentID);
                                        sqlStatement2.execute();
                                        working = true;
                                        connection.close();
                                    }
                                    catch(Exception e){
                                        System.out.println("Invalid input, please try again.");
                                        System.out.println(e.getMessage());
                                    }
                                }
                                break;
                            case 2:
                                while(!working){
                                    try{
                                        System.out.println("Please enter the student's new last name: ");
                                        newname = inputScannersub.nextLine();

                                        String sql4 = "UPDATE students\n" +
                                                "SET last_name = ?\n" +
                                                "WHERE id = ?";
                                        PreparedStatement sqlStatement2 = connection.prepareStatement(sql4);
                                        sqlStatement2.setString(1,newname);
                                        sqlStatement2.setInt(2,studentID);
                                        sqlStatement2.execute();
                                        working = true;
                                        connection.close();
                                    }
                                    catch(Exception e){
                                        System.out.println("Invalid input, please try again.");
                                        System.out.println(e.getMessage());
                                    }
                                }
                                break;
                            case 3:
                                while(!working){
                                    try{
                                        System.out.println("Please enter the student's new Date of Birth in YYYY-MM-DD format: ");
                                        birthdate = Date.valueOf(inputScannersub.nextLine());
                                        String sql5 = "UPDATE students\n" +
                                                "SET birth_date = ?\n" +
                                                "WHERE id = ?";
                                        PreparedStatement sqlStatement2 = connection.prepareStatement(sql5);
                                        sqlStatement2.setString(1,birthdate.toString());
                                        sqlStatement2.setInt(2,studentID);
                                        sqlStatement2.execute();
                                        working = true;
                                        connection.close();
                                    }
                                    catch(Exception e){
                                        System.out.println("Invalid input, please try again.");
                                        System.out.println(e.getMessage());
                                    }
                                }
                                break;
                            default:
                                System.out.println("Invalid input, please try again.");
                        }
                    }
            }
            catch (SQLException sqlException)
            {
                System.out.println("!!! SQLException: failed to alter Students table");
                System.out.println(sqlException.getMessage());
            }
    }
    public void UpdateExistingStudentInformation(String DOB){
        boolean shouldexit = false;
        int choice;
        boolean uniqueid = false;
        int newid;
        boolean working = false;
        Date birthdate = null;
        String newname = null;
        String studentDOBretry = null;
        Scanner inputScannersub = new Scanner(System.in);
        String sql =
                "SELECT *\n" +
                        "FROM students\n" +
                        "WHERE students.birth_date = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement res = connection.prepareStatement(sql);
            res.setString(1,DOB);
            if (!res.executeQuery().next()) {
                System.out.println("Invalid Date of Birth, try entering a valid student Date of Birth here: ");
                studentDOBretry = inputScannersub.next();
                connection.close();
                UpdateExistingStudentInformation(studentDOBretry);
            }
            while (!shouldexit) {
                System.out.println("What would you like to alter?");
                System.out.println(" 0 - Student ID (note: must be unique within the table).");
                System.out.println(" 1 - Student First Name");
                System.out.println(" 2 - Student Last Name");
                System.out.println(" 3 - Student Date of Birth");

                try {
                    choice = Integer.parseInt(inputScannersub.nextLine());
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
                                newid = inputScannersub.nextInt();
                                String sql1 = "SELECT students.id\n" +
                                        "FROM students\n" +
                                        "WHERE students.id";
                                Statement sqlStatement = connection.createStatement();
                                ResultSet resultSet2 = sqlStatement.executeQuery(sql1 + "=" + newid);
                                if (!resultSet2.next()) {
                                    uniqueid = true;
                                    String sql2 = "UPDATE students\n" +
                                            "SET id = ?\n" +
                                            "WHERE birth_date = ?";
                                    PreparedStatement sqlStatement2 = connection.prepareStatement(sql2);
                                    sqlStatement2.setInt(1,newid);
                                    sqlStatement2.setString(2,DOB);
                                    sqlStatement2.execute();
                                }
                                connection.close();
                            } catch (Exception e) {
                                System.out.println("Invalid input, please try again.");
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    case 1:
                        while(!working){
                            try{
                                System.out.println("Please enter the student's new first name: ");
                                newname = inputScannersub.next();
                                String sql3 = "UPDATE students\n" +
                                        "SET first_name = ?\n" +
                                        "WHERE birth_date = ?";
                                PreparedStatement sqlStatement2 = connection.prepareStatement(sql3);
                                sqlStatement2.setString(1,newname);
                                sqlStatement2.setString(2,DOB);
                                sqlStatement2.execute();
                                working = true;
                                connection.close();
                            }
                            catch(Exception e){
                                System.out.println("Invalid input, please try again.");
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    case 2:
                        while(!working){
                            try{
                                System.out.println("Please enter the student's new last name: ");
                                newname = inputScannersub.nextLine();

                                String sql4 = "UPDATE students\n" +
                                        "SET last_name = ?\n" +
                                        "WHERE birth_date = ?";
                                PreparedStatement sqlStatement2 = connection.prepareStatement(sql4);
                                sqlStatement2.setString(1,newname);
                                sqlStatement2.setString(2,DOB);
                                sqlStatement2.execute();
                                working = true;
                                connection.close();
                            }
                            catch(Exception e){
                                System.out.println("Invalid input, please try again.");
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    case 3:
                        while(!working){
                            try{
                                System.out.println("Please enter the student's new Date of Birth in YYYY-MM-DD format: ");
                                birthdate = Date.valueOf(inputScannersub.nextLine());
                                String sql5 = "UPDATE students\n" +
                                        "SET birth_date = ?\n" +
                                        "WHERE birth_date = ?";
                                PreparedStatement sqlStatement2 = connection.prepareStatement(sql5);
                                sqlStatement2.setString(1,birthdate.toString());
                                sqlStatement2.setString(2,DOB);
                                sqlStatement2.execute();
                                working = true;
                                connection.close();
                            }
                            catch(Exception e){
                                System.out.println("Invalid input, please try again.");
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    default:
                        System.out.println("Invalid input, please try again.");
                }
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to alter Students table");
            System.out.println(sqlException.getMessage());
        }
    }
    public void UpdateExistingStudentInformation(String First, String Last){
        boolean shouldexit = false;
        int choice;
        boolean uniqueid = false;
        int newid;
        boolean working = false;
        Date birthdate = null;
        String newname = null;
        String studentNameretry = null;
        Scanner inputScannersub = new Scanner(System.in);
        String sql =
                "SELECT *\n" +
                        "FROM students\n" +
                        "WHERE students.first_name = ? and students.last_name = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement res = connection.prepareStatement(sql);
            res.setString(1,First);
            res.setString(2, Last);
            if (!res.executeQuery().next()) {
                System.out.println("Invalid name, try entering a valid student name here: ");
                String fix = inputScannersub.nextLine();
                String[] fixsplit = fix.split(" ");
                First = fixsplit[0];
                First = First.replace(" ", "");
                Last = fixsplit[1];
                Last = Last.replace(" ", "");
                connection.close();
                UpdateExistingStudentInformation(First, Last);
            }
            while (!shouldexit) {
                System.out.println("What would you like to alter?");
                System.out.println(" 0 - Student ID (note: must be unique within the table).");
                System.out.println(" 1 - Student First Name");
                System.out.println(" 2 - Student Last Name");
                System.out.println(" 3 - Student Date of Birth");

                try {
                    choice = Integer.parseInt(inputScannersub.nextLine());
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
                                newid = inputScannersub.nextInt();
                                uniqueid = true;
                                String sql2 = "UPDATE students\n" +
                                        "SET id = ?\n" +
                                        "WHERE first_name = ? and last_name = ?";
                                PreparedStatement sqlStatement2 = connection.prepareStatement(sql2);
                                sqlStatement2.setInt(1,newid);
                                sqlStatement2.setString(2,First);
                                sqlStatement2.setString(3,Last);
                                sqlStatement2.execute();
                                connection.close();
                            } catch (Exception e) {
                                System.out.println("Invalid input, please try again.");
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    case 1:
                        while(!working){
                            try{
                                System.out.println("Please enter the student's new first name: ");
                                newname = inputScannersub.next();
                                String sql3 = "UPDATE students\n" +
                                        "SET first_name = ?\n" +
                                        "WHERE first_name = ? and last_name = ?";
                                PreparedStatement sqlStatement2 = connection.prepareStatement(sql3);
                                sqlStatement2.setString(1,newname);
                                sqlStatement2.setString(2,First);
                                sqlStatement2.setString(3,Last);
                                sqlStatement2.execute();
                                working = true;
                                connection.close();
                            }
                            catch(Exception e){
                                System.out.println("Invalid input, please try again.");
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    case 2:
                        while(!working){
                            try{
                                System.out.println("Please enter the student's new last name: ");
                                newname = inputScannersub.nextLine();
                                String sql4 = "UPDATE students\n" +
                                        "SET last_name = ?\n" +
                                        "WHERE first_name = ? and last_name = ?";
                                PreparedStatement sqlStatement2 = connection.prepareStatement(sql4);
                                sqlStatement2.setString(1,newname);
                                sqlStatement2.setString(2,First);
                                sqlStatement2.setString(3,Last);
                                sqlStatement2.execute();
                                working = true;
                                connection.close();
                            }
                            catch(Exception e){
                                System.out.println("Invalid input, please try again.");
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    case 3:
                        while(!working){
                            try{
                                System.out.println("Please enter the student's new Date of Birth in YYYY-MM-DD format: ");
                                birthdate = Date.valueOf(inputScannersub.nextLine());
                                String sql5 = "UPDATE students\n" +
                                        "SET birth_date = ?\n" +
                                        "WHERE first_name = ? and last_name = ?";
                                PreparedStatement sqlStatement2 = connection.prepareStatement(sql5);
                                sqlStatement2.setString(1,birthdate.toString());
                                sqlStatement2.setString(2,First);
                                sqlStatement2.setString(3,Last);
                                sqlStatement2.execute();
                                working = true;
                                connection.close();
                            }
                            catch(Exception e){
                                System.out.println("Invalid input, please try again.");
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    default:
                        System.out.println("Invalid input, please try again.");
                }
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to alter Students table");
            System.out.println(sqlException.getMessage());
        }
    }
    public void DeleteExistingStudent(int studentID){
            int studentIDretry = 0;
            Scanner inputScannersub = new Scanner(System.in);
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
                    studentIDretry = inputScannersub.nextInt();
                    DeleteExistingStudent(studentIDretry);
                }
                    String sql1 = "DELETE\n" +
                            "FROM students\n" +
                            "WHERE students.id = ?";
                    String sql2 = "DELETE\n" +
                            " FROM registered_students\n" +
                            "WHERE student_id = ?";
                    try {
                        PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
                        preparedStatement2.setInt(1, studentID);
                        preparedStatement2.execute();
                        PreparedStatement preparedStatement = connection.prepareStatement(sql1);
                        preparedStatement.setInt(1, studentID);
                        preparedStatement.execute();
                        connection.close();
                    } catch (SQLException sqlException) {
                        System.out.println("!!! SQLException: failed to alter Students table");
                        System.out.println(sqlException.getMessage());
                    }
            } catch (Exception e) {
                System.out.println("Invalid input, try again.");
            }
        }
    public void DeleteExistingStudent(String First, String Last) {
        String studentNameretry = null;
        Scanner inputScannersub = new Scanner(System.in);
        String sql =
                "SELECT *\n" +
                        "FROM students\n" +
                        "WHERE students.first_name = ? and students.last_name = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement res = connection.prepareStatement(sql);
            res.setString(1, First);
            res.setString(2, Last);
            if (!res.executeQuery().next()) {
                System.out.println("Invalid name, try entering a valid student name here: ");
                studentNameretry = inputScannersub.next("First Last");
                connection.close();
                UpdateExistingStudentInformation(studentNameretry);
            }
            String sql1 = "DELETE\n" +
                    "FROM students\n" +
                    "WHERE students.first_name = ?  and students.last_name = ?";
            String sql2 = "SELECT id" +
                    " FROM students\n" +
                    "WHERE first_name = ? and last_name = ?";
            String sql3 = "DELETE\n" +
                    "FROM registered_students\n" +
                    " WHERE registered_students.student_id = ?";
                try {
                    PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
                    preparedStatement2.setString(1, First);
                    preparedStatement2.setString(2, Last);
                    ResultSet res2 = preparedStatement2.executeQuery();
                    PreparedStatement preparedStatement3 = connection.prepareStatement(sql3);
                    System.out.println(res2.getString(1));
                    preparedStatement3.setInt(1,res2.getInt(1));
                    preparedStatement3.execute();
                    PreparedStatement preparedStatement = connection.prepareStatement(sql1);
                    preparedStatement.setString(1, First);
                    preparedStatement.setString(2, Last);
                    preparedStatement.execute();
                    connection.close();
                } catch (SQLException sqlException) {
                    System.out.println("!!! SQLException: failed to alter Students table");
                    System.out.println(sqlException.getMessage());
                }
        } catch (Exception e) {
            System.out.println("Invalid input, please try again");
            System.out.println(e.getMessage());
        }
    }
    public void DeleteExistingStudent(String DOB) {
        String studentDOBretry = null;
        Scanner inputScannersub = new Scanner(System.in);
        String sql =
                "SELECT *\n" +
                        "FROM students\n" +
                        "WHERE students.birth_date = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement res = connection.prepareStatement(sql);
            res.setString(1, DOB);
            if (!res.executeQuery().next()) {
                System.out.println("Invalid Date of Birth, try entering a valid student Date of Birth here: ");
                studentDOBretry = inputScannersub.next();
                connection.close();
                UpdateExistingStudentInformation(studentDOBretry);
            }
            String sql1 = "DELETE\n" +
                    "FROM students\n" +
                    "WHERE students.birth_date = ?";
            String sql2 = "SELECT id" +
                    " FROM students\n" +
                    "WHERE birth_date = ?";
            String sql3 = "DELETE\n" +
                    "FROM registered_students\n" +
                    " WHERE registered_students.student_id = ?";
            try {
                PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
                preparedStatement2.setString(1, DOB);
                ResultSet res2 = preparedStatement2.executeQuery();
                PreparedStatement preparedStatement3 = connection.prepareStatement(sql3);
                System.out.println(res2.getString(1));
                preparedStatement3.setInt(1,res2.getInt(1));
                preparedStatement3.execute();
                PreparedStatement preparedStatement = connection.prepareStatement(sql1);
                preparedStatement.setString(1, DOB);
                preparedStatement.execute();
                connection.close();
            } catch (SQLException sqlException) {
                System.out.println("!!! SQLException: failed to alter Students table");
                System.out.println(sqlException.getMessage());
            }
        }
            catch(Exception e){
            System.out.println("Invalid input, please try again.");
                System.out.println(e.getMessage());
            }
    }

    public int classSearch(String classCode) {
        int classid = 0;
        String classCodeRetry = null;
        Scanner inputScannersub = new Scanner(System.in);
        String sql = "SELECT id\n" +
                "FROM classes\n" +
                "WHERE code = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, classCode);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                System.out.println("No such class, please enter a valid class code here: \n");
                classCodeRetry = inputScannersub.nextLine();
                connection.close();
                classSearch(classCodeRetry);
            }
            classid = resultSet.getInt(1);
            connection.close();
        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
        }
        return classid;
    }
    public String classSearch(int classID) {
        String classCode = null;
        int classIDretry = 0;
        Scanner inputScannersub = new Scanner(System.in);
        String sql = "SELECT code\n" +
                "FROM classes\n" +
                "WHERE id = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, classID);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                System.out.println("No such class, please enter a valid class code here: \n");
                classIDretry = inputScannersub.nextInt();
                classSearch(classIDretry);
            }
            classCode = resultSet.getString(1);
            connection.close();
        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
        }
        return classCode;
    }
    public void AddStudentToClass(int StudentID, int classID) {
        Scanner scannerSub = new Scanner(System.in);
        String sql = "INSERT INTO registered_students (class_id, student_id, signup_date)\n" +
                "VALUES (?,?,?)";
        String sql1 = "SELECT id\n" +
                "FROM students\n" +
                "WHERE id = ?";
        String sql2 = "SELECT id\n" +
                "FROM classes\n" +
                "WHERE id = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setInt(1, StudentID);
            ResultSet res = preparedStatement1.executeQuery();
            if (!res.next()) {
                System.out.println("No such Student ID, please try again here: \n");
                StudentID = scannerSub.nextInt();
                connection.close();
                AddStudentToClass(StudentID, classID);
            }
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            preparedStatement2.setInt(1, classID);
            ResultSet res2 = preparedStatement2.executeQuery();
            if (!res2.next()) {
                System.out.println("No such class ID, please try again here: \n");
                classID = scannerSub.nextInt();
                connection.close();
                AddStudentToClass(StudentID, classID);
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, classID);
            preparedStatement.setInt(2, StudentID);
            java.util.Date utilDate = new java.util.Date();
            java.sql.Date timestamp = new java.sql.Date(utilDate.getTime());
            preparedStatement.setDate(3, timestamp);
            preparedStatement.execute();
            connection.close();
        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }
    public void AddStudentToClass(String First, String Last, int classID) {
        Scanner scannerSub = new Scanner(System.in);
        int StudentID;
        String sql1 = "SELECT id\n" +
                "FROM students\n" +
                "WHERE first_name = ? and last_name = ?";
        String sql2 = "SELECT id\n" +
                "FROM classes\n" +
                "WHERE id = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setString(1, First);
            preparedStatement1.setString(2, Last);
            ResultSet res = preparedStatement1.executeQuery();
            if (!res.next()) {
                System.out.println("Invalid name, try entering a valid student name here: ");
                String fix = scannerSub.nextLine();
                String[] fixsplit = fix.split(" ");
                First = fixsplit[0];
                First = First.replace(" ", "");
                Last = fixsplit[1];
                Last = Last.replace(" ", "");
                connection.close();
                AddStudentToClass(First, Last, classID);
            }
            StudentID = res.getInt(1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            preparedStatement2.setInt(1, classID);
            ResultSet res2 = preparedStatement2.executeQuery();
            if (!res2.next()) {
                System.out.println("No such class ID, please try again here: \n");
                classID = scannerSub.nextInt();
                connection.close();
                AddStudentToClass(StudentID, classID);
            }
            classID = res2.getInt(1);
            connection.close();
            AddStudentToClass(StudentID, classID);
        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }
    public void AddStudentToClass(String DOB, int classID) {
        Scanner scannerSub = new Scanner(System.in);
        int StudentID;
        String sql1 = "SELECT id\n" +
                "FROM students\n" +
                "WHERE birth_date = ?";
        String sql2 = "SELECT id\n" +
                "FROM classes\n" +
                "WHERE id = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setString(1, DOB);
            ResultSet res = preparedStatement1.executeQuery();
            if (!res.next()) {
                System.out.println("Invalid date of birth, try entering a valid student name here: ");
                DOB = scannerSub.nextLine();
                connection.close();
                AddStudentToClass(DOB, classID);
            }
            StudentID = res.getInt(1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            preparedStatement2.setInt(1, classID);
            ResultSet res2 = preparedStatement2.executeQuery();
            if (!res2.next()) {
                System.out.println("No such class ID, please try again here: \n");
                classID = scannerSub.nextInt();
                connection.close();
                AddStudentToClass(StudentID, classID);
            }
            classID = res2.getInt(1);
            connection.close();
            AddStudentToClass(StudentID, classID);
            connection.close();
        } catch (SQLException sqlException) {
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
    public void RemoveStudentFromClass(int StudentID, int classID){
        Scanner scannerSub = new Scanner(System.in);
        String sql = "DELETE \n" +
                "FROM registered_students\n" +
                "WHERE student_id = ? and class_id = ?";
        String sql1 = "SELECT id\n" +
                "FROM students\n" +
                "WHERE id = ?";
        String sql2 = "SELECT id\n" +
                "FROM classes\n" +
                "WHERE id = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setInt(1, StudentID);
            ResultSet res = preparedStatement1.executeQuery();
            if (!res.next()) {
                System.out.println("No such Student ID, please try again here: \n");
                StudentID = scannerSub.nextInt();
                connection.close();
                RemoveStudentFromClass(StudentID, classID);
            }
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            preparedStatement2.setInt(1, classID);
            ResultSet res2 = preparedStatement2.executeQuery();
            if (!res2.next()) {
                System.out.println("No such class ID, please try again here: \n");
                classID = scannerSub.nextInt();
                connection.close();
                RemoveStudentFromClass(StudentID, classID);
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, StudentID);
            preparedStatement.setInt(2, classID);
            preparedStatement.execute();
            connection.close();
        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }
    public void RemoveStudentFromClass(String First, String Last, int classID) {
        Scanner scannerSub = new Scanner(System.in);
        int StudentID;
        String sql1 = "SELECT id\n" +
                "FROM students\n" +
                "WHERE first_name = ? and last_name = ?";
        String sql2 = "SELECT id\n" +
                "FROM classes\n" +
                "WHERE id = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setString(1, First);
            preparedStatement1.setString(2, Last);
            ResultSet res = preparedStatement1.executeQuery();
            if (!res.next()) {
                System.out.println("Invalid name, try entering a valid student name here: ");
                String fix = scannerSub.nextLine();
                String[] fixsplit = fix.split(" ");
                First = fixsplit[0];
                First = First.replace(" ", "");
                Last = fixsplit[1];
                Last = Last.replace(" ", "");
                connection.close();
                RemoveStudentFromClass(First, Last, classID);
            }
            StudentID = res.getInt(1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            preparedStatement2.setInt(1, classID);
            ResultSet res2 = preparedStatement2.executeQuery();
            if (!res2.next()) {
                System.out.println("No such class ID, please try again here: \n");
                classID = scannerSub.nextInt();
                connection.close();
                RemoveStudentFromClass(StudentID, classID);
            }
            classID = res2.getInt(1);
            connection.close();
            RemoveStudentFromClass(StudentID, classID);
        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }
    public void RemoveStudentFromClass(String DOB, int classID) {
        Scanner scannerSub = new Scanner(System.in);
        int StudentID;
        String sql1 = "SELECT id\n" +
                "FROM students\n" +
                "WHERE birth_date = ?";
        String sql2 = "SELECT id\n" +
                "FROM classes\n" +
                "WHERE id = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setString(1, DOB);
            ResultSet res = preparedStatement1.executeQuery();
            if (!res.next()) {
                System.out.println("Invalid date of birth, try entering a valid student name here: ");
                DOB = scannerSub.nextLine();
                connection.close();
                RemoveStudentFromClass(DOB, classID);
            }
            StudentID = res.getInt(1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            preparedStatement2.setInt(1, classID);
            ResultSet res2 = preparedStatement2.executeQuery();
            if (!res2.next()) {
                System.out.println("No such class ID, please try again here: \n");
                classID = scannerSub.nextInt();
                connection.close();
                RemoveStudentFromClass(StudentID, classID);
            }
            classID = res2.getInt(1);
            connection.close();
            RemoveStudentFromClass(StudentID, classID);
            connection.close();
        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }
    public void showAllStudentsInClass(String classCode){
        Scanner scannersub = new Scanner(System.in);
        String sql =
                "SELECT *\n" +
                        "FROM(\n"+
                "SELECT students.id, students.first_name || ' ' || students.last_name AS student_full_name, classes.code, classes.title\n" +
                "FROM students\n" +
                "INNER JOIN registered_students ON students.id = registered_students.student_id\n" +
                "INNER JOIN classes ON classes.id = registered_students.class_id\n" +
                "ORDER BY student_id)\n" +
                        "WHERE code = ?;" ;
        try{
            Connection connection = getDatabaseConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, classCode);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(!resultSet.next()){
                System.out.println("Either no students are enrolled in this class, or this class ID does not exist, please try again by entering a new classs ID here: \n");
                classCode = scannersub.nextLine();
                showAllStudentsInClass(classCode);
            }
            printTableHeader(new String[]{"students.id", "student_full_name", "classes.code", "classes.title"});

            while (resultSet.next())
            {
                int id = resultSet.getInt("id");
                String studentFullName = resultSet.getString("student_full_name");
                String code = resultSet.getString("code");
                String title = resultSet.getString("title");

                System.out.printf("| %d | %s | %s | %s |%n", id, studentFullName, code, title);
            }
        connection.close();
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the registered_students table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }
    }
    public void showAllStudentClasses(String First, String Last){
        Scanner scannersub = new Scanner(System.in);
        String sql =
                "SELECT ALL *\n" +
                        "FROM(\n"+
                        "SELECT students.id, students.first_name || ' ' || students.last_name AS student_full_name, classes.code, classes.title\n" +
                        "FROM students\n" +
                        "INNER JOIN registered_students ON students.id = registered_students.student_id\n" +
                        "INNER JOIN classes ON classes.id = registered_students.class_id\n" +
                        "ORDER BY class_id)\n" +
                        "WHERE student_full_name = ?;" ;
        try{
            Connection connection = getDatabaseConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, First +" "+ Last);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(!resultSet.next()){
                System.out.println("Either this student is not enrolled in any classes or this student does not exist, please try again by re-entering the student's first and last name separated by a space here:  \n");
                String fix = scannersub.nextLine();
                String[] fixsplit = fix.split(" ");
                First = fixsplit[0];
                First = First.replace(" ", "");
                Last = fixsplit[1];
                Last = Last.replace(" ", "");
                showAllStudentClasses(First, Last);
            }
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, First + " " + Last);
            resultSet = preparedStatement.executeQuery();
            printTableHeader(new String[]{"students.id", "student_full_name", "classes.code", "classes.title"});

            while (resultSet.next())
            {
                int id = resultSet.getInt("id");
                String studentFullName = resultSet.getString("student_full_name");
                String code = resultSet.getString("code");
                String title = resultSet.getString("title");

                System.out.printf("| %d | %s | %s | %s |%n", id, studentFullName, code, title);
            }
            connection.close();
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the registered_students table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }
    }
    public void showAllStudentClasses(int studentID){
        Scanner scannersub = new Scanner(System.in);
        String First = null;
        String Last = null;
        Boolean proceed = false;
        ResultSet resultSet = null;
        String sql =
                "SELECT ALL *\n" +
                        "FROM(\n"+
                        "SELECT students.id, students.first_name || ' ' || students.last_name AS student_full_name, classes.code, classes.title\n" +
                        "FROM students\n" +
                        "INNER JOIN registered_students ON students.id = registered_students.student_id\n" +
                        "INNER JOIN classes ON classes.id = registered_students.class_id\n" +
                        "ORDER BY class_id)\n" +
                        "WHERE student_full_name = ?;" ;
        String sql1 = "SELECT first_name, last_name\n" +
                "FROM students\n" +
                "WHERE students.id = ?";
        try{
            Connection connection = getDatabaseConnection();
            while(!proceed) {
                PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
                preparedStatement1.setInt(1, studentID);
                ResultSet resultSet1 = preparedStatement1.executeQuery();
                if (!resultSet1.next()) {
                    System.out.println("Either this student is not enrolled in any classes or this student does not exist, please try again by re-entering the student's ID here:  \n");
                    studentID = scannersub.nextInt();
                    showAllStudentClasses(studentID);
                }
                First = resultSet1.getString(1);
                Last = resultSet1.getString(2);
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, First + " " + Last);
                resultSet = preparedStatement.executeQuery();
                if (!resultSet.next()) {
                    System.out.println("Either this student is not enrolled in any classes or this student does not exist, please try again by re-entering the student's ID here:  \n");
                    studentID = scannersub.nextInt();
                }
                proceed = true;
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, First + " " + Last);
            resultSet = preparedStatement.executeQuery();
            printTableHeader(new String[]{"students.id", "student_full_name", "classes.code", "classes.title"});

            while (resultSet.next())
            {
                int id = resultSet.getInt("id");
                String studentFullName = resultSet.getString("student_full_name");
                String code = resultSet.getString("code");
                String title = resultSet.getString("title");

                System.out.printf("| %d | %s | %s | %s |%n", id, studentFullName, code, title);
            }
            connection.close();
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the registered_students table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }
    }
    public void showAllStudentClasses(String DOB){
        Scanner scannersub = new Scanner(System.in);
        String First = null;
        String Last = null;
        Boolean proceed = false;
        ResultSet resultSet = null;
        String sql =
                "SELECT ALL *\n" +
                        "FROM(\n"+
                        "SELECT students.id, students.first_name || ' ' || students.last_name AS student_full_name, classes.code, classes.title\n" +
                        "FROM students\n" +
                        "INNER JOIN registered_students ON students.id = registered_students.student_id\n" +
                        "INNER JOIN classes ON classes.id = registered_students.class_id\n" +
                        "ORDER BY class_id)\n" +
                        "WHERE student_full_name = ?;" ;
        String sql1 = "SELECT first_name, last_name\n" +
                "FROM students\n" +
                "WHERE students.birth_date = ?";
        try{
            Connection connection = getDatabaseConnection();
            while(!proceed) {
                PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
                preparedStatement1.setString(1, DOB);
                ResultSet resultSet1 = preparedStatement1.executeQuery();
                if (!resultSet1.next()) {
                    System.out.println("Either this student is not enrolled in any classes or this student does not exist, please try again by re-entering the student's birth date in YYYY-MM-DD format here:  \n");
                    DOB = scannersub.nextLine();
                    showAllStudentClasses(DOB);
                }
                First = resultSet1.getString(1);
                Last = resultSet1.getString(2);
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, First + " " + Last);
                resultSet = preparedStatement.executeQuery();
                if (!resultSet.next()) {
                    System.out.println("Either this student is not enrolled in any classes or this student does not exist, please try again by re-entering the student's birth date in YYYY-MM-DD format here:  \n");
                    DOB = scannersub.nextLine();
                }
                proceed = true;
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, First + " " + Last);
            resultSet = preparedStatement.executeQuery();
            printTableHeader(new String[]{"students.id", "student_full_name", "classes.code", "classes.title"});

            while (resultSet.next())
            {
                int id = resultSet.getInt("id");
                String studentFullName = resultSet.getString("student_full_name");
                String code = resultSet.getString("code");
                String title = resultSet.getString("title");

                System.out.printf("| %d | %s | %s | %s |%n", id, studentFullName, code, title);
            }
            connection.close();
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
