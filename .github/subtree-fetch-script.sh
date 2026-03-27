# Remote 등록
git remote add server-origin https://github.com/leegwichan/Eatda-Server
git remote add web-origin https://github.com/leegwichan/Eatda-Web

# Remote Fetch
git subtree pull --prefix=server server-origin develop
git subtree pull --prefix=web web-origin main
