# Changelog

## 3.4.1-2

Fork release based on upstream XXL-JOB `3.4.1`.

### Changed

- Publish Docker images as multi-platform images for `linux/amd64` and
  `linux/arm64`.
- Fork release version `3.4.1-2`.

### Notes

- `3.4.1-1` remains the first fork release and was published as an amd64 Docker
  image.
- Use `3.4.1-2` or newer when running on ARM64 hosts.

## 3.4.1-1

Fork release based on upstream XXL-JOB `3.4.1`.

### Added

- PostgreSQL schema initialization script.
- PostgreSQL-compatible admin mapper behavior.
- MySQL and PostgreSQL mapper integration coverage.
- PostgreSQL Docker Compose verification stack.
- Fork release version `3.4.1-1`.

### Fixed

- Preserved case-insensitive query behavior when switching from MySQL to
  PostgreSQL.
- Fixed scheduler lock compatibility for PostgreSQL.
- Fixed log list rendering when a pending job log has no `handle_time`.
- Switched the admin Docker image timezone to the standard `Asia/Shanghai`
  timezone name.

### Notes

- MySQL remains a supported compatibility target.
- PostgreSQL is supported by this fork release line.
- Docker Hub publishing is expected to use immutable version tags such as
  `3.4.1-1`.
