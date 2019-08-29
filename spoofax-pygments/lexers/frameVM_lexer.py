class FrameVMLexer(AbstractSpoofaxLexer):
    def __init__(self, **options):
        table_location = 'fvm.tbl'
        types = {
            # Common
            'LAYOUT': Comment,
            'INT': Number.Integer,
            'INTLEZ': Number.Integer,
            'INTLZ': Number.Integer,
            'CONTSLOT': Name.Constant,
            'Header': Keyword.Declaration,
            'PsdBlock': String.Doc,
            'LABEL': Name.Label,
            'Path': Punctuation,
            'EmptyPath': Punctuation,
            
            # Stacy
            'SInstr': Name.Builtin,
            'SOnReturnInstr': Name.Builtin,
            'SControlInstr': Name.Builtin,
            
            # Roger
            'Exp': Name.Builtin,
            'Stmt': Name.Builtin,
            'ControlStmt': Name.Builtin,
            'RGRVar': Name.Variable
        }
        arguments = ['--parseForest', 'LayoutSensitive', '--reducing', 'LayoutSensitive', '--stack', 'Basic']
        super(FrameVMLexer, self).__init__('FrameVMLexer', table_location, types, default_type=Text, arguments=arguments, **options)
