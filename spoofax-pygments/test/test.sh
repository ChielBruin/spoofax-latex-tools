#!/usr/bin/env bash

rm -rf _minted-test/ test.pdf && latexmk -shell-escape -pdf test
