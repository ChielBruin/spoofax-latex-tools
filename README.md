# Spoofax LaTeX tools
> A collection of tools for writing about Spoofax in a LaTeX document


## sg2tikz
Convert a scope graph "drawn" in ascii-art to a `.tex` file containing the image as a Tikz picture.

### Usage
Can be executed using `python sg2tikz.py sg.txt`, with the contents of `sg.txt` being:

```
# This is a caption
    (s1)-->[x]
      ^:P
      |
      |
    (s2)<--[x]
     / \
    /   |
   |    \
   |     -->[y]
   \
    -->[z]
```


## Spoofax-pygments
A [Pygments](http://pygments.org/) lexer implementation for Spoofax languages.
This lexer can be invoked within a `minted`-environment in LaTeX to color code snippets of Spoofax languages.

### Usage
> I have not been able to get this to work on Overleaf. Presumably, they do not have Java installed.

Add the files `spoofax-pygmentize-core.jar` and `spoofax_lexer.py` in the source root of your LaTeX project.
You can download these files on the releases page, or take the Python file from the source and generate the JAR yourself by running `./release.sh` in the `spoofax-pygments/`-folder.
After copying the files, you can use the `spoofax_lexer` in the following way inside your LaTeX document.
Make sure to run the LaTeX compiler using `-shell-escape` for Minted lexers to work at all.

```latex
\documentclass{minimal}

\usepackage{minted}

\begin{document}

\begin{figure}
  \begin{minted}{spoofax_lexer.py -O "parseTable=fvm.sdf3,preset=layoutSensitive" -x}
BLOCK:
  ipush 0 // This is a comment
  return // Also a comment
BLOCK2:
  ipush 0
  return
  \end{minted}
  \caption{Some nicely colored code example}
\end{figure}

\begin{figure}
  \inputminted[firstline=5, lastline=9]{spoofax_lexer.py -O "parseTable=fvm.sdf3,preset=layoutSensitive" -x}{test.stc}
  \caption{Just a few lines this time}
\end{figure}

\begin{figure}
  \begin{minted}{spoofax_lexer.py -O "language=org/metaborg/org.metaborg.meta.lang.template" -x}
module test

context-free syntax

    Start = "Hello World!"
  \end{minted}
  \caption{Coloring using a language definition from Maven}
\end{figure}

\end{document}
```

The possible options for the lexer (passed via the `-O` argument) are:
- `preset` - One of the parser presets supported by JSGLR2 (currently `standard` (default), `dataDependent`, `layoutSensitive`, `incremental`)
- `language` - The identifier of a Spoofax language in the local Maven repository, in the format `[groupId]/[artifactId]/[version]`, where the `groupId` is slash-separated, the `artifactId` is period-separated, and the `version` is optional. Examples: `org/metaborg/lang.java`, `org/metaborg/lang.java/1.1.0-SNAPSHOT`.
- `parseTable` - A file reference to a parse table (e.g. `sdf.tbl`) relative to the LaTeX source directory.
- `esv` - A file reference to an ESV definition (e.g. `editor.esv.af`) relative to the LaTeX source directory.

Either the `language` or the `parseTable` option is required for the lexer to work. If only a `parseTable` is given, [this default ESV definition](https://github.com/metaborg/spoofax/blob/master/meta.lib.spoofax/editor/libspoofax/color/default.esv) is used for coloring.

### Limitations

The Spoofax lexer does only supports complete, valid programs as input. This gives two limitations:
- If you want to have a colorized snippet of just a few lines from the middle of a program, you therefore have to use the `firstline` and `lastline` arguments to get those lines from a valid program.
- The `escapeinline` option of Minted is not supported, because this causes Pygments to split up the file in multiple (incomplete) parts.

### Debugging

LaTeX swallows a lot of errors in its log with respect to the custom lexers. If you have trouble setting op the tool, you could try running Pygments directly on the command-line, in order to identify the problem. The command for this is: `pygmentize -l spoofax_lexer.py -O "language=..." -x -f latex <SOME FILE TO PARSE>` (note that the custom lexer only supports the LaTeX formatter; other formatters like `html` or `terminal` are not supported)
