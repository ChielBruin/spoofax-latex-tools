class DynamixLexer(AbstractSpoofaxLexer):
    def __init__(self, **options):
        table_location = 'dnx.tbl'
        types = {
            'LAYOUT': Comment,
            'INT': Number.Integer,
            'STRING': String.Double,
            'ID': Name.Variable,
            'PRIMNAME': Name.Builtin,
            'CONSTRUCTOR': Name.Namespace,
            'DEFNAME': Name.Function,
            'CONTNAME': Name.Constant,
            'LABEL': Name.Label,
            'LINKNAME': Name.Label,
            'NAMESPACE': Name.Namespace,
            'MODULENAME': Name.Constant,

            'Imports': Keyword.Declaration,
            'Import': Name.Namespace,
            'Header': Keyword.Declaration,
            'Module': Keyword.Declaration,
            'ModuleElem': Text,

            'Definitions': Keyword.Declaration,
            'Definition.Def': Text,
            'Definition.TypeDef': Name.Decorator,
            'Instruction': Name.Keyword      
        }
        super(DynamixLexer, self).__init__('DynamixLexer', table_location, types, default_type=Text, **options)
