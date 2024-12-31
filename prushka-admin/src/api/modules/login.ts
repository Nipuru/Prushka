import http from "@/api";

/**
 * @name 登录模块
 */
// * 用户登录接口
export const login = (params: { username: string, password: string }) => {
	return http.post(`/admin/auth/login`, params);
};

// * 获取按钮权限
export const getAuthorButtons = () => {
	return http.get(`/admin/auth/buttons`);
};

// * 获取菜单列表
export const getMenuList = () => {
	return http.get(`/admin/auth/menus`);
};
