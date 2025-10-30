import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class readCourseInfo2 {
    public static void main(String[] args) {

        try {

            // Create a scanner and read in the file
            Scanner scanner = new Scanner(new File("Filename.txt"));

            // While the file still has lines
            while (scanner.hasNext()) {

                // read in the line
                String line = scanner.nextLine();

                // The if statements all refer to specific actions
                // based on what is or is not in the file
                // in the specific line being read currently
                // more specific comments about each if statement in the if statements

                if (line.startsWith("COMSC-")) {
                    // Puts a gap between every course
                    System.out.println(" ");

                    // finds the first space (between course code and name)
                    int stringFind = line.indexOf(" ");

                    // Makes a substring of the course code and prints it
                    String stringFirst = line.substring(0, stringFind);
                    System.out.println(stringFirst);

                    // Makes a substring starting from the space index + 1 (the start of the course
                    // name) to the end
                    // Just a substring containing the course name
                    // prints the substring containing the course name
                    String stringSecond = line.substring((stringFind + 1));
                    System.out.println(stringSecond);
                }

                if (line.startsWith("Fall") || line.startsWith("Spring")) {
                    // Seperates the Fall and Spring part from the Course Credits part
                    int stringFind = line.indexOf(".");
                    // Makes a substring of only the semesters part
                    String string = line.substring(0, stringFind);
                    // prints the semesters it is offered
                    System.out.println(string);
                }

                if (line.startsWith("Crosslisted as: ")) {
                    // Makes a substring and prints the other course codes
                    // that the course is crosslisted as
                    String string = line.substring(16);
                    System.out.println(string);

                }

                if (line.startsWith("Applies to requirement(s): ")) {
                    // Takes the requirements and converts them to our excel code
                    // Appends the encoded requirements to a string
                    String string = "";
                    if (line.contains("Humanities")) {
                        string = string + "H, ";
                    }

                    if (line.contains("Language")) {
                        string = string + "L, ";
                    }

                    if (line.contains("Multicultural Perspectives")) {
                        string = string + "MP, ";
                    }

                    if (line.contains("Math Sciences")) {
                        string = string + "MS, ";
                    }

                    if (line.contains("Social Sciences")) {
                        string = string + "SS, ";
                    }

                    // prints the string with all the requirements
                    System.out.println(string);
                }

                if (line.startsWith("Prereq: ")) {
                    // makes a substring with and prints only the prereqs
                    String string = line.substring(8);
                    if (line.contains("Coreq: ")) {
                        // Finds out where it starts talking about the coreq
                        int stringFind = line.indexOf("Coreq: ");

                        // Makes a substring of just the prereqs and prints it
                        String stringOnly = line.substring(8, (stringFind));
                        System.out.println(stringOnly);
                    } else {
                        // if no coreqs, then just print the prereqs
                        System.out.println(string);
                    }
                }

                if (line.contains("Coreq: ")) {
                    // Finds out where it starts talking about the coreq
                    int stringFind = line.indexOf("Coreq: ");

                    // Makes a substring of the coreq and prints it
                    String stringCo = line.substring((stringFind + 7));
                    System.out.println(stringCo);
                }

                if (line.startsWith("Advisory: ")) {
                    // makes a substring with and prints only the extra notes
                    String string = line.substring(10);
                    System.out.println(string);
                }

                if (line.startsWith("Notes: ")) {
                    // makes a substring with and prints only the extra notes
                    // I don't know why this is different from the advisory
                    // Take it up with the registar
                    String string = line.substring(7);
                    System.out.println(string);
                }

                if (line.startsWith("Restrictions: ")) {
                    // makes a substring with and prints only the extra notes
                    // look there's more
                    // -_/(-_-)\_-
                    String string = line.substring(14);
                    System.out.println(string);
                }
            }

            // Close the scanner
            scanner.close();

        } catch (FileNotFoundException exception) {
            // If this prints then the system is not finding the file
            // That is a seperate and big problem
            System.out.println("File not found");
        }
    }
}
