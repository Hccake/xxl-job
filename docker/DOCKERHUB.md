# Docker Hub Overview

This file contains Docker Hub overview text that can be pasted manually into the
Docker Hub repositories for this fork.

复制每个 `~~~markdown` 代码块内部的内容，分别粘贴到 Docker Hub 对应仓库的
Overview。

## hccake/xxl-job-admin

~~~markdown
# XXL-JOB Admin / XXL-JOB 调度中心

这是 `hccake/xxl-job` fork 发布的 XXL-JOB 调度中心镜像。该 fork 基于上游
XXL-JOB，保留 MySQL 兼容性，并增加 PostgreSQL 支持。

XXL-JOB Admin image from the `hccake/xxl-job` fork. This fork is based on
upstream XXL-JOB, keeps MySQL compatibility, and adds PostgreSQL support.

## Tags / 标签

生产环境建议使用不可变版本 tag。可在 Docker Hub Tags 页面查看所有可用版本。

Use an immutable version tag in production. Check the Docker Hub Tags page for
all available versions.

| Tag | Description / 说明 |
| --- | --- |
| `3.4.1-2` | 推荐版本，支持 `linux/amd64` 和 `linux/arm64` / Recommended version with `linux/amd64` and `linux/arm64` support |
| `3.4.1-1` | 第一个 fork 发布版本，仅发布 amd64 镜像 / First fork release, published as an amd64 image |

除非你的环境有明确的镜像更新策略，否则不建议把 `latest` 作为生产部署目标。

`latest` is not recommended for production unless your environment has an
explicit image update policy.

## Pull / 拉取镜像

下面示例使用 `3.4.1-2`。后续版本发布时，请替换命令中的镜像 tag。

The example below uses `3.4.1-2`. For future releases, replace the image tag in
the command.

```bash
docker pull hccake/xxl-job-admin:3.4.1-2
```

## Platforms / 平台

`3.4.1-2` 及后续版本发布为多平台镜像，支持 `linux/amd64` 和 `linux/arm64`。

`3.4.1-2` and later versions are published as multi-platform images for
`linux/amd64` and `linux/arm64`.

## Database Requirement / 数据库要求

该镜像不会自动创建 XXL-JOB 数据库表结构。启动调度中心前，需要先初始化数据库。

This image does not create the XXL-JOB database schema by itself. Initialize the
database before starting the admin service.

数据库初始化脚本在 GitHub 仓库中维护：

Schema files are available in the GitHub repository:

- PostgreSQL: `doc/db/tables_xxl_job_postgresql.sql`
- MySQL: `doc/db/tables_xxl_job.sql`

## Run With PostgreSQL / 使用 PostgreSQL 运行

连接已有 PostgreSQL 数据库时，请将占位符替换为你的真实数据库地址、端口、库名、
用户名和密码。

When connecting to an existing PostgreSQL database, replace the placeholders with
your actual database host, port, database name, username, and password.

```bash
docker run -d --name xxl-job-admin \
  -p 8080:8080 \
  -e PARAMS="--spring.datasource.url=jdbc:postgresql://<postgres-host>:5432/<database> \
  --spring.datasource.username=<username> \
  --spring.datasource.password=<password> \
  --spring.datasource.driver-class-name=org.postgresql.Driver" \
  hccake/xxl-job-admin:3.4.1-2
```

打开调度中心：

Open the admin UI:

```text
http://localhost:8080/
```

默认登录信息：

Default login:

| Field / 字段 | Value / 值 |
| --- | --- |
| Username / 用户名 | `admin` |
| Password / 密码 | `123456` |

生产环境请立即修改默认密码。

Change the default password immediately in production.

使用 MySQL 时，先使用 `doc/db/tables_xxl_job.sql` 初始化数据库，再通过 `PARAMS`
传入 MySQL datasource 配置。

When using MySQL, initialize the database with `doc/db/tables_xxl_job.sql`, then
pass the MySQL datasource configuration through `PARAMS`.

## Runtime Configuration / 运行配置

镜像沿用上游 XXL-JOB Docker 约定，可通过 `PARAMS` 环境变量传入 Spring Boot
启动参数。

The image follows the upstream XXL-JOB Docker convention. Runtime arguments can
be passed through the `PARAMS` environment variable.

常用环境变量：

Common environment variables:

| Variable | Description / 说明 |
| --- | --- |
| `PARAMS` | Spring Boot 启动参数 / Spring Boot command-line parameters |
| `JAVA_OPTS` | JVM 参数，例如 `-Xms256m -Xmx512m` / JVM options, for example `-Xms256m -Xmx512m` |
| `LOG_HOME` | XXL-JOB 日志目录 / XXL-JOB log home |

## Access Token / 访问令牌

默认访问令牌是 `default_token`。如果修改 admin 的 `xxl.job.accessToken`，执行器
也必须使用相同 token。

The default access token is `default_token`. If you change the admin
`xxl.job.accessToken`, executors must use the same token.

admin 侧可通过 `PARAMS` 设置：

Set it on the admin side through `PARAMS`:

```text
--xxl.job.accessToken=<token>
```

## Database Support / 数据库支持

| Database | Status / 状态 |
| --- | --- |
| MySQL | 支持 / Supported |
| PostgreSQL | 本 fork 支持 / Supported by this fork |
| Other databases | 暂不支持 / Not supported |

## Production Notes / 生产注意事项

- 使用不可变版本 tag，不建议生产直接使用 `latest`。
- 使用外部持久化数据库，并为数据库配置备份。
- 修改默认登录密码和默认 access token。
- 根据业务需要配置 JVM 参数、日志目录、邮件告警和日志保留周期。

- Use immutable version tags. Avoid `latest` for production deployments.
- Use an external persistent database and configure database backups.
- Change the default login password and default access token.
- Configure JVM options, log home, mail alerts, and log retention for your workload.

## Links / 链接

- Source: https://github.com/Hccake/xxl-job
- Fork notes: https://github.com/Hccake/xxl-job/blob/master/FORK.md
- Changelog: https://github.com/Hccake/xxl-job/blob/master/CHANGELOG.md

## License / 许可证

XXL-JOB 使用 GPLv3 许可证。

XXL-JOB is licensed under GPLv3.
~~~

## hccake/xxl-job-executor-sample-springboot

~~~markdown
# XXL-JOB Spring Boot Executor Sample / XXL-JOB Spring Boot 执行器示例

这是 `hccake/xxl-job` fork 发布的 Spring Boot 执行器示例镜像。

Spring Boot executor sample image from the `hccake/xxl-job` fork.

该镜像主要用于本地体验和示例演示。生产执行器通常应该基于自己的 Job Handler、
配置和业务依赖构建独立镜像。

This image is intended for local evaluation and examples. Production executor
applications should usually build their own images with their own job handlers,
configuration, and dependencies.

## Tags / 标签

生产环境建议使用不可变版本 tag。可在 Docker Hub Tags 页面查看所有可用版本。

Use an immutable version tag in production. Check the Docker Hub Tags page for
all available versions.

| Tag | Description / 说明 |
| --- | --- |
| `3.4.1-2` | 推荐版本，支持 `linux/amd64` 和 `linux/arm64` / Recommended version with `linux/amd64` and `linux/arm64` support |
| `3.4.1-1` | 第一个 fork 发布版本，仅发布 amd64 镜像 / First fork release, published as an amd64 image |

## Pull / 拉取镜像

下面示例使用 `3.4.1-2`。后续版本发布时，请替换命令中的镜像 tag。

The example below uses `3.4.1-2`. For future releases, replace the image tag in
the command.

```bash
docker pull hccake/xxl-job-executor-sample-springboot:3.4.1-2
```

## Platforms / 平台

`3.4.1-2` 及后续版本发布为多平台镜像，支持 `linux/amd64` 和 `linux/arm64`。

`3.4.1-2` and later versions are published as multi-platform images for
`linux/amd64` and `linux/arm64`.

## Quick Start / 快速启动

连接已有 XXL-JOB Admin 服务运行。请将 `<admin-url>` 替换为执行器可以访问的
调度中心地址。

Run it with an existing XXL-JOB Admin service. Replace `<admin-url>` with the
actual admin address that the executor can reach.

```bash
docker run -d --name xxl-job-executor-sample-springboot \
  -p 9999:9999 \
  -e PARAMS="--xxl.job.admin.addresses=<admin-url>" \
  hccake/xxl-job-executor-sample-springboot:3.4.1-2
```

当执行器和调度中心位于同一个 Docker 网络时，可使用如下示例：

Example when the executor and admin are in the same Docker network:

```bash
docker run -d --name xxl-job-executor-sample-springboot \
  --network xxl-job-net \
  -p 9999:9999 \
  -e PARAMS="--xxl.job.admin.addresses=http://xxl-job-admin:8080" \
  hccake/xxl-job-executor-sample-springboot:3.4.1-2
```

## Runtime Configuration / 运行配置

镜像沿用上游 XXL-JOB Docker 约定，可通过 `PARAMS` 环境变量传入 Spring Boot
启动参数。

The image follows the upstream XXL-JOB Docker convention. Runtime arguments can
be passed through the `PARAMS` environment variable.

常用环境变量：

Common environment variables:

| Variable | Description / 说明 |
| --- | --- |
| `PARAMS` | Spring Boot 启动参数 / Spring Boot command-line parameters |
| `JAVA_OPTS` | JVM 参数，例如 `-Xms128m -Xmx256m` / JVM options, for example `-Xms128m -Xmx256m` |
| `LOG_HOME` | XXL-JOB 日志目录 / XXL-JOB log home |

## Access Token / 访问令牌

默认访问令牌是 `default_token`。如果 admin 修改了 access token，请为执行器传入
相同的 `xxl.job.admin.accessToken`。

The default access token is `default_token`. If the admin access token was
changed, pass the same `xxl.job.admin.accessToken` to the executor.

```text
-e PARAMS="--xxl.job.admin.addresses=<admin-url> --xxl.job.admin.accessToken=<token>"
```

## Notes / 说明

这是示例执行器镜像。生产任务建议构建自己的执行器应用镜像。

This image is a sample executor. For production workloads, create your own
executor application image.

## Links / 链接

- Source: https://github.com/Hccake/xxl-job
- Fork notes: https://github.com/Hccake/xxl-job/blob/master/FORK.md
- Changelog: https://github.com/Hccake/xxl-job/blob/master/CHANGELOG.md

## License / 许可证

XXL-JOB 使用 GPLv3 许可证。

XXL-JOB is licensed under GPLv3.
~~~
