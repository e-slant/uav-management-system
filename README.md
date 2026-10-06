无人机实时监控管理系统

基于 SpringBoot + Vue3 的无人机实时监控与航迹管理系统，支持 TCP/UDP 双协议接入、无人机状态实时展示与目标点下发。

项目结构

.
├── backend/     # 后端：SpringBoot + JDBC + MySQL + Socket(TCP/UDP)
├── frontend/    # 前端：Vue3 + 高德地图 + ECharts + axios
└── README.md


核心功能





无人机实时定位与地图打点展示



飞行参数实时刷新（经纬度、高度、三轴姿态角、速度、延迟等）



地图点选下发目标点



TCP / UDP 双协议运行时热切换



飞行轨迹曲线绘制与历史数据存储

技术栈

后端：Java / SpringBoot / JDBC / MySQL / ServerSocket / 多线程 / 命令模式 前端：Vue3 (Composition API) / 高德地图 JS API / ECharts / axios

运行说明

后端





创建 MySQL 数据库，导入建表 SQL



修改 backend/src/main/resources/ 下的数据库配置为你的本地配置



用 IDEA 打开 backend 目录，运行启动类

前端





进入 frontend 目录



安装依赖：npm install



启动开发服务：npm run serve（或按 HBuilderX 的运行方式）



浏览器访问提示的本地地址



注意：前端需要配置高德地图 Key 才能正常显示地图，请在入口文件中填入自己的 Key。