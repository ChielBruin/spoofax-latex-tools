import json
import os
import subprocess
import sys
import traceback

import pygments.formatters
from pygments.formatters.latex import LatexFormatter
from pygments.lexer import Lexer
from pygments.style import Style, StyleMeta
from pygments.token import Token
# check if pygments still has a compatibility layer
try:
    from pygments.util import add_metaclass
    has_deprecated_pygments = True
except ImportError:
    has_deprecated_pygments = False


# Subclass of Style that we can mess with without interfering with the original implementation
class EmptyStyle(Style):
    pass


# This file is loaded when Pygmentize reads `-f spoofax_lexer.py` in its argument list.
# The Lexer has two options:
#     "language" - the language (SDF and ESV) to load from your Maven repository
#     "parseTable" - an `sdf.tbl` file to use for parsing
#     "esv" - an `editor.esv.af` file to use for coloring
#     "preset" - the JSGLR2 parser preset to use (e.g. layoutSensitive)
class CustomLexer(Lexer):
    jar_location = os.path.abspath("spoofax-pygmentize-core.jar")

    def __init__(self, **options):
        super(CustomLexer, self).__init__(**options)
        self.options = options

    def get_tokens_unprocessed(self, text):
        try:
            language = self.options.get("language")
            sdf = self.options.get("parseTable")
            esv = self.options.get("esv")
            if not language and not sdf:
                raise Exception("""Either the option `language` or `parseTable` should be passed to the Spoofax lexer.
    Example: \\begin{minted}{spoofax_lexer.py -O "language=org/metaborg/org.metaborg.meta.lang.template" -x}""")

            command = ['java', '-jar', self.jar_location, "-v"]
            if language:
                command.extend(["--language", language])
            if sdf:
                command.extend(["--parseTable", sdf])
            if esv:
                command.extend(["--esv", esv])
            command.extend(['--preset', self.options.get("preset", "standard"), text])

            command_output = subprocess.check_output(command)

            # If the object that Java returned is empty, just return one big error token.
            if len(command_output) < 5:
                return [(0, Token.Error, text)]

            out = json.loads(command_output.decode("utf-8"))

            # Load the styles found by the Java implementation in the EmptyStyle class,
            # so that the CustomFormatter can later access them.
            EmptyStyle.styles = {}
            for tokenKind, styleString in out["styles"].items():
                # From the Pygments doc: "You can create a new token type by accessing an attribute of Token".
                # See http://pygments.org/docs/tokens/
                # The attribute however is dynamic, based on the name passed from the JSON, so we have to `eval` it
                new_token = eval("Token.Generic." + tokenKind)
                EmptyStyle.styles[new_token] = styleString

            return [(o["offset"], eval("Token.Generic." + o["tokenKind"]), o["tokenString"]) for o in out["tokens"]]
        except Exception as e:
            print("spoofax_lexer.py threw an exception:", file=sys.stderr)
            traceback.print_exc()
            return [(0, Token.Error, text)]

    def analyse_text(text):
        # TODO: implement this function?
        # This function should return a likelihood that the current lexer can lex this file.
        # However, JSGLR2 doesn't support this, so always return 0
        return 0


class CustomFormatter(LatexFormatter):

    def format_unencoded(self, tokensource, outfile):
        # This line will force the CustomLexer above to produce a stream of tokens,
        # which will also store styles in the EmptyStyle class
        tokenlist = list(tokensource)

        # This extra style class is created to trigger the __new__ method in StyleMeta, to initialize the styles
        # that have been set in the CustomLexer
        if has_deprecated_pygments:
            @add_metaclass(StyleMeta)
            class ExtraStyle(EmptyStyle):
                pass
        else:
            class ExtraStyle(EmptyStyle):
                pass


        outfile.write("{\n")

        # Make sure that the settings for writing custom style commands (see next block) has the right settings.
        # The command prefix is `PYGdefault` when no style has been set using `\usemintedstyle`.
        cp = self.commandprefix
        cp = (cp + "default") if cp == "PYG" else cp
        self.options["commandprefix"] = cp
        self.options["style"] = ExtraStyle

        # The style commands in LaTeX are written directly to the block of code, instead of to the pygstyle file
        # because the style can be different for every block of code (yay for ESV).
        outfile.write("\\makeatletter\n")
        for name, definition in LatexFormatter(**self.options).cmd2def.items():
            outfile.write(r'\expandafter\def\csname %s@tok@%s\endcsname{%s}' % (cp, name, definition))
            outfile.write("\n")
        outfile.write("\\makeatother\n")

        # After writing the style commands, just print out the Verbatim environment as usual
        LatexFormatter.format_unencoded(self, tokenlist, outfile)
        outfile.write("}\n")


# In the Minted source code, Pygmentize is called as follows:
#     \MintedPygmentize\space -l #2 -f latex -P commandprefix=PYG -F tokenmerge
# where the `\MintedPygmentize` command defaults to `pygmentize`.
# This makes it impossible to pass a custom formatter to Pygmentize, because any value passed to `-f` in argument #2
# will always be overwritten by the `latex` formatter.
# Therefore, the following line makes Pygments think that the LaTeX formatter (selected by Minted via `-f latex`)
# has already been loaded, secretly substituting our own formatter.
#
# See https://github.com/gpoore/minted/blob/master/source/minted.sty#L767-L768
pygments.formatters._formatter_cache["LaTeX"] = CustomFormatter
