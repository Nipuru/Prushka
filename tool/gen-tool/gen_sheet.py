from impl.table import Table

from impl.export_excel import export_excel

if __name__ == '__main__':
    for t in Table.tables:
        export_excel(t)
        
        