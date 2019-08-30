import subprocess, os, tempfile, shutil, zipfile

def compose_lexers(tempfile, lexers):
  with open(os.path.join(tempfile.name, 'spoofax-lexer.py'), 'w') as out_file:
    # first write the base Lexer
    with open('lexers/base_lexer.py', 'r') as base_lex:
      for line in base_lex.readlines():
        out_file.write(line)
    out_file.write('\n\n#########################\n# Lexer implementations #\n#########################\n\n')
    out_file.write('from pygments.token import Text, Name, Number, String, Comment, Punctuation, Other, Keyword, Operator\n\n')
    
    for lexer in lexers:
      with open(lexer, 'r') as lex:
        for line in lex.readlines():
          out_file.write(line)
      out_file.write('\n\n\n')


if __name__ == '__main__':
  subprocess.call(['mvn', 'install'], cwd='core')
  
  tempfile = tempfile.TemporaryDirectory()
  print('Using temporary file at: %s' % tempfile.name)

  sources = list(filter( lambda x: x.endswith('.py') and not x.endswith('base_lexer.py'), map(lambda x: os.path.join('lexers', x), os.listdir('lexers'))))
  tables = list(filter( lambda x: x.endswith('.tbl'), map(lambda x: os.path.join('lexers', 'tables', x), os.listdir('lexers/tables'))))
  jar = os.path.join('core', 'target', next(filter( lambda x: x.endswith('-shaded.jar'), os.listdir('core/target'))))
  
  print('Found %d lexer files and %d parse tables' % (len(sources), len(tables)))

  compose_lexers(tempfile, sources)

  for tablefile in tables:
    shutil.copyfile(tablefile, os.path.join(tempfile.name, os.path.basename(tablefile)))

  shutil.copyfile(jar, os.path.join(tempfile.name, 'spoofax-pygmentize-core.jar'))

  print('Zipping files')
  with zipfile.ZipFile('Spoofax-pygments-release.zip', 'w', zipfile.ZIP_DEFLATED) as zip:
    for file in os.listdir(tempfile.name):
      zip.write(os.path.join(tempfile.name, file), arcname=os.path.basename(file))
