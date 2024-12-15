
ck_list = []

def initialize(excel_path, check_config, debug_log = True):
    ck_list.clear()
    
    for ck in ck_list:
        ck.initialize(excel_path, check_config)

def check_excel(table_name):
    err_num = 0
    for ck in ck_list:
        err_num = ck.check( table_name )
    if err_num != 0:
        print('检查表%s共发现%d个错误，先检查数据填写是否正确吧' % ( table_name, err_num ))
        return err_num
    return 0