# Docker 部署说明

这套部署方式包含三个容器：

- `bishe-mysql`：MySQL 数据库，首次启动时导入 `docs/sql/init.sql`。
- `bishe-backend`：Spring Boot 后端，只在 Docker 内部网络暴露 `8080`。
- `bishe-frontend`：Nginx 前端站点，对外暴露 `80`，并把 `/api/` 代理到后端。

## 1. 上传项目

如果服务器还没有 Docker，先安装 Docker 和 Compose v2。阿里云 Linux / CentOS 系统通常可以执行：

```bash
dnf install -y yum-utils
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
systemctl enable --now docker
docker compose version
```

如果提示 `dnf` 不存在，把上面的 `dnf` 换成 `yum` 再执行。

把整个项目上传到服务器，例如：

```bash
scp -r ./bishe root@你的服务器IP:/opt/bishe
```

如果服务器上已经有旧版本，也可以只更新代码后重新构建。

## 2. 准备环境变量

进入项目目录：

```bash
cd /opt/bishe
cp .env.docker.example .env.docker
vi .env.docker
```

至少修改这些值：

```env
MYSQL_ROOT_PASSWORD=换成MySQL_root密码
MYSQL_PASSWORD=换成项目数据库用户密码
JWT_SECRET=换成很长的随机字符串
LLM_API_KEY=你的大模型API_KEY
LLM_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
LLM_MODEL=qwen-plus
```

`LLM_BASE_URL` 填到 `/v1` 即可，后端会自动拼接 `/chat/completions`。

## 3. 启动

如果之前宝塔里的 Nginx 还在占用 `80` 端口，需要先停止旧 Nginx，或者把 `.env.docker` 里的 `WEB_PORT=80` 临时改成其他端口，例如 `WEB_PORT=8081`。

```bash
docker compose --env-file .env.docker up -d --build
```

查看容器状态：

```bash
docker compose --env-file .env.docker ps
```

查看后端日志：

```bash
docker compose --env-file .env.docker logs -f backend
```

## 4. 访问

浏览器打开：

```text
http://你的服务器IP/
```

只需要在阿里云安全组放行 `80`。后端 `8080` 和 MySQL `3306` 不需要放行到公网。

## 5. 更新版本

上传新代码后，在项目目录执行：

```bash
docker compose --env-file .env.docker up -d --build
```

如果只是重启：

```bash
docker compose --env-file .env.docker restart
```

## 6. 数据库说明

`docs/sql/init.sql` 只会在 MySQL 数据卷第一次创建时自动执行。

如果你要清空数据库并重新导入初始化数据：

```bash
docker compose --env-file .env.docker down -v
docker compose --env-file .env.docker up -d --build
```

这会删除数据库数据卷，请只在确认不需要旧数据时使用。
