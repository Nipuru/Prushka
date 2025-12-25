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

export interface SheetData {
  id: number
  name: string
  data: any
}

// 获取表格元数据
export function getSheetMetadata(): Promise<SheetMetadata[]> {
  return service({
    url: '/sheet/getSheetMetadata',
    method: 'get'
  })
}

export interface SheetListResult {
  list: SheetData[]
  total: number
}

export function getSheetList(tableName: string, tableQuery: PageState): Promise<SheetListResult> {
  return service({
    url: '/sheet/getSheetList',
    method: 'get',
    params: { tableName, ...tableQuery }
  })
}

export function deleteSheet(id: number) {
  return service({
    url: '/sheet/deleteSheet',
    method: 'get',
    params: { id }
  })
}

export function insertSheet(data: SheetData) {
  return service({
    url: '/sheet/insertSheet',
    method: 'post',
    data
  })
}

export function updateSheet(data: SheetData) {
  return service({
    url: '/sheet/updateSheet',
    method: 'post',
    data
  })
}

