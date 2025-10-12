/*rename to Grader.java*/

// place in project folder

/*
>javac --add-modules java.desktop -cp ..\src Grader.java
>java -ea --add-modules java.desktop -cp .;..\out;..\src Grader
*/

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Grader {

    private static final List<String[]> results = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the folder path where the submission files are: ");
        String submissionsFolderPath = scanner.nextLine().trim();

        System.out.print("Enter the solution file path: ");
        String solutionFilePath = scanner.nextLine().trim();

        File submissionsFolder = new File(submissionsFolderPath);
        File solutionFile = new File(solutionFilePath);

        if (!submissionsFolder.exists() || !submissionsFolder.isDirectory()) {
            System.out.println("Invalid folder path.");
            return;
        }

        if (!solutionFile.exists()) {
            System.out.println("Solution file not found.");
            return;
        }

        System.out.println("\nRunning solution file...");
        if (!compileAndRun(solutionFile, true)) {
            System.out.println("Solution file did not compile or run correctly. Aborting.");
            return;
        }

        File[] submissionFiles = submissionsFolder.listFiles((dir, name) -> name.endsWith(".java"));
        if (submissionFiles == null || submissionFiles.length == 0) {
            System.out.println("No Java files found in submissions folder.");
            return;
        }

        for (File submission : submissionFiles) {
            System.out.println("\nGrading " + submission.getName() + "...");
            String result = gradeStudent(submission);
            results.add(new String[]{submission.getName(), result});
        }

        printSummary();
    }

    private static String gradeStudent(File submission) {
        try {
            boolean success = compileAndRun(submission, false);
            if (success) {
                return "Passed - 100%";
            } else {
                return "Compilation or Runtime Error";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static boolean compileAndRun(File javaFile, boolean isSolution) {
        try {
            String className = getPublicClassName(javaFile);
            if (className == null) {
                System.out.println("Could not detect public class name in " + javaFile.getName());
                return false;
            }

            String packageName = getPackageName(javaFile);
            String fullClassName = (packageName == null) ? className : packageName + "." + className;

            File tempDir = Files.createTempDirectory("grader").toFile();
            File tempFile = new File(tempDir, className + ".java");
            Files.copy(javaFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String classpath = System.getProperty("user.dir") + File.pathSeparator + "..\\src";

            // Compile

	    ProcessBuilder compileProcess = new ProcessBuilder(
    		"javac", "--add-modules", "java.desktop",
    		"-d", tempDir.getAbsolutePath(), // ensure proper folder structure for packages
    		"-cp", classpath + File.pathSeparator + System.getProperty("user.dir"),
    		tempFile.getAbsolutePath()
	    );

            compileProcess.redirectErrorStream(true);
            Process compile = compileProcess.start();
            printProcessOutput(compile);
            int compileExit = compile.waitFor();
            if (compileExit != 0) {
                System.out.println("Compilation Error (" + (isSolution ? "solution" : "student") + "):");
                return false;
            }

            // Run
            ProcessBuilder runProcess = new ProcessBuilder(
                "java",
                "-ea:" + fullClassName,
                "--add-modules", "java.desktop",
                "-cp", classpath + File.pathSeparator + tempDir.getAbsolutePath(),
                fullClassName
            );
            runProcess.redirectErrorStream(true);
            Process run = runProcess.start();
            String runOutput = captureProcessOutput(run);
	    System.out.print(runOutput); // still print everything

	    boolean hasFailure = runOutput.contains("Failed") || runOutput.contains("Exception"); 

	    int runExit = run.waitFor();

	    return runExit == 0 && !hasFailure;

        } catch (Exception e) {
            System.out.println("Error running " + (isSolution ? "solution" : "student") + ": " + e.getMessage());
            return false;
        }
    }

    private static String captureProcessOutput(Process process) throws IOException {
    	StringBuilder output = new StringBuilder();
    	try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        	String line;
        	while ((line = reader.readLine()) != null) {
            	output.append(line).append(System.lineSeparator());
        	}
    	}
    	return output.toString();
}

    private static String getPublicClassName(File javaFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(javaFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("public class ")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 3) {
                        return parts[2].split("<")[0].trim();
                    }
                }
            }
        }
        return null;
    }

    private static String getPackageName(File javaFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(javaFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("package ")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        return parts[1].replace(";", "").trim();
                    }
                }
            }
        }
        return null;
    }

    private static void printProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    private static void printSummary() {
        System.out.println("\n=====================================");
        System.out.println("           GRADING SUMMARY");
        System.out.println("=====================================");
        System.out.printf("%-70s | %-30s%n", "Student File", "Result");
        System.out.println("---------------------------------------------------------------------------------");

        for (String[] entry : results) {
            System.out.printf("%-70s | %-30s%n", entry[0], entry[1]);
        }

        System.out.println("=====================================");
        System.out.println("Total Students Graded: " + results.size());
    }
}
