<template>
  <transition
    :name="name"
    :mode="mode"
    :appear="appear"
    @before-enter="beforeEnter"
    @enter="enter"
    @after-enter="afterEnter"
    @before-leave="beforeLeave"
    @leave="leave"
    @after-leave="afterLeave"
  >
    <slot></slot>
  </transition>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps({
  // 过渡效果名称
  name: {
    type: String,
    default: 'fade'
  },
  // 过渡模式
  mode: {
    type: String,
    default: 'out-in'
  },
  // 是否在初始渲染时应用过渡效果
  appear: {
    type: Boolean,
    default: false
  },
  // 过渡持续时间（毫秒）
  duration: {
    type: Number,
    default: 300
  },
  // 过渡延迟时间（毫秒）
  delay: {
    type: Number,
    default: 0
  },
  // 过渡时间函数
  timingFunction: {
    type: String,
    default: 'ease'
  }
})

const emit = defineEmits([
  'before-enter',
  'enter',
  'after-enter',
  'before-leave',
  'leave',
  'after-leave'
])

// 过渡钩子函数
const beforeEnter = (el: Element) => {
  emit('before-enter', el)
}

const enter = (el: Element, done: () => void) => {
  const duration = props.duration
  const delay = props.delay
  const easing = props.timingFunction
  
  el.style.transition = `all ${duration}ms ${easing} ${delay}ms`
  
  emit('enter', el, done)
  
  // 如果没有监听enter事件，自动调用done
  if (!emit('enter', el, done)) {
    setTimeout(done, duration + delay)
  }
}

const afterEnter = (el: Element) => {
  el.style.transition = ''
  emit('after-enter', el)
}

const beforeLeave = (el: Element) => {
  emit('before-leave', el)
}

const leave = (el: Element, done: () => void) => {
  const duration = props.duration
  const delay = props.delay
  const easing = props.timingFunction
  
  el.style.transition = `all ${duration}ms ${easing} ${delay}ms`
  
  emit('leave', el, done)
  
  // 如果没有监听leave事件，自动调用done
  if (!emit('leave', el, done)) {
    setTimeout(done, duration + delay)
  }
}

const afterLeave = (el: Element) => {
  el.style.transition = ''
  emit('after-leave', el)
}
</script>

<style>
/* 淡入淡出效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--transition-duration, 0.3s) ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 滑动效果 */
.slide-enter-active,
.slide-leave-active {
  transition: transform var(--transition-duration, 0.3s) ease, opacity var(--transition-duration, 0.3s) ease;
}

.slide-enter-from {
  transform: translateX(20px);
  opacity: 0;
}

.slide-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}

/* 缩放效果 */
.scale-enter-active,
.scale-leave-active {
  transition: transform var(--transition-duration, 0.3s) ease, opacity var(--transition-duration, 0.3s) ease;
}

.scale-enter-from,
.scale-leave-to {
  transform: scale(0.9);
  opacity: 0;
}

/* 从上滑入效果 */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: transform var(--transition-duration, 0.3s) ease, opacity var(--transition-duration, 0.3s) ease;
}

.slide-down-enter-from {
  transform: translateY(-20px);
  opacity: 0;
}

.slide-down-leave-to {
  transform: translateY(20px);
  opacity: 0;
}

/* 从下滑入效果 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform var(--transition-duration, 0.3s) ease, opacity var(--transition-duration, 0.3s) ease;
}

.slide-up-enter-from {
  transform: translateY(20px);
  opacity: 0;
}

.slide-up-leave-to {
  transform: translateY(-20px);
  opacity: 0;
}

/* 旋转效果 */
.rotate-enter-active,
.rotate-leave-active {
  transition: transform var(--transition-duration, 0.3s) ease, opacity var(--transition-duration, 0.3s) ease;
}

.rotate-enter-from,
.rotate-leave-to {
  transform: rotate(90deg);
  opacity: 0;
}

/* 翻转效果 */
.flip-enter-active,
.flip-leave-active {
  transition: transform var(--transition-duration, 0.3s) ease, opacity var(--transition-duration, 0.3s) ease;
  transform-style: preserve-3d;
}

.flip-enter-from {
  transform: rotateY(90deg);
  opacity: 0;
}

.flip-leave-to {
  transform: rotateY(-90deg);
  opacity: 0;
}
</style>