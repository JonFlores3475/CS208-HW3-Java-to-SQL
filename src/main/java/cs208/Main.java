package cs208;

import java.sql.Date;
import java.util.Scanner;

public class Main {
    private static Database database;
    private static Scanner inputScanner;

    public static void main(String[] args) {
        System.out.println("Starting the School Management System...");

        // TODO: create a SQLite data source in IntelliJ with this file name
        String sqliteFileName = "cs208_hw3.sqlite";

        database = new Database(sqliteFileName);
        try {
            database.getDatabaseConnection();
        } catch (Exception exception) {
            // there is really no point in continuing if we cannot connect to the database
            System.err.println("Exiting the program...");
            return;

            // alternatively, we could have used
            // System.exit(1);
        }

        inputScanner = new Scanner(System.in);

        chooseMenuOptions();

        inputScanner.close();
    }

    private static void printMenuOptions() {
        System.out.println();
        System.out.println(Utils.characterRepeat('=', 27) + " School Management System " + Utils.characterRepeat('=', 27));
        System.out.println(Utils.characterRepeat('-', 37) + " MENU " + Utils.characterRepeat('-', 37));
        System.out.println(" 0 - Test the database connection");
        System.out.println(" 1 - Print this menu");
        System.out.println(" 2 - Exit the program");
        System.out.println("10 - List all classes");
        System.out.println("11 - Add new class");
        System.out.println("12 - Update existing class information");
        System.out.println("13 - Delete existing class");
        System.out.println("20 - List all students");
        System.out.println("21 - Add new student");
        System.out.println("22 - Update existing student information");
        System.out.println("23 - Delete existing student");
        System.out.println("30 - List all registered students");
        System.out.println("31 - Add a new student to a class");
        System.out.println("32 - Drop an existing student from a class");
        System.out.println("33 - Show all students that are taking a class");
        System.out.println("34 - Show all classes in which a student is enrolled");
    }

    public static void chooseMenuOptions() {
        boolean shouldExit = false;

        while (!shouldExit) {
            printMenuOptions();
            System.out.print("Enter your choice (0, 1, 2, 10, 11, etc.): ");

            int choice;
            try {
                choice = Integer.parseInt(inputScanner.nextLine());
            } catch (Exception e) {
                System.out.println("Invalid choice, expected an integer value. Please enter a number such as 0, 1, 2, 10, 11, etc.");
                continue;
            }

            switch (choice) {
                case 0:
                    menuTestConnection();
                    break;

                case 1:
                    System.out.println("Reprinting the menu options...");

                    // this will jump at the start of the while loop to reprint the menu
                    continue;

                case 2:
                    shouldExit = true;
                    System.out.println("Exiting the program...");
                    break;

                case 10:
                    menuListAllClasses();
                    break;

                case 11:
                    menuAddNewClass();
                    break;

                case 12:
                    menuUpdateExistingClassInformation();
                    break;

                case 13:
                    menuDeleteExistingClass();
                    break;

                case 20:
                    menuListAllStudents();
                    break;

                case 21:
                    menuAddNewStudent();
                    break;

                case 22:
                    menuUpdateExistingStudentInformation();
                    break;

                case 23:
                    menuDeleteExistingStudent();
                    break;

                case 30:
                    menuListAllRegisteredStudents();
                    break;
                case 31:
                    menuAddStudentToClass();
                    break;
                case 32:
                    menuRemoveStudentFromClass();
                    break;
                case 33:
                    menuShowAllStudentsInClass();
                    break;
                case 34:
                    menuShowAllStudentClasses();
                    break;

                //TODO: add your code here


                default:
                    System.out.println("Invalid choice. Please enter a number such as 0, 1, 2, 10, 11, etc.");
            }
            shouldExit = true;
        }
    }

    public static void menuTestConnection() {
        System.out.println("Testing database connection...");
        database.testConnection();

    }

    public static void menuListAllClasses() {
        System.out.println("Listing all classes...");
        database.listAllClasses();
    }

    private static void menuAddNewClass() {
        System.out.println("Adding new class...");
        Scanner newScanner = new Scanner(System.in);

        String code = null;
        String title = null;
        String description = null;
        int maxStudents = 0;
        try {
            System.out.print("Enter the class code: ");
            code = newScanner.nextLine();

            System.out.print("Enter the class title: ");
            title = newScanner.nextLine();

            System.out.print("Enter the class description: ");
            description = newScanner.nextLine();

            System.out.print("Enter the class max students: ");
            maxStudents = Integer.parseInt(newScanner.nextLine());

        } catch (Exception e) {
            System.out.println("Invalid input, please try again.");
            return;
        }

        Class newClass = new Class(code, title, description, maxStudents);
        database.addNewClass(newClass);
    }

    private static void menuUpdateExistingClassInformation() {
        System.out.println("Updating existing class information...");

        int id = 0;
        String code = null;
        String title = null;
        String description = null;
        int maxStudents = 0;
        Class oldClassInfo;
        try {
            System.out.print("Enter the existing class id you want to update: ");
            id = Integer.parseInt(inputScanner.nextLine());
            oldClassInfo = database.getOldClassInfo(id);

            System.out.print("Enter a new class code: ");
            String check = inputScanner.nextLine();
            if(check.isBlank() || check.equals(oldClassInfo.getCode())){
                code = oldClassInfo.getCode();
            } else {
                code = check;
            }
            System.out.print("Enter a new class title: ");
            title = inputScanner.nextLine();

            System.out.print("Enter a new class description: ");
            description = inputScanner.nextLine();

            System.out.print("Enter a new class max students: ");
            maxStudents = Integer.parseInt(inputScanner.nextLine());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        Class classToUpdate = new Class(id, code, title, description, maxStudents);
        database.updateExistingClassInformation(classToUpdate);
    }

    private static void menuDeleteExistingClass() {
        System.out.println("Deleting existing class...");

        int id = 0;
        try {
            System.out.print("Enter the existing class id you want to delete: ");
            id = Integer.parseInt(inputScanner.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid input, please try again.");
            return;
        }

        database.deleteExistingClass(id);
    }

    private static void menuListAllStudents() {
        System.out.println("Listing all students...");
        database.listAllStudents();
    }

    private static void menuAddNewStudent() {
        Scanner newScanner2 = new Scanner(System.in);
        System.out.println("Adding new student...");
        ;
        String firstName = null;
        String lastName = null;
        Date birthDate = null;
        try {
            // TODO: add your code here

            System.out.println(("Enter the student's first name: "));
            firstName = String.valueOf(newScanner2.nextLine());
            System.out.println(("Enter the student's last name: "));
            lastName = String.valueOf(newScanner2.nextLine());
            System.out.print("Enter the student birth date in ISO format (yyyy-mm-dd): ");
            birthDate = Date.valueOf(newScanner2.nextLine());
            database.addNewStudent(new Student(firstName, lastName, birthDate));

        } catch (Exception e) {
            System.out.println("Invalid input, please try again.");
        }
    }

    private static void menuUpdateExistingStudentInformation() {
        int choice0;
        try {
            System.out.println("How would you like to search for your student?\n" +
                    "0 - Student ID number\n" +
                    "1 - First and Last name\n" +
                    "2 - Date of Birth");
            choice0 = inputScanner.nextInt();
            switch (choice0) {
                case 0:
                    System.out.println("Please enter an existing integer Student ID: ");
                    int StudentID = inputScanner.nextInt();
                    database.UpdateExistingStudentInformation(StudentID);
                    break;
                case 1:
                    System.out.println("Please enter the student first name and last name separated by a space: ");
                    Scanner inputScanner2 = new Scanner(System.in);
                    String fix = inputScanner2.nextLine();
                    String[] fixsplit = fix.split(" ");
                    String First = fixsplit[0];
                    First = First.replace(" ", "");
                    String Last = fixsplit[1];
                    Last = Last.replace(" ", "");
                    database.UpdateExistingStudentInformation(First, Last);
                    break;
                case 2:
                    System.out.println("Please enter the student date of birth in YYYY-MM-DD format: ");
                    Scanner inputScanner3 = new Scanner(System.in);
                    String DOB = inputScanner3.next();
                    database.UpdateExistingStudentInformation(DOB);
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
                    return;
            }
        } catch (Exception e) {
            System.out.println("Invalid input, please try again.");
            return;
        }
        System.out.println("Updating existing student information...");
    }

    private static void menuDeleteExistingStudent() {
        int choice0;
        try {
            System.out.println("How would you like to search for your student?\n" +
                    "0 - Student ID number\n" +
                    "1 - First and Last name\n" +
                    "2 - Date of Birth");
            choice0 = inputScanner.nextInt();
            switch (choice0) {
                case 0:
                    System.out.println("Please enter an existing integer Student ID: ");
                    int StudentID = inputScanner.nextInt();
                    database.DeleteExistingStudent(StudentID);
                    break;
                case 1:
                    System.out.println("Please enter the student first name and last name separated by a space: ");
                    Scanner inputScanner2 = new Scanner(System.in);
                    String fix = inputScanner2.nextLine();
                    String[] fixsplit = fix.split(" ");
                    String First = fixsplit[0];
                    First = First.replace(" ", "");
                    String Last = fixsplit[1];
                    Last = Last.replace(" ", "");
                    database.DeleteExistingStudent(First, Last);
                    break;
                case 2:
                    System.out.println("Please enter the student date of birth in YYYY-MM-DD format: ");
                    Scanner inputScanner3 = new Scanner(System.in);
                    String DOB = inputScanner3.next();
                    database.DeleteExistingStudent(DOB);
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
                    return;
            }
        } catch (Exception e) {
            System.out.println("Invalid input, please try again.");
            return;
        }
        System.out.println("Deleting existing student...");
    }

    private static void menuAddStudentToClass() {
        int choice0;
        int choice1;
        int classid = 0;
        String classCode = null;
        boolean proceed = false;
        boolean proceed2 = false;
        try {
           while(!proceed){
               System.out.println("How would you like to search for your class?\n" +
                   "0 - Class ID\n" +
                   "1 - Class code\n" +
                   "2 - Create new class first");
               choice1 = inputScanner.nextInt();
            switch (choice1) {
                case 0:
                    System.out.println("Please enter your class id:\n");
                    classid = inputScanner.nextInt();
                    proceed = true;
                    break;
                case 1:
                    Scanner inputScanner4 = new Scanner(System.in);
                    System.out.println("Pleas enter class code:\n");
                    classCode = inputScanner4.nextLine();
                    classid = database.classSearch(classCode);
                    proceed = true;
                    break;
                case 2:
                    try {
                        menuAddNewClass();
                        System.out.println("New class created!");
                        break;
                    } catch (Exception e) {
                        System.out.println("Please try again.");
                        return;
                    }

                default:
                    System.out.println("Invalid choice, please try again.");
            }
           }
           while(!proceed2) {
               System.out.println("How would you like to search for your student?\n" +
                       "0 - Student ID number\n" +
                       "1 - First and Last name\n" +
                       "2 - Date of Birth\n" +
                       "3 - Add new Student first.");
               choice0 = inputScanner.nextInt();
               switch (choice0) {
                   case 0:
                       System.out.println("Please enter an existing integer Student ID: ");
                       int StudentID = inputScanner.nextInt();
                       database.AddStudentToClass(StudentID, classid);
                       proceed2 = true;
                       break;
                   case 1:
                       System.out.println("Please enter the student first name and last name separated by a space: ");
                       Scanner inputScanner2 = new Scanner(System.in);
                       String fix = inputScanner2.nextLine();
                       String[] fixsplit = fix.split(" ");
                       String First = fixsplit[0];
                       First = First.replace(" ", "");
                       String Last = fixsplit[1];
                       Last = Last.replace(" ", "");
                       database.AddStudentToClass(First, Last, classid);
                       proceed2 = true;
                       break;
                   case 2:
                       System.out.println("Please enter the student date of birth in YYYY-MM-DD format: ");
                       Scanner inputScanner3 = new Scanner(System.in);
                       String DOB = inputScanner3.next();
                       database.AddStudentToClass(DOB, classid);
                       proceed2 = true;
                       break;
                   case 3:
                       try {
                           menuAddNewStudent();
                           System.out.println("New student added!");
                           break;
                       } catch (Exception e) {
                           System.out.println(e.getMessage());
                           System.out.println("Please try again");
                           return;
                       }
                   default:
                       System.out.println("Invalid input, please try again.");
               }
           }
            System.out.println("Student added to class!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Please try again.");
        }
    }
    private static void menuRemoveStudentFromClass() {
        int choice0;
        int choice1;
        int classid = 0;
        String classCode = null;
        boolean proceed = false;
        boolean proceed2 = false;
        try {
            while(!proceed){
                System.out.println("How would you like to search for your class?\n" +
                        "0 - Class ID\n" +
                        "1 - Class code\n");
                choice1 = inputScanner.nextInt();
                switch (choice1) {
                    case 0:
                        System.out.println("Please enter your class id:\n");
                        classid = inputScanner.nextInt();
                        proceed = true;
                        break;
                    case 1:
                        Scanner inputScanner4 = new Scanner(System.in);
                        System.out.println("Pleas enter class code:\n");
                        classCode = inputScanner4.nextLine();
                        classid = database.classSearch(classCode);
                        proceed = true;
                        break;

                    default:
                        System.out.println("Invalid choice, please try again.");
                }
            }
            while(!proceed2) {
                System.out.println("How would you like to search for your student?\n" +
                        "0 - Student ID number\n" +
                        "1 - First and Last name\n" +
                        "2 - Date of Birth\n");
                choice0 = inputScanner.nextInt();
                switch (choice0) {
                    case 0:
                        System.out.println("Please enter an existing integer Student ID: ");
                        int StudentID = inputScanner.nextInt();
                        database.RemoveStudentFromClass(StudentID, classid);
                        proceed2 = true;
                        break;
                    case 1:
                        System.out.println("Please enter the student first name and last name separated by a space: ");
                        Scanner inputScanner2 = new Scanner(System.in);
                        String fix = inputScanner2.nextLine();
                        String[] fixsplit = fix.split(" ");
                        String First = fixsplit[0];
                        First = First.replace(" ", "");
                        String Last = fixsplit[1];
                        Last = Last.replace(" ", "");
                        database.RemoveStudentFromClass(First, Last, classid);
                        proceed2 = true;
                        break;
                    case 2:
                        System.out.println("Please enter the student date of birth in YYYY-MM-DD format: ");
                        Scanner inputScanner3 = new Scanner(System.in);
                        String DOB = inputScanner3.next();
                        database.RemoveStudentFromClass(DOB, classid);
                        proceed2 = true;
                        break;
                    default:
                        System.out.println("Invalid input, please try again.");
                }
            }
            System.out.println("Student removed from class!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Please try again.");
        }
    }
    private static void menuShowAllStudentsInClass() {
        int choice1;
        int classid = 0;
        String classCode = null;
        boolean proceed = false;
        try {
            while (!proceed) {
                System.out.println("How would you like to search for your class?\n" +
                        "0 - Class ID\n" +
                        "1 - Class code\n");
                choice1 = inputScanner.nextInt();
                switch (choice1) {
                    case 0:
                        System.out.println("Please enter your class id:\n");
                        classid = inputScanner.nextInt();
                        classCode = database.classSearch(classid);
                        proceed = true;
                        break;
                    case 1:
                        Scanner inputScanner4 = new Scanner(System.in);
                        System.out.println("Pleas enter class code:\n");
                        classCode = inputScanner4.nextLine();
                        proceed = true;
                        break;

                    default:
                        System.out.println("Invalid choice, please try again.");
                }
            }
            System.out.println("Listing all registered students...");
            database.showAllStudentsInClass(classCode);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Please try again.");
        }
    }
    private static void menuShowAllStudentClasses(){
        int choice0;
        try {
            System.out.println("How would you like to search for your student?\n" +
                    "0 - Student ID number\n" +
                    "1 - First and Last name\n" +
                    "2 - Date of Birth");
            choice0 = inputScanner.nextInt();
            switch (choice0) {
                case 0:
                    System.out.println("Please enter an existing integer Student ID: ");
                    int StudentID = inputScanner.nextInt();
                    database.showAllStudentClasses(StudentID);
                    break;
                case 1:
                    System.out.println("Please enter the student first name and last name separated by a space: ");
                    Scanner inputScanner2 = new Scanner(System.in);
                    String fix = inputScanner2.nextLine();
                    String[] fixsplit = fix.split(" ");
                    String First = fixsplit[0];
                    First = First.replace(" ", "");
                    String Last = fixsplit[1];
                    Last = Last.replace(" ", "");
                    database.showAllStudentClasses(First, Last);
                    break;
                case 2:
                    System.out.println("Please enter the student date of birth in YYYY-MM-DD format: ");
                    Scanner inputScanner3 = new Scanner(System.in);
                    String DOB = inputScanner3.next();
                    database.showAllStudentClasses(DOB);
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
                    return;
            }
        } catch (Exception e) {
            System.out.println("Invalid input, please try again.");
            return;
        }
    }
    private static void menuListAllRegisteredStudents()
    {
        System.out.println("Listing all registered students...");
        database.listAllRegisteredStudents();
    }

}
