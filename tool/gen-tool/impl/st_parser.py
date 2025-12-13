import re
import os

class StParser:
    def __init__(self, content):
        self.content = content
        self.table_name = None
        self.key = None
        self.vkey = None
        self.akey = None
        self.subkey = None
        self.unique = []
        self.fields = []
        self.data_rows = []

    def parse(self):
        lines = self.content.strip().split('\n')
        in_data_section = False
        in_table = False

        for line in lines:
            line = line.strip()

            if not line or line.startswith('//'):
                continue

            if line.startswith('table '):
                match = re.match(r'table\s+(\w+)\s*\{', line)
                if match:
                    self.table_name = match.group(1)
                    in_table = True
                continue

            if not in_table:
                continue

            if line == '}':
                in_table = False
                break

            if line.startswith('@data'):
                in_data_section = True
                continue

            if in_data_section:
                comment_idx = line.find('//')
                if comment_idx != -1:
                    line = line[:comment_idx].strip()
                if line.endswith(','):
                    line = line[:-1].strip()
                if line:
                    self.data_rows.append(line)
                continue

            if line.startswith('@key('):
                match = re.match(r'@key\((\w+)\)', line)
                if match:
                    self.key = match.group(1)
                continue

            if line.startswith('@vkey('):
                match = re.match(r'@vkey\((\w+)\)', line)
                if match:
                    self.vkey = match.group(1)
                continue

            if line.startswith('@akey('):
                match = re.match(r'@akey\((\w+)(?:,\s*subkey=(\w+))?\)', line)
                if match:
                    self.akey = match.group(1)
                    if match.group(2):
                        self.subkey = match.group(2)
                continue

            if line.startswith('@unique('):
                match = re.match(r'@unique\(\[([^\]]+)\]\)', line)
                if match:
                    fields = [f.strip() for f in match.group(1).split(',')]
                    self.unique.append(fields)
                continue

            field_match = re.match(r'(\w+):\s*(ref<[\w.]+>|[\w\[\]]+)?(?:\s*//\s*(.+))?', line)
            if field_match:
                field_name = field_match.group(1)
                field_type = field_match.group(2)
                field_comment = field_match.group(3) if field_match.group(3) else ''

                self.fields.append({
                    'name': field_name,
                    'type': field_type,
                    'comment': field_comment.strip(),
                })

        return self

    def get_table_config(self):
        config = {}

        if self.key:
            config['key'] = self.key
        if self.vkey:
            config['vkey'] = self.vkey
        if self.akey:
            config['akey'] = self.akey
        if self.subkey:
            config['subkey'] = self.subkey
        if self.unique:
            config['unique'] = self.unique

        return config

    def parse_data_row(self, row_str):
        values = []
        current = ""
        in_quotes = False
        in_array = 0

        i = 0
        while i < len(row_str):
            char = row_str[i]

            if char == '"':
                in_quotes = not in_quotes
                current += char
            elif char == '[':
                in_array += 1
                current += char
            elif char == ']':
                in_array -= 1
                current += char
            elif char == ',' and not in_quotes and in_array == 0:
                values.append(current.strip())
                current = ""
            else:
                current += char
            i += 1

        if current.strip():
            values.append(current.strip())

        return values

def parse_st_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    parser = StParser(content)
    return parser.parse()

def find_all_st_files(directory):
    st_files = []
    for file in os.listdir(directory):
        if file.endswith('.st'):
            st_files.append(os.path.join(directory, file))
    return st_files
