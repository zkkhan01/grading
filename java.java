import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Grader {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter the folder path where the submission files are: ");
        String submissionsFolder = sc.nextLine().trim();

        File folder = new File(submissionsFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Invalid folder path.");
            return;
        }

        Map<String, Integer> finalGrades = gradeSubmissions(folder);
        System.out.println("\nFinal Grades:");
        finalGrades.forEach((k, v) -> System.out.println(k + ": Grade = " + v));
    }

    public static Map<String, Integer> gradeSubmissions(File folder) {
        Map<String, Integer> grades = new LinkedHashMap<>();

        // Create a temp folder for grading one file at a time
        File tempDir = new File(folder, "_tempGrade");
        tempDir.mkdir();

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (!file.getName().endsWith(".java")) continue;
            System.out.println("\nGrading " + file.getName() + "...");

            try {
                // Clean temp directory
                for (File f : Objects.requireNonNull(tempDir.listFiles())) f.delete();

                // Copy student's file into tempDir as OrderedSequentialSearchST.java
                Path dest = Paths.get(tempDir.getAbsolutePath(), "OrderedSequentialSearchST.java");
                Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                // Compile + run
                String status = runJavaFile(dest.toFile());
                int score = switch (status) {
                    case "success" -> 100;
                    case "runtime_error" -> 75;
                    case "compile_error" -> 50;
                    default -> 0;
                };
                grades.put(file.getName(), score);

            } catch (Exception e) {
                System.out.println("Error grading " + file.getName() + ": " + e.getMessage());
                grades.put(file.getName(), 0);
            }
        }

        // Clean up temp folder
        for (File f : Objects.requireNonNull(tempDir.listFiles())) f.delete();
        tempDir.delete();

        return grades;
    }

    private static String runJavaFile(File javaFile) {
        try {
            Process compile = new ProcessBuilder("javac", javaFile.getAbsolutePath())
                    .directory(javaFile.getParentFile())
                    .redirectErrorStream(true)
                    .start();
            String compileOut = new String(compile.getInputStream().readAllBytes());
            int compileExit = compile.waitFor();

            if (compileExit != 0) {
                System.out.println("Compilation Error:\n" + compileOut);
                return "compile_error";
            }

            Process run = new ProcessBuilder("java", "-cp", javaFile.getParent(), "OrderedSequentialSearchST")
                    .redirectErrorStream(true)
                    .start();
            String runOut = new String(run.getInputStream().readAllBytes());
            int runExit = run.waitFor();

            if (runExit != 0) {
                System.out.println("Runtime Error:\n" + runOut);
                return "runtime_error";
            }

            System.out.println("Output:\n" + runOut);
            return "success";
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            return "exception";
        }
    }
}
