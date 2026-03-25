# 登录接口测试报告

**项目：** auth-service
**执行时间：** 2026-03-25 18:23:02
**测试文件：** `src/test/java/com/example/auth/LoginApiTest.java`
**运行环境：** JDK 21 · Spring Boot 3.2.3 · H2 内存数据库

---

## 总体结果

| 项目 | 数值 |
|------|------|
| 总用例数 | **10** |
| ✅ 通过 | **10** |
| ❌ 失败 | **0** |
| ⏭ 跳过 | **0** |
| 总耗时 | 7.397 s |
| 构建状态 | **BUILD SUCCESS** |

---

## 测试覆盖范围说明

> **如何判断是否完成闭环？**
>
> 闭环测试需满足以下条件：
> 1. **数据流完整**：从 HTTP 请求 → 控制器 → 应用服务 → 领域模型 → 数据库 → 响应，全链路均被覆盖。
> 2. **正向路径可用**：系统正常工作时，完整的业务流程能走通。
> 3. **异常边界全覆盖**：所有领域异常场景均有对应测试。
> 4. **参数校验前置**：输入非法时，请求在进入业务逻辑之前被拦截。
>
> 本测试套件通过 **TC01（E2E 闭环）** 验证完整链路，通过 TC02–TC10 覆盖所有分支。

### 覆盖矩阵

| 层级 | 覆盖内容 | 是否覆盖 |
|------|---------|---------|
| 接口层 | `POST /api/auth/login` 路由 | ✅ |
| 接口层 | `@Valid` 参数校验（空值、格式非法） | ✅ |
| 接口层 | `POST /api/auth/register` 注册接口 | ✅ (TC01) |
| 接口层 | `GET /api/auth/me` Token 鉴权接口 | ✅ (TC01) |
| 应用层 | `AuthApplicationService.login()` | ✅ |
| 领域层 | 用户不存在 → `UserNotFoundException` | ✅ |
| 领域层 | 密码错误 → `InvalidPasswordException` | ✅ |
| 领域层 | 账号锁定 → `AccountLockedException` | ✅ |
| 领域层 | 账号未激活 → `AccountInactiveException` | ✅ |
| 基础设施层 | H2 数据库读写 | ✅ |
| 安全层 | JWT Token 生成与解析 | ✅ (TC01) |
| 安全层 | Bearer Token 携带请求鉴权 | ✅ (TC01) |
| 异常处理 | `GlobalExceptionHandler` 统一响应 | ✅ |

---

## 测试用例详情

### TC01 · [E2E 闭环] 注册 → 登录 → 携带 Token 访问受保护接口

> ⚠️ **最关键用例** — 验证系统完整业务链路是否走通

| 属性 | 内容 |
|------|------|
| 类型 | 端到端闭环 (End-to-End) |
| 步骤 | ① `POST /api/auth/register` 注册账号 → ② `POST /api/auth/login` 登录获取 Token → ③ 携带 `Authorization: Bearer <token>` 访问 `/api/auth/me` |
| 验证点 | 注册返回 HTTP 201；登录返回 HTTP 200 + 非空 token；/me 返回 HTTP 200 + "authenticated" 字段 |
| 结果 | ✅ **PASS** |
| 意义 | 证明"注册→登录→鉴权"完整闭环可用，JWT 签发与验证机制正常 |

---

### TC02 · [成功] 登录成功 — 响应字段完整性

| 属性 | 内容 |
|------|------|
| 前置条件 | 已注册用户 `tc02@example.com` |
| 输入 | 正确的邮箱与密码 |
| 预期 | HTTP 200，`code=200`，`data.token` `data.userId` `data.username` `data.email` 均不为空 |
| 结果 | ✅ **PASS** |

---

### TC03 · [失败] 邮箱不存在 — 404

| 属性 | 内容 |
|------|------|
| 前置条件 | 无（邮箱在数据库中不存在） |
| 输入 | `ghost@example.com` |
| 预期 | HTTP 404，`code=404`，`message` 包含 "not found" |
| 结果 | ✅ **PASS** |
| 触发路径 | `AuthApplicationService.login()` → `orElseThrow(UserNotFoundException)` → `GlobalExceptionHandler` → 404 |

---

### TC04 · [失败] 密码错误 — 400

| 属性 | 内容 |
|------|------|
| 前置条件 | 已注册用户 `tc04@example.com`，密码 `Correct@9999` |
| 输入 | 正确邮箱 + 错误密码 `WrongPassword!` |
| 预期 | HTTP 400，`code=400`，`message="Invalid password"` |
| 结果 | ✅ **PASS** |
| 触发路径 | `User.login()` → `password.matches()` 返回 false → `InvalidPasswordException` → 400 |

---

### TC05 · [失败] 账号锁定 — 403

| 属性 | 内容 |
|------|------|
| 前置条件 | 注册用户 `tc05@example.com` 后调用 `user.lock()` 并持久化 |
| 输入 | 正确邮箱与密码 |
| 预期 | HTTP 403，`code=403`，`message="Account is locked"` |
| 结果 | ✅ **PASS** |
| 触发路径 | `User.login()` → `status == LOCKED` → `AccountLockedException` → 403 |

---

### TC06 · [失败] 账号未激活 — 403

| 属性 | 内容 |
|------|------|
| 前置条件 | 通过 `User.reconstitute()` 直接构造 `status=INACTIVE` 的用户并保存 |
| 输入 | 正确邮箱与密码 |
| 预期 | HTTP 403，`code=403`，`message="Account is not activated"` |
| 结果 | ✅ **PASS** |
| 触发路径 | `User.login()` → `status == INACTIVE` → `AccountInactiveException` → 403 |

---

### TC07 · [参数] 邮箱为空 — 400 校验

| 属性 | 内容 |
|------|------|
| 输入 | `email=""` |
| 预期 | HTTP 400，`code=400`（`@NotBlank` 触发 `MethodArgumentNotValidException`） |
| 结果 | ✅ **PASS** |

---

### TC08 · [参数] 邮箱格式非法 — 400 校验

| 属性 | 内容 |
|------|------|
| 输入 | `email="not-an-email"` |
| 预期 | HTTP 400，`code=400`（`@Email` 触发校验失败） |
| 结果 | ✅ **PASS** |

---

### TC09 · [参数] 密码为空 — 400 校验

| 属性 | 内容 |
|------|------|
| 输入 | `password=""` |
| 预期 | HTTP 400，`code=400`（`@NotBlank` 触发 `MethodArgumentNotValidException`） |
| 结果 | ✅ **PASS** |

---

### TC10 · [参数] 请求体字段全部缺失 — 400

| 属性 | 内容 |
|------|------|
| 输入 | `{}` 空 JSON |
| 预期 | HTTP 400，`code=400` |
| 结果 | ✅ **PASS** |

---

## 闭环完整性评估

```
注册接口 ─────────────────────────────────┐
                                          ▼
用户数据库 ←──────── 应用服务 ←────── 登录接口 (POST /api/auth/login)
(H2 内存)                │                       │
                         │ 查无此人 → 404         │ 参数非法 → 400 (校验层)
                         │ 密码错误 → 400         │
                         │ 账号锁定 → 403         │
                         │ 未激活   → 403         │
                         │                        │
                         └──── 生成 JWT Token ────┘
                                          │
                                          ▼
                              鉴权接口 GET /api/auth/me
                              (验证 Bearer Token 有效)
```

**结论：闭环已完成。** 所有路径均有测试覆盖，无失败用例。

---

## 未覆盖范围（超出本次测试目标）

| 场景 | 说明 |
|------|------|
| 并发登录 | 同一账号高并发场景未测试 |
| Token 过期 | JWT 24h 过期后访问未测试 |
| 非法 Token | 篡改/伪造 Token 访问 /me 未测试 |
| 注册接口完整测试 | 本报告仅覆盖登录闭环，注册边界用例未展开 |
