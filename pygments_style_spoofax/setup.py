#!/usr/bin/python3

from setuptools import setup, find_packages

# Based on: https://github.com/hugomaiavieira/pygments-style-github/blob/master/setup.py

setup(
    name='pygments_style_spoofax',
    version='0.1',
    description='Pygments version of the Spoofax theme.',
    long_description='Pygments version of the Spoofax theme.',
    keywords='pygments style github',
    license='GPLv3',

    packages=find_packages(),
    install_requires=['pygments >= 2.3'],

    entry_points='''[pygments.styles]
                    spoofax=pygments_style_spoofax:SpoofaxStyle''',

    classifiers=[
        'Development Status :: 4 - Beta',
        'Environment :: Plugins',
        'Intended Audience :: Developers',
        'License :: OSI Approved :: BSD License',
        'Operating System :: OS Independent',
        'Programming Language :: Python',
        'Programming Language :: Python :: 2',
        'Programming Language :: Python :: 3',
        'Topic :: Software Development :: Libraries :: Python Modules',
    ],
)
