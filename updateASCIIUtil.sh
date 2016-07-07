#!/bin/bash

# See http://redsymbol.net/articles/unofficial-bash-strict-mode/
set -euo pipefail

./makeASCIIUtil.sh > shared/src/main/scala/fm/common/ASCIIUtil.scala