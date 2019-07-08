Spring Boot 与 Kubernetes 微服务实践 ～ 全面掌握云原生应用的架构设计与实现
===
## 课程收益

通过手把手架构、设计和开发一个准生产级的微服务SAAS应用，并最终部署到Kubernetes容器云环境，帮助学员：

* 掌握如何在实践中设计微服务架构和前后分离架构
* 掌握如何基于Spring(Boot)搭建微服务基础框架，包括
  * 如何设计轻量级服务开发框架
  * 如何设计轻量级安全框架和实现SSO
  * 如何设计轻量级可编程网关
* 掌握微服务测试技术和相关实践
* 掌握可运维架构理念和相关实践
* 掌握服务容器化和容器云部署相关实践，包括
  * 如何将服务部署到本地Docker Compose环境
  * 云原生(CloudNative)架构理念和Kubernetes核心概念
  * 如何将服务部署到Kubernetes(本地+阿里云)容器云环境
* 理解云时代的软件工程流程和相关实践：
  * 需求->架构设计->框架搭建->服务实现->测试->可运维架构->容器云部署
  * 建立DevOps研发运维一体化视角
* 理解如何架构和设计一个SaaS多租户应用
* 进一步提升Java/Spring开发技能

课程项目源码开放在github上，可以作为学习现代微服务、云原生和SAAS应用架构的一个参考，也可作为类似项目的脚手架。

## 大纲

### 一、课程介绍和案例需求
1. 课程背景介绍
2. 课程目标和主要内容
3. 课程案例需求
4. 课程补充说明

### 二、系统架构设计和技术栈选型
1. 为何采⽤微服务架构?
2. 架构设计和技术栈选型
3. 数据和接口模型设计～账户服务
4. 数据和接口模型设计～业务服务
5. Dubbo、SpringCloud和K8s该如何选择(上)
6. Dubbo、SpringCloud和K8s该如何选择(中)
7. Dubbo、SpringCloud和K8s该如何选择(下)
8. 技术中台到底讲什么？

### 三、服务开发框架设计和实践

1. Staffjoy项目结构组织
2. 谷歌为何采用单体仓库(Mono-Repo)
3. 微服务接口参数校验为何重要？
4. 如何实现统一异常处理？
5. DTO和DMO为什么要互转？
6. 如何实现基于Feign的强类型接口？
7. 为什么框架层就要考虑分环境配置？
8. 异步处理为何要复制线程上下文信息？
9. 为你的接口添加Swagger文档
10. 主流微服务框架概览

### 四、可编程网关设计和实践
1. 网关和BFF是如何演化出来的(上)
2. 网关和BFF是如何演化出来的(下)
3. 网关和反向代理是什么关系？
4. 网关需要分集群部署吗？
5. 如何设计一个最简网关？
6. Faraday网关代码解析(上)
7. Faraday网关代码解析(下)
8. 生产级网关需要考虑哪些环节？
9. 主流开源网关概览

### 五、安全框架设计和实践

1. 安全认证架构演进～单块阶段(上)
2. 安全认证架构演进～单块阶段(下)
3. 安全认证架构演进～微服务阶段
4. 基于JWT令牌的安全认证架构
5. JWT的原理是什么？
6. JWT有哪两种主要流程？
7. Staffjoy安全认证架构和SSO
8. 用户认证代码剖析
9. 服务调用鉴权代码剖析
10. 如何设计用户角色鉴权？

### 六、服务测试设计和实践

1. SpringBoot微服务测试该如何分类？
2. 什么是契约驱动测试？
3. 什么是测试金字塔？
4. 单元测试案例分析
5. 集成测试案例分析
6. 组件测试案例分析
7. Mock vs Spy

### 七、可运维架构设计和实践

1. 何谓生产就绪(Production Ready)?
2. SpringBoot如何实现分环境配置？
3. Apollo vs Spring Cloud Config vs K8s ConfigMap
4. 如何配置本地开发测试用机密数据？
5. CAT vs Zipkin Vs Skywalking
6. 结构化日志和审计日志
7. 集中异常监控和Sentry
8. ELK & Prometheus & Skywalking + K8s部署架构

### 八、服务容器化和Docker Compose部署

1. 统一网关部署架构回顾
2. 手工服务部署和测试
3. Skywalking调用链监控实验
4. Docker和Docker Compose简介
5. 容器化和镜像构建～Account服务案例
6. 容器化和镜像构建～MyAccount SPA应用案例
7. Docker Compose服务部署文件剖析
8. 将Staffjoy部署到本地Docker Compose环境

### 九、云原生架构和Kubernetes容器云部署
1. 到底什么是云原生架构？
2. K8s背景和架构
3. K8s有哪些基本概念？
4. 深入理解Service和Service Discovery
5. NodePort vs LoadBalancer vs Ingress
6. 深入理解K8s网络
7. 本地测试K8s环境搭建
8. 本地测试K8s部署文件剖析
9. 将Staffjoy部署到本地K8s环境
10. K8s应用动态配置实验
11. K8s应用动态扩容实验
12. 生产环境K8s部署文件剖析
13. 阿里云K8s环境创建
14. 将Staffjoy部署到阿里云生产环境

### 十、项目复盘、应用和扩展环节

### 十一、附录：Staffjoy微服务实现简析

* 1 Account服务
* 2 Company服务
* 3 Mail、SMS和Bot服务
* 4 Faraday网关服务
* 5 WhoAmI会话服务
* 6 Landing Page Web应用
* 7 MyAccount/MyCompany SPA应用
