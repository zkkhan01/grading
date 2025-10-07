import os
import re
import subprocess

def renameFiles(hwn):
    for file in os.listdir(hwn):
        extract = re.findall(r'- (\D.*)\.java', file)
        try:
            new_name = re.sub(r'\W', '', extract[0]) + '.java'
        except:
            continue
        src = os.path.join(hwn, file)
        dst = os.path.join(hwn, new_name)
        os.rename(src, dst)

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

def grade_submissions(solution_file, submissions_folder, base_name):
    # Assuming grading logic similar to previous examples
    grades = {}
    for filename in os.listdir(submissions_folder):
        if filename.endswith(".java"):
            student_file = os.path.join(submissions_folder, filename)
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
    return grades

if __name__ == '__main__':
    submissions_folder = input("Enter the folder path where the submission files are: ").strip()
    sol_file = input("Enter the solution file path: ").strip()
    base_name = input("Enter the base file name to rename submissions to (e.g., hw0.java): ").strip()

    renameFiles(submissions_folder)
    final_grades = grade_submissions(sol_file, submissions_folder, base_name)

    print("\nFinal grades:")
    for submission, grade in final_grades.items():
        print(f"{submission}: Grade = {grade}")
