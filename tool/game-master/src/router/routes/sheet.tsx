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
    orderNo: 2,
  },
  children: [
    {
      path: ':sheetName',
      name: 'SheetDetail',
      element: LazyLoad(lazy(() => import('@/views/sheet'))),
      meta: {
        title: '配置表详情',
        key: 'sheetDetail',
        hideMenu: true
      }
    }
  ]
}

export default SheetRoute
