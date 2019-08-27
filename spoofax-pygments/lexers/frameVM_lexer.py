from pygments.token import Text, Name, Number, String, Comment, Punctuation, Other, Keyword, Operator


class FrameVMLexer(AbstractSpoofaxLexer):
    def __init__(self, **options):
        table_location = 'fvm.tbl'
        types = {
            'INT': Number.Integer,
            'SInstr': Keyword.Reserved,
            'SStackInstr': Keyword.Reserved,
            'SIntInstr': Keyword.Reserved,
            'STypeInstr': Keyword.Reserved,
            'SContInstr': Keyword.Reserved,
            'SClosInstr': Keyword.Reserved,
            'SFrameInstr': Keyword.Reserved,
            'LABEL': Name.Label,
            'SControlInstr': Keyword.Reserved
        }
        arguments = ['--parseForest', 'LayoutSensitive', '--reducing', 'LayoutSensitive', '--stack', 'Basic']
        super(FrameVMLexer, self).__init__('FrameVMLexer', table_location, types, default_type=Text, arguments=arguments, **options)
