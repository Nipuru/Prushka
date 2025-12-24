import { service } from '@/utils/axios'
import { PageState } from '@/views/table/table-basic/types'

export interface SheetMetadata {
  table_name: string
  table_comment: string
  holder_class: string
  indexes: {
    key: string | null
    vkey: string | null
    akey: string | null
    subkey: string | null
  }
  unique: string[][]
  fields: {
    name: string
    type: string
    comment: string
    resolved_type: string | null
  }[]
  data: any[][]
}

// 获取表格元数据
export function getSheetMetadata(): Promise<SheetMetadata[]> {
  return service({
    url: '/sheet/getSheetMetadata',
    method: 'get'
  })
}

export function getSheetList(tableName: string, tableQuery: PageState) {
  return service({
    url: '/table/getTableList',
    method: 'get',
    params: { tableName: tableName, ...tableQuery }
  })
}

export function deleteSheet(id: number) {
  return service({
    url: '/table/getTableList',
    method: 'get',
    params: { id: id }
  })
}

export function insertSheet(tableName: string, data: any) {
  return service({
    url: '/table/getTableList',
    method: 'post',
    params: { tableName: tableName},
    data
  })
}

export function updateSheet(id: number, data: any) {
  return service({
    url: '/table/getTableList',
    method: 'post',
    params: { id: id },
    data
  })
}

