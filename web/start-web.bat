@echo off
chcp 65001 >nul
cd /d "%~dp0app"
echo 正在启动地牢生成实验台...
echo 启动完成后，请在浏览器打开 http://localhost:3000
call npm run dev
pause
