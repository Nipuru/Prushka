import { useEffect, useState, useMemo, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Card,
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  InputNumber,
  Spin,
  message,
  Popconfirm,
  Descriptions,
  Tag,
  Switch
} from 'antd'
import { PlusOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import {
  getSheetMetadata,
  getSheetList,
  insertSheet,
  updateSheet,
  deleteSheet,
  type SheetMetadata,
  type SheetData
} from '@/api/sheet'

export default function SheetEdit() {
  const { sheetName } = useParams<{ sheetName: string }>()
  const navigate = useNavigate()
  const [form] = Form.useForm()

  const [loading, setLoading] = useState(true)
  const [tableLoading, setTableLoading] = useState(false)
  const [metadata, setMetadata] = useState<SheetMetadata | null>(null)
  const [dataSource, setDataSource] = useState<SheetData[]>([])
  const [total, setTotal] = useState(0)
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10 })

  const [modalVisible, setModalVisible] = useState(false)
  const [modalTitle, setModalTitle] = useState('新增')
  const [editingRecord, setEditingRecord] = useState<SheetData | null>(null)
  const [submitLoading, setSubmitLoading] = useState(false)

  // 获取元数据
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

  // 获取数据列表
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

  // 根据字段类型渲染表单项
  const renderFormItem = (field: SheetMetadata['fields'][0]) => {
    const { name, type, comment } = field
    const label = comment || name

    // 数字类型
    if (type === 'Int' || type === 'Long' || type === 'Double' || type === 'Float') {
      return (
        <Form.Item key={name} name={name} label={label}>
          <InputNumber style={{ width: '100%' }} />
        </Form.Item>
      )
    }

    // 布尔类型
    if (type === 'Boolean') {
      return (
        <Form.Item key={name} name={name} label={label} valuePropName="checked">
          <Switch />
        </Form.Item>
      )
    }

    // 默认文本
    return (
      <Form.Item key={name} name={name} label={label}>
        <Input.TextArea autoSize={{ minRows: 1, maxRows: 4 }} />
      </Form.Item>
    )
  }

  // 新增
  const handleAdd = () => {
    setModalTitle('新增')
    setEditingRecord(null)
    form.resetFields()
    setModalVisible(true)
  }

  // 编辑
  const handleEdit = (record: SheetData) => {
    setModalTitle('编辑')
    setEditingRecord(record)
    // 表单填充 data 内的字段数据
    form.setFieldsValue(record.data)
    setModalVisible(true)
  }

  // 删除
  const handleDelete = async (record: SheetData) => {
    try {
      await deleteSheet(record.id)
      message.success('删除成功')
      fetchData()
    } catch (error) {
      message.error('删除失败')
    }
  }

  // 提交表单
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

  // 渲染单元格值
  const renderCellValue = (value: any) => {
    if (value === null || value === undefined) return '-'
    if (typeof value === 'boolean') return value ? '是' : '否'
    if (typeof value === 'object') return JSON.stringify(value)
    return String(value)
  }

  // 动态生成表格列
  const columns: ColumnsType<SheetData> = useMemo(() => {
    if (!metadata) return []

    const fieldColumns: ColumnsType<SheetData> = metadata.fields.map(field => ({
      title: field.comment || field.name,
      dataIndex: ['data', field.name], // 访问嵌套的 data 对象
      key: field.name,
      ellipsis: true,
      width: 150,
      render: renderCellValue
    }))

    // 添加操作列
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
      {/* 基本信息 */}
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

      {/* 数据表格 */}
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

      {/* 新增/编辑弹窗 */}
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
            .filter(f => f.name !== 'id') // id 字段不允许编辑
            .map(field => renderFormItem(field))}
        </Form>
      </Modal>
    </div>
  )
}
