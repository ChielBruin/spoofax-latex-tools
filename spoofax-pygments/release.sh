#!/usr/bin/env bash

set -e

(cd core && mvn install)

cp core/target/*-shaded.jar spoofax-pygmentize-core.jar
