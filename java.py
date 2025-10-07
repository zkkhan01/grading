import os
import subprocess

# Paths (update these to match your folder structure)
solution_file = "Solution.java"
student_folder = "student_submissions"  # Folder containing student .java files

# Compile and run the solution file to check expected behavior (optional)
def run_java(filepath):
    basename = os.path.splitext(os.path.basename(filepath))[0]
    try:
        # Compile the Java file
        compile_proc = subprocess.run(["javac", filepath], capture_output=True, text=True)
        if compile_proc.returncode != 0:
            return False, compile_proc.stderr
        # Run the Java file
        run_proc = subprocess.run(["java", "-cp", os.path.dirname(filepath), basename], capture_output=True, text=True)
        if run_proc.returncode != 0:
            return False, run_proc.stderr
        return True, run_proc.stdout
    except Exception as e:
        return False, str(e)

# Grade student submissions
grades = {}

for filename in os.listdir(student_folder):
    if filename.endswith(".java"):
        student_file = os.path.join(student_folder, filename)
        print(f"Grading {filename}...")
        success, output = run_java(student_file)
        if success:
            grades[filename] = 100
        else:
            grades[filename] = 0
            print(f"Error for {filename}: {output}")

# Print grades
for fname, grade in grades.items():
    print(f"{fname}: {grade}")
