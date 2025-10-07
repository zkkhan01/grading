import os
import subprocess

def grade_submissions(solution_file, test_file, submissions_folder, base_name):
    """
    Grades multiple student submissions by renaming them one by one to the required base_name and running the test file.
    Prints the results directly to the terminal.
    
    Args:
      solution_file (str): Path to the solution file (optional for manual checking).
      test_file (str): Path to the test file that imports the student code using base_name.
      submissions_folder (str): Folder containing all student submission files.
      base_name (str): The base name the student submission must be renamed to (e.g. hw1.py).
    """
    submissions = [f for f in os.listdir(submissions_folder) if f.endswith('.py')]
    grades = {}

    for submission in submissions:
        orig_path = os.path.join(submissions_folder, submission)
        temp_path = os.path.join(submissions_folder, base_name)

        if os.path.exists(temp_path):
            os.remove(temp_path)

        os.rename(orig_path, temp_path)

        try:
            result = subprocess.run(['python', test_file], capture_output=True, text=True, timeout=10)
            grades[submission] = 100 if result.returncode == 0 else 0
        except Exception:
            grades[submission] = 0

        os.rename(temp_path, orig_path)

    for student, grade in grades.items():
        print(f"{student}: {grade}")
