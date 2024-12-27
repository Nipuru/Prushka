import { Login } from "@/api/interface/index";
import qs from "qs";

import http from "@/api";

/**
 * @name 登录模块
 */
// * 用户登录接口
export const loginApi = (params: Login.ReqLoginForm) => {
	return http.post<Login.ResLogin>(`/hooks/login`, params);
};

// * 获取按钮权限
export const getAuthorButtons = () => {
	return http.get<Login.ResAuthButtons>(`/hooks/auth/buttons`);
};

// * 获取菜单列表
export const getMenuList = () => {
	return http.get<Menu.MenuOptions[]>(`/hooks/menu/list`);
};
