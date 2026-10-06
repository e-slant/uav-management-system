<template>
	<transition name="fade">
		<div v-if="visible" class="custom-toast-container">
			<div class="custom-toast-content">
				{{ message }}
			</div>
		</div>
	</transition>
</template>

<script setup>
	import {
		ref
	} from 'vue'

	const visible = ref(false)
	const message = ref('')
	let timer = null

	const show = (msg, duration = 2000) => {
		message.value = msg
		visible.value = true

		// 清除之前的定时器
		if (timer) clearTimeout(timer)

		// 设置自动消失
		timer = setTimeout(() => {
			visible.value = false
		}, duration)
	}

	// 暴露方法给外部调用
	defineExpose({
		show
	})
</script>

<style scoped>
	.custom-toast-container {
		position: fixed;
		top: 10%;
		left: 50%;
		transform: translate(-50%, -50%);
		z-index: 99999;
		/* 设置一个极高的 z-index 确保在最顶层 */
		pointer-events: none;
		/* 防止遮罩层阻挡点击事件 */
	}

	.custom-toast-content {
		background: rgba(0, 0, 0, 0.8);
		color: #ffffff;
		padding: 12px 20px;
		border-radius: 8px;
		font-size: 14px;
		max-width: 80vw;
		text-align: center;
		box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
	}

	/* 动画效果 */
	.fade-enter-active,
	.fade-leave-active {
		transition: opacity 0.3s;
	}

	.fade-enter-from,
	.fade-leave-to {
		opacity: 0;
	}
</style>