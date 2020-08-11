# Spoofax LaTeX tools
> A collection of tools for writing about Spoofax in a LaTeX document

- [sg2tikz](#sg2tikz) - Convert ascii-art scope graphs to LaTeX TikZ pictures
- [Spoofax-pygments](#Spoofax-pygments) - Pygments lexer for Spoofax languages, can be used with `minted` in LaTeX
- [Pygments style: Spoofax](#pygments_style_spoofax) - Pygments style with Spoofax colors, can be used for regular programming languages


## sg2tikz
Convert a scope graph "drawn" in ascii-art to a `.tex` file containing the image as a TikZ picture.

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
Make sure to run the LaTeX compiler using `-shell-escape` for Minted lexers to work at all.

Below, you find some examples of how to use the `spoofax_lexer` in your LaTeX document.
You can find more examples in the [`test/`](test/test.tex) directory.

```latex
\documentclass{minimal}
\usepackage{minted}
\begin{document}

\begin{figure}
  \begin{minted}{spoofax_lexer.py -O "language=org/metaborg/org.metaborg.meta.lang.template" -x}
module test

context-free syntax

    Start = "Hello World!"
  \end{minted}
  \caption{Parsing and coloring using a language definition from Maven}
\end{figure}

\begin{figure}
  \inputminted[firstline=5, lastline=9]{spoofax_lexer.py -O "language=org/metaborg/org.metaborg.meta.lang.template" -x}{grammar.sdf3}
  \caption{Parsing and coloring of just a few lines of the file grammar.sdf3}
\end{figure}

\begin{figure}
  \begin{minted}{spoofax_lexer.py -O "parseTable=fvm/sdf3.tbl,esv=fvm/editor.esv.af,preset=layoutSensitive" -x}
BLOCK:
  ipush 0 // This is a comment
  return // Also a comment
BLOCK2:
  ipush 0
  return
  \end{minted}
  \caption{Parsing and coloring using a parse table and ESV definition in the fvm/ directory and a non-standard parser preset}
\end{figure}

\end{document}
```

The possible options for the lexer (passed via the `-O` argument) are:
- `preset` - One of the parser presets supported by JSGLR2 (currently `standard` (default), `dataDependent`, `layoutSensitive`, `incremental`). Note that this setting is not automatically read from `metaborg.yaml` in case you use the `language` option.
- `language` - The identifier of a Spoofax language in the local Maven repository, in the format `[groupId]/[artifactId]/[version]`, where the `groupId` is slash-separated, the `artifactId` is period-separated, and the `version` is optional. Examples: `org/metaborg/lang.java`, `org/metaborg/lang.java/1.1.0-SNAPSHOT`.

  If you want to use a language that you don't want to build from source, but it is available on a Maven repository, you can download it to your local Maven repository using [this command](https://stackoverflow.com/a/1776808).
- `parseTable` - A file reference to a parse table relative to the directory of `spoofax_lexer.py`. After building a language with Spoofax, this file can be grabbed from `target/metaborg/sdf.tbl`.
- `esv` - A file reference to an ESV definition relative to the directory of `spoofax_lexer.py`. After building a language with Spoofax, this file can be grabbed from `target/metaborg/editor.esv.af`.

Either the `language` or the `parseTable` option is required for the lexer to work. If only a `parseTable` is given, [this default ESV definition](https://github.com/metaborg/spoofax/blob/master/meta.lib.spoofax/editor/libspoofax/color/default.esv) is used for coloring.

It is also possible to add the lexer string as a command in latex, allowing you to use it as a regular language in minted.
```latex
\documentclass{minimal}
\usepackage{minted}
\newcommand{\templateLang}{spoofax_lexer.py -O "language=org/metaborg/org.metaborg.meta.lang.template" -x}
\setminted[\templateLang]{
  xleftmargin=1em,
  linenos,
  autogobble
}
\begin{document}

\begin{figure}
  \begin{minted}{\templateLang}
module test

context-free syntax

    Start = "Hello World!"
  \end{minted}
  \caption{Parsing and coloring using a language definition from Maven}
\end{figure}

\end{document}
```

### Limitations

The Spoofax lexer does only supports complete, valid programs as input. This gives two limitations:
- If you want to have a colorized snippet of just a few lines from the middle of a program, you have to use the `firstline` and `lastline` options of Minted to get those lines from a valid program.
- The `escapeinline` option of Minted is not supported, because this causes Pygments to split up the file in multiple (incomplete) parts.

### Debugging

LaTeX swallows a lot of errors in its log with respect to the custom lexers. If you have trouble setting op the tool, you could try running Pygments directly on the command-line, in order to identify the problem. The command for this is: `pygmentize -l spoofax_lexer.py -O "language=..." -x -f latex <SOME FILE TO PARSE>` (note that the custom lexer only supports the LaTeX formatter; other formatters like `html` or `terminal` are not supported)

## Pygments style: Spoofax

A Pygments style that uses the default Spoofax colors, which can be used for code snippets that are highlighted using existing Pygments lexers for general-purpose languages.

### Installation

This is a Python package that registers itself [as a plugin to Pygments](https://pygments.org/docs/plugins/) upon installation.
Install using the following command, inside the `pygments_style_spoofax` directory:
```bash
$ (sudo) python3 setup.py install
```

### Usage

After installation, this style can be used as follows:
- Using Pygments from the command line: `pygmentize -S spoofax ...`
- Using `minted` in LaTeX: `\usemintedstyle{spoofax}` or `\setminted{style=spoofax}`
    - Set specifically for one language (e.g. Java): `\usemintedstyle[java]{spoofax}` or `\setminted[java]{style=spoofax}`
