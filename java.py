import os
import subprocess

student_folder = "student_submissions"

def run_java(filepath):
    basename = os.path.splitext(os.path.basename(filepath))[0]
    try:
        compile_proc = subprocess.run(["javac", filepath], capture_output=True, text=True)
        if compile_proc.returncode != 0:
            return "compile_error", compile_proc.stderr
        run_proc = subprocess.run(["java", "-cp", os.path.dirname(filepath), basename], capture_output=True, text=True)
        if run_proc.returncode != 0:
            return "runtime_error", run_proc.stderr
        return "success", run_proc.stdout
    except Exception as e:
        return "exception", str(e)

grades = {}

for filename in os.listdir(student_folder):
    if filename.endswith(".java"):
        student_file = os.path.join(student_folder, filename)
        print(f"Grading {filename}...")
        status, output = run_java(student_file)
        if status == "success":
            grades[filename] = 100
        elif status == "compile_error":
            grades[filename] = 50
            print(f"Compilation Error in {filename}:\n{output}")
        elif status == "runtime_error":
            grades[filename] = 75
            print(f"Runtime Error in {filename}:\n{output}")
        else:
            grades[filename] = 0
            print(f"Unexpected Error in {filename}:\n{output}")

for fname, grade in grades.items():
    print(f"{fname}: {grade}")
