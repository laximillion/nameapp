import java.io.*;
import java.util.*;
import java.util.regex.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class CourseDataManager {
    
    private static final String COURSES_FILE = "data/courses.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public static class Course {
        String courseCode;
        String title;
        String department;
        String credits;
        String semester;
        List<String> requirements = new ArrayList<>();
        List<String> prerequisites = new ArrayList<>();
        List<String> corequisites = new ArrayList<>();
        List<String> advisories = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        List<String> crossListedAs = new ArrayList<>();
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Course: ").append(courseCode).append("\n");
            sb.append("Title: ").append(title).append("\n");
            sb.append("Department: ").append(department).append("\n");
            sb.append("Credits: ").append(credits).append("\n");
            sb.append("Semester: ").append(semester).append("\n");
            sb.append("Cross-listed as: ").append(crossListedAs.isEmpty() ? "None" : crossListedAs).append("\n");
            sb.append("Requirements: ").append(requirements.isEmpty() ? "None" : requirements).append("\n");
            sb.append("Prerequisites: ").append(prerequisites.isEmpty() ? "None" : prerequisites).append("\n");
            sb.append("Corequisites: ").append(corequisites.isEmpty() ? "None" : corequisites).append("\n");
            sb.append("Advisories: ").append(advisories.isEmpty() ? "None" : advisories).append("\n");
            sb.append("Notes: ").append(notes.isEmpty() ? "None" : notes).append("\n");
            return sb.toString();
        }
    }
    
    // ==================== PARSING METHODS ====================
    
    public static List<Course> parseCourseFile(String filePath) throws IOException {
        List<Course> courses = new ArrayList<>();
        StringBuilder fileContent = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
        }
        
        String content = fileContent.toString();
        String department = extractDepartment(content);
        
        if (department == null) {
            throw new IllegalArgumentException("ERROR: Could not find department code in parentheses on first line");
        }
        
        // FIXED: Match course codes at the beginning of lines, with optional letter suffixes
        // Pattern matches: ANTHR-105, ANTHR-216AD, ANTHR-316BF, etc.
        // The (?m) enables multiline mode so ^ matches start of line
        String coursePattern = "(?m)^(" + department + "-\\d+[A-Z]*)\\s+(.+)$";
        Pattern pattern = Pattern.compile(coursePattern);
        Matcher matcher = pattern.matcher(content);
        
        // Find all course headers and their positions
        List<Integer> courseStarts = new ArrayList<>();
        while (matcher.find()) {
            courseStarts.add(matcher.start());
        }
        
        // Extract each course section
        for (int i = 0; i < courseStarts.size(); i++) {
            int start = courseStarts.get(i);
            // End is either the start of the next course, or end of file
            int end = (i < courseStarts.size() - 1) ? courseStarts.get(i + 1) : content.length();
            
            String section = content.substring(start, end).trim();
            
            try {
                Course course = parseCourseSection(section, department);
                courses.add(course);
            } catch (Exception e) {
                System.err.println("WARNING: Error parsing course section: " + e.getMessage());
                System.err.println("Section preview: " + section.substring(0, Math.min(100, section.length())));
            }
        }
        
        return courses;
    }
    
    private static String extractDepartment(String content) {
        Pattern p = Pattern.compile("\\(([A-Z]+)\\)");
        Matcher m = p.matcher(content);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
    
    private static Course parseCourseSection(String section, String department) {
        Course course = new Course();
        course.department = department;
        
        String[] lines = section.split("\n");
        
        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            // CRITICAL FIX: Now handles letter suffixes (e.g., ANTHR-216AD)
            Pattern codePattern = Pattern.compile("(" + department + "-\\d+[A-Z]*)\\s+(.+)");
            Matcher m = codePattern.matcher(firstLine);
            if (m.find()) {
                course.courseCode = m.group(1);
                course.title = m.group(2).trim();
            } else {
                throw new IllegalArgumentException("Could not extract course code and title from: " + firstLine);
            }
        }
        
        // Look for the semester/credits line - it's usually the 2nd or 3rd non-empty line
        // Skip blank lines after the title
        int lineIndex = 1;
        while (lineIndex < lines.length && lines[lineIndex].trim().isEmpty()) {
            lineIndex++;
        }
        
        if (lineIndex < lines.length) {
            String creditsLine = lines[lineIndex].trim();
            course.semester = extractSemester(creditsLine);
            course.credits = extractCredits(creditsLine);
        }
        
        // Parse metadata lines starting after the credits line
        for (int i = lineIndex + 1; i < lines.length; i++) {
            String line = lines[i].trim();
            
            if (line.isEmpty()) continue;
            
            // Check for metadata lines
            if (line.startsWith("Applies to requirement")) {
                course.requirements.addAll(extractRequirements(line));
            } else if (line.startsWith("Prereq:")) {
                course.prerequisites.addAll(extractCourses(line));
            } else if (line.startsWith("Coreq:")) {
                course.corequisites.addAll(extractCourses(line));
            } else if (line.startsWith("Advisory:")) {
                course.advisories.add(line.substring("Advisory:".length()).trim());
            } else if (line.startsWith("Notes:")) {
                String noteContent = line.substring("Notes:".length()).trim();
                course.notes.add(noteContent);
                // Extract cross-listed courses from notes
                course.crossListedAs.addAll(extractCrossListings(noteContent));
            } else if (line.startsWith("Crosslisted as:")) {
                // CRITICAL FIX: Handle explicit cross-listing line
                String crossListContent = line.substring("Crosslisted as:".length()).trim();
                course.crossListedAs.addAll(extractCourses(crossListContent));
            }
        }
        
        return course;
    }
    
    private static String extractSemester(String line) {
        // FIXED: Check for "Fall and Spring" or "Spring and Fall" first
        if (line.contains("Fall and Spring") || line.contains("Spring and Fall")) {
            return "Fall and Spring";
        }
        
        String[] semesters = {"Spring", "Fall", "Summer", "Not Scheduled"};
        for (String sem : semesters) {
            if (line.contains(sem)) {
                return sem;
            }
        }
        return "Unknown";
    }
    
    private static String extractCredits(String line) {
        // FIXED: Handle credit ranges like "1 - 4" or "1 - 8"
        Pattern rangePattern = Pattern.compile("Credits:\\s*(\\d+)\\s*-\\s*(\\d+)");
        Matcher rangeMatcher = rangePattern.matcher(line);
        if (rangeMatcher.find()) {
            return rangeMatcher.group(1) + "-" + rangeMatcher.group(2);
        }
        
        // Handle single credit values
        Pattern p = Pattern.compile("Credits:\\s*(\\d+)");
        Matcher m = p.matcher(line);
        if (m.find()) {
            return m.group(1);
        }
        return "Unknown";
    }
    
    private static List<String> extractRequirements(String line) {
        List<String> reqs = new ArrayList<>();
        Map<String, String> reqMap = new HashMap<>();
        reqMap.put("Math Sciences", "MS");
        reqMap.put("Humanities", "H");
        reqMap.put("Social Sciences", "SS");
        reqMap.put("Natural Sciences", "NS");
        reqMap.put("Arts", "A");
        reqMap.put("Multicultural Perspectives", "MP");
        
        for (Map.Entry<String, String> entry : reqMap.entrySet()) {
            if (line.contains(entry.getKey())) {
                reqs.add(entry.getValue());
            }
        }
        
        return reqs;
    }
    
    private static List<String> extractCourses(String line) {
        List<String> courses = new ArrayList<>();
        // FIXED: Handle letter suffixes in course codes
        Pattern p = Pattern.compile("([A-Z]+)-\\d+[A-Z]*");
        Matcher m = p.matcher(line);
        while (m.find()) {
            courses.add(m.group(0));
        }
        return courses;
    }
    
    private static List<String> extractCrossListings(String noteContent) {
        List<String> crossListings = new ArrayList<>();
        String lowerNote = noteContent.toLowerCase();
        if (lowerNote.contains("same as") || lowerNote.contains("cross-listed") || 
            lowerNote.contains("also listed as") || lowerNote.contains("cross listed")) {
            
            Pattern p = Pattern.compile("([A-Z]+)-\\d+[A-Z]*");
            Matcher m = p.matcher(noteContent);
            while (m.find()) {
                crossListings.add(m.group(0));
            }
        }
        return crossListings;
    }
    
    // ==================== JSON STORAGE METHODS ====================
    
    /**
     * Save all courses to a single JSON file as a map.
     * Cross-listed courses are stored as separate entries with the same data.
     * 
     * Example: If COMSC-250 is cross-listed as PSYCH-215, the JSON will have TWO entries:
     *   "COMSC-250": { courseCode: "COMSC-250", crossListedAs: ["PSYCH-215"], ... }
     *   "PSYCH-215": { courseCode: "PSYCH-215", crossListedAs: ["COMSC-250"], ... }
     * 
     * This allows you to look up either code and get complete information.
     */
    public static void saveCoursesToJSON(List<Course> courses) throws IOException {
        // Create the data directory if it doesn't exist yet
        // COURSES_FILE is "data/courses.json", so this creates the "data" folder
        File file = new File(COURSES_FILE);
        file.getParentFile().mkdirs();
        
        // LinkedHashMap maintains insertion order, making the JSON file easier to read
        // Key = course code (e.g., "COMSC-100"), Value = Course object
        Map<String, Course> courseMap = new LinkedHashMap<>();
        
        // Loop through each parsed course
        for (Course course : courses) {
            // Add the main course entry to the map
            // Example: courseMap.put("COMSC-100", <Course object for COMSC-100>)
            courseMap.put(course.courseCode, course);
            
            // Handle cross-listings: create duplicate entries for each cross-listed code
            // Example: If COMSC-250 has crossListedAs = ["PSYCH-215"]
            // This loop creates a separate entry for "PSYCH-215" with the same course data
            for (String crossListedCode : course.crossListedAs) {
                // Create a modified copy where the cross-listed code becomes the primary code
                Course crossListedCourse = createCrossListedCopy(course, crossListedCode);
                // Add this copy to the map under the cross-listed code
                courseMap.put(crossListedCode, crossListedCourse);
            }
        }
        
        // Write the entire map to the JSON file
        // try-with-resources automatically closes the FileWriter when done
        try (FileWriter writer = new FileWriter(COURSES_FILE)) {
            // gson.toJson() converts the Java Map into JSON format and writes it
            // The map becomes: { "COMSC-100": {...}, "COMSC-106": {...}, ... }
            gson.toJson(courseMap, writer);
        }
        
        // Print summary information
        System.out.println("Saved " + courseMap.size() + " course entries to " + COURSES_FILE);
        System.out.println("(Includes " + courses.size() + " unique courses with cross-listings)");
    }
    
    /**
     * Creates a copy of the course with a different primary course code.
     * Updates the crossListedAs field to include the original code.
     * 
     * Example: If the original is COMSC-250 cross-listed as PSYCH-215,
     * calling createCrossListedCopy(original, "PSYCH-215") creates a new Course where:
     *   - courseCode = "PSYCH-215" (instead of "COMSC-250")
     *   - crossListedAs = ["COMSC-250"] (the original code is now in the cross-listing list)
     * 
     * This ensures both course codes can be looked up independently and each points to the other.
     */
    private static Course createCrossListedCopy(Course original, String newCourseCode) {
        // Create a new Course object for the cross-listed version
        Course copy = new Course();
        
        // Set the course code to the cross-listed code (e.g., "PSYCH-215")
        copy.courseCode = newCourseCode;
        
        // Copy basic information (same title, credits, semester, etc.)
        copy.title = original.title;
        
        // Extract department from the new course code
        // Example: "PSYCH-215" → split on "-" → "PSYCH"
        copy.department = newCourseCode.split("-")[0];
        
        copy.credits = original.credits;
        copy.semester = original.semester;
        
        // Create new ArrayLists with copies of all the original lists
        // This prevents modifications to the copy from affecting the original
        copy.requirements = new ArrayList<>(original.requirements);
        copy.prerequisites = new ArrayList<>(original.prerequisites);
        copy.corequisites = new ArrayList<>(original.corequisites);
        copy.advisories = new ArrayList<>(original.advisories);
        copy.notes = new ArrayList<>(original.notes);
        
        // Handle the cross-listing field specially:
        // Start with a copy of the original's cross-listings
        copy.crossListedAs = new ArrayList<>(original.crossListedAs);
        
        // Remove the new course code from the list (don't list itself as a cross-listing)
        // Example: If newCourseCode is "PSYCH-215", remove "PSYCH-215" from the list
        copy.crossListedAs.remove(newCourseCode);
        
        // Add the original course code to the cross-listing list
        // Example: Add "COMSC-250" to the cross-listings for "PSYCH-215"
        // Add at position 0 so the original code appears first in the list
        if (!copy.crossListedAs.contains(original.courseCode)) {
            copy.crossListedAs.add(0, original.courseCode);
        }
        
        return copy;
    }
    
    /**
     * Load all courses from the JSON file into a map.
     * 
     * Returns a Map where:
     *   - Key = course code (String like "COMSC-100")
     *   - Value = Course object with all the course information
     * 
     * This should be called once at application startup and kept in memory.
     * With hundreds of courses, the entire map uses very little memory (~500KB)
     * and provides instant O(1) lookups by course code.
     * 
     * Example usage:
     *   Map<String, Course> catalog = loadAllCourses();
     *   Course cs100 = catalog.get("COMSC-100");  // Instant lookup
     */
    public static Map<String, Course> loadAllCourses() throws IOException {
        File file = new File(COURSES_FILE);
        
        // Check if the file exists before trying to read it
        if (!file.exists()) {
            throw new FileNotFoundException("Courses file not found: " + COURSES_FILE);
        }
        
        // Open the JSON file and parse it back into a Map
        // try-with-resources automatically closes the FileReader when done
        try (FileReader reader = new FileReader(COURSES_FILE)) {
            // TypeToken is needed because of Java's type erasure with generics
            // It tells Gson exactly what type to deserialize into: Map<String, Course>
            // Without this, Gson wouldn't know the generic types at runtime
            Type type = new TypeToken<Map<String, Course>>(){}.getType();
            
            // gson.fromJson() reads the JSON and converts it back into Java objects
            // Returns a Map<String, Course> matching the structure we saved
            return gson.fromJson(reader, type);
        }
    }
    
    /**
     * Load a single course by its course code.
     * 
     * NOTE: This method loads the ENTIRE JSON file every time it's called.
     * For efficiency, consider loading all courses once with loadAllCourses() 
     * and keeping the map in memory, then doing lookups on that map instead.
     * 
     * Good for: One-off lookups or simple scripts
     * Not recommended for: Repeated lookups in a web application
     * 
     * Example:
     *   Course cs100 = loadCourse("COMSC-100");
     *   Course psych215 = loadCourse("PSYCH-215"); // Works for cross-listed codes too
     */
    public static Course loadCourse(String courseCode) throws IOException {
        // Load the entire map from the JSON file
        // This reads and parses the whole file every time this method is called
        Map<String, Course> allCourses = loadAllCourses();
        
        // Look up the requested course code in the map
        // This is an O(1) hash map lookup, very fast
        Course course = allCourses.get(courseCode);
        
        // If the course code wasn't found in the map, throw an error
        if (course == null) {
            throw new IllegalArgumentException("Course not found: " + courseCode);
        }
        
        return course;
    }
    
    // ==================== MAIN METHOD FOR TESTING ====================
    
    public static void main(String[] args) {
        try {
            // STEP 1: Parse the text file
            // Read the raw course text file and extract course information
            System.out.println("Parsing course file...\n");
            List<Course> courses = parseCourseFile("(ARCH).txt");
            System.out.println("Successfully parsed " + courses.size() + " courses\n");
            
            // STEP 2: Save to single JSON file
            // Convert the list of courses into a map and write to data/courses.json
            // This also handles creating duplicate entries for cross-listed courses
            System.out.println("Saving courses to JSON...\n");
            saveCoursesToJSON(courses);
            
            // STEP 3: Test loading all courses
            // Read the JSON file back and convert it to a Map<String, Course>
            System.out.println("\n" + "=".repeat(80));
            System.out.println("Testing course retrieval...\n");
            
            Map<String, Course> allCourses = loadAllCourses();
            System.out.println("Loaded " + allCourses.size() + " course entries from JSON\n");
            
            // STEP 4: Test loading a specific course
            // Demonstrate how to look up a single course by its code
            if (!courses.isEmpty()) {
                // Get the first course's code to test with
                String testCode = courses.get(0).courseCode;
                
                // Load that course from the JSON (loads entire file, then does map lookup)
                Course loaded = loadCourse(testCode);
                System.out.println("Successfully retrieved: " + testCode);
                System.out.println(loaded);
                
                // STEP 5: Test cross-listing functionality
                // If the course has cross-listings, test that we can look up the cross-listed code too
                if (!loaded.crossListedAs.isEmpty()) {
                    // Get the first cross-listed code
                    String crossCode = loaded.crossListedAs.get(0);
                    
                    // Load using the cross-listed code - should return equivalent course data
                    Course crossListed = loadCourse(crossCode);
                    System.out.println("=".repeat(80));
                    System.out.println("Cross-listed course retrieved: " + crossCode);
                    System.out.println(crossListed);
                }
            }
            
        } catch (IOException e) {
            // Handle any file I/O errors
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}