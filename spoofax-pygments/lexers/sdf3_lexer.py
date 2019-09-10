class SDF3Lexer(AbstractSpoofaxLexer):
    def __init__(self, **options):
        table_location = 'sdf3.tbl'
        types = {
            'LAYOUT': Comment,
            'StrCon': String,
            'Lit': String,
            'CiLit': String,

            # Module declaration
            'Module': Keyword.Namespace,
            'ModuleName': Text,

            # Imports
            'ImpSection': Keyword.Namespace,
            'Import': Text,

            # Grammar sections
            'Grammar': Keyword.Namespace,
            'Section': Keyword.Namespace,
            'Symbols': Text,  # Nested inside 'start-symbols' declarations
            'GeneralProduction': Text,  # Nested inside 'context-free syntax'
            'SdfProduction': Text,  # Nested inside '(lexical) syntax'
            'DeclSymbol': Text,  # Nested inside 'sorts'

            # Templates
            'Template': Operator,  # The angular/square brackets delimiting a template
            'TemplatePart1': Operator,  # The angular brackets delimiting a placeholder
            'TemplatePart2': Operator,  # The square brackets delimiting a placeholder
            'TemplateString1': String,  # The template text outside of placeholders
            'TemplateString2': String,  # The template text outside of placeholders
            'TemplateEscape1': Text,  # Escape characters like \\, \<, \>
            'TemplateEscape2': Text,  # Escape characters like \\, \[, \]
            'PlaceholderOptions': Keyword.Namespace,

            # Symbols
            'Symbol': Operator,  # Symbol productions are mostly things like <<Symbol>?> etc.
            'Symbol.Cf': Keyword,  # Except for -CF, -LEX, -VAR and LAYOUT; those are keywords
            'Symbol.Lex': Keyword,
            'Symbol.Varsym': Keyword,
            'Symbol.Layout': Keyword,
            'Symbol.Sort': Text,  # This is the base symbol, i.e. the sort name
            'Constructor': Name.Variable,

            # Character Classes
            'CharClass': Operator,  # The square brackets around the character class, as well as operators (~, /\, ...)
            'CharRange': Text,
            'ShortChar.Escaped': String,  # Escaped characters (\n, \ , \t, \@, \$, ...)
            'ShortChar.Regular': Number,  # Regular (alphanumeric) characters
            'NumChar.Digits': Number,  # Numeric escape sequence: e.g. \32
            'Character': Keyword,  # All other types of characters are keywords (\EOF, \TOP, ...)

            'ATermAttribute': Keyword,
        }
        super(SDF3Lexer, self).__init__('SDF3Lexer', table_location, types, default_type=Text, **options)
