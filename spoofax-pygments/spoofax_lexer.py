import os
import subprocess

from pygments.lexer import Lexer
from pygments.token import Comment, Error, Keyword, Literal, Name, Number, Operator, Other, Punctuation, String, Token


class CustomLexer(Lexer):
    jar_location = os.path.abspath('spoofax-pygmentize-core.jar')

    def __init__(self, **options):
        super(CustomLexer, self).__init__(**options)

    def get_tokens_unprocessed(self, text):
        try:
            out = subprocess.check_output(['java', '-jar', self.jar_location,
                                           '--pt', self.options.get("language", "org/metaborg/org.metaborg.meta.lang.template"),
                                           '--preset', self.options.get("preset", "standard"),
                                           text])
            return eval(out)
        except:
            return [(0, Token.Error, text)]

    def analyse_text(text):
        # TODO: implement this function?
        return 0
