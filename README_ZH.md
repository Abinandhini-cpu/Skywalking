Sky Walking | [Engligh](README.md)
==========

<img src="https://sky-walking.github.io/page-resources/3.0/skywalking.png" alt="Sky Walking logo" height="90px" align="right" />

**SkyWalking 3**: 针对分布式系统的APM系统，也被称为分布式追踪系统

[![Build Status](https://travis-ci.org/wu-sheng/sky-walking.svg?branch=master)](https://travis-ci.org/wu-sheng/sky-walking)
[![Coverage Status](https://coveralls.io/repos/github/wu-sheng/sky-walking/badge.svg?branch=master&forceUpdate=2)](https://coveralls.io/github/wu-sheng/sky-walking?branch=master)
![license](https://img.shields.io/aur/license/yaourt.svg)
[![codebeat badge](https://codebeat.co/badges/579e4dce-1dc7-4f32-a163-c164eafa1335)](https://codebeat.co/projects/github-com-wu-sheng-sky-walking)
[![Join the chat at https://gitter.im/sky-walking/Lobby](https://badges.gitter.im/sky-walking/Lobby.svg)](https://gitter.im/sky-walking/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![OpenTracing-1.x Badge](https://img.shields.io/badge/OpenTracing--1.x-enabled-blue.svg)](http://opentracing.io)


* 自动java探针，**不需要修改应用程序源代码**
  * 高性能探针，针对单实例5000tps的应用，在**不需要采样的情况下**，只增加**10%**的CPU开销。
  * [中间件，框架与类库支持列表](https://github.com/wu-sheng/sky-walking/wiki/3.2-supported-list).
* 手动探针
  * [使用OpenTracing手动探针API](http://opentracing.io/documentation/pages/supported-tracers)
  * 使用 **@Trace** 标注追踪业务方法
  * 将 traceId 集成到 log4j, log4j2 或 logback这些日志组件中
* 纯Java后端Collector实现，提供RESTful和gRPC接口。兼容接受其他语言探针发送数据 
  * [如何将探针的Metric和Trace数据上传到Collector？]()
* UI工程请查看 [wu-sheng/sky-walking-ui](https://github.com/wu-sheng/sky-walking-ui)
* 中文QQ群：392443393


# Contributors
_按首次加入时间排序_
* 吴晟 [**PMC Member**] [@wu-sheng](https://github.com/wu-sheng)  Principle Engineer, 2012 Lab, Huawei. 
* 张鑫 [**PMC Member**] [@ascrutae](https://github.com/ascrutae)   
* 谭真 [@mircoteam](https://github.com/mircoteam)  Advanced R&D Engineers, Creative & Interactive Group.
* 徐妍 [@TastySummer](https://github.com/TastySummer)
* 彭勇升 [**PMC Member**] [@pengys5](https://github.com/pengys5) 
* 戴文
* 柏杨 [@bai-yang](https://github.com/bai-yang)  Senior Engineer, Alibaba Group.
* 陈凤 [@trey03](https://github.com/trey03)
* [其他贡献者](https://github.com/wu-sheng/sky-walking/graphs/contributors)

This project adheres to the Contributor Covenant [code of conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to wu.sheng@foxmail.com.


# Architecture
* 3.2+版本架构图
<img src="https://sky-walking.github.io/page-resources/3.2/architecture/3.2-architecture.jpg"/>

# Screenshots
- 追踪基于 dubbox 和 [motan](https://github.com/weibocom/motan)的分布式系统，生成的拓扑截图
<img src="https://sky-walking.github.io/page-resources/3.2/topological_graph_test_project.png?forceUpdate=0"/>

- 调用链查询
<img src="https://sky-walking.github.io/page-resources/3.2/trace_segment.png"/>

- Span信息查询
<img src="https://sky-walking.github.io/page-resources/3.0/span.png" />

- 实例全局视图
<img src="https://sky-walking.github.io/page-resources/3.2/instance_health.png"/>

- JVM明细信息
<img src="https://sky-walking.github.io/page-resources/3.2/instance_graph.png"/>

- 服务依赖树.
<img src="https://sky-walking.github.io/page-resources/3.2/service_dependency_tree.png"/>


# Test reports
- 自动化集成测试报告
  - [Java探针测试报告](https://github.com/sky-walking/agent-integration-test-report)
- 性能测试报告
  - [Java探针测试报告](https://sky-walking.github.io/Agent-Benchmarks/)

# Document
* [WIKI](https://github.com/wu-sheng/sky-walking/wiki)

