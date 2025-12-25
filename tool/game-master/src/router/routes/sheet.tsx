import { lazy } from '@loadable/component'
import type { RouteObject } from '../types'
import { LayoutGuard } from '../guard'
import { LazyLoad } from '@/components/LazyLoad'

// Sheet module page
const SheetRoute: RouteObject = {
  path: '/sheet',
  name: 'Sheet',
  element: <LayoutGuard />,
  meta: {
    title: '配置表',
    icon: 'table',
    orderNo: 2
  },
  children: [
    {
      path: 'list',
      name: 'SheetList',
      element: LazyLoad(lazy(() => import('@/views/sheet/list'))),
      meta: {
        title: '配置表列表',
        key: 'sheetList'
      }
    },
    {
      path: 'editor/:sheetName',
      name: 'SheetEdit',
      element: LazyLoad(lazy(() => import('@/views/sheet/edit'))),
      meta: {
        title: '配置表编辑',
        key: 'sheetEdit',
        hideMenu: true
      }
    }
  ]
}

export default SheetRoute
