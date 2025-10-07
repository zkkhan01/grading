import os
import importlib.util
import doctest
import sys
import re
from io import StringIO
import contextlib

def renameFiles(hwn):
    for file in os.listdir(hwn):
        extract = re.findall(r'- (\D.*)\.py', file)
        try:
            new_name = re.sub(r'\W', '', extract[0]) + '.py'
        except:
            continue
        src = os.path.join(hwn, file)
        dst = os.path.join(hwn, new_name)
        os.rename(src, dst)


def grade_submission_doctest(submission_path, test_file, base_name, submissions_folder):
    temp_path = os.path.join(submissions_folder, base_name)

    if os.path.exists(temp_path):
        os.remove(temp_path)

    os.rename(submission_path, temp_path)

    try:
        sys.path.insert(0, submissions_folder)

        if base_name[:-3] in sys.modules:
            del sys.modules[base_name[:-3]]

        importlib.invalidate_caches()
        module = importlib.import_module(base_name[:-3])

        output_buffer = StringIO()
        with contextlib.redirect_stdout(output_buffer):
            failure_count, test_count = doctest.testfile(
                test_file, module_relative=False, verbose=False, optionflags=doctest.ELLIPSIS
            )
        output = output_buffer.getvalue()

        grade = 100 if failure_count == 0 else 0

        if failure_count > 0:
            print(f"\n*** Test failures for student: {os.path.basename(submission_path)} ***")
            print(output)
            print(f"Failed {failure_count} out of {test_count} tests.\n")

    except Exception as e:
        print(f"Error grading {os.path.basename(submission_path)}: {e}")
        grade = 0
    finally:
        sys.path.remove(submissions_folder)
        os.rename(temp_path, submission_path)

    return grade


def grade_submissions(test_file, submissions_folder, base_name):
    submissions = [f for f in os.listdir(submissions_folder) if f.endswith('.py')]
    grades = {}

    for submission in submissions:
        submission_path = os.path.join(submissions_folder, submission)
        print(f"Grading submission: {submission}")
        grade = grade_submission_doctest(submission_path, test_file, base_name, submissions_folder)
        grades[submission] = grade

    return grades


if __name__ == '__main__':
    submissions_folder = input("Enter the folder path where the submission files are: ").strip()
    test_file = input("Enter the test file path: ").strip()
    base_name = input("Enter the base file name to rename submissions to (e.g., hw0.py): ").strip()

    renameFiles(submissions_folder)
    final_grades = grade_submissions(test_file, submissions_folder, base_name)

    print("\nFinal grades:")
    for submission, grade in final_grades.items():
        print(f"{submission}: Grade = {grade}")

