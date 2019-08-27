# Spoofax Latex tools
> A collection of tools for writing about Spoofax in a Latex document


## sg2tikz
Convert a scope graph "drawn" in ascii-art to a `.tex` file containing the image as a Tikz picture.

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
