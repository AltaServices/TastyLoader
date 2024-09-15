@echo off
setlocal

set REPO_DIR=repo-dir
set GITHUB_REPO=https://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@github.com/Tc554/loader-releases
set FINAL_NAME=%1
set JAR_FILE=target\%FINAL_NAME%.jar

if not exist %REPO_DIR% (
    git clone %GITHUB_REPO% %REPO_DIR%
) else (
    cd %REPO_DIR%
    git pull
    cd ..
)

copy %JAR_FILE% %REPO_DIR%

cd %REPO_DIR%
git add .
git commit -m "Updated jar"
git push

endlocal
