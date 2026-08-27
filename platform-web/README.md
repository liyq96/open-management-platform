# Open Management Platform Web

企业权限管理平台前端，技术栈为 React 19、TypeScript、Vite 7、Ant Design 6、TanStack Query、Zustand 和 Axios。

## 当前功能

- 验证码、账号密码登录、Token 恢复和退出登录
- 后端当前菜单、当前权限和按钮权限控制
- 用户、部门、岗位、角色、菜单、权限和租户管理
- 用户角色、用户岗位、角色权限和角色菜单分配
- 修改密码和管理员重置密码
- 登录日志和操作日志
- 响应式后台布局、抽屉表单和统一错误提示

## 环境要求

- Node.js 20.19 或更高版本
- pnpm 11
- 后端网关默认运行在 `http://127.0.0.1:9100`

## 本地开发

```bash
pnpm install
pnpm dev
```

开发服务器默认地址为 `http://127.0.0.1:5173`，`/api` 请求会代理到后端网关。

如需使用其他 API 基础地址，可复制 `.env.example` 为 `.env.local` 并修改 `VITE_API_BASE_URL`：

```dotenv
VITE_API_BASE_URL=/api
```

使用完整地址时应包含 `/api`，例如 `http://127.0.0.1:9100/api`。浏览器跨域直连时还需要 Gateway 提供对应的 CORS 配置，日常开发优先使用 Vite 同域代理。

## 检查与构建

```bash
pnpm run typecheck
pnpm run build
pnpm preview
```

生产构建输出到 `dist` 目录。

## 生产部署

1. 执行 `pnpm run build` 生成 `dist`。
2. 使用 Nginx 或其他静态 Web 服务器托管 `dist`。
3. 将 `/api` 反向代理到 `platform-gateway:9100`。
4. 由于项目使用 BrowserRouter，未知前端路由必须回退到 `index.html`。
5. `dist` 是构建产物，不提交到 Git。

## 权限规则

- 登录态使用后端签发的 Bearer Token。
- 页面菜单同时受 `/api/v1/system/menu/current` 与 `/api/v1/system/permission/current` 控制。
- 页面按钮使用后端权限码控制，最终授权仍以后端 `@PreAuthorize` 校验为准。
- 后端雪花 `Long` 主键在 HTTP 边界统一返回字符串，前端以 `string | number` 兼容接收并按字符串传回。
