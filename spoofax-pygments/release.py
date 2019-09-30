import os
import shutil
import subprocess

if __name__ == '__main__':
    subprocess.call(['mvn', 'install'], cwd='core')

    jar = os.path.join('core', 'target',
                       next(filter(lambda x: x.endswith('-shaded.jar'), os.listdir(os.path.join('core', 'target')))))

    shutil.copyfile(jar, 'spoofax-pygmentize-core.jar')
