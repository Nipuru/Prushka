

import os
from .config import Config
from .common import get_xls_sheet, open_file, to_json_str
from .check_excel import check_excel


table_name = 'st_blockstate_sapling'   # 表名

def export_blockstate_sapling(check = True):
    if check and check_excel(table_name) != 0: return
    
    xls_file = open_file( Config.excel_path, table_name )
    print('开始从%s导出树苗方块状态......' % ( table_name ))
    
    sheet = get_xls_sheet( xls_file, table_name )
    if sheet == None:
        print('%s表没找到名字叫%s的标签页' % ( table_name, table_name ))
        return
    
    labels = __getLabels( sheet.row_values( 1 ) )
    type_idx = __getLabelIndex( labels, 'type' )
    stage_idx = __getLabelIndex( labels, 'stage' )
    model_idx = __getLabelIndex( labels, 'model' )	
    
    __store_json(sheet, type_idx, stage_idx, model_idx)

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

def __store_json(sheet, type_idx, stage_idx, model_idx):
    objs = {}
    for i in range( 3, sheet.nrows ):
        line = sheet.row_values( i )
        type = to_json_str(line[type_idx])
        stage = to_json_str(line[stage_idx])
        model = to_json_str(line[model_idx])
        obj = '"stage=%s":{"model":"%s"}' % ( stage, model )
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
        print('导出树苗[%s]方块状态失败:[%s]' % (type, str(e)) )