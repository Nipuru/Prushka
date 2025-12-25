import type { AppMenu } from '../types'
import { basicRoutes } from '..'
import { transformRouteToMenu } from '../helpers'
import { filter } from '@/utils/helper/treeHelper'

// Get async menus
export async function getAsyncMenus(): Promise<AppMenu[]> {
  const staticMenus = transformRouteToMenu(basicRoutes)
  staticMenus.sort((a, b) => {
    return (a?.orderNo || staticMenus.length) - (b?.orderNo || staticMenus.length)
  })

  return filter(staticMenus, item => !item.hideMenu)
}
