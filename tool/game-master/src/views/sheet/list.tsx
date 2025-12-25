import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Card, Table, Input, Spin } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { getSheetMetadata, type SheetMetadata } from '@/api/sheet'

const { Search } = Input

export default function SheetList() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [metadata, setMetadata] = useState<SheetMetadata[]>([])
  const [filteredData, setFilteredData] = useState<SheetMetadata[]>([])
  const [searchText, setSearchText] = useState('')

  useEffect(() => {
    const fetchMetadata = async () => {
      setLoading(true)
      try {
        const data = await getSheetMetadata()
        setMetadata(data)
        setFilteredData(data)
      } catch (error) {
        console.error('Failed to fetch sheet metadata:', error)
      } finally {
        setLoading(false)
      }
    }
    fetchMetadata()
  }, [])

  const handleSearch = (value: string) => {
    setSearchText(value)
    if (!value) {
      setFilteredData(metadata)
    } else {
      const filtered = metadata.filter(
        item =>
          item.table_name.toLowerCase().includes(value.toLowerCase()) ||
          item.table_comment.toLowerCase().includes(value.toLowerCase())
      )
      setFilteredData(filtered)
    }
  }

  const columns: ColumnsType<SheetMetadata> = [
    {
      title: '表名',
      dataIndex: 'table_name',
      key: 'table_name',
      width: 200
    },
    {
      title: '表注释',
      dataIndex: 'table_comment',
      key: 'table_comment',
      width: 200
    },
    {
      title: '主键',
      dataIndex: ['indexes', 'key'],
      key: 'key',
      width: 120,
      render: (text: string | null) => text || '-'
    },
    {
      title: '字段数',
      key: 'fieldCount',
      width: 100,
      render: (_, record) => record.fields.length
    },
    {
      title: '数据行数',
      key: 'dataCount',
      width: 100,
      render: (_, record) => record.data.length
    },
    {
      title: 'Holder 类',
      dataIndex: 'holder_class',
      key: 'holder_class',
      ellipsis: true
    }
  ]

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
        <Spin size="large" tip="加载中..." />
      </div>
    )
  }

  return (
    <Card
      title="配置表列表"
      extra={
        <Search
          placeholder="搜索表名或注释"
          allowClear
          value={searchText}
          onChange={e => handleSearch(e.target.value)}
          onSearch={handleSearch}
          style={{ width: 250 }}
        />
      }
    >
      <Table
        columns={columns}
        dataSource={filteredData}
        rowKey="table_name"
        size="middle"
        pagination={{ pageSize: 15, showSizeChanger: true, showTotal: total => `共 ${total} 条` }}
        onRow={record => ({
          onClick: () => navigate(`/sheet/editor/${record.table_name}`),
          style: { cursor: 'pointer' }
        })}
      />
    </Card>
  )
}
