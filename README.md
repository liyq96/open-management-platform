# Open Management Platform

Open Management Platform 是一个面向企业后台场景的权限管理平台，采用前后端分离架构。后端基于 JDK 21、Spring Boot 3.2 和 Spring Cloud Alibaba，前端基于 React 19、TypeScript、Vite 7 和 Ant Design 6。

平台当前覆盖账号认证、用户与组织管理、RBAC 权限、租户管理和审计日志等基础能力。

## 当前功能

- 验证码、账号密码登录、RS256 JWT 签发和退出黑名单
- 用户新增、修改、删除、分页查询和详情查询
- 当前用户修改密码、管理员重置用户密码
- 部门树、岗位、角色、菜单、权限分组和权限管理
- 用户角色、用户岗位、角色权限和角色菜单分配
- 租户管理和租户数据隔离
- 新租户管理员、组织、所选菜单、权限和超级管理员角色的一次性初始化
- 登录日志和管理操作日志
- Gateway 统一路由、JWT 校验和请求 ID
- React 管理端、后端动态菜单、分组权限授权、无权限页和响应式布局

当前阶段不使用 Refresh Token、OAuth2/OIDC 和 Flyway。

## 技术栈

### 后端

| 技术 | 版本 |
| --- | --- |
| JDK | 21 |
| Spring Boot | 3.2.9 |
| Spring Cloud | 2023.0.3 |
| Spring Cloud Alibaba | 2023.0.3.4 |
| MyBatis-Plus | 3.5.10.1 |
| PostgreSQL | 16.x |
| Redis | 5.0.14 |
| Nacos | 2.4.3 |

### 前端

| 技术 | 版本 |
| --- | --- |
| React | 19.2.8 |
| TypeScript | 5.9.3 |
| Vite | 7.3.6 |
| Ant Design | 6.5.2 |
| React Router | 7.x |
| TanStack Query | 5.x |
| Zustand | 5.x |
| Axios | 1.x |

## 工程结构

```text
open-management-platform/
├── platform-gateway                 # 统一入口、JWT 校验和路由转发
├── platform-auth                    # 验证码、登录、Token 签发和退出
├── platform-system                  # 用户、组织、权限、租户和审计业务
├── platform-common
│   ├── platform-common-core         # 通用响应、分页和错误模型
│   ├── platform-common-web          # Web、异常处理和 JSON 序列化
│   ├── platform-common-security     # JWT、安全上下文和 RSA 支持
│   ├── platform-common-redis        # Redis 公共能力
│   └── platform-common-database     # MyBatis-Plus 和数据库公共能力
├── platform-web                     # React 管理端
├── config/nacos                     # 可导入 Nacos 的配置模板
├── sql                              # PostgreSQL 初始化脚本
└── pom.xml                          # Maven 聚合父工程
```

## 默认端口

| 服务 | 端口 |
| --- | --- |
| platform-web | 5173（开发环境） |
| platform-gateway | 9100 |
| platform-auth | 9200 |
| platform-system | 9300 |
| Nacos | 8848 |
| PostgreSQL | 5432 |
| Redis | 6379 |

## 本地运行

### 1. 环境准备

- JDK 21
- Maven 3.9+
- Node.js 20.19+
- pnpm 11
- PostgreSQL 16.x
- Redis 5.0.14
- Nacos 2.4.3

### 2. 初始化数据库

创建数据库 `open_platform`，然后使用数据库工具的“执行脚本”功能完整执行：

```text
sql/01_init.sql
```

初始化脚本会创建首次登录租户和账号 `platform / admin / Admin@123456`，并绑定超级管理员角色。首次登录后必须立即修改默认密码。

### 3. 准备 JWT 密钥

仓库不提供可共用的默认密钥，每个部署环境都必须生成自己的 RSA 密钥对。认证服务需要私钥和公钥，Gateway 与 System 服务只需要公钥。在项目根目录执行：

```bash
mkdir -p config/keys
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out config/keys/jwt-private.pem
openssl pkey -in config/keys/jwt-private.pem -pubout -out config/keys/jwt-public.pem
```

Windows PowerShell 如果没有 `mkdir -p` 参数，可先执行 `New-Item -ItemType Directory -Force config/keys`。

密钥会生成到：

```text
./config/keys/jwt-private.pem
./config/keys/jwt-public.pem
```

Nacos 模板默认通过 `JWT_PRIVATE_KEY=file:./config/keys/jwt-private.pem` 和 `JWT_PUBLIC_KEY=file:./config/keys/jwt-public.pem` 读取它们。生产环境应通过密钥管理服务或受权限保护的文件挂载提供密钥。密钥、数据库密码和 Redis 密码禁止提交到仓库。更详细的说明见 [`config/keys/README.md`](./config/keys/README.md)。

### 4. 启动后端

三个服务的 `application.yml` 只负责连接Nacos并导入远程配置。可直接发布到Nacos的配置模板统一放在 `config/nacos/` 目录。首次使用时先将以下Data ID发布到目标命名空间的 `PLATFORM_GROUP` 分组：

```text
platform-common.yml
platform-database.yml
platform-auth.yml
platform-system.yml
platform-gateway.yml
```

模板不会写入真实密码和本地绝对路径，数据库、Redis和密钥配置通过以下环境变量注入：

| 环境变量 | 默认值 |
| --- | --- |
| `POSTGRES_URL` | `jdbc:postgresql://127.0.0.1:5432/open_platform` |
| `POSTGRES_USERNAME` | `postgres` |
| `POSTGRES_PASSWORD` | 空 |
| `REDIS_HOST` | `127.0.0.1` |
| `REDIS_PORT` | `6379` |
| `REDIS_PASSWORD` | 空 |
| `JWT_PRIVATE_KEY` | `file:./config/keys/jwt-private.pem` |
| `JWT_PUBLIC_KEY` | `file:./config/keys/jwt-public.pem` |

在 Nacos 控制台中逐个创建同名 Data ID，选择 YAML 格式，并将 `config/nacos/` 下对应文件的内容发布到 `PLATFORM_GROUP` 分组。

在项目根目录先完成构建：

```bash
mvn clean install
```

分别启动三个服务：

```bash
mvn -pl platform-auth spring-boot:run
mvn -pl platform-system spring-boot:run
mvn -pl platform-gateway spring-boot:run
```

使用 IDEA 时，如果本地PostgreSQL或Redis启用了密码，需要在启动配置中设置对应环境变量。先启动Nacos并发布上述Data ID，再运行 `PlatformAuthApplication`、`PlatformSystemApplication` 和 `PlatformGatewayApplication`。

### 5. 启动前端

```bash
cd platform-web
pnpm install
pnpm dev
```

浏览器访问 `http://127.0.0.1:5173`。开发服务器会将 `/api` 请求代理到 `http://127.0.0.1:9100`。

## 认证和权限流程

1. 前端获取验证码并提交租户编码、账号、密码、验证码编号和验证码内容。
2. `platform-auth` 校验验证码、BCrypt 密码、账号和租户状态。
3. 认证服务使用 RSA 私钥签发 RS256 JWT。
4. 前端通过 `Authorization: Bearer <token>` 访问 Gateway。
5. Gateway 与 System 服务使用 RSA 公钥验证 Token，并检查退出黑名单和认证版本。
6. 前端读取当前菜单和权限控制页面入口，后端通过 `@PreAuthorize` 完成最终授权。

菜单授权和权限授权是两个独立动作：菜单决定导航入口是否可见，权限决定页面数据与操作是否可访问。权限通过独立的权限分组整理，权限分组与菜单没有数据库关联。用户拥有菜单但缺少页面查看权限时，前端展示无权限页且不会请求该页面的业务数据。

除租户目录表 `tenant_info` 外，所有业务表均通过 MyBatis-Plus 租户拦截器强制追加 `tenant_id` 条件；业务代码不依赖手写 SQL 完成租户隔离。后端雪花 `Long` 主键在 HTTP JSON 中统一返回字符串，避免浏览器解析时丢失精度。

## 构建和测试

后端全量测试：

```bash
mvn test
```

前端类型检查和生产构建：

```bash
cd platform-web
pnpm run typecheck
pnpm run build
```

## 文档

- [前端运行说明](./platform-web/README.md)
- [JWT RSA 密钥说明](./config/keys/README.md)

## 协作约定

- 修改后端后至少运行对应 Maven 模块测试。
- 修改前端后至少运行 `pnpm run typecheck`。
- 提交信息使用 `feat:`、`fix:`、`refactor:`、`test:`、`docs:`、`chore:` 或 `build:`。
- 禁止提交 `.env`、密钥、密码、日志、编译产物和数据库备份。
