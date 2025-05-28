

import os
from .config import Config
from .common import get_xls_sheet, open_file, to_json_bool, to_json_str
from .check_excel import check_excel


table_name = 'st_bitmap'   # 表名

def export_bitmap(check = True):
    if check and check_excel(table_name) != 0: return
    
    xls_file = open_file( Config.excel_path, table_name )
    print('开始从%s导出位图字体......' % ( table_name ))
    
    sheet = get_xls_sheet( xls_file, table_name )
    if sheet == None:
        print('%s表没找到名字叫%s的标签页' % ( table_name, table_name ))
        return
    
    labels = __getLabels( sheet.row_values( 1 ) )
    				

    row_idx = __getLabelIndex( labels, 'row' )
    column_idx = __getLabelIndex( labels, 'column' )
    ascent_idx = __getLabelIndex( labels, 'ascent' )
    height_idx = __getLabelIndex( labels, 'height' )	
    file_idx = __getLabelIndex( labels, 'file' )
    
    __store_json(sheet, row_idx, column_idx, ascent_idx, height_idx, file_idx)

def __getLabels( line ):
    labels = []
    for l in line:
        if l and l != '':
            labels.append( l.strip() )
        else:
            labels.append( '' )
    return labels    

def __getLabelIndex( line, label ):
    index = line.index( label )
    if index == -1:
        print('%s表没有%s字段' % ( table_name, label ))
    return index

def __store_json(sheet, row_idx, column_idx, ascent_idx, height_idx, file_idx):
    objs = []
    unicode = 0x1000
    for i in range( 3, sheet.nrows ):
        line = sheet.row_values( i )
        row = to_json_str(line[row_idx])
        column = to_json_str(line[column_idx])
        ascent = to_json_str(line[ascent_idx])
        height = to_json_str(line[height_idx])
        file = to_json_str(line[file_idx])
        chars = []
        
        for _ in range( int(row) ):
            line = ''
            for _ in range( int(column) ):
                line += '\\u%03x' % unicode
                unicode += 1
            chars.append('"%s"' % line)
        obj = '{"file":"%s","ascent":%s,"height":%s,"type":"bitmap","chars":[%s]}' % ( file, ascent, height, ",".join(chars) )
        objs.append(obj)
    data = ','.join(objs)
    if data != None:
        __save_json_file(data)
        
        
def __save_json_file(data):
    context = '{"providers":[%s]}'
    try:
        path = '%s/assets/minecraft/font/afyer_bitmap.json' % (Config.resource_path)
        print('正在保存 %s' % (path))
        os.makedirs(os.path.dirname(path), exist_ok=True)
        f = open( path, 'w', encoding='utf-8' )
        f.write(  str((context % ( data )).encode('utf-8'), encoding='utf-8') )
        f.close()
    except Exception as e:
        print('导出位图字体失败:[%s]' % (str(e)) )