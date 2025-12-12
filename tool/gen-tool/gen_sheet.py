from impl.st_parser import find_all_st_files, parse_st_file
from impl.config import Config
from impl.export_excel import export_excel

if __name__ == '__main__':
    st_files = find_all_st_files(Config.excel_path)
    for st_file_path in st_files:
        try:
            st = parse_st_file(st_file_path)
            if st.table_name:
                export_excel(st.table_name)
        except Exception as e:
            print(f'处理st文件失败: {st_file_path}, 错误: {str(e)}')
