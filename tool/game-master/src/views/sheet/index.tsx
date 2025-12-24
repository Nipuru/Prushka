import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Card, Descriptions, Table, Spin, Tag } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { getSheetMetadata, type SheetMetadata } from '@/api/sheet'

interface FieldInfo {
  name: string
  type: string
  comment: string
  resolved_type: string | null
}

export default function SheetDetail() {
  const { sheetName } = useParams<{ sheetName: string }>()
  const [loading, setLoading] = useState(true)
  const [metadata, setMetadata] = useState<SheetMetadata | null>(null)

  useEffect(() => {
    const fetchMetadata = async () => {
      setLoading(true)
      try {
        const data = await getSheetMetadata()
        const sheet = data.find(item => item.table_name === sheetName)
        setMetadata(sheet || null)
      } catch (error) {
        console.error('Failed to fetch sheet metadata:', error)
      } finally {
        setLoading(false)
      }
    }
    fetchMetadata()
  }, [sheetName])

  const fieldColumns: ColumnsType<FieldInfo> = [
    {
      title: '字段名',
      dataIndex: 'name',
      key: 'name',
      width: 150
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 120,
      render: (type: string) => <Tag color="blue">{type}</Tag>
    },
    {
      title: '解析类型',
      dataIndex: 'resolved_type',
      key: 'resolved_type',
      width: 150,
      render: (type: string | null) => type ? <Tag color="green">{type}</Tag> : '-'
    },
    {
      title: '注释',
      dataIndex: 'comment',
      key: 'comment'
    }
  ]

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
        <Spin size="large" tip="加载中..." />
      </div>
    )
  }

  if (!metadata) {
    return (
      <Card>
        <div style={{ textAlign: 'center', padding: '40px' }}>
          未找到配置表: {sheetName}
        </div>
      </Card>
    )
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      <Card title="基础信息">
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="表名">{metadata.table_name}</Descriptions.Item>
          <Descriptions.Item label="表注释">{metadata.table_comment}</Descriptions.Item>
          <Descriptions.Item label="主键">{metadata.indexes.key || '-'}</Descriptions.Item>
          <Descriptions.Item label="数据行数">{metadata.data.length}</Descriptions.Item>
          <Descriptions.Item label="Holder 类" span={2}>
            <code>{metadata.holder_class}</code>
          </Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="索引信息">
        <Descriptions column={4} bordered size="small">
          <Descriptions.Item label="Key">{metadata.indexes.key || '-'}</Descriptions.Item>
          <Descriptions.Item label="VKey">{metadata.indexes.vkey || '-'}</Descriptions.Item>
          <Descriptions.Item label="AKey">{metadata.indexes.akey || '-'}</Descriptions.Item>
          <Descriptions.Item label="SubKey">{metadata.indexes.subkey || '-'}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title={`字段列表 (${metadata.fields.length})`}>
        <Table
          columns={fieldColumns}
          dataSource={metadata.fields}
          rowKey="name"
          size="small"
          pagination={false}
        />
      </Card>
    </div>
  )
}
