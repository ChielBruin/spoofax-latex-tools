import re
import subprocess
import os

from pygments.lexer import Lexer
from pygments.token import Other


class AbstractSpoofaxLexer(Lexer):
    parse_regex = r'(?:\((?P<startIndex>[0-9]+), (?P<endIndex>-?[0-9]+), "(?P<sort>[A-Za-z0-9\_]+)", "(?P<constructor>[A-Za-z0-9\_]+)"\)(?:, )?)'
    jar_location = os.path.abspath('spoofax-pygmentize-core.jar')

    name = None
    aliases = None
    filenames = None
    mimetypes = None

    def __init__(self, name, table, types, arguments=[], aliasses=[], filenames=[], mimetypes=[], default_type=Other, **options):
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
        matches = re.findall(self.parse_regex, out)
        
        if len(matches) is 0:
            # There was some sort of parsing error
            yield (0, Error, text)
        else:
            for (start_index, end_index, sort, constructor) in matches:
                start_idx = int(start_index)
                end_idx = int(end_index)
                end_idx = len(text) - 1 if end_idx == -1 else end_idx
                sort = None if sort == 'null' else sort
                constructor = None if constructor == 'null' else constructor
            
                type = self._get_type(sort, constructor)
                yield (start_idx, type, text[start_idx:end_idx])
            yield (len(text) - 1, Other, '\n')    # make sure the last token is always a newline, otherwise minted ignores the last token

    def analyse_text(text):
        # TODO: implement this function?
        return 0

    def _get_type(self, sort, constructor):
        # Prefer a sort.constructor entry over a sort entry
        if not constructor is None:
            name = '%s.%s' % (sort, constructor)
            if name in self.types:
                return self.types[name]
        
        if sort in self.types:
            return self.types[sort]
        
        return self.default_type
