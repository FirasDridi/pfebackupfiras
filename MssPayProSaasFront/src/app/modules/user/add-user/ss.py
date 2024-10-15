import os

# Use raw string to avoid invalid escape sequence warning
project_directory = r'C:\workspaces\AdminService\src\main\java\com\mss\adminservice'
resources_directory = r'C:\workspaces\AdminService\src\main\resources'
output_file = r'C:\Users\FIRAS\Desktop\all_code.txt'
included_directories = ['Config', 'Controller', 'Entities', 'Eureka', 'Repo', 'Service']

print(f"Starting to process files in {project_directory} and {resources_directory}")

def read_file_content(file_path):
    try:
        with open(file_path, 'r', encoding='utf-8') as infile:
            return infile.read()
    except UnicodeDecodeError:
        print(f"UnicodeDecodeError encountered in {file_path}, trying latin-1 encoding")
        try:
            with open(file_path, 'r', encoding='latin-1') as infile:
                return infile.read()
        except Exception as e:
            print(f"Failed to read {file_path}: {e}")
            return ''

with open(output_file, 'w', encoding='utf-8') as outfile:
    # Process .java files in the main project directory
    for root, dirs, files in os.walk(project_directory):
        # Check if the current directory is one of the included directories
        if any(dir_name.lower() in root.lower() for dir_name in included_directories) or root == project_directory:
            for file in files:
                if file.endswith('.java'):  # Only include .java files
                    file_path = os.path.join(root, file)
                    print(f"Processing file: {file_path}")
                    outfile.write(f"-----\nName: {file}\nPath: {file_path}\n-----\n")
                    content = read_file_content(file_path)
                    outfile.write(content)
                    outfile.write('\n\n')

    # Process .properties and application.yml files in the resources directory
    for root, dirs, files in os.walk(resources_directory):
        for file in files:
            if file.endswith('.properties') or file == 'application.yml':  # Include .properties and application.yml files
                file_path = os.path.join(root, file)
                print(f"Processing file: {file_path}")
                outfile.write(f"-----\nName: {file}\nPath: {file_path}\n-----\n")
                content = read_file_content(file_path)
                outfile.write(content)
                outfile.write('\n\n')

print("All files have been processed.")
