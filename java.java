import java.io.*;
import java.util.*;

public class Grader {

    // Compile a Java file
    public static String compileJava(File file) {
        try {
            ProcessBuilder pb = new ProcessBuilder("javac", file.getAbsolutePath());
            Process process = pb.start();
            process.waitFor();

            if (process.exitValue() != 0) {
                String error = new String(process.getErrorStream().readAllBytes());
                return "compile_error:" + error;
            }
            return "success";
        } catch (Exception e) {
            return "exception:" + e.getMessage();
        }
    }

    // Run a Java file (by class name)
    public static String runJava(File file) {
        try {
            String className = file.getName().replace(".java", "");
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", file.getParent(), className);
            Process process = pb.start();
            process.waitFor();

            if (process.exitValue() != 0) {
                String error = new String(process.getErrorStream().readAllBytes());
                return "runtime_error:" + error;
            }
            String output = new String(process.getInputStream().readAllBytes());
            return "success:" + output;
        } catch (Exception e) {
            return "exception:" + e.getMessage();
        }
    }

    // Grade student submissions
    public static Map<String, Integer> gradeSubmissions(File folder) {
        Map<String, Integer> grades = new HashMap<>();

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".java"));
        if (files == null) return grades;

        for (File file : files) {
            System.out.println("Grading " + file.getName() + "...");
            String compileStatus = compileJava(file);

            if (compileStatus.startsWith("compile_error")) {
                System.out.println("Compilation Error: " + compileStatus);
                grades.put(file.getName(), 50);
                continue;
            }

            String runStatus = runJava(file);
            if (runStatus.startsWith("success:")) {
                System.out.println("Output: " + runStatus.substring(8));
                grades.put(file.getName(), 100);
            } else if (runStatus.startsWith("runtime_error")) {
                System.out.println("Runtime Error: " + runStatus);
                grades.put(file.getName(), 75);
            } else {
                System.out.println("Unexpected Error: " + runStatus);
                grades.put(file.getName(), 0);
            }
        }

        return grades;
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter submissions folder path: ");
        String folderPath = sc.nextLine();

        File folder = new File(folderPath);
        Map<String, Integer> results = gradeSubmissions(folder);

        System.out.println("\nFinal Grades:");
        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
