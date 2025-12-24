import type { AppConfig } from '@/types/config'
import type { AppModeEnum, ThemeEnum } from '@/enums/appEnum'
import type { RouteObject } from '@/router/types'

export interface UserInfo {
  username: string
  token: string
}

export interface UserState {
  userInfo: Nullable<UserInfo>
  token?: string
  sessionTimeout?: boolean
  lastUpdateTime: number
}

export interface MenuOptions {
  path: string
  title: string
  icon?: string
  isLink?: string
  close?: boolean
  children?: MenuOptions[]
}

export interface MenuState {
  menuList: MenuOptions[]
  isCollapse: boolean
}

export interface TagsState {
  visitedTags: RouteObject[]
  cachedTags: Set<string>
}

export interface AppState {
  appMode?: AppModeEnum

  themeMode?: ThemeEnum

  appConfig: AppConfig | null
}
