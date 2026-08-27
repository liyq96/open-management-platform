# JWT RSA 密钥说明

本目录用于存放当前环境的 JWT RSA 密钥。真实密钥文件已被 Git 忽略，禁止将私钥提交到代码仓库。

## 生成开发环境密钥

在项目根目录执行以下命令：

```bash
mkdir -p config/keys
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out config/keys/jwt-private.pem
openssl pkey -in config/keys/jwt-private.pem -pubout -out config/keys/jwt-public.pem
```

Windows PowerShell 可先使用以下命令创建目录：

```powershell
New-Item -ItemType Directory -Force config/keys
```

然后继续执行上面的两条 OpenSSL 密钥生成命令。

## 密钥文件用途

生成后会得到以下两个文件：

- `jwt-private.pem`：私钥，仅由 `platform-auth` 使用，用于签发 JWT。
- `jwt-public.pem`：公钥，由 `platform-auth`、`platform-gateway` 和 `platform-system` 使用，用于验证 JWT。

私钥泄漏后，攻击者可以伪造合法 Token，因此不能向其他服务、前端或外部系统提供私钥。

## Nacos 配置

Nacos 配置模板通过以下环境变量读取密钥：

```text
JWT_PRIVATE_KEY=file:./config/keys/jwt-private.pem
JWT_PUBLIC_KEY=file:./config/keys/jwt-public.pem
```

默认路径相对于应用的工作目录。如果服务不是从项目根目录启动，需要将环境变量设置为实际文件位置，例如：

```text
JWT_PRIVATE_KEY=file:E:/deployment/open-management-platform/keys/jwt-private.pem
JWT_PUBLIC_KEY=file:E:/deployment/open-management-platform/keys/jwt-public.pem
```

上述路径只是格式示例，请根据实际部署目录修改。

## 生产环境要求

- 每个环境独立生成密钥对，禁止在开发、测试和生产环境之间共用。
- 优先通过密钥管理服务、容器 Secret 或受权限保护的只读文件挂载提供密钥。
- RSA 私钥不得以明文存入 Nacos、数据库或日志。
- 更换密钥对后，Auth、Gateway 和 System 必须使用同一对密钥并重启相关服务。
- 私钥泄漏时应立即更换密钥，并使已签发的 Token 失效。
