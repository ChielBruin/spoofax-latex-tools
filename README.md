# Spoofax Latex tools
> A collection of tools for writing about Spoofax in a Latex document


## sg2tikz
Convert a scope graph in ascii to one using Tikz

Example input:

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
