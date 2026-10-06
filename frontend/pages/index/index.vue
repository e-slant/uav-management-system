<template>
	<MyToast ref="toast" />
	<div class="dashboard">
		<!-- 左侧菜单 -->
		<div class="sidebar" @click.stop>
			<!-- 无人机列表 -->
			<div class="section">
				<h3>无人机列表</h3>

				<div v-for="drone in drones" :key="drone.number" class="drone-item"
					:class="{ active: selectedDrone?.number === drone.number }" @click="selectDrone(drone)">
					<span>{{ drone.number }}</span>
				</div>
				<button class="btn-simulate" @click="openSimulateModal">
					+ 模拟无人机
				</button>
				<button class="btn-clear" @click="clearSelectedDrone" v-if="selectedDrone">
					🗑 清除选中无人机
				</button>
			</div>

			<!-- 选中无人机信息 -->
			<div v-if="selectedDrone" class="section info-panel">
				<h3>{{ selectedDrone.number }}</h3>
				<div class="info-grid">
					<div>经度: {{ selectedDrone.longitude?.toFixed(6) }}</div>
					<div>纬度: {{ selectedDrone.latitude?.toFixed(6) }}</div>
					<div>高度: {{ selectedDrone.altitude?.toFixed(1) }} m</div>
					<div>翻转角: {{ selectedDrone.roll?.toFixed(1) }}°</div>
					<div>俯仰角: {{ selectedDrone.pitch?.toFixed(1) }}°</div>
					<div>航向角: {{ selectedDrone.heading?.toFixed(1) }}°</div> <!-- ✅ 新增 -->
					<div>延迟: {{ selectedDrone.delay }} ms</div>
				</div>
			</div>

			<!-- 路径图 -->
			<div v-if="selectedDrone" class="section">
				<h3>飞行路径</h3>
				<div ref="chartRef" class="chart"></div>
			</div>

			<!-- 服务器配置 -->
			<div class="section">
				<h3>服务器配置</h3>
				<div class="form-group">
					<label>主机地址</label>
					<input v-model="serverConfig.host" placeholder="127.0.0.1" />
				</div>
				<div class="form-group">
					<label>通信端口号</label>
					<input v-model="serverConfig.port" type="number" placeholder="8080" />
				</div>
				<div class="form-group">
					<label>数据库名</label>
					<input v-model="serverConfig.database" placeholder="drone_db" />
				</div>
				<div class="form-group">
					<label>表名1</label>
					<input v-model="serverConfig.table1" placeholder="drone_info" />
				</div>
				<div class="form-group">
					<label>表名2</label>
					<input v-model="serverConfig.table2" placeholder="flight_log" />
				</div>
				<div class="form-group">
					<label>用户名</label>
					<input v-model="serverConfig.username" placeholder="root" />
				</div>
				<div class="form-group">
					<label>密码</label>
					<input v-model="serverConfig.password" type="password" placeholder="******" />
				</div>

				<!-- 协议切换 -->
				<div class="protocol-switch">
					<button :class="{ active: protocol === 'TCP' }" @click="switchProtocol('TCP')">
						TCP
					</button>
					<button :class="{ active: protocol === 'UDP' }" @click="switchProtocol('UDP')">
						UDP
					</button>
				</div>

				<button class="btn-save" @click="saveConfig">完成</button>
			</div>
		</div>

		<!-- 地图区域 -->
		<div id="mapContainer" class="map-container"></div>

		<!-- 模拟无人机弹窗 -->
		<div v-if="showSimulateModal" class="modal" @click.self="showSimulateModal = false">
			<div class="modal-content">
				<h3>模拟无人机</h3>
				<div class="form-group">
					<label>无人机编号（最多5字符）</label>
					<input v-model="simulateName" placeholder="例如: D001" maxlength="5" />
				</div>
				<div class="form-group">
					<label>飞行速度 (m/s)</label>
					<input v-model.number="simulateSpeed" type="number" min="1" max="50" placeholder="例如: 10" />
				</div>
				<div class="modal-actions">
					<button @click="confirmSimulate">确认</button>
					<button @click="showSimulateModal = false">取消</button>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup>
	import {
		ref,
		reactive,
		onMounted,
		onUnmounted,
		watch,
		nextTick
	} from 'vue'
	import * as echarts from 'echarts'
	import axios from 'axios'
	import MyToast from '@/components/toast.vue'

	const drones = ref([{
		// id: 0,  ❌ 删除
		number: "",
		longitude: 0,
		latitude: 0,
		altitude: 0,
		roll: 0,
		pitch: 0,
		heading: 0,
		delay: 0,
		speed: 0,
		path: [{
			longitude: 0,
			latitude: 0
		}]
	}])
	const selectedDrone = ref(null)
	const protocol = ref('TCP')
	const showSimulateModal = ref(false)
	const simulateName = ref('')
	const simulateSpeed = ref(10) // ✅ 新增：速度，默认 10 m/s
	const isSimulating = ref(false)

	const serverConfig = reactive({
		host: '127.0.0.1',
		port: '8080',
		database: 'drone_db',
		table1: 'drone_info',
		table2: 'flight_log',
		username: 'root',
		password: ''
	})

	const chartRef = ref(null)
	let chartInstance = null
	let map = null
	let mapMarkers = {}
	let updateTimer = null
	let configTimer = null
	const host = "http://localhost:8080"
	const toast = ref(null)

	let resizeHandler = null
	onMounted(() => {
		initMap()
		drones.value = []
		startDataSimulation()
		resizeHandler = () => chartInstance?.resize()
		window.addEventListener('resize', resizeHandler)
		loadServerConfig()
		configTimer = setInterval(loadServerConfig, 1000)
	})
	onUnmounted(() => {
		if (updateTimer) clearInterval(updateTimer)
		if (configTimer) clearInterval(configTimer)
		if (resizeHandler) window.removeEventListener('resize', resizeHandler)
		if (chartInstance) chartInstance.dispose()
		if (map) map.destroy()
	})


	const initMap = () => {
		map = new AMap.Map('mapContainer', {
			zoom: 13,
			center: [114.502461, 38.045474],
			viewMode: '2D'
		})

		map.on('click', (e) => {
			const lnglat = e.lnglat
			const lng = lnglat.getLng()
			const lat = lnglat.getLat()

			if (e.target && e.target.getPosition) {
				return
			}

			if (isSimulating.value) {
				confirmSimulatePosition(lng, lat)
				return
			}

			if (selectedDrone.value) {
				sendMoveTarget(selectedDrone.value.number, lng, lat)
				return
			}

			if (!e.target.closest('.sidebar')) {
				selectedDrone.value = null
				if (chartInstance) {
					chartInstance.clear()
				}
			}
		})
	}
	// 服务器配置加载
	const loadServerConfig = () => {
		axios(host + "/loadPage").then((res) => {
			const result = res.data
			if (result.state == "ok") {
				Object.assign(serverConfig, {
					host: result.host,
					port: result.port,
					database: result.database,
					table1: result.table1,
					table2: result.table2,
					username: result.username,
					password: result.password,

				})
				protocol.value = result.protocol
				// ✅ 读取成功后停止轮询，配置只需读一次
				if (configTimer) {
					clearInterval(configTimer)
					configTimer = null
				}
				toast.value.show("服务器配置已读取")
			} else {
				// 后端还没初始化好，保持轮询，不打扰用户
				console.log("等待服务器初始化...")
			}
		}).catch(err => {
			console.error('读取服务器配置失败', err)
		})
	}

	const openSimulateModal = () => {
		simulateName.value = ''
		simulateSpeed.value = 10 // ✅ 重置速度
		showSimulateModal.value = true
	}

	// ✅✅✅ 修改：增加速度校验
	const confirmSimulate = () => {
		if (!simulateName.value.trim()) {
			toast.value.show('请输入无人机编号')
			return
		}
		if (simulateName.value.length > 5) {
			toast.value.show('编号不能超过5个字符')
			return
		}
		if (!simulateSpeed.value || simulateSpeed.value < 1 || simulateSpeed.value > 50) {
			toast.value.show('速度范围 1~50 m/s')
			return
		}
		simulateName.value = simulateName.value.padStart(5, '0')
		// TCP 模式：只能有 1 个无人机
		if (protocol.value === 'TCP' && drones.value.length >= 1) {
			toast.value.show('TCP 模式只能模拟 1 个无人机，请先清除现有无人机')
			return
		}

		// UDP 模式：最多 5 个无人机
		if (protocol.value === 'UDP' && drones.value.length >= 10) {
			toast.value.show('UDP 模式最多模拟 10 个无人机')
			return
		}

		// 检查编号是否重复
		if (mapMarkers[simulateName.value]) { // 之前是 Number(simulateName.value)
			toast.value.show('编号已存在，请使用其他编号')
			return
		}

		showSimulateModal.value = false
		isSimulating.value = true
	}

	const confirmSimulatePosition = (lng, lat) => {
		isSimulating.value = false
		// ✅✅✅ 修改：发送请求时携带速度参数
		axios.post(host + "/createDrone", {
			"lon": String(lng),
			"lat": String(lat),
			"number": simulateName.value,
			"speed": String(simulateSpeed.value / 10000) // ✅ 新增速度参数
		}).then((res) => {
			if (res.data != 'success') {
				toast.value.show("模拟无人机创建失败，请检查通信配置")
				return
			}
		})

		const newDrone = {
			// id: Number(simulateName.value),  ❌ 删除
			number: simulateName.value,
			longitude: lng,
			latitude: lat,
			altitude: 100,
			roll: 0,
			pitch: 0,
			heading: 0,
			delay: 0,
			speed: simulateSpeed.value,
			path: [{
				longitude: lng,
				latitude: lat
			}]
		}


		drones.value.push(newDrone)
		selectedDrone.value = newDrone
		addMapMarker(newDrone)
		sendSimulateDrone(newDrone)

		simulateName.value = ''
		simulateSpeed.value = 10 // ✅ 重置
	}

	// ✅✅✅ 新增：清除选中的无人机
	const clearSelectedDrone = () => {
		if (!selectedDrone.value) return
		axios.post(host + "/clearDrone", {
			number: selectedDrone.value.number
		}).then(
			(res) => {
				const droneNumber = selectedDrone.value.number // 之前是 droneId
				const index = drones.value.findIndex(d => d.number === droneNumber)
				if (index !== -1) {
					drones.value.splice(index, 1)
				}

				if (mapMarkers[droneNumber]) {
					map.remove(mapMarkers[droneNumber])
					delete mapMarkers[droneNumber]
				}

				selectedDrone.value = null
				if (chartInstance) chartInstance.clear()
				toast.value.show(`已清除无人机 ${droneNumber}`)
			}
		)
	}

	watch(selectedDrone, (newVal) => {
		if (newVal) {
			nextTick(() => {
				if (!chartRef.value) return
				if (!chartInstance) {
					chartInstance = echarts.init(chartRef.value)
				}
				chartInstance.clear() // ✅ 先清旧图
				chartInstance.resize() // ✅ 适配当前容器尺寸
				updateChart()
			})
		} else {
			if (chartInstance) chartInstance.clear()
		}
	}, {
		deep: false
	})


	const updateChart = () => {
		if (!chartInstance) return

		// 没有选中或没有路径数据
		if (!selectedDrone.value?.path?.length) {
			chartInstance.clear()
			chartInstance.setOption({
				title: {
					text: '暂无路径数据',
					left: 'center',
					top: 'center'
				}
			}, true)
			return
		}

		const pts = selectedDrone.value.path.map(p => [p.longitude, p.latitude])

		// 计算经纬度范围，避免所有点重合时坐标轴无法确定区间
		const lngs = pts.map(p => p[0])
		const lats = pts.map(p => p[1])
		let minLng = Math.min(...lngs),
			maxLng = Math.max(...lngs)
		let minLat = Math.min(...lats),
			maxLat = Math.max(...lats)
		// 重合时给一个小的展开量，保证坐标轴有范围
		if (maxLng - minLng < 1e-6) {
			minLng -= 0.001;
			maxLng += 0.001
		}
		if (maxLat - minLat < 1e-6) {
			minLat -= 0.001;
			maxLat += 0.001
		}

		chartInstance.setOption({
			title: {
				text: ''
			},
			tooltip: {
				trigger: 'item'
			},
			grid: {
				left: 50,
				right: 20,
				top: 20,
				bottom: 30
			},
			xAxis: {
				type: 'value',
				name: '经度',
				min: minLng,
				max: maxLng,
				scale: true
			},
			yAxis: {
				type: 'value',
				name: '纬度',
				min: minLat,
				max: maxLat,
				scale: true
			},
			series: [{
				type: 'line',
				data: pts,
				smooth: false,
				showSymbol: true,
				symbolSize: 5,
				lineStyle: {
					color: '#1890ff',
					width: 2
				},
				itemStyle: {
					color: '#1890ff'
				}
			}]
		}, true) // ✅ 关键：notMerge = true，强制刷新
	}


	const selectDrone = (drone) => {
		selectedDrone.value = drone
	}

	const addMapMarker = (drone) => {
		const marker = new AMap.Marker({
			position: [drone.longitude, drone.latitude],
			title: drone.number,
			icon: new AMap.Icon({
				size: new AMap.Size(32, 32),
				image: '/static/icon.png',
				imageSize: new AMap.Size(32, 32)
			}),
			offset: new AMap.Pixel(-16, -16),
			label: {
				content: `<div style="
	        background: rgba(0,0,0,0.8);
	        color: #fff;
	        padding: 4px 10px;
	        border-radius: 6px;
	        font-size: 13px;
	        font-weight: bold;
	        white-space: nowrap;
	        border: 1px solid #1890ff;
	        box-shadow: 0 2px 8px rgba(0,0,0,0.3);
	      ">${drone.number} (${drone.speed}m/s)</div>`, // ✅ 显示速度
				direction: 'center',
				offset: new AMap.Pixel(0, -40)
			}
		})

		marker.on('click', () => {
			selectDrone(drone)
		})

		map.add(marker)
		mapMarkers[drone.number] = marker
	}

	const clearAllDrones = () => {
		//接口
		drones.value = []
		selectedDrone.value = null
		Object.values(mapMarkers).forEach(marker => {
			map.remove(marker)
		})
		mapMarkers = {}
		if (chartInstance) {
			chartInstance.clear()
		}
		isSimulating.value = false
		console.log(`✅ 已清除所有无人机（${protocol.value} 模式）`)
	}

	const startDataSimulation = () => {
		updateTimer = setInterval(() => {
			axios.get(host + '/droneData').then((res) => {
				const temp = res.data || []
				if (temp.length === 0) return

				// 1️⃣ 用 number 做匹配，更新已有无人机；新无人机才 push
				temp.forEach(item => {
					const num = item.number
					let exist = drones.value.find(d => d.number === num)
					if (exist) {
						exist.longitude = item.longitude
						exist.latitude = item.latitude
						exist.altitude = item.height
						exist.roll = item.flip
						exist.pitch = item.pitch
						exist.heading = item.heading
						exist.delay = item.delay
						if (item.speed != null) exist.speed = item.speed
					} else {
						drones.value.push({
							// id: Number(num),  ❌ 删除
							number: num,
							longitude: item.longitude,
							latitude: item.latitude,
							altitude: item.height,
							roll: item.flip,
							pitch: item.pitch,
							heading: item.heading ?? 0,
							delay: item.delay,
							speed: item.speed ?? simulateSpeed.value,
							path: [{
								longitude: item.longitude,
								latitude: item.latitude
							}]
						})
					}
				})

				// 2️⃣ 数据更新完，再统一刷新路径和 marker
				drones.value.forEach(drone => {
					// ✅ 只在坐标变化超过阈值时才追加路径点，避免重复点堆成平线
					const last = drone.path[drone.path.length - 1]
					const moved = !last ||
						Math.abs(last.longitude - drone.longitude) > 1e-7 ||
						Math.abs(last.latitude - drone.latitude) > 1e-7
					if (moved) {
						drone.path.push({
							longitude: drone.longitude,
							latitude: drone.latitude
						})
						if (drone.path.length > 100) drone.path.shift()
					}

					if (mapMarkers[drone.number]) {
						mapMarkers[drone.number].setPosition([drone.longitude, drone.latitude])
					} else {
						addMapMarker(drone)
					}
				})

				// 3️⃣ 如果当前有选中的无人机，刷新右下角图表
				if (selectedDrone.value) {
					updateChart()
				}
			}).catch(err => {
				console.error('轮询 droneData 失败', err)
			})
		}, 500)
	}

	const sendSimulateDrone = (drone) => {
		console.log(`[${protocol.value}] 发送模拟无人机请求:`, drone)
	}

	const sendMoveTarget = (droneId, lng, lat) => {
		console.log(`[${protocol.value}] 发送移动目标:`, {
			droneId,
			lng,
			lat
		})
		axios.post(host + "/target", {
			"number": droneId,
			"x": String(lng),
			"y": String(lat)
		})
	}
	// 服务器保存
	const saveConfig = () => {
		axios.post(host + "/saveConfig",
			serverConfig
		).then(() => {
			clearAllDrones()
			console.log(`[${protocol.value}] 发送服务器配置:`, serverConfig)
			toast.value.show("配置已保存（控制台查看请求）")
		})
	}

	const switchProtocol = (p) => {
		axios.get(host + "/Agreement", {
			params: {
				agreement: p === 'TCP'
			}
		}).then((res) => {
			const state = res.data
			console.log(state)
			if (state == "ok") {
				protocol.value = p
				clearAllDrones()
				if (p === 'TCP') {
					toast.value.show('已切换到 TCP 模式，只能模拟 1 个无人机')
				} else {
					toast.value.show('已切换到 UDP 模式，最多可模拟 10 个无人机')
				}
			} else {
				toast.value.show("请等待服务器初始化完毕")
			}
		})
	}
</script>

<style scoped>
	* {
		margin: 0;
		padding: 0;
		box-sizing: border-box;
	}



	.dashboard {
		display: flex;
		height: 100vh;
		background: #0f0f23;
		color: #eee;
		font-family: 'Segoe UI', sans-serif;
	}

	/* ===== 左侧边栏 ===== */
	.sidebar {
		width: 320px;
		background: #1a1a2e;
		padding: 16px;
		overflow-y: auto;
		border-right: 1px solid #333;
	}

	.section {
		margin-bottom: 20px;
		padding-bottom: 16px;
		border-bottom: 1px solid #333;
	}

	.section h3 {
		font-size: 14px;
		color: #1890ff;
		margin-bottom: 10px;
	}

	/* 无人机列表 */
	.drone-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 10px;
		margin-bottom: 6px;
		background: #16213e;
		border-radius: 6px;
		cursor: pointer;
		transition: 0.2s;
	}

	.drone-item:hover {
		background: #0f3460;
	}

	.drone-item.active {
		background: #0f3460;
		border-left: 3px solid #1890ff;
	}

	.status {
		font-size: 12px;
		padding: 2px 8px;
		border-radius: 10px;
	}

	/* 	.status.online {
		background: #52c41a;
		color: #fff;
	} */

	.status.offline {
		background: #ff4d4f;
		color: #fff;
	}

	/* 信息面板 */
	.info-grid {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 8px;
		font-size: 13px;
	}

	.info-grid div {
		background: #16213e;
		padding: 6px 10px;
		border-radius: 4px;
	}

	/* 路径图 */
	.chart {
		height: 200px;
		width: 100%;
	}

	/* 表单 */
	.form-group {
		margin-bottom: 10px;
	}

	.form-group label {
		display: block;
		font-size: 12px;
		color: #aaa;
		margin-bottom: 4px;
	}

	.form-group input {
		width: 100%;
		padding: 6px 10px;
		background: #16213e;
		border: 1px solid #333;
		border-radius: 4px;
		color: #eee;
		font-size: 13px;
		height: 14%;
	}

	/* 协议切换 */
	.protocol-switch {
		display: flex;
		gap: 8px;
		margin: 12px 0;
	}

	.protocol-switch button {
		flex: 1;
		padding: 8px;
		background: #16213e;
		border: 1px solid #333;
		color: #aaa;
		border-radius: 4px;
		cursor: pointer;
	}

	.protocol-switch button.active {
		background: #1890ff;
		color: #fff;
		border-color: #1890ff;
	}

	.btn-simulate,
	.btn-save {
		width: 100%;
		padding: 10px;
		background: #1890ff;
		color: #fff;
		border: none;
		border-radius: 4px;
		cursor: pointer;
		font-size: 14px;
		margin-top: 8px;
	}

	.btn-clear {
		width: 100%;
		padding: 10px;
		background: #ff4d4f;
		color: #fff;
		border: none;
		border-radius: 4px;
		cursor: pointer;
		font-size: 14px;
		margin-top: 8px;
	}

	.btn-clear:hover {
		background: #ff7875;
	}

	.btn-simulate:hover,
	.btn-save:hover {
		background: #40a9ff;
	}

	/* ===== 地图容器 ===== */
	.map-container {
		flex: 1;
		position: relative;
		background: #0a0a1a;
	}


	/* ===== 弹窗 ===== */
	.modal {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.6);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 1000;
	}

	.modal-content {
		background: #1a1a2e;
		padding: 24px;
		border-radius: 8px;
		width: 360px;
	}

	.modal-content h3 {
		margin: 0 0 16px;
		color: #1890ff;
	}

	.modal-content input {
		width: 100%;
		padding: 8px 12px;
		background: #16213e;
		border: 1px solid #333;
		border-radius: 4px;
		color: #eee;
		margin-bottom: 16px;
		height: 14%;
	}

	.modal-actions {
		display: flex;
		gap: 12px;
	}

	.modal-actions button {
		flex: 1;
		padding: 8px;
		border: none;
		border-radius: 4px;
		cursor: pointer;
	}

	.modal-actions button:first-child {
		background: #1890ff;
		color: #fff;
	}

	.modal-actions button:last-child {
		background: #333;
		color: #eee;
	}
</style>