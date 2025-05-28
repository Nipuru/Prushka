

import os
from .config import Config
from .common import get_xls_sheet, open_file, to_json_bool, to_json_str
from .check_excel import check_excel


table_name = 'st_blockstate_note'   # 表名

def export_blockstate_note(check = True):
    if check and check_excel(table_name) != 0: return
    
    xls_file = open_file( Config.excel_path, table_name )
    print('开始从%s导出音符盒方块状态......' % ( table_name ))
    
    sheet = get_xls_sheet( xls_file, table_name )
    if sheet == None:
        print('%s表没找到名字叫%s的标签页' % ( table_name, table_name ))
        return
    
    labels = __getLabels( sheet.row_values( 1 ) )
    instrument_idx = __getLabelIndex( labels, 'instrument' )
    note_idx = __getLabelIndex( labels, 'note' )
    powered_idx = __getLabelIndex( labels, 'powered' )
    model_idx = __getLabelIndex( labels, 'model' )	
    x_idx = __getLabelIndex( labels, 'x' )
    y_idx = __getLabelIndex( labels, 'y' )
    
    __store_json(sheet, instrument_idx, note_idx, powered_idx, model_idx, x_idx, y_idx)

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

def __store_json(sheet, instrument_idx, note_idx, powered_idx, model_idx, x_idx, y_idx):
    objs = []
    for i in range( 3, sheet.nrows ):
        line = sheet.row_values( i )
        instrument = to_json_str(line[instrument_idx])
        note = to_json_str(line[note_idx])
        powered = to_json_bool(line[powered_idx])
        model = to_json_str(line[model_idx])
        x = to_json_str(line[x_idx])
        y = to_json_str(line[y_idx])
        m = '"model":"%s"' % model
        if x != '0':
            m += ',"x":%s' % x
        if y != '0':
            m += ',"y":%s' % y
        obj = '"instrument=%s,note=%s,powered=%s":{%s}' % ( instrument, note, powered, m )
        objs.append(obj)
    data = ','.join(objs)
    if data != None:
        __save_json_file(data)
        
        
def __save_json_file(data):
    context = '{"variants":{%s}}'
    try:
        path = '%s/assets/minecraft/blockstates/note_block.json' % (Config.resource_path)
        print('正在保存 %s' % (path))
        os.makedirs(os.path.dirname(path), exist_ok=True)
        f = open( path, 'w', encoding='utf-8' )
        f.write(  str((context % ( data )).encode('utf-8'), encoding='utf-8') )
        f.close()
    except Exception as e:
        print('导出音符盒方块状态失败:[%s]' % (str(e)) )