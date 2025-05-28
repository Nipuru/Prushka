// * 请求响应参数(包含data)
export interface Result<T = any> {
	code: string;
	msg: string;
	data?: T;
}

// * 分页响应参数
export interface TableData<T> {
	pageNum: number;
	pageSize: number;
	total: number;
	list: T[];
}