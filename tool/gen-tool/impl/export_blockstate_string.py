

import os
from .config import Config
from .common import get_xls_sheet, open_file, to_json_bool, to_json_str
from .check_excel import check_excel


table_name = 'st_blockstate_string'   # 表名

def export_blockstate_string(check = True):
    if check and check_excel(table_name) != 0: return
    
    xls_file = open_file( Config.excel_path, table_name )
    print('开始从%s导出线方块状态......' % ( table_name ))
    
    sheet = get_xls_sheet( xls_file, table_name )
    if sheet == None:
        print('%s表没找到名字叫%s的标签页' % ( table_name, table_name ))
        return
    
    labels = __getLabels( sheet.row_values( 1 ) )
    										
    disarmed_idx = __getLabelIndex( labels, 'disarmed' )
    attached_idx = __getLabelIndex( labels, 'attached' )
    north_idx = __getLabelIndex( labels, 'north' )
    south_idx = __getLabelIndex( labels, 'south' )	
    east_idx = __getLabelIndex( labels, 'east' )
    west_idx = __getLabelIndex( labels, 'west' )
    powered_idx = __getLabelIndex( labels, 'powered' )
    model1_idx = __getLabelIndex( labels, 'model1' )
    model2_idx = __getLabelIndex( labels, 'model2' )
    model3_idx = __getLabelIndex( labels, 'model3' )
    model4_idx = __getLabelIndex( labels, 'model4' )
    
    __store_json(sheet, disarmed_idx, attached_idx, north_idx, south_idx, east_idx, west_idx, powered_idx, model1_idx, model2_idx, model3_idx, model4_idx)

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

def __store_json(sheet, disarmed_idx, attached_idx, north_idx, south_idx, east_idx, west_idx, powered_idx, model1_idx, model2_idx, model3_idx, model4_idx):
    objs = []
    for i in range( 3, sheet.nrows ):
        line = sheet.row_values( i )
        disarmed = to_json_bool(line[disarmed_idx])
        attached = to_json_bool(line[attached_idx])
        northed = to_json_bool(line[north_idx])
        south = to_json_bool(line[south_idx])
        east = to_json_bool(line[east_idx])
        west = to_json_bool(line[west_idx])
        powered = to_json_bool(line[powered_idx])
        model1 = to_json_str(line[model1_idx])
        model2 = to_json_str(line[model2_idx])
        model3 = to_json_str(line[model3_idx])
        model4 = to_json_str(line[model4_idx])
        obj = '{"when":{"disarmed":%s,"attached":%s,"northed":%s,"south":%s,"east":%s,"west":%s,"powered":%s},"apply":[{"model":"%s"},{"model":"%s","y":90},{"model":"%s","y":180},{"model":"%s","y":270}]}' % ( disarmed, attached, northed, south, east, west, powered, model1, model2, model3, model4 )
        objs.append(obj)
    data = ','.join(objs)
    if data != None:
        __save_json_file(data)
        
        
def __save_json_file(data):
    context = '{"multipart": [%s]}'
    try:
        path = '%s/assets/minecraft/blockstates/tripwire.json' % (Config.resource_path)
        print('正在保存 %s' % (path))
        os.makedirs(os.path.dirname(path), exist_ok=True)
        f = open( path, 'w', encoding='utf-8' )
        f.write(  str((context % ( data )).encode('utf-8'), encoding='utf-8') )
        f.close()
    except Exception as e:
        print('导出线方块状态失败:[%s]' % (str(e)) )