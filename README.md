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

New lexers can easily be added by providing a parsetable and a mapping from the parsetree-nodes to a Pygments token type.
For a full list of available tokens, see [this page](http://pygments.org/docs/tokens/#keyword-tokens).


Available lexers:
- `FrameVMLexer`

### Usage
> I have not been able to get this to work on Overleaf. Presumably they do not have Java installed

Extract the files in `Spoofax-pygments-release.zip` in the root of your Latex project.
Now you can use the `spoofax-pygments-lexer` in the folowing way inside your Latex document:
```Latex
\documentclass[twoside,a4paper,11pt]{memoir}

\usepackage{minted}

\begin{document}

\chapter{My Spoofax implementation}
\begin{figure}
  \begin{minted}{spoofax-lexer.py:FrameVMLexer -x}
BLOCK:
  ipush 0 // This is a comment
  return // Also a comment
BLOCK2:
  ipush 0
  return
  \end{minted}
  \caption{Some nicely colored code example}
\end{figure}

\end{document}

```
Lastly, make sure to run the Latex compiler using `-shell-escape` for custom Minted lexers to work at all.

Latex swallows a lot of errors in its log with respect to the custom lexers. If you have trouble setting op the tool, you could try running pygments directly on the command-line, in order to identify the problem. The command for this is: `pygmentize -l spoofax-lexer.py:FrameVMLexer -x <SOME STRING TO PARSE>`
