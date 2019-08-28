import re
import subprocess
import os
import json

from pygments.lexer import Lexer
from pygments.token import Comment, Error


class AbstractSpoofaxLexer(Lexer):
    parse_regex = r'\((?P<startIndex>[0-9]+), (?P<endIndex>-?[0-9]+), "(?P<sort>[A-Za-z0-9\_]+)", "(?P<constructor>[A-Za-z0-9\_]+)", \[(?P<children>.*)\]\)$'
    jar_location = os.path.abspath('spoofax-pygmentize-core.jar')

    name = None
    aliases = None
    filenames = None
    mimetypes = None

    def __init__(self, name, table, types, arguments=[], aliasses=[], filenames=[], mimetypes=[], default_type=Comment, **options):
        super(AbstractSpoofaxLexer, self).__init__(**options)
        self.name = name
        self.aliasses = aliasses
        self.filenames = filenames
        self.mimetypes = mimetypes

        self.table_location = os.path.abspath(table)
        self.arguments = arguments

        self.types = types
        self.default_type = default_type

    def get_tokens_unprocessed(self, text):
        out = subprocess.check_output(['java', '-jar', self.jar_location, '-pt', self.table_location] + self.arguments + [text])
        
        try:
            root = json.loads(out)
            for (start, end, type) in self._tokens_from_dict(root, []):
                yield (start, type, text[start:end])
            yield (len(text) - 1, Other, '\n')    # make sure the last token is always a newline, otherwise minted ignores the last token
        except:
            # There was some sort of parsing error
            yield (0, Error, text)

    def analyse_text(text):
        # TODO: implement this function?
        return 0

    def _tokens_from_dict(self, token, type_stack):
        sort = token['s']
        constructor = token['c']
        type_stack = [(sort, constructor)] + type_stack

        type = self._get_type(type_stack)
        yield (token['si'], token['ei'], type)
        
        for child in token['ch']:
            for result in self._tokens_from_dict(child, type_stack):
                yield result
          
    def _get_type(self, stack):
        for (sort, constructor) in stack:
            # Prefer a sort.constructor entry over a sort entry
            if not constructor is None:
                name = '%s.%s' % (sort, constructor)
                if name in self.types:
                    return self.types[name]
        
            if sort in self.types:
                return self.types[sort]
        return self.default_type
