from impl import table
from impl.config import Config
from impl.export_excel import export_excel

if __name__ == '__main__':
    export_bitmap()
    for t in table.tables:
        export_excel(t)
        
        