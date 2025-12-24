import { service } from '@/utils/axios'

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
