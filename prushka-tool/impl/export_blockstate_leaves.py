

import os
from .config import Config
from .common import get_xls_sheet, open_file, split_array, to_json_bool, to_json_str
from .check_excel import check_excel


table_name = 'st_blockstate_leaves'   # 表名

def export_blockstate_leaves(check = True):
    if check and check_excel(table_name) != 0: return
    
    xls_file = open_file( Config.excel_path, table_name )
    print('开始从%s导出树叶方块状态......' % ( table_name ))
    
    sheet = get_xls_sheet( xls_file, table_name )
    if sheet == None:
        print('%s表没找到名字叫%s的标签页' % ( table_name, table_name ))
        return
    
    labels = __getLabels( sheet.row_values( 1 ) )
    type_idx = __getLabelIndex( labels, 'type' )
    distance_idx = __getLabelIndex( labels, 'distance' )
    persistent_idx = __getLabelIndex( labels, 'persistent' )
    models_idx = __getLabelIndex( labels, 'models' )	
    
    __store_json(sheet, type_idx, distance_idx, persistent_idx, models_idx)

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

def __store_json(sheet, type_idx, distance_idx, persistent_idx, models_idx):
    objs = {}
    for i in range( 3, sheet.nrows ):
        line = sheet.row_values( i )
        type = to_json_str(line[type_idx])
        distance = to_json_str(line[distance_idx])
        persistent = to_json_bool(line[persistent_idx])
        models = split_array(to_json_str(line[models_idx]))
        m = ','.join(['{"model":%s}' % model for model in models])
        obj = '"distance=%s,persistent=%s":[%s]' % ( distance, persistent, m )
        if type in objs:
            objs[type].append(obj)
        else:
            objs[type] = [obj]
    for type in objs:
        __save_json_file(type, ','.join(objs[type]))
        
def __save_json_file(type, data):
    context = '{"variants":{%s}}'
    try:
        path = '%s/assets/minecraft/blockstates/%s.json' % (Config.resource_path, type)
        print('正在保存 %s' % (path))
        os.makedirs(os.path.dirname(path), exist_ok=True)
        f = open( path, 'w', encoding='utf-8' )
        f.write(  str((context % ( data )).encode('utf-8'), encoding='utf-8') )
        f.close()
    except Exception as e:
        print('导出树叶[%s]方块状态失败:[%s]' % (type, str(e)) )