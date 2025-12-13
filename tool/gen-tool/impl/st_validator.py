from .st_parser import StParser
import re

SUPPORTED_TYPES = {'int32', 'int64', 'float32', 'float64', 'bool', 'string'}

class ValidationError:
    def __init__(self, message, level='error'):
        self.message = message
        self.level = level

    def __str__(self):
        return f"[{self.level.upper()}] {self.message}"

class StValidator:
    def __init__(self, st_parser, file_path='', all_tables=None):
        self.st = st_parser
        self.file_path = file_path
        self.all_tables = all_tables or {}  # {table_name: StParser}
        self.errors = []
        self.warnings = []

    def validate(self):
        self.errors = []
        self.warnings = []

        self._check_table_name()
        self._check_fields()
        self._check_field_types()
        self._check_duplicate_fields()
        self._check_index_fields()
        self._check_key_conflicts()
        self._check_subkey_usage()
        self._check_ref_fields()

        return len(self.errors) == 0

    def _check_table_name(self):
        if not self.st.table_name:
            self.errors.append(ValidationError("缺少table定义"))

    def _check_fields(self):
        if not self.st.fields:
            self.errors.append(ValidationError("表必须至少有一个字段"))

    def _check_field_types(self):
        for field in self.st.fields:
            field_type = field['type']

            # ref 类型单独校验
            if field_type.startswith('ref<'):
                continue

            if field_type.startswith('[]'):
                element_type = field_type[2:]
                if element_type not in SUPPORTED_TYPES:
                    self.errors.append(ValidationError(
                        f"字段 '{field['name']}' 的类型 '{field_type}' 不支持，数组元素类型必须是: {', '.join(SUPPORTED_TYPES)}"
                    ))
            elif field_type not in SUPPORTED_TYPES:
                self.errors.append(ValidationError(
                    f"字段 '{field['name']}' 的类型 '{field_type}' 不支持，支持的类型: {', '.join(SUPPORTED_TYPES)}"
                ))

    def _check_duplicate_fields(self):
        field_names = [f['name'] for f in self.st.fields]
        seen = set()
        for name in field_names:
            if name in seen:
                self.errors.append(ValidationError(f"字段名 '{name}' 重复"))
            seen.add(name)

    def _check_index_fields(self):
        field_names = [f['name'] for f in self.st.fields]

        if self.st.key and self.st.key not in field_names:
            self.errors.append(ValidationError(f"@key 指定的字段 '{self.st.key}' 不存在"))

        if self.st.vkey and self.st.vkey not in field_names:
            self.errors.append(ValidationError(f"@vkey 指定的字段 '{self.st.vkey}' 不存在"))

        if self.st.akey and self.st.akey not in field_names:
            self.errors.append(ValidationError(f"@akey 指定的字段 '{self.st.akey}' 不存在"))

        if self.st.subkey and self.st.subkey not in field_names:
            self.errors.append(ValidationError(f"@subkey 指定的字段 '{self.st.subkey}' 不存在"))

    def _check_key_conflicts(self):
        if self.st.key and self.st.akey:
            self.errors.append(ValidationError("@key 和 @akey 不能同时使用"))

        if not self.st.key and not self.st.akey:
            self.warnings.append(ValidationError("表没有定义索引 (@key 或 @akey)", level='warning'))

    def _check_subkey_usage(self):
        if self.st.subkey and not self.st.akey:
            self.errors.append(ValidationError("@subkey 只能配合 @akey 使用"))

    def _check_unique_fields(self):
        field_names = [f['name'] for f in self.st.fields]

        for unique_group in self.st.unique:
            for field_name in unique_group:
                if field_name not in field_names:
                    self.errors.append(ValidationError(
                        f"@unique 中的字段 '{field_name}' 不存在"
                    ))

    def _check_ref_fields(self):
        for field in self.st.fields:
            field_type = field['type']

            if not field_type.startswith('ref<'):
                continue

            # 解析 ref<表名.字段>
            match = re.match(r'ref<(\w+)\.(\w+)>', field_type)
            if not match:
                self.errors.append(ValidationError(
                    f"字段 '{field['name']}' 的引用类型格式错误: '{field_type}'，正确格式: ref<表名.字段>"
                ))
                continue

            ref_table = match.group(1)
            ref_field = match.group(2)

            # 检查引用的表是否存在
            if ref_table not in self.all_tables:
                self.errors.append(ValidationError(
                    f"字段 '{field['name']}' 引用的表 '{ref_table}' 不存在"
                ))
                continue

            # 检查引用的字段是否存在
            ref_st = self.all_tables[ref_table]
            ref_field_info = None
            for f in ref_st.fields:
                if f['name'] == ref_field:
                    ref_field_info = f
                    break

            if ref_field_info is None:
                self.errors.append(ValidationError(
                    f"字段 '{field['name']}' 引用的字段 '{ref_table}.{ref_field}' 不存在"
                ))
                continue

            # 检查引用的字段是否是基本类型
            ref_field_type = ref_field_info['type']
            if ref_field_type not in SUPPORTED_TYPES:
                self.errors.append(ValidationError(
                    f"字段 '{field['name']}' 引用的字段 '{ref_table}.{ref_field}' 类型为 '{ref_field_type}'，引用类型只能引用基本类型"
                ))

    def get_report(self):
        report = []

        if self.file_path:
            report.append(f"文件: {self.file_path}")

        if self.st.table_name:
            report.append(f"表名: {self.st.table_name}")

        if self.errors:
            report.append(f"\n发现 {len(self.errors)} 个错误:")
            for error in self.errors:
                report.append(f"  {error}")

        if self.warnings:
            report.append(f"\n发现 {len(self.warnings)} 个警告:")
            for warning in self.warnings:
                report.append(f"  {warning}")

        if not self.errors and not self.warnings:
            report.append("[OK] 语法检查通过")

        return '\n'.join(report)

def validate_st_file(file_path):
    from .st_parser import parse_st_file

    try:
        st = parse_st_file(file_path)
    except Exception as e:
        return False, f"解析失败: {str(e)}"

    validator = StValidator(st, file_path)
    is_valid = validator.validate()
    report = validator.get_report()

    return is_valid, report
