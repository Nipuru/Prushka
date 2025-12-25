import { useEffect, useState, useMemo, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Card, Table, Button, Space, Modal, Form, Input, InputNumber, Spin, message, Popconfirm, Descriptions, Tag, Switch, Select } from 'antd'
import { PlusOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { getSheetMetadata, getSheetList, insertSheet, updateSheet, deleteSheet, type SheetMetadata, type SheetData } from '@/api/sheet'

export default function SheetEdit() {
  const { sheetName } = useParams<{ sheetName: string }>()
  const navigate = useNavigate()
  const [form] = Form.useForm()

  const [loading, setLoading] = useState(true)
  const [tableLoading, setTableLoading] = useState(false)
  const [metadata, setMetadata] = useState<SheetMetadata | null>(null)
  const [allMetadata, setAllMetadata] = useState<SheetMetadata[]>([])
  const [dataSource, setDataSource] = useState<SheetData[]>([])
  const [total, setTotal] = useState(0)
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10 })

  const [modalVisible, setModalVisible] = useState(false)
  const [modalTitle, setModalTitle] = useState('新增')
  const [editingRecord, setEditingRecord] = useState<SheetData | null>(null)
  const [submitLoading, setSubmitLoading] = useState(false)

  useEffect(() => {
    const fetchMetadata = async () => {
      setLoading(true)
      try {
        const data = await getSheetMetadata()
        setAllMetadata(data)
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

  const fetchData = useCallback(async () => {
    if (!sheetName) return
    setTableLoading(true)
    try {
      const result = await getSheetList(sheetName, pagination)
      setDataSource(result.list)
      setTotal(result.total)
    } catch (error) {
      console.error('Failed to fetch sheet data:', error)
    } finally {
      setTableLoading(false)
    }
  }, [sheetName, pagination])

  useEffect(() => {
    if (metadata) {
      fetchData()
    }
  }, [metadata, fetchData])

  // 解析引用类型 resolved_type 格式: "表名.字段名"
  const getRefOptions = useCallback(
    (resolvedType: string) => {
      const [tableName, fieldName] = resolvedType.split('.')
      const refTable = allMetadata.find(m => m.table_name === tableName)
      if (!refTable || !refTable.data) return []

      const fieldIndex = refTable.fields.findIndex(f => f.name === fieldName)
      if (fieldIndex === -1) return []

      const options = refTable.data.map(row => row[fieldIndex]).filter(v => v !== null && v !== undefined)
      return [...new Set(options)].map(value => ({ label: String(value), value }))
    },
    [allMetadata]
  )

  const isArrayType = (type: string) => type.startsWith('[]')
  const getArrayElementType = (type: string) => type.match(/^\[\](.+)$/)?.[1] || 'string'
  const isNumberType = (type: string) => ['int32', 'int64', 'float32', 'float64'].includes(type)
  const isFloatType = (type: string) => ['float32', 'float64'].includes(type)
  const isBooleanType = (type: string) => type === 'bool'

  const renderFormItem = (field: SheetMetadata['fields'][0]) => {
    const { name, type, comment, resolved_type } = field
    const label = comment || name

    console.log(type, isNumberType(type))

    if (resolved_type) {
      return (
        <Form.Item key={name} name={name} label={label}>
          <Select
            mode={isArrayType(type) ? 'multiple' : undefined}
            allowClear
            showSearch
            placeholder={`请选择${label}`}
            options={getRefOptions(resolved_type)}
            filterOption={(input, option) =>
              String(option?.label ?? '').toLowerCase().includes(input.toLowerCase())
            }
          />
        </Form.Item>
      )
    }

    if (isArrayType(type)) {
      const elementType = getArrayElementType(type)
      return (
        <Form.Item key={name} name={name} label={label}>
          <Select
            mode="tags"
            allowClear
            placeholder={`请输入${label}，按回车添加`}
            tokenSeparators={[',']}
            {...(isNumberType(elementType) && {
              onChange: (values: string[]) => {
                form.setFieldValue(name, values.map(v => Number(v) || 0))
              }
            })}
          />
        </Form.Item>
      )
    }

    if (isNumberType(type)) {
      return (
        <Form.Item key={name} name={name} label={label}>
          <InputNumber style={{ width: '100%' }} precision={isFloatType(type) ? 6 : 0} />
        </Form.Item>
      )
    }

    if (isBooleanType(type)) {
      return (
        <Form.Item key={name} name={name} label={label} valuePropName="checked">
          <Switch />
        </Form.Item>
      )
    }

    return (
      <Form.Item key={name} name={name} label={label}>
        <Input.TextArea autoSize={{ minRows: 1, maxRows: 4 }} />
      </Form.Item>
    )
  }

  const handleAdd = () => {
    setModalTitle('新增')
    setEditingRecord(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record: SheetData) => {
    setModalTitle('编辑')
    setEditingRecord(record)
    form.setFieldsValue(record.data)
    setModalVisible(true)
  }

  const handleDelete = async (record: SheetData) => {
    try {
      await deleteSheet(record.id)
      message.success('删除成功')
      fetchData()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      setSubmitLoading(true)

      if (editingRecord) {
        await updateSheet({ id: editingRecord.id, name: sheetName!, data: values })
        message.success('更新成功')
      } else {
        await insertSheet({ id: 0, name: sheetName!, data: values })
        message.success('新增成功')
      }

      setModalVisible(false)
      fetchData()
    } catch (error) {
      console.error('Submit failed:', error)
    } finally {
      setSubmitLoading(false)
    }
  }

  const renderCellValue = (value: any) => {
    if (value === null || value === undefined) return '-'
    if (typeof value === 'boolean') return value ? '是' : '否'
    if (Array.isArray(value)) return value.join(', ')
    if (typeof value === 'object') return JSON.stringify(value)
    return String(value)
  }

  const columns: ColumnsType<SheetData> = useMemo(() => {
    if (!metadata) return []

    const fieldColumns: ColumnsType<SheetData> = metadata.fields.map(field => ({
      title: field.comment || field.name,
      dataIndex: ['data', field.name],
      key: field.name,
      ellipsis: true,
      width: 150,
      render: renderCellValue
    }))

    fieldColumns.push({
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确认删除?"
            description="删除后无法恢复"
            onConfirm={() => handleDelete(record)}
            okText="确认"
            cancelText="取消"
          >
            <Button type="link" size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    })

    return fieldColumns
  }, [metadata])

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
        <div style={{ textAlign: 'center', padding: '40px' }}>未找到配置表: {sheetName}</div>
      </Card>
    )
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      <Card
        title={
          <Space>
            <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/sheet')} />
            <span>{metadata.table_comment || metadata.table_name}</span>
          </Space>
        }
        size="small"
      >
        <Descriptions column={4} size="small">
          <Descriptions.Item label="表名">{metadata.table_name}</Descriptions.Item>
          <Descriptions.Item label="主键">
            <Tag color="blue">{metadata.indexes.key || '-'}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="字段数">{metadata.fields.length}</Descriptions.Item>
          <Descriptions.Item label="数据总数">{total}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card
        title="数据列表"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增
          </Button>
        }
      >
        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={tableLoading}
          scroll={{ x: 'max-content' }}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total,
            showSizeChanger: true,
            showTotal: t => `共 ${t} 条`,
            onChange: (page, pageSize) => setPagination({ current: page, pageSize })
          }}
        />
      </Card>

      <Modal
        title={modalTitle}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        confirmLoading={submitLoading}
        width={600}
        destroyOnClose
      >
        <Form form={form} layout="vertical" style={{ maxHeight: '60vh', overflowY: 'auto' }}>
          {metadata.fields
            .filter(f => f.name !== 'id')
            .map(f => renderFormItem(f))}
        </Form>
      </Modal>
    </div>
  )
}
