import os

# Use raw string to avoid invalid escape sequence warning
project_directory = r'C:\workspaces\projet\MssPayProSaasFront\src'
output_file = r'C:\Users\FIRAS\Desktop\all_code.txt'

print(f"Starting to process files in {project_directory}")

# Function to read the first n lines of a file
def read_first_n_lines(file_path, n=10):
    lines = []
    try:
        with open(file_path, 'r', encoding='utf-8') as infile:
            for i in range(n):
                line = infile.readline()
                if not line:
                    break
                lines.append(line)
    except UnicodeDecodeError:
        print(f"UnicodeDecodeError encountered in {file_path}, trying latin-1 encoding")
        try:
            with open(file_path, 'r', encoding='latin-1') as infile:
                for i in range(n):
                    line = infile.readline()
                    if not line:
                        break
                    lines.append(line)
        except Exception as e:
            print(f"Failed to read {file_path}: {e}")
    return lines

with open(output_file, 'w', encoding='utf-8') as outfile:
    for root, dirs, files in os.walk(project_directory):
        for file in files:
            if file.endswith(('.ts', '.html', '.scss', '.css', '.json')):  # Include relevant file types
                file_path = os.path.join(root, file)
                print(f"Processing file: {file_path}")
                outfile.write(f"-----\nName: {file}\nPath: {file_path}\n-----\n")
                lines = read_first_n_lines(file_path)
                outfile.writelines(lines)
                outfile.write('\n\n')

print("All files have been processed.")
