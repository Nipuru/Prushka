import os
import xlrd
import json


xls_datas = {}

def open_file( xls_path, table ):
    xls_file = xls_datas.get(table)
    if xls_file:
        return xls_file
    file_name = xls_path + os.altsep + table + '.xlsx'
    try:
        xls_file = xlrd.open_workbook( file_name )
    except Exception as e:
        print('打开文件[%s]失败，检查Excel数据目录里是否存在[%s]' % (file_name, str(e)))
        return None
    xls_datas[table] = xls_file
    return xls_file

def get_xls_sheet( xls, sheet_name ):
    try:
        sheet = xls.sheet_by_name( sheet_name )
    except Exception as e:
        print(e)
        return None
    return sheet

def to_json_bool(value):
    result = False
    if isinstance( value, float ) or isinstance( value, int ):
        result = value == 1
    elif isinstance( value, str ):
        result = value.lower() == 'true'
    return 'true' if result else 'false'
    
def to_json_str( value ):
    if isinstance( value, float ) or isinstance( value, int ):
        if value - int( value ) > 0:
            return str( value )
        return str( int( value ) )
    if isinstance( value, str ):
        if value.strip() == '':
            return None
        value = value.replace( "\r", "" )
        value = value.replace( "\n", "\\n" )
        return str(value.encode('utf-8'), encoding='utf-8')
    return None
        
def split_array( text ):
    elements = []
    element = ""
    nested_level = 0
    in_string = False
    for char in text[1:-1].strip():
        if char == '"' and (not element or element[-1] != '\\'):
            in_string = not in_string
            element += char
        elif in_string:
            element += char
        elif char == '[' or char == '{':
            nested_level += 1
            element += char
        elif char == ']' or char == '}':
            nested_level -= 1
            element += char
        elif char == ',' and nested_level == 0:
            elements.append(element.strip())
            element = ""
        else:
            element += char
    if element:
        elements.append(element.strip())
    return elements
def is_array( text ):
    if len( text ) < 2:
        return False
    if text[0] == '[' and text[len(text) - 1] == ']':
        return True
    return False

