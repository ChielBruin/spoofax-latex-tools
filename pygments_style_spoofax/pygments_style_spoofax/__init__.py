from pygments.style import Style
from pygments.token import Comment, Error, Generic, Keyword, Literal, Name, Number, Operator, Punctuation, String, Text


class SpoofaxStyle(Style):
    """
    The default Spoofax colors.

    Source: https://github.com/metaborg/spoofax/blob/master/meta.lib.spoofax/editor/libspoofax/color/default.esv
    keyword    : 127 0 85 bold
    identifier : _
    string     : 0 0 255
    number     : 0 128 0
    var        : 139 69 19 italic
    operator   : 0 0 128
    layout     : 63 127 95 italic
    """
    default_style = ""
    styles = {
        Keyword:        'bold #7f0055',
        Name:           '#000000',
        String:         '#0000ff',
        Number:         '#008000',
        Name.Variable:  'italic #8B4513',
        Operator:       '#000080',
        Comment:        'italic #3f7f5f',

        # Tokens that Pygments has but Spoofax doesn't:
        Literal:        '#008000',  # = Number
        Punctuation:    '#000080',  # = Operator
    }
