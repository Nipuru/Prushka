export interface LoginParams {
  username: string
  password: string
}

export interface UserInfo {
  username: string
  token: string
}

export type ThemeMode = 'dark' | 'light'

export type LocaleType = 'zh_CN' | 'en'

export interface styleState {
  fontFamily?: string
  fontSize?: string
  lineHeight?: string
  color?: string
  backgroundColor?: string
  fontWeight?: string
  fontStyle?: string
  textShadow?: string
  textAlign?: string
}
