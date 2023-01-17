## pig 整合前后端分离项目登录

![](https://minio.pigx.vip/alei/2023/01/5331045f8c867c6ae6c4e62aa6434850.png)

```sql
INSERT INTO `pig`.`sys_oauth_client_details`(`client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`, `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`, `additional_information`, `autoapprove`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('jeesite', '', 'jeesite', 'server', 'authorization_code', 'http://127.0.0.1:3100/sso', '', 168000000, 168000000, '', 'false', '2023-01-17 08:31:46', '2023-01-17 09:32:06', 'admin', 'admin');
```

## 后端配置

> 配置  jeesite 前端地址 默认 3100 端口  
> 配置  PIG 网关地址

- 增加sso的配置
  ![](https://minio.pigx.vip/alei/2023/01/eb241f5b203e21d5cb762f8886359f21.png)

  

## 前端配置

- 登录页面配置
![](https://minio.pigx.vip/alei/2023/01/17ad9445a17f4acdb6fe47553c02e7bd.png)

- 增加sso 页面，回调使用，用于code 处理
  ![](https://minio.pigx.vip/alei/2023/01/bfe2b67054ec830b02ce60e9ffbc03e2.png)

- 退出系统
  ![](https://minio.pigx.vip/alei/2023/01/371c233f36a77003fdc48d5f3736f197.png)