from pygments.token import Text, Name, Number, String, Comment, Punctuation, Other, Keyword, Operator


class FrameVMLexer(AbstractSpoofaxLexer):
    def __init__(self, **options):
        table_location = 'fvm.tbl'
        types = {
            'INT': Number,
            'SIntInstr.STC_IPush': Text,
            'SIntInstr': Keyword,
            'LABEL': Name.Function
        }
        arguments = ['--parseForest', 'LayoutSensitive', '--reducing', 'LayoutSensitive', '--stack', 'Basic']
        super(FrameVMLexer, self).__init__('FrameVMLexer', table_location, types, default_type=Text, arguments=arguments, **options)
