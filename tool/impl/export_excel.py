# 导出类型 json
import os
from .config import Config
from .common import get_xls_sheet, is_array, open_file, to_json_str
from .check_excel import check_excel
        
def export_excel(table_name, check = True):
    if check and check_excel(table_name) != 0: return
    
    xls_file = open_file( Config.excel_path, table_name )
    print('开始导入表%s......' % ( table_name ))
        
    sheet = get_xls_sheet( xls_file, table_name )
    if sheet == None:
        print('%s表没找到名字叫%s的标签页' % ( table_name, table_name ))
        return
    
    labels = __getLabels( sheet.row_values( 1 ) )
    column_types = __getLabels( sheet.row_values( 2 ) )
    label_indexes = []
    for i in range( len(labels) ):
        lable = labels[i]
        if lable == '':
            continue
        label_indexes.append(i)
    
    if len(label_indexes) == 0:
        print('%s表服务端字段行为空，无法生成' % ( table_name ))
        return
    
    __store_json(sheet, labels, label_indexes, column_types)
    
def __getLabels( line ):
    labels = []
    for l in line:
        if l and l != '':
            labels.append( l.strip() )
        else:
            labels.append( '' )
    return labels

def __store_json(sheet, labels, label_indexes, column_types):
    objs = []
    for i in range( 3, sheet.nrows ):
        line = sheet.row_values( i )
        obj = __add_json_line(label_indexes, labels, column_types, line)
        if obj != None:
            objs.append(obj)
    data = ',\n'.join(objs)
    if data != None:
        __save_json_file(sheet.name, data)
        
def __add_json_line(label_indexes, labels, column_types, line):
    fields = []
    for k in range( len( label_indexes ) ):
        idx = label_indexes[k]
        data = to_json_str( line[idx])
        if data == None:
            continue
        if column_types[idx].startswith('string'):
            fields.append( '"%s":"%s"' % ( labels[idx], data ) )
        else:
            fields.append( '"%s":%s' % ( labels[idx], data ) )
    return '{%s}' % ( ','.join( fields ) )

def __save_json_file(table_name, data):
    context = '[%s]'
    try:
        path = '%s/%s.json' % (Config.data_path, table_name)
        print('正在保存 %s' % (path))
        os.makedirs(os.path.dirname(path), exist_ok=True)
        f = open( path, 'w', encoding='utf-8' )
        f.write( str((context % ( data )).encode('utf-8'), encoding='utf-8') )
        f.close()
    except Exception as e:
        print('表%s保存 json 失败:[%s]' % (table_name, str(e)) )
        