import os
from .config import Config
from .st_parser import parse_st_file

def export_bitmap():
    st_file_path = os.path.join(Config.excel_path, 'bitmap.st')

    if not os.path.exists(st_file_path):
        print('bitmap.st文件不存在，跳过位图字体导出')
        return

    try:
        st = parse_st_file(st_file_path)
    except Exception as e:
        print('解析bitmap.st失败: %s' % str(e))
        return

    if not st.data_rows:
        print('bitmap.st没有@data数据，跳过位图字体导出')
        return

    print('开始从bitmap.st导出位图字体......')

    field_names = [f['name'] for f in st.fields]

    try:
        row_idx = field_names.index('row')
        column_idx = field_names.index('column')
        ascent_idx = field_names.index('ascent')
        height_idx = field_names.index('height')
        file_idx = field_names.index('file')
    except ValueError as e:
        print('bitmap表缺少必要字段: %s' % str(e))
        return

    objs = []
    unicode = 0x1000

    for row_str in st.data_rows:
        values = st.parse_data_row(row_str)

        if len(values) <= max(row_idx, column_idx, ascent_idx, height_idx, file_idx):
            continue

        row = values[row_idx].strip()
        column = values[column_idx].strip()
        ascent = values[ascent_idx].strip()
        height = values[height_idx].strip()
        file = values[file_idx].strip()

        chars = []
        for _ in range(int(row)):
            line = ''
            for _ in range(int(column)):
                line += '\\u%03x' % unicode
                unicode += 1
            chars.append('"%s"' % line)

        obj = '{"file":"%s","ascent":%s,"height":%s,"type":"bitmap","chars":[%s]}' % (
            file, ascent, height, ",".join(chars)
        )
        objs.append(obj)

    data = ','.join(objs)
    if data:
        __save_json_file(data)

def __save_json_file(data):
    context = '{"providers":[%s]}'
    try:
        path = '%s/assets/prushka/font/bitmap.json' % (Config.resource_path)
        print('正在保存 %s' % (path))
        os.makedirs(os.path.dirname(path), exist_ok=True)
        f = open(path, 'w', encoding='utf-8')
        f.write(str((context % (data)).encode('utf-8'), encoding='utf-8'))
        f.close()
    except Exception as e:
        print('导出位图字体失败:[%s]' % (str(e)))
