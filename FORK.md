# XXL-JOB Fork Notes

## 中文

本仓库是 [xuxueli/xxl-job](https://github.com/xuxueli/xxl-job) 的 fork。
当前 fork 发布线基于上游 XXL-JOB `3.4.1`。

### Fork 定位

本 fork 尽量保持上游 XXL-JOB 的既有行为，同时为调度中心数据库增加
PostgreSQL 支持。MySQL 仍然是兼容目标。

### 版本策略

fork 发布版本使用“上游版本 + fork 修订号”的格式：

- `3.4.1-1`：基于上游 `3.4.1` 的第一个 fork 发布版本。

修订号后缀保持通用命名，不使用 `pg` 等数据库名称。这样后续版本即使包含
数据库支持之外的修复或运维改进，也不需要重新调整命名规则。

### 相对上游的主要差异

- 新增 PostgreSQL 初始化脚本：`doc/db/tables_xxl_job_postgresql.sql`。
- 标准化调度中心 mapper SQL，使同一套 mapper 行为可同时适配 MySQL 和
  PostgreSQL。
- 保留 MySQL 的大小写不敏感查询语义，并让 PostgreSQL 查询路径匹配该行为。
- 增加 MySQL 和 PostgreSQL mapper 行为的集成测试覆盖。
- 新增 PostgreSQL Docker Compose 验证环境：
  `docker/docker-compose-postgresql.yml`。
- 修复上游日志列表回归：执行中的日志 `handle_time = null` 时，DTO 转换不再因
  `date must not be null` 抛错。
- 调整 admin Docker 镜像时区为标准 `Asia/Shanghai` 名称。

### 数据库支持

| 数据库 | 状态 | 说明 |
| --- | --- | --- |
| MySQL | 支持 | 上游既有目标，本 fork 应保持兼容。 |
| PostgreSQL | 本 fork 支持 | 使用本 fork 提供的 PostgreSQL schema 和 mapper 兼容性改造。 |
| 其他数据库 | 暂不支持 | 当前发布线不提供兼容性承诺。 |

### Docker 镜像

计划发布的 fork 镜像名：

- `hccake/xxl-job-admin:3.4.1-1`
- `hccake/xxl-job-executor-sample-springboot:3.4.1-1`

本地开发可以继续使用 Docker Compose 构建的 `:local` 镜像。正式发布镜像应使用
不可变版本 tag。

### PostgreSQL 快速启动

构建本地镜像并启动 PostgreSQL 验证环境：

```bash
docker compose -f docker/docker-compose-postgresql.yml up -d --build
```

打开调度中心：

```text
http://localhost:8080/
```

停止环境：

```bash
docker compose -f docker/docker-compose-postgresql.yml down
```

PostgreSQL 数据目录默认是 `docker/data/postgres`。初始化脚本只会在数据目录首次
创建时执行。

### 发布检查项

- fork 版本的 Maven 构建通过。
- MySQL 和 PostgreSQL mapper 测试通过。
- PostgreSQL Docker Compose 环境可以正常启动。
- admin 容器健康检查为 healthy。
- executor sample 可以注册到 admin 服务。
- Docker Hub 镜像从 release tag 构建，并通过 Compose 验证。

## English

This repository is a fork of [xuxueli/xxl-job](https://github.com/xuxueli/xxl-job).
The current fork release line is based on upstream XXL-JOB `3.4.1`.

### Purpose

The fork keeps upstream XXL-JOB behavior as much as possible while adding
PostgreSQL support for the admin database. MySQL remains a compatibility target.

### Version Policy

Fork releases use the upstream version plus a fork revision suffix:

- `3.4.1-1`: first fork release based on upstream `3.4.1`.

The suffix is intentionally generic. It is not named after PostgreSQL because
future fork releases may include fixes or operational improvements beyond
database support.

### Main Differences From Upstream

- Adds PostgreSQL schema initialization script:
  `doc/db/tables_xxl_job_postgresql.sql`.
- Standardizes admin mapper SQL so the same mapper behavior works with MySQL
  and PostgreSQL.
- Preserves MySQL case-insensitive behavior by making PostgreSQL query paths
  match the expected comparisons.
- Adds integration coverage for MySQL and PostgreSQL mapper behavior.
- Adds PostgreSQL Docker Compose verification setup:
  `docker/docker-compose-postgresql.yml`.
- Fixes a log-list regression where pending job logs with `handle_time = null`
  could fail DTO conversion with `date must not be null`.
- Uses standard timezone naming in the admin Docker image.

### Database Support

| Database | Status | Notes |
| --- | --- | --- |
| MySQL | Supported | Existing upstream target; compatibility should be preserved. |
| PostgreSQL | Supported by this fork | Uses the PostgreSQL schema and mapper compatibility changes in this fork. |
| Other databases | Not supported | No compatibility guarantee in the current release line. |

### Docker Images

Planned fork image names:

- `hccake/xxl-job-admin:3.4.1-1`
- `hccake/xxl-job-executor-sample-springboot:3.4.1-1`

Local development may continue to use the `:local` images built by Docker
Compose. Published images should use immutable version tags.

### PostgreSQL Quick Start

Build the local images and start the PostgreSQL verification stack:

```bash
docker compose -f docker/docker-compose-postgresql.yml up -d --build
```

Open the admin UI:

```text
http://localhost:8080/
```

To stop the stack:

```bash
docker compose -f docker/docker-compose-postgresql.yml down
```

The PostgreSQL data directory defaults to `docker/data/postgres`. The database
initialization script only runs when the data directory is first created.

### Release Checklist

- Maven build passes for the fork version.
- MySQL and PostgreSQL mapper tests pass.
- PostgreSQL Docker Compose stack starts successfully.
- Admin container health check reports healthy.
- Executor sample registers with the admin service.
- Docker Hub images are built from the release tag and verified through Compose.
