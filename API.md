



# Nova Mall 接口文档

> **项目名称**: nova-mall 电商平台
>
> **技术栈**: Spring Boot 3.5 + Spring Security JWT + MyBatis-Plus + Elasticsearch 7.17
>
> **基础路径**: Admin后台模块默认端口8080，Portal前台模块默认端口8081
>
> **认证方式**: 需认证接口请在请求头携带 `Authorization: Bearer {token}`
>
> **通用返回格式**:
> ```json
> { "code": 200, "message": "操作成功", "data": {} }
> ```
> | 参数名 | 类型 | 说明 |
> |--------|------|------|
> | code | number | 响应码，200代表成功 |
> | message | string | 提示信息 |
> | data | object | 返回的数据 |

---

## Admin 后台管理模块

---

### 后台用户管理

---

#### 用户注册

**基本信息**

请求路径：`/admin/register`

请求方式：POST

接口描述：该接口用于注册后台管理员账号

**请求参数**

格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| username | string | 必须 | 用户名 |
| password | string | 必须 | 密码 |
| icon | string | 非必须 | 用户头像 |
| email | string | 非必须 | 邮箱 |
| nickName | string | 非必须 | 用户昵称 |
| note | string | 非必须 | 备注 |

请求参数样例：

```json
{
  "username": "admin",
  "password": "123456",
  "email": "admin@mall.com",
  "nickName": "管理员"
}
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码，200代表成功 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 返回注册成功的用户信息 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "icon": "",
    "email": "admin@mall.com",
    "nickName": "管理员",
    "note": "",
    "createTime": "2025-01-01T00:00:00",
    "loginTime": null,
    "status": 1
  }
}
```

---

#### 用户登录

**基本信息**

请求路径：`/admin/login`

请求方式：POST

接口描述：该接口用于后台管理员登录，返回JWT Token

**请求参数**

格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| username | string | 必须 | 用户名 |
| password | string | 必须 | 密码 |

请求参数样例：

```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码，200代表成功 |
| message | string | 非必须 | 提示信息 |
| data.token | string | 非必须 | JWT Token |
| data.tokenHead | string | 非必须 | Token前缀（如 "Bearer "） |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenHead": "Bearer "
  }
}
```

---

#### 刷新Token

**基本信息**

请求路径：`/admin/refreshToken`

请求方式：GET

接口描述：该接口用于刷新JWT Token

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| Authorization | string | 必须 | 请求头携带，格式：Bearer {旧token} |

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码，200代表成功 |
| message | string | 非必须 | 提示信息 |
| data.token | string | 非必须 | 新的JWT Token |
| data.tokenHead | string | 非必须 | Token前缀 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenHead": "Bearer "
  }
}
```

---

#### 获取当前登录用户信息

**基本信息**

请求路径：`/admin/info`

请求方式：GET

接口描述：该接口用于获取当前登录管理员的用户名、头像、菜单列表和角色列表

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| Authorization | string | 必须 | 请求头携带，格式：Bearer {token} |

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码，200代表成功 |
| message | string | 非必须 | 提示信息 |
| data.username | string | 非必须 | 用户名 |
| data.menus | object[] | 非必须 | 菜单列表 |
| data.icon | string | 非必须 | 用户头像 |
| data.roles | string[] | 非必须 | 角色名称列表 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "username": "admin",
    "menus": [],
    "icon": "http://xxx.com/avatar.jpg",
    "roles": ["超级管理员"]
  }
}
```

---

#### 登出

**基本信息**

请求路径：`/admin/logout`

请求方式：POST

接口描述：该接口用于管理员登出

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| Authorization | string | 必须 | 请求头携带，格式：Bearer {token} |

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码，200代表成功 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | null |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

#### 分页获取用户列表

**基本信息**

请求路径：`/admin/list`

请求方式：GET

接口描述：该接口用于根据用户名或姓名分页获取后台用户列表

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| keyword | string | 非必须 | 用户名或姓名模糊搜索 |
| pageSize | number | 非必须 | 每页数量，默认5 |
| pageNum | number | 非必须 | 页码，默认1 |

请求参数样例：

```
/admin/list?keyword=admin&pageNum=1&pageSize=10
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data.pageNum | number | 非必须 | 当前页码 |
| data.pageSize | number | 非必须 | 每页数量 |
| data.totalPage | number | 非必须 | 总页数 |
| data.total | number | 非必须 | 总条数 |
| data.list | object[] | 非必须 | 用户列表 |
| data.list[].id | number | 非必须 | 用户ID |
| data.list[].username | string | 非必须 | 用户名 |
| data.list[].icon | string | 非必须 | 头像 |
| data.list[].email | string | 非必须 | 邮箱 |
| data.list[].nickName | string | 非必须 | 昵称 |
| data.list[].note | string | 非必须 | 备注 |
| data.list[].createTime | string | 非必须 | 创建时间 |
| data.list[].status | number | 非必须 | 帐号状态：0=禁用，1=启用 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "totalPage": 3,
    "total": 25,
    "list": [
      {
        "id": 1,
        "username": "admin",
        "icon": "",
        "email": "admin@mall.com",
        "nickName": "管理员",
        "note": "",
        "createTime": "2025-01-01T00:00:00",
        "status": 1
      }
    ]
  }
}
```

---

#### 获取指定用户信息

**基本信息**

请求路径：`/admin/{id}`

请求方式：GET

接口描述：该接口用于根据ID获取指定用户信息

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 路径参数，用户ID |

请求参数样例：

```
/admin/1
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 用户信息 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "icon": "",
    "email": "admin@mall.com",
    "nickName": "管理员",
    "note": "",
    "createTime": "2025-01-01T00:00:00",
    "loginTime": "2025-06-01T12:00:00",
    "status": 1
  }
}
```

---

#### 修改指定用户信息

**基本信息**

请求路径：`/admin/update/{id}`

请求方式：POST

接口描述：该接口用于修改指定用户的信息

**请求参数**

路径参数：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 用户ID |

请求体格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| username | string | 非必须 | 用户名 |
| password | string | 非必须 | 密码（会自动加密） |
| icon | string | 非必须 | 头像 |
| email | string | 非必须 | 邮箱 |
| nickName | string | 非必须 | 昵称 |
| note | string | 非必须 | 备注 |
| status | number | 非必须 | 帐号状态：0=禁用，1=启用 |

请求参数样例：

```json
{
  "nickName": "新昵称",
  "email": "new@mall.com",
  "status": 1
}
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

---

#### 修改指定用户密码

**基本信息**

请求路径：`/admin/updatePassword`

请求方式：POST

接口描述：该接口用于修改指定用户的密码

**请求参数**

格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| username | string | 必须 | 用户名 |
| oldPassword | string | 必须 | 旧密码 |
| newPassword | string | 必须 | 新密码 |

请求参数样例：

```json
{
  "username": "admin",
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 1=成功，-1=参数不合法，-2=用户不存在，-3=旧密码错误 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

---

#### 删除指定用户

**基本信息**

请求路径：`/admin/delete/{id}`

请求方式：POST

接口描述：该接口用于删除指定用户

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 路径参数，用户ID |

请求参数样例：

```
/admin/delete/1
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

---

#### 修改账号状态

**基本信息**

请求路径：`/admin/updateStatus/{id}`

请求方式：POST

接口描述：该接口用于修改指定用户的启用/禁用状态

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 路径参数，用户ID |
| status | number | 必须 | 查询参数，0=禁用，1=启用 |

请求参数样例：

```
/admin/updateStatus/1?status=0
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

---

#### 给用户分配角色

**基本信息**

请求路径：`/admin/role/update`

请求方式：POST

接口描述：该接口用于给指定用户分配角色

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| adminId | number | 必须 | 用户ID |
| roleIds | number[] | 必须 | 角色ID列表 |

请求参数样例：

```
/admin/role/update?adminId=1&roleIds=1&roleIds=2
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 2
}
```

---

#### 获取指定用户的角色

**基本信息**

请求路径：`/admin/role/{adminId}`

请求方式：GET

接口描述：该接口用于获取指定用户已分配的角色列表

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| adminId | number | 必须 | 路径参数，用户ID |

请求参数样例：

```
/admin/role/1
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object[] | 非必须 | 角色列表 |
| data[].id | number | 非必须 | 角色ID |
| data[].name | string | 非必须 | 角色名称 |
| data[].description | string | 非必须 | 角色描述 |
| data[].status | number | 非必须 | 启用状态：0=禁用，1=启用 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "超级管理员",
      "description": "拥有所有权限",
      "status": 1
    }
  ]
}
```

---

### 后台角色管理

---

#### 添加角色

**基本信息**

请求路径：`/role/create`

请求方式：POST

接口描述：该接口用于添加新的后台角色

**请求参数**

格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| name | string | 必须 | 角色名称 |
| description | string | 非必须 | 角色描述 |
| sort | number | 非必须 | 排序 |
| status | number | 非必须 | 启用状态：0=禁用，1=启用 |

请求参数样例：

```json
{
  "name": "商品管理员",
  "description": "管理商品相关操作",
  "sort": 0,
  "status": 1
}
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

---

#### 修改角色

**基本信息**

请求路径：`/role/update/{id}`

请求方式：POST

接口描述：该接口用于修改指定角色的信息

**请求参数**

路径参数：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 角色ID |

请求体格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| name | string | 非必须 | 角色名称 |
| description | string | 非必须 | 角色描述 |
| sort | number | 非必须 | 排序 |
| status | number | 非必须 | 启用状态 |

请求参数样例：

```json
{
  "name": "商品管理员（已修改）",
  "description": "管理商品相关操作"
}
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

---

#### 批量删除角色

**基本信息**

请求路径：`/role/delete`

请求方式：POST

接口描述：该接口用于批量删除角色

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| ids | number[] | 必须 | 角色ID列表 |

请求参数样例：

```
/role/delete?ids=1&ids=2
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 2
}
```

---

#### 获取所有角色

**基本信息**

请求路径：`/role/listAll`

请求方式：GET

接口描述：该接口用于获取所有角色列表（不分页）

**请求参数**

无

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object[] | 非必须 | 角色列表 |
| data[].id | number | 非必须 | 角色ID |
| data[].name | string | 非必须 | 角色名称 |
| data[].description | string | 非必须 | 角色描述 |
| data[].status | number | 非必须 | 启用状态 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "id": 1, "name": "超级管理员", "description": "拥有所有权限", "status": 1 },
    { "id": 2, "name": "商品管理员", "description": "管理商品", "status": 1 }
  ]
}
```

---

#### 分页获取角色列表

**基本信息**

请求路径：`/role/list`

请求方式：GET

接口描述：该接口用于根据角色名称分页获取角色列表

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| keyword | string | 非必须 | 角色名称模糊搜索 |
| pageSize | number | 非必须 | 每页数量，默认5 |
| pageNum | number | 非必须 | 页码，默认1 |

请求参数样例：

```
/role/list?keyword=管理&pageNum=1&pageSize=10
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 分页数据（CommonPage格式） |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "totalPage": 1,
    "total": 2,
    "list": [
      { "id": 1, "name": "超级管理员", "description": "拥有所有权限", "status": 1 }
    ]
  }
}
```

---

#### 修改角色状态

**基本信息**

请求路径：`/role/updateStatus/{id}`

请求方式：POST

接口描述：该接口用于修改角色的启用/禁用状态

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 路径参数，角色ID |
| status | number | 必须 | 查询参数，0=禁用，1=启用 |

请求参数样例：

```
/role/updateStatus/1?status=0
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

---

#### 获取角色相关菜单

**基本信息**

请求路径：`/role/listMenu/{roleId}`

请求方式：GET

接口描述：该接口用于获取指定角色已关联的菜单列表

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| roleId | number | 必须 | 路径参数，角色ID |

请求参数样例：

```
/role/listMenu/1
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object[] | 非必须 | 菜单列表 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "id": 1, "parentId": 0, "title": "商品管理", "name": "pms", "icon": "product", "hidden": 0 }
  ]
}
```

---

#### 获取角色相关资源

**基本信息**

请求路径：`/role/listResource/{roleId}`

请求方式：GET

接口描述：该接口用于获取指定角色已关联的资源（API接口）列表

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| roleId | number | 必须 | 路径参数，角色ID |

请求参数样例：

```
/role/listResource/1
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object[] | 非必须 | 资源列表 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "id": 1, "name": "商品管理", "url": "/product/**", "description": "商品管理相关接口", "categoryId": 1 }
  ]
}
```

---

#### 给角色分配菜单

**基本信息**

请求路径：`/role/allocMenu`

请求方式：POST

接口描述：该接口用于给指定角色分配菜单（先删后插策略）

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| roleId | number | 必须 | 角色ID |
| menuIds | number[] | 必须 | 菜单ID列表 |

请求参数样例：

```
/role/allocMenu?roleId=1&menuIds=1&menuIds=2&menuIds=3
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 3
}
```

---

#### 给角色分配资源

**基本信息**

请求路径：`/role/allocResource`

请求方式：POST

接口描述：该接口用于给指定角色分配资源（API接口权限，先删后插策略）

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| roleId | number | 必须 | 角色ID |
| resourceIds | number[] | 必须 | 资源ID列表 |

请求参数样例：

```
/role/allocResource?roleId=1&resourceIds=1&resourceIds=2
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 2
}
```

---

### 后台菜单管理

---

#### 创建后台菜单

**基本信息**

请求路径：`/menu/create`

请求方式：POST

接口描述：该接口用于创建后台菜单

**请求参数**

格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| parentId | number | 必须 | 父菜单ID，0表示一级菜单 |
| title | string | 必须 | 菜单标题 |
| level | number | 非必须 | 菜单级别 |
| sort | number | 非必须 | 排序 |
| name | string | 必须 | 菜单名称（路由名称） |
| icon | string | 非必须 | 菜单图标 |
| hidden | number | 非必须 | 是否隐藏：0=显示，1=隐藏 |

请求参数样例：

```json
{
  "parentId": 0,
  "title": "商品管理",
  "level": 0,
  "sort": 0,
  "name": "pms",
  "icon": "product",
  "hidden": 0
}
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

---

#### 修改后台菜单

**基本信息**

请求路径：`/menu/update/{id}`

请求方式：POST

接口描述：该接口用于修改后台菜单

**请求参数**

路径参数：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 菜单ID |

请求体格式：application/json（字段同创建接口）

请求参数样例：

```json
{
  "title": "商品管理（已修改）",
  "icon": "product-new"
}
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

---

#### 获取菜单详情

**基本信息**

请求路径：`/menu/{id}`

请求方式：GET

接口描述：该接口用于根据ID获取菜单详情

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 路径参数，菜单ID |

请求参数样例：

```
/menu/1
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 菜单详情 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "parentId": 0,
    "title": "商品管理",
    "level": 0,
    "sort": 0,
    "name": "pms",
    "icon": "product",
    "hidden": 0
  }
}
```

---

#### 删除后台菜单

**基本信息**

请求路径：`/menu/delete/{id}`

请求方式：POST

接口描述：该接口用于根据ID删除后台菜单

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 路径参数，菜单ID |

请求参数样例：

```
/menu/delete/1
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

---

#### 分页查询后台菜单

**基本信息**

请求路径：`/menu/list/{parentId}`

请求方式：GET

接口描述：该接口用于分页查询指定父菜单下的子菜单

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| parentId | number | 必须 | 路径参数，父菜单ID |
| pageSize | number | 非必须 | 每页数量，默认5 |
| pageNum | number | 非必须 | 页码，默认1 |

请求参数样例：

```
/menu/list/0?pageNum=1&pageSize=10
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 分页数据（CommonPage格式） |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "totalPage": 1,
    "total": 3,
    "list": [
      { "id": 1, "parentId": 0, "title": "商品管理", "name": "pms", "icon": "product", "hidden": 0 }
    ]
  }
}
```

---

#### 树形结构返回所有菜单

**基本信息**

请求路径：`/menu/treeList`

请求方式：GET

接口描述：该接口用于以树形结构返回所有菜单列表

**请求参数**

无

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object[] | 非必须 | 树形菜单列表 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "parentId": 0,
      "title": "商品管理",
      "name": "pms",
      "icon": "product",
      "children": [
        { "id": 4, "parentId": 1, "title": "商品列表", "name": "pms:product", "children": [] }
      ]
    }
  ]
}
```

---

#### 修改菜单显示状态

**基本信息**

请求路径：`/menu/updateHidden/{id}`

请求方式：POST

接口描述：该接口用于修改菜单的显示/隐藏状态

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 路径参数，菜单ID |
| hidden | number | 必须 | 查询参数，0=显示，1=隐藏 |

请求参数样例：

```
/menu/updateHidden/1?hidden=1
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

---

### 后台资源管理

---

#### 添加后台资源

**基本信息**

请求路径：`/resource/create`

请求方式：POST

接口描述：该接口用于添加后台资源（API接口权限控制）

**请求参数**

格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| name | string | 必须 | 资源名称 |
| url | string | 必须 | 资源URL路径（支持Ant风格） |
| description | string | 非必须 | 资源描述 |
| categoryId | number | 非必须 | 资源分类ID |

请求参数样例：

```json
{
  "name": "商品管理",
  "url": "/product/**",
  "description": "商品管理相关接口",
  "categoryId": 1
}
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

---

#### 修改后台资源

**基本信息**

请求路径：`/resource/update/{id}`

请求方式：POST

接口描述：该接口用于修改后台资源

**请求参数**

路径参数：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 资源ID |

请求体格式：application/json（字段同创建接口）

请求参数样例：

```json
{
  "name": "商品管理（已修改）",
  "url": "/product/**"
}
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

---

#### 获取资源详情

**基本信息**

请求路径：`/resource/{id}`

请求方式：GET

接口描述：该接口用于根据ID获取资源详情

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 路径参数，资源ID |

请求参数样例：

```
/resource/1
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 资源详情 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "商品管理",
    "url": "/product/**",
    "description": "商品管理相关接口",
    "categoryId": 1
  }
}
```

---

#### 删除后台资源

**基本信息**

请求路径：`/resource/delete/{id}`

请求方式：POST

接口描述：该接口用于删除后台资源

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 路径参数，资源ID |

请求参数样例：

```
/resource/delete/1
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

---

#### 分页模糊查询后台资源

**基本信息**

请求路径：`/resource/list`

请求方式：GET

接口描述：该接口用于分页模糊查询后台资源

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| categoryId | number | 非必须 | 资源分类ID |
| nameKeyword | string | 非必须 | 资源名称模糊搜索 |
| urlKeyword | string | 非必须 | 资源URL模糊搜索 |
| pageSize | number | 非必须 | 每页数量，默认5 |
| pageNum | number | 非必须 | 页码，默认1 |

请求参数样例：

```
/resource/list?nameKeyword=商品&pageNum=1&pageSize=10
```

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 分页数据（CommonPage格式） |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "totalPage": 1,
    "total": 1,
    "list": [
      { "id": 1, "name": "商品管理", "url": "/product/**", "description": "商品管理相关接口", "categoryId": 1 }
    ]
  }
}
```

---

#### 查询所有后台资源

**基本信息**

请求路径：`/resource/listAll`

请求方式：GET

接口描述：该接口用于查询所有后台资源（不分页）

**请求参数**

无

**响应数据**

参数格式：application/json

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object[] | 非必须 | 全部资源列表 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "id": 1, "name": "商品管理", "url": "/product/**", "description": "商品管理相关接口", "categoryId": 1 },
    { "id": 2, "name": "订单管理", "url": "/order/**", "description": "订单管理相关接口", "categoryId": 2 }
  ]
}
```

---

---

### 资源分类管理

---

#### 查询所有资源分类

**基本信息**

请求路径：`/resourceCategory/listAll`

请求方式：GET

接口描述：该接口用于查询所有后台资源分类（不分页）

**请求参数**

无

**响应数据**

参数格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object[] | 非必须 | 资源分类列表 |
| data[].id | number | 非必须 | 分类ID |
| data[].name | string | 非必须 | 分类名称 |
| data[].sort | number | 非必须 | 排序 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "id": 1, "name": "商品管理", "sort": 0 },
    { "id": 2, "name": "订单管理", "sort": 1 }
  ]
}
```

---

#### 添加资源分类

**基本信息**

请求路径：`/resourceCategory/create`

请求方式：POST

接口描述：该接口用于添加后台资源分类

**请求参数**

格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| name | string | 必须 | 分类名称 |
| sort | number | 非必须 | 排序 |

请求参数样例：

```json
{ "name": "商品管理", "sort": 0 }
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 1 }
```

---

#### 修改资源分类

**基本信息**

请求路径：`/resourceCategory/update/{id}`

请求方式：POST

接口描述：该接口用于修改后台资源分类

**请求参数**

路径参数：`id`(number, 必须) - 分类ID

请求体格式：application/json（字段同创建接口）

请求参数样例：

```json
{ "name": "商品管理（已修改）" }
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 1 }
```

---

#### 删除资源分类

**基本信息**

请求路径：`/resourceCategory/delete/{id}`

请求方式：POST

接口描述：该接口用于删除后台资源分类

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 路径参数，分类ID |

请求参数样例：

```
/resourceCategory/delete/1
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 1 }
```

---

### 会员等级管理

---

#### 查询所有会员等级

**基本信息**

请求路径：`/memberLevel/list`

请求方式：GET

接口描述：该接口用于根据默认状态查询所有会员等级

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| defaultStatus | number | 必须 | 默认等级状态：0=非默认，1=默认 |

请求参数样例：

```
/memberLevel/list?defaultStatus=0
```

**响应数据**

参数格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object[] | 非必须 | 会员等级列表 |
| data[].id | number | 非必须 | 等级ID |
| data[].name | string | 非必须 | 等级名称 |
| data[].growthPoint | number | 非必须 | 成长值 |
| data[].defaultStatus | number | 非必须 | 是否默认等级 |
| data[].freeFreightPoint | number | 非必须 | 免运费门槛 |
| data[].commentGrowthPoint | number | 非必须 | 评论获得成长值 |
| data[].priviledgeFreeFreight | number | 非必须 | 是否有免运费特权 |
| data[].priviledgeSignIn | number | 非必须 | 是否有签到特权 |
| data[].priviledgeComment | number | 非必须 | 是否有评论特权 |
| data[].priviledgePromotion | number | 非必须 | 是否有活动特权 |
| data[].priviledgeMemberPrice | number | 非必须 | 是否有会员价格特权 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "普通会员",
      "growthPoint": 0,
      "defaultStatus": 1,
      "freeFreightPoint": 0,
      "priviledgeFreeFreight": 0,
      "priviledgeSignIn": 0,
      "priviledgeComment": 0,
      "priviledgePromotion": 0,
      "priviledgeMemberPrice": 0
    }
  ]
}
```

---

### 商品管理

---

#### 创建商品

**基本信息**

请求路径：`/product/create`

请求方式：POST

接口描述：该接口用于创建商品，包含基本信息、阶梯价、满减、会员价、SKU、属性值等

**请求参数**

格式：application/json

参数说明（继承PmsProduct所有字段 + 以下扩展字段）：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| productLadderList | object[] | 非必须 | 阶梯价格设置 |
| productFullReductionList | object[] | 非必须 | 满减价格设置 |
| memberPriceList | object[] | 非必须 | 会员价格设置 |
| skuStockList | object[] | 非必须 | SKU库存信息 |
| productAttributeValueList | object[] | 非必须 | 商品属性值 |

请求参数样例：

```json
{
  "brandId": 1,
  "productCategoryId": 1,
  "name": "测试商品",
  "productSn": "20250001",
  "publishStatus": 1,
  "verifyStatus": 1,
  "skuStockList": [
    { "sku": "默认", "stock": 100, "price": 99.00 }
  ]
}
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 1 }
```

---

#### 查询商品

**基本信息**

请求路径：`/product/list`

请求方式：GET

接口描述：该接口用于分页查询商品列表，支持多种筛选条件

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| publishStatus | number | 非必须 | 上架状态 |
| verifyStatus | number | 非必须 | 审核状态 |
| keyword | string | 非必须 | 商品名称模糊搜索 |
| productSn | string | 非必须 | 商品货号 |
| productCategoryId | number | 非必须 | 商品分类编号 |
| brandId | number | 非必须 | 商品品牌编号 |
| pageSize | number | 非必须 | 每页数量，默认50 |
| pageNum | number | 非必须 | 页码，默认1 |

请求参数样例：

```
/product/list?keyword=手机&pageNum=1&pageSize=10
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 分页数据（CommonPage格式） |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "totalPage": 5,
    "total": 50,
    "list": [
      { "id": 1, "name": "测试商品", "productSn": "20250001", "price": 99.00, "publishStatus": 1 }
    ]
  }
}
```

---

#### 更新商品

**基本信息**

请求路径：`/product/update/{id}`

请求方式：POST

接口描述：该接口用于更新指定商品的信息

**请求参数**

路径参数：`id`(number, 必须) - 商品ID

请求体格式：application/json（字段同创建接口）

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 1 }
```

---

#### 批量修改审核状态

**基本信息**

请求路径：`/product/update/verifyStatus`

请求方式：POST

接口描述：该接口用于批量修改商品的审核状态

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| ids | number[] | 必须 | 商品ID列表 |
| verifyStatus | number | 必须 | 审核状态 |
| detail | string | 必须 | 审核详情 |

请求参数样例：

```
/product/update/verifyStatus?ids=1&ids=2&verifyStatus=1&detail=审核通过
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 2 }
```

---

#### 批量上下架商品

**基本信息**

请求路径：`/product/update/publishStatus`

请求方式：POST

接口描述：该接口用于批量上架或下架商品

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| ids | number[] | 必须 | 商品ID列表 |
| publishStatus | number | 必须 | 上架状态：0=下架，1=上架 |

请求参数样例：

```
/product/update/publishStatus?ids=1&ids=2&publishStatus=1
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 2 }
```

---

### 品牌管理

---

#### 添加品牌

**基本信息**

请求路径：`/brand/create`

请求方式：POST

接口描述：该接口用于添加商品品牌

**请求参数**

格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| name | string | 必须 | 品牌名称 |
| firstLetter | string | 非必须 | 品牌首字母 |
| sort | number | 非必须 | 排序 |
| factoryStatus | number | 非必须 | 是否为厂家制造商：0=否，1=是 |
| showStatus | number | 非必须 | 是否显示：0=不显示，1=显示 |
| logo | string | 必须 | 品牌logo |
| bigPic | string | 非必须 | 品牌大图 |
| brandStory | string | 非必须 | 品牌故事 |

请求参数样例：

```json
{
  "name": "华为",
  "firstLetter": "H",
  "sort": 0,
  "factoryStatus": 1,
  "showStatus": 1,
  "logo": "http://xxx.com/huawei.jpg"
}
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 1 }
```

---

#### 根据品牌名称分页获取品牌列表

**基本信息**

请求路径：`/brand/list`

请求方式：GET

接口描述：该接口用于根据品牌名称分页获取品牌列表

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| keyword | string | 非必须 | 品牌名称模糊搜索 |
| showStatus | number | 非必须 | 显示状态 |
| pageSize | number | 非必须 | 每页数量，默认5 |
| pageNum | number | 非必须 | 页码，默认1 |

请求参数样例：

```
/brand/list?keyword=华为&pageNum=1&pageSize=10
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 分页数据（CommonPage格式） |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "totalPage": 1,
    "total": 1,
    "list": [
      { "id": 1, "name": "华为", "logo": "http://xxx.com/huawei.jpg", "showStatus": 1 }
    ]
  }
}
```

---

### 商品分类管理

---

#### 添加商品分类

**基本信息**

请求路径：`/productCategory/create`

请求方式：POST

接口描述：该接口用于添加商品分类

**请求参数**

格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| parentId | number | 必须 | 父分类ID，0表示一级分类 |
| name | string | 必须 | 分类名称 |
| level | number | 非必须 | 分类级别 |
| sort | number | 非必须 | 排序 |
| icon | string | 非必须 | 图标 |
| productUnit | string | 非必须 | 商品单位 |
| showStatus | number | 非必须 | 显示状态：0=不显示，1=显示 |
| navStatus | number | 非必须 | 导航栏显示状态：0=不显示，1=显示 |
| keywords | string | 非必须 | 关键字 |
| description | string | 非必须 | 描述 |

请求参数样例：

```json
{
  "parentId": 0,
  "name": "手机",
  "level": 0,
  "sort": 0,
  "showStatus": 1,
  "navStatus": 1,
  "productUnit": "件"
}
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 1 }
```

---

#### 查询所有一级分类及子分类

**基本信息**

请求路径：`/productCategory/list/withChildren`

请求方式：GET

接口描述：该接口用于查询所有一级分类及其子分类（树形结构）

**请求参数**

无

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object[] | 非必须 | 分类列表（含children） |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "手机",
      "children": [
        { "id": 2, "name": "华为手机", "children": [] }
      ]
    }
  ]
}
```

---

### 订单管理

---

#### 查询订单

**基本信息**

请求路径：`/order/list`

请求方式：GET

接口描述：该接口用于分页查询订单列表

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| receiverKeyword | string | 非必须 | 收货人关键字 |
| orderSn | string | 非必须 | 订单编号 |
| receiveStatus | number | 非必须 | 收货状态 |
| status | number | 非必须 | 订单状态：0=待付款，1=待发货，2=已发货，3=已完成，4=已关闭 |
| orderType | number | 非必须 | 订单类型：0=正常订单，1=秒杀订单 |
| sourceType | number | 非必须 | 订单来源：0=PC，1=APP |
| createTime | string | 非必须 | 创建时间范围 |
| pageSize | number | 非必须 | 每页数量，默认5 |
| pageNum | number | 非必须 | 页码，默认1 |

请求参数样例：

```
/order/list?status=1&pageNum=1&pageSize=10
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 分页数据（CommonPage格式） |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "totalPage": 5,
    "total": 50,
    "list": [
      { "id": 1, "orderSn": "2025060100001", "status": 1, "totalAmount": 299.00 }
    ]
  }
}
```

---

#### 获取订单详情

**基本信息**

请求路径：`/order/{id}`

请求方式：GET

接口描述：该接口用于获取订单详情，包含订单信息、商品信息和操作记录

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 路径参数，订单ID |

请求参数样例：

```
/order/1
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 订单详情 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "orderSn": "2025060100001",
    "status": 1,
    "totalAmount": 299.00,
    "payAmount": 279.00,
    "orderItemList": [],
    "historyList": []
  }
}
```

---

#### 批量发货

**基本信息**

请求路径：`/order/update/delivery`

请求方式：POST

接口描述：该接口用于批量发货订单

**请求参数**

格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| orderId | number | 必须 | 订单ID |
| deliveryCompany | string | 必须 | 物流公司 |
| deliverySn | string | 必须 | 物流单号 |

请求参数样例：

```json
[
  { "orderId": 1, "deliveryCompany": "顺丰速运", "deliverySn": "SF1234567890" }
]
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 1 }
```

---

#### 批量关闭订单

**基本信息**

请求路径：`/order/update/close`

请求方式：POST

接口描述：该接口用于批量关闭订单

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| ids | number[] | 必须 | 订单ID列表 |
| note | string | 必须 | 备注 |

请求参数样例：

```
/order/update/close?ids=1&ids=2&note=超时关闭
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 2 }
```

---

#### 修改收货人信息

**基本信息**

请求路径：`/order/update/receiverInfo`

请求方式：POST

接口描述：该接口用于修改订单的收货人信息

**请求参数**

格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| orderId | number | 必须 | 订单ID |
| receiverName | string | 必须 | 收货人姓名 |
| receiverPhone | string | 必须 | 收货人电话 |
| receiverPostCode | string | 非必须 | 邮编 |
| receiverProvince | string | 非必须 | 省份 |
| receiverCity | string | 非必须 | 城市 |
| receiverRegion | string | 非必须 | 区/县 |
| receiverDetailAddress | string | 非必须 | 详细地址 |

请求参数样例：

```json
{
  "orderId": 1,
  "receiverName": "张三",
  "receiverPhone": "13800138000",
  "receiverProvince": "广东省",
  "receiverCity": "深圳市",
  "receiverRegion": "南山区",
  "receiverDetailAddress": "科技园路1号"
}
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 1 }
```

---

#### 修改订单费用信息

**基本信息**

请求路径：`/order/update/moneyInfo`

请求方式：POST

接口描述：该接口用于修改订单的费用信息

**请求参数**

格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| orderId | number | 必须 | 订单ID |
| freightAmount | number | 非必须 | 运费 |
| discountAmount | number | 非必须 | 折扣金额 |
| status | number | 非必须 | 订单状态 |

请求参数样例：

```json
{ "orderId": 1, "freightAmount": 0.00, "discountAmount": 10.00 }
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 1 }
```

---

### 优惠券管理

---

#### 添加优惠券

**基本信息**

请求路径：`/coupon/create`

请求方式：POST

接口描述：该接口用于添加优惠券

**请求参数**

格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| name | string | 必须 | 优惠券名称 |
| type | number | 必须 | 类型：0=全场赠券，1=指定分类，2=指定商品 |
| useType | number | 必须 | 使用类型：0=全场通用，1=指定分类，2=指定商品 |
| platform | number | 非必须 | 使用平台：0=全部，1=移动，2=PC |
| amount | number | 必须 | 优惠金额 |
| perMemberLimit | number | 非必须 | 每人限领张数 |
| enableTime | string | 必须 | 可使用开始时间 |
| startTime | string | 必须 | 优惠券有效期开始时间 |
| endTime | string | 必须 | 优惠券有效期结束时间 |
| code | string | 非必须 | 优惠码 |
| publishCount | number | 非必须 | 发行数量 |
| minPoint | number | 非必须 | 使用门槛：满X元可用 |
| productRelationList | object[] | 非必须 | 关联商品列表 |
| productCategoryRelationList | object[] | 非必须 | 关联分类列表 |

请求参数样例：

```json
{
  "name": "满100减10",
  "type": 0,
  "useType": 0,
  "platform": 0,
  "amount": 10,
  "minPoint": 100,
  "perMemberLimit": 1,
  "publishCount": 1000,
  "enableTime": "2025-01-01T00:00:00",
  "startTime": "2025-01-01T00:00:00",
  "endTime": "2025-12-31T23:59:59"
}
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 1 }
```

---

#### 根据优惠券名称和类型分页获取优惠券列表

**基本信息**

请求路径：`/coupon/list`

请求方式：GET

接口描述：该接口用于根据优惠券名称和类型分页获取优惠券列表

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| name | string | 非必须 | 优惠券名称 |
| type | number | 非必须 | 优惠券类型 |
| pageSize | number | 非必须 | 每页数量，默认5 |
| pageNum | number | 非必须 | 页码，默认1 |

请求参数样例：

```
/coupon/list?name=满减&pageNum=1&pageSize=10
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 分页数据（CommonPage格式） |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "totalPage": 1,
    "total": 1,
    "list": [
      { "id": 1, "name": "满100减10", "type": 0, "amount": 10, "minPoint": 100 }
    ]
  }
}
```

---

### 限时购活动管理

---

#### 添加限时购活动

**基本信息**

请求路径：`/flash/create`

请求方式：POST

接口描述：该接口用于添加限时购（秒杀）活动

**请求参数**

格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| title | string | 必须 | 活动标题 |
| startTime | string | 必须 | 开始时间 |
| endTime | string | 必须 | 结束时间 |
| status | number | 非必须 | 上下线状态：0=下线，1=上线 |

请求参数样例：

```json
{
  "title": "618限时秒杀",
  "startTime": "2025-06-18T00:00:00",
  "endTime": "2025-06-18T23:59:59",
  "status": 1
}
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 1 }
```

---

#### 根据活动名称分页查询限时购活动

**基本信息**

请求路径：`/flash/list`

请求方式：GET

接口描述：该接口用于根据活动名称分页查询限时购活动

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| keyword | string | 非必须 | 活动名称模糊搜索 |
| pageSize | number | 非必须 | 每页数量，默认5 |
| pageNum | number | 非必须 | 页码，默认1 |

请求参数样例：

```
/flash/list?keyword=618&pageNum=1&pageSize=10
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 分页数据（CommonPage格式） |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "totalPage": 1,
    "total": 1,
    "list": [
      { "id": 1, "title": "618限时秒杀", "startTime": "2025-06-18T00:00:00", "endTime": "2025-06-18T23:59:59", "status": 1 }
    ]
  }
}
```

---

### 首页轮播广告管理

---

#### 添加广告

**基本信息**

请求路径：`/home/advertise/create`

请求方式：POST

接口描述：该接口用于添加首页轮播广告

**请求参数**

格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| name | string | 必须 | 广告名称 |
| type | number | 非必须 | 广告类型：0=首页轮播，1=首页推荐 |
| pic | string | 必须 | 广告图片URL |
| startTime | string | 非必须 | 开始时间 |
| endTime | string | 非必须 | 结束时间 |
| status | number | 非必须 | 上下线状态：0=下线，1=上线 |
| url | string | 非必须 | 广告链接 |
| sort | number | 非必须 | 排序 |
| note | string | 非必须 | 备注 |

请求参数样例：

```json
{
  "name": "618大促",
  "type": 0,
  "pic": "http://xxx.com/banner.jpg",
  "url": "http://xxx.com/promotion",
  "status": 1,
  "sort": 0,
  "startTime": "2025-06-01T00:00:00",
  "endTime": "2025-06-30T23:59:59"
}
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 1 }
```

---

#### 分页查询广告

**基本信息**

请求路径：`/home/advertise/list`

请求方式：GET

接口描述：该接口用于分页查询广告列表

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| name | string | 非必须 | 广告名称 |
| type | number | 非必须 | 广告类型 |
| endTime | string | 非必须 | 结束时间 |
| pageSize | number | 非必须 | 每页数量，默认5 |
| pageNum | number | 非必须 | 页码，默认1 |

请求参数样例：

```
/home/advertise/list?type=0&pageNum=1&pageSize=10
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 分页数据（CommonPage格式） |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "totalPage": 1,
    "total": 1,
    "list": [
      { "id": 1, "name": "618大促", "type": 0, "pic": "http://xxx.com/banner.jpg", "status": 1 }
    ]
  }
}
```

---

## Portal 前台模块

---

### 会员管理

---

#### 会员注册

**基本信息**

请求路径：`/sso/register`

请求方式：POST

接口描述：该接口用于前台会员注册

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| username | string | 必须 | 用户名 |
| password | string | 必须 | 密码 |
| telephone | string | 必须 | 手机号 |
| authCode | string | 必须 | 验证码 |

请求参数样例：

```
/sso/register?username=test&password=123456&telephone=13800138000&authCode=123456
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | null |

响应数据样例：

```json
{ "code": 200, "message": "注册成功", "data": null }
```

---

#### 会员登录

**基本信息**

请求路径：`/sso/login`

请求方式：POST

接口描述：该接口用于前台会员登录，返回JWT Token

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| username | string | 必须 | 用户名 |
| password | string | 必须 | 密码 |

请求参数样例：

```
/sso/login?username=test&password=123456
```

**响应数据**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data.token | string | 非必须 | JWT Token |
| data.tokenHead | string | 非必须 | Token前缀 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenHead": "Bearer "
  }
}
```

---

#### 获取验证码

**基本信息**

请求路径：`/sso/getAuthCode`

请求方式：GET

接口描述：该接口用于获取手机验证码

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| telephone | string | 必须 | 手机号 |

请求参数样例：

```
/sso/getAuthCode?telephone=13800138000
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | string | 非必须 | 验证码 |

响应数据样例：

```json
{ "code": 200, "message": "获取验证码成功", "data": "123456" }
```

---

#### 获取当前会员信息

**基本信息**

请求路径：`/sso/info`

请求方式：GET

接口描述：该接口用于获取当前登录会员的信息

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| Authorization | string | 必须 | 请求头携带，格式：Bearer {token} |

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 会员信息 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "test",
    "nickname": "测试用户",
    "phone": "13800138000",
    "integration": 100,
    "growth": 50
  }
}
```

---

### 首页内容

---

#### 获取首页内容

**基本信息**

请求路径：`/home/content`

请求方式：GET

接口描述：该接口用于获取首页所有展示模块的数据（广告+品牌+秒杀+新品+人气+专题）

**请求参数**

无

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data.advertiseList | object[] | 非必须 | 轮播广告列表 |
| data.brandList | object[] | 非必须 | 推荐品牌列表 |
| data.homeFlashPromotion | object | 非必须 | 当前秒杀场次信息 |
| data.newProductList | object[] | 非必须 | 新品推荐列表 |
| data.hotProductList | object[] | 非必须 | 人气推荐列表 |
| data.subjectList | object[] | 非必须 | 推荐专题列表 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "advertiseList": [],
    "brandList": [],
    "homeFlashPromotion": null,
    "newProductList": [],
    "hotProductList": [],
    "subjectList": []
  }
}
```

---

#### 获取秒杀活动信息

**基本信息**

请求路径：`/home/flashPromotion`

请求方式：GET

接口描述：该接口用于获取当前秒杀场次信息

**请求参数**

无

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 秒杀场次信息 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "startTime": "2025-06-18T10:00:00",
    "endTime": "2025-06-18T12:00:00",
    "products": []
  }
}
```

---

### 购物车管理

---

#### 添加商品到购物车

**基本信息**

请求路径：`/cart/add`

请求方式：POST

接口描述：该接口用于添加商品到购物车，已存在则增加数量

**请求参数**

格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| productId | number | 必须 | 商品ID |
| productSkuId | number | 必须 | 商品SKU ID |
| quantity | number | 必须 | 数量 |

请求参数样例：

```json
{ "productId": 1, "productSkuId": 1, "quantity": 2 }
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 1 }
```

---

#### 获取当前会员的购物车列表

**基本信息**

请求路径：`/cart/list`

请求方式：GET

接口描述：该接口用于获取当前登录会员的购物车列表

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| Authorization | string | 必须 | 请求头携带，格式：Bearer {token} |

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object[] | 非必须 | 购物车列表 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "id": 1, "productId": 1, "productName": "测试商品", "quantity": 2, "price": 99.00 }
  ]
}
```

---

#### 获取包含促销信息的购物车列表

**基本信息**

请求路径：`/cart/list/promotion`

请求方式：GET

接口描述：该接口用于获取当前会员的购物车列表，包含促销优惠信息

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| cartIds | number[] | 非必须 | 购物车项ID列表（为空则获取全部） |

请求参数样例：

```
/cart/list/promotion?cartIds=1&cartIds=2
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object[] | 非必须 | 含促销信息的购物车列表 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "id": 1, "productName": "测试商品", "quantity": 2, "price": 99.00, "promotionMessage": "满减优惠", "reduceAmount": 10.00 }
  ]
}
```

---

#### 修改购物车商品数量

**基本信息**

请求路径：`/cart/update/quantity`

请求方式：GET

接口描述：该接口用于修改购物车中指定商品的数量

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| id | number | 必须 | 购物车项ID |
| quantity | number | 必须 | 新数量 |

请求参数样例：

```
/cart/update/quantity?id=1&quantity=3
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 1 }
```

---

#### 删除购物车中的指定商品

**基本信息**

请求路径：`/cart/delete`

请求方式：POST

接口描述：该接口用于删除购物车中的指定商品

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| ids | number[] | 必须 | 购物车项ID列表 |

请求参数样例：

```
/cart/delete?ids=1&ids=2
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 2 }
```

---

#### 清空购物车

**基本信息**

请求路径：`/cart/clear`

请求方式：POST

接口描述：该接口用于清空当前会员的购物车

**请求参数**

无

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 受影响的行数 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 5 }
```

---

### 订单管理（前台）

---

#### 生成确认订单信息

**基本信息**

请求路径：`/order/generateConfirmOrder`

请求方式：POST

接口描述：该接口用于根据购物车信息生成确认订单页面数据

**请求参数**

格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| cartIds | number[] | 必须 | 购物车项ID列表 |

请求参数样例：

```json
[1, 2, 3]
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 确认订单信息 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "cartPromotionItemList": [],
    "memberReceiveAddressList": [],
    "couponHistoryDetailList": [],
    "memberIntegration": 100,
    "integrationConsumeSetting": null,
    "calcAmount": { "totalAmount": 299.00, "freightAmount": 0, "promotionAmount": 20.00, "payAmount": 279.00 }
  }
}
```

---

#### 生成订单

**基本信息**

请求路径：`/order/generateOrder`

请求方式：POST

接口描述：该接口用于根据提交信息创建订单

**请求参数**

格式：application/json

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| memberReceiveAddressId | number | 必须 | 收货地址ID |
| couponId | number | 非必须 | 优惠券ID |
| useIntegration | number | 非必须 | 使用积分数 |
| payType | number | 必须 | 支付方式：1=支付宝，2=微信 |
| cartIds | number[] | 必须 | 购物车项ID列表 |

请求参数样例：

```json
{
  "memberReceiveAddressId": 1,
  "couponId": null,
  "useIntegration": 0,
  "payType": 1,
  "cartIds": [1, 2]
}
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data.order | object | 非必须 | 订单信息 |
| data.orderItemList | object[] | 非必须 | 订单商品列表 |

响应数据样例：

```json
{
  "code": 200,
  "message": "下单成功",
  "data": {
    "order": { "id": 1, "orderSn": "2025060100001", "payAmount": 279.00 },
    "orderItemList": []
  }
}
```

---

#### 按状态分页获取用户订单列表

**基本信息**

请求路径：`/order/list`

请求方式：GET

接口描述：该接口用于按状态分页获取当前用户的订单列表

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| status | number | 必须 | 订单状态：-1=全部，0=待付款，1=待发货，2=已发货，3=已完成，4=已关闭 |
| pageNum | number | 非必须 | 页码，默认1 |
| pageSize | number | 非必须 | 每页数量，默认5 |

请求参数样例：

```
/order/list?status=-1&pageNum=1&pageSize=10
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 分页数据（CommonPage格式） |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "totalPage": 2,
    "total": 15,
    "list": [
      { "id": 1, "orderSn": "2025060100001", "status": 1, "payAmount": 279.00 }
    ]
  }
}
```

---

#### 获取订单详情

**基本信息**

请求路径：`/order/detail/{orderId}`

请求方式：GET

接口描述：该接口用于根据订单ID获取订单详情

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| orderId | number | 必须 | 路径参数，订单ID |

请求参数样例：

```
/order/detail/1
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 订单详情 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "orderSn": "2025060100001",
    "status": 1,
    "totalAmount": 299.00,
    "payAmount": 279.00,
    "orderItemList": []
  }
}
```

---

#### 用户取消订单

**基本信息**

请求路径：`/order/cancelUserOrder`

请求方式：POST

接口描述：该接口用于用户取消自己的订单

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| orderId | number | 必须 | 订单ID |

请求参数样例：

```
/order/cancelUserOrder?orderId=1
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | null |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": null }
```

---

#### 用户确认收货

**基本信息**

请求路径：`/order/confirmReceiveOrder`

请求方式：POST

接口描述：该接口用于用户确认收货

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| orderId | number | 必须 | 订单ID |

请求参数样例：

```
/order/confirmReceiveOrder?orderId=1
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | null |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": null }
```

---

### 支付管理

---

#### 电脑网站支付

**基本信息**

请求路径：`/pay/pay`

请求方式：GET

接口描述：该接口用于生成电脑网站支付页面，直接返回HTML

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| outTradeNo | string | 必须 | 商户订单号 |
| totalAmount | number | 必须 | 支付金额 |
| subject | string | 必须 | 订单标题 |
| payType | number | 非必须 | 支付方式，默认1=支付宝 |

请求参数样例：

```
/pay/pay?outTradeNo=2025060100001&totalAmount=279.00&subject=测试订单
```

**响应数据**

直接返回HTML页面（支付宝收银台页面），非JSON格式。

---

#### 支付异步回调

**基本信息**

请求路径：`/pay/notify`

请求方式：POST

接口描述：该接口用于接收支付平台的异步回调通知

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| payType | number | 必须 | 支付方式 |
| params | map | 必须 | 支付平台回调参数 |

**响应数据**

返回字符串：`"success"` 表示处理成功，`"failure"` 表示失败。

---

### 商品搜索

---

#### 导入所有商品到ES

**基本信息**

请求路径：`/esProduct/importAll`

请求方式：POST

接口描述：该接口用于将数据库中所有商品导入到Elasticsearch

**请求参数**

无

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | number | 非必须 | 导入的商品数量 |

响应数据样例：

```json
{ "code": 200, "message": "操作成功", "data": 50 }
```

---

#### 简单搜索

**基本信息**

请求路径：`/esProduct/search/simple`

请求方式：GET

接口描述：该接口用于根据关键字通过名称或副标题简单搜索商品

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| keyword | string | 非必须 | 搜索关键字 |
| pageNum | number | 非必须 | 页码，默认0 |
| pageSize | number | 非必须 | 每页数量，默认5 |

请求参数样例：

```
/esProduct/search/simple?keyword=手机&pageNum=0&pageSize=10
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 分页数据（CommonPage格式） |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 0,
    "pageSize": 10,
    "totalPage": 5,
    "total": 50,
    "list": [
      { "id": 1, "name": "华为手机", "price": 3999.00, "sale": 100 }
    ]
  }
}
```

---

#### 综合搜索、筛选、排序

**基本信息**

请求路径：`/esProduct/search`

请求方式：GET

接口描述：该接口用于根据关键字、品牌、分类、排序综合搜索商品

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| keyword | string | 非必须 | 搜索关键字 |
| brandId | number | 非必须 | 品牌ID |
| productCategoryId | number | 非必须 | 分类ID |
| pageNum | number | 非必须 | 页码，默认0 |
| pageSize | number | 非必须 | 每页数量，默认5 |
| sort | number | 非必须 | 排序：0=相关度，1=新品，2=销量，3=价格升序，4=价格降序 |

请求参数样例：

```
/esProduct/search?keyword=手机&brandId=1&sort=4&pageNum=0&pageSize=10
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data | object | 非必须 | 分页数据（CommonPage格式） |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 0,
    "pageSize": 10,
    "totalPage": 5,
    "total": 50,
    "list": [
      { "id": 1, "name": "华为手机", "brandName": "华为", "price": 3999.00, "sale": 100 }
    ]
  }
}
```

---

#### 获取搜索的相关品牌、分类及筛选属性

**基本信息**

请求路径：`/esProduct/search/relate`

请求方式：GET

接口描述：该接口用于获取搜索关键字相关的品牌、分类和属性信息，用于搜索结果页的筛选条件展示

**请求参数**

参数说明：

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| keyword | string | 非必须 | 搜索关键字 |

请求参数样例：

```
/esProduct/search/relate?keyword=手机
```

**响应数据**

| 参数名 | 类型 | 是否必须 | 备注 |
|--------|------|----------|------|
| code | number | 必须 | 响应码 |
| message | string | 非必须 | 提示信息 |
| data.brandList | object[] | 非必须 | 相关品牌列表 |
| data.productCategoryList | object[] | 非必须 | 相关分类列表 |
| data.productAttributes | object[] | 非必须 | 相关属性列表 |

响应数据样例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "brandList": [
      { "id": 1, "name": "华为", "logo": "http://xxx.com/huawei.jpg" }
    ],
    "productCategoryList": [
      { "id": 1, "name": "手机" }
    ],
    "productAttributes": []
  }
    
}
```