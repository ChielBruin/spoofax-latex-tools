# Spoofax Latex tools
> A collection of tools for writing about Spoofax in a Latex document


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
These lexers can be invoked within a `minted`-environment in Latex to color code snippets of Spoofax languages.

New lexers can easily be added by providing a parsetable as `language` option to the lexer (see examples below).

**TODO:** A later version of the Spoofax lexer should allow importing custom ESV coloring definitions.

### Usage
> I have not been able to get this to work on Overleaf. Presumably they do not have Java installed

Add the files `spoofax-pygmentize-core.jar` and `spoofax_lexer.py` in the source root of your LaTeX project.
You can download these files on the releases page, or generate them yourself by running `python3 release.py` in the `spoofax-pygments/`-folder.
After copying the files, you can use the `spoofax_lexer` in the following way inside your LaTeX document:
```latex
\documentclass[twoside,a4paper,11pt]{memoir}

\usepackage{minted}

\begin{document}

\chapter{My Spoofax implementation}
\begin{figure}
  \begin{minted}{spoofax_lexer.py -O "language=fvm.sdf3,preset=layoutSensitive" -x}
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
  \inputminted[firstline=5, lastline=9]{spoofax_lexer.py -O "language=fvm.sdf3,preset=layoutSensitive" -x}{test.stc}
  \caption{Just a few lines this time}
\end{figure}

\begin{figure}
  \begin{minted}{spoofax_lexer.py -O "language=org/metaborg/org.metaborg.meta.lang.template" -x}
module test

context-free syntax

    Start = "Hello World!"
  \end{minted}
  \caption{Color coding using a language definition from Maven}
\end{figure}

\end{document}
```

The Spoofax lexer does only supports valid programs as input. If you want to have a colorized snippet of just a few lines from the middle of a program, you therefore have to use the `firstline` and `lastline` arguments to get those lines from a valid program.
Lastly, make sure to run the Latex compiler using `-shell-escape` for Minted lexers to work at all.

Latex swallows a lot of errors in its log with respect to the custom lexers. If you have trouble setting op the tool, you could try running pygments directly on the command-line, in order to identify the problem. The command for this is: `pygmentize -l spoofax_lexer.py -O "language=..." -x <SOME FILE TO PARSE>`
