import os

from .table import Table
from .config import Config
from .common import get_xls_sheet, open_file, to_json_str
from .check_excel import check_excel

type_mapping = {
    "int32": "Int",
    "int64": "Long",
    "float32": "Float",
    "float64": "Double",
    "bool": "Boolean",
    "string": "String",
}

sheet_loader_template = """
// This file is auto-generated. DO NOT EDIT.
// Generated by tool
package top.nipuru.prushka.common.sheet

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder

object Sheet {
    fun load(tablePath: String) {
        val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
<sheets>
    }
}
"""



sheet_template = """
// This file is auto-generated. DO NOT EDIT.
// Generated by tool
package top.nipuru.prushka.common.sheet

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

<map>

data class <name>(
<field>
)

internal fun load<name>(gson: Gson, tablePath: String) {
    val jsonFile = File(tablePath, "<table_name>.json")
    val jsonString = jsonFile.readText()
    val type = object : TypeToken<List<<name>>>() {}.type
    val list = gson.fromJson<List<<name>>>(jsonString, type)
<mapping>
}
"""

def generate_code():
    sheets = []
    sheets.append("st_bitmap")
    
    for table_name in Table.tables:
        print('正在生成 %s' % (table_name))
        table = Table.tables[table_name]
        exclude = []
        if "exclude" in table:
            exclude = table["exclude"]
        class_name = snake_to_pascal(table_name)
        field_name = snake_to_camel(table_name)
        xls_file = open_file( Config.excel_path, table_name )
        sheet = get_xls_sheet( xls_file, table_name )
        if sheet == None:
            print('%s表没找到名字叫%s的标签页' % ( table_name, table_name ))
            continue
        
        comments = __getLabels( sheet.row_values( 0 ) )
        labels = __getLabels( sheet.row_values( 1 ) )
        column_types = __getLabels( sheet.row_values( 2 ) )
        key = []
        vkey = []
        akey = []
        subkey = []
        for i in range( len(labels) ):
            if labels[i] == '':
                continue
            if labels[i] in exclude:
                continue
            k = [snake_to_camel(labels[i]), __kotlin_type(column_types[i])]
            if "key" in table and table['key'] == labels[i]:
                key = k
            elif "vkey" in table and table['vkey'] == labels[i]:
                vkey = k
            elif "akey" in table and table['akey'] == labels[i]:
                akey = k
            elif "subkey" in table and table['subkey'] == labels[i]:
                subkey = k
        if key and akey:
            print('%s表同时存在key和akey' % (table_name))
            continue
        maps = []
        if key:
            maps.append(f"lateinit var {field_name}Map: Map<{key[1]}/* {key[0]} */, {class_name}>\n    private set")
        if vkey:
            maps.append(f"lateinit var {field_name}VMap: Map<{vkey[1]}/* {vkey[0]} */, {class_name}>\n    private set")
        if akey:
            if subkey:
                maps.append(f"lateinit var {field_name}AMap: Map<Pair<{akey[1]}/* {akey[0]} */, {subkey[1]}/* {subkey[0]} */>>, {class_name}>\n    private set")
            else:
                maps.append(f"lateinit var {field_name}AMap: Map<{akey[1]}/* {akey[0]} */, List<{class_name}>>\n    private set")
            
        fields = []
        for i in range( len(labels) ):
            if labels[i] == '':
                continue
            if labels[i] in exclude:
                continue
            name = snake_to_camel(labels[i])
            typ = __kotlin_type(column_types[i])
            comment = comments[i]
            fields.append(f"    /** {comment} */ \n    val {name}: {typ}")
            
        mapping = ""
        if key:
            mapping += f"    val map = mutableMapOf<{key[1]}, {class_name}>()\n"
        if vkey:
            mapping += f"    val vMap = mutableMapOf<{vkey[1]}, {class_name}>()\n"
        if akey:
            if subkey:
                mapping += f"    val aMap = mutableMapOf<Pair<{akey[1]}, {subkey[1]}>, {class_name}>()\n"
            else:
                mapping += f"    val aMap = mutableMapOf<{akey[1]}, MutableList<{class_name}>>()\n"
        mapping += "    for (data in list) {\n"
        if key:
            mapping += f"        map[data.{key[0]}] = data\n"
        if vkey:
            mapping += f"        vMap[data.{vkey[0]}] = data\n"
        if akey:
            if subkey:
                mapping += f"        aMap[data.{akey[0]} to data.{subkey[0]}] = data\n"
            else:
                mapping += f"        aMap.getOrPut(data.{akey[0]}) {{ mutableListOf() }}.add(data)\n"
        mapping += "    }\n"
        if key:
            mapping += f"    {field_name}Map = map\n"
        if vkey:
            mapping += f"    {field_name}VMap = vMap\n"
        if akey:
            mapping += f"    {field_name}AMap = aMap\n"
        mapping = mapping.rstrip()
        sheet_code = sheet_template.replace('<name>', class_name).replace('<table_name>', table_name).replace('<field>', ',\n'.join(fields)).replace('<map>', '\n'.join(maps)).replace('<mapping>', mapping)
        sheet_path = '%s/prushka-common/src/main/kotlin/top/nipuru/prushka/common/sheet/%s.kt' % (Config.code_path, class_name)
        write_file(sheet_path, sheet_code)
            
        table = Table.tables[table_name]
        sheets.append(table_name)
        
        
    load_sheets = []
    for sheet in sheets:
        load_sheets.append(f"        load{snake_to_pascal(sheet)}(gson, tablePath)")
    sheet_loader_code = sheet_loader_template.replace('<sheets>', '\n'.join(load_sheets))
    sheet_loader_path = '%s/prushka-common/src/main/kotlin/top/nipuru/prushka/common/sheet/Sheet.kt' % (Config.code_path)
    write_file(sheet_loader_path, sheet_loader_code)
    
def __kotlin_type(column_type):
    is_nullable = column_type.endswith("?")
    base_type = column_type.rstrip("?")
    kotlin_type = type_mapping.get(base_type)
    if kotlin_type is None:
        raise ValueError(f"不支持的数据类型: {column_type}")
    return f"{kotlin_type}?" if is_nullable else kotlin_type

def snake_to_pascal(snake_str):
    return ''.join(word.capitalize() for word in snake_str.split('_'))

def snake_to_camel(snake_str):
    words = snake_str.split('_')
    return words[0] + ''.join(word.capitalize() for word in words[1:])

def __getLabels( line ):
    labels = []
    for l in line:
        if l and l != '':
            labels.append( l.strip() )
        else:
            labels.append( '' )
    return labels

def write_file(path, context):
    print('正在保存 %s' % (path))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    f = open( path, 'w', encoding='utf-8' )
    f.write(  context.strip() )
    f.close()