<template>
  <div class="tags-view-container">
    <el-scrollbar class="tags-scroll">
      <div class="tags-wrapper">
        <div
          v-for="tag in tags"
          :key="tag.path"
          class="tag-item"
          :class="{ active: isActive(tag.path) }"
          @click="goTo(tag)"
          @contextmenu.prevent="openMenu(tag, $event)"
        >
          <span class="tag-text">{{ tag.title }}</span>
          <el-icon
            v-if="!tag.affix"
            class="tag-close"
            :size="12"
            @click.stop="closeTag(tag)"
          >
            <Close />
          </el-icon>
        </div>
      </div>
    </el-scrollbar>
    <ul
      v-show="menuVisible"
      :style="{ left: menuLeft + 'px', top: menuTop + 'px' }"
      class="contextmenu"
    >
      <li @click="refreshSelected">
        <el-icon><Refresh /></el-icon>刷新页面
      </li>
      <li v-if="!selectedTag?.affix" @click="closeSelected">
        <el-icon><Close /></el-icon>关闭当前
      </li>
      <li @click="closeOthers">
        <el-icon><Minus /></el-icon>关闭其他
      </li>
      <li @click="closeAll">
        <el-icon><CircleClose /></el-icon>关闭所有
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Close, Refresh, Minus, CircleClose } from '@element-plus/icons-vue'
import { useTagsViewStore } from '@/stores/tagsView'

const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()

const menuVisible = ref(false)
const menuLeft = ref(0)
const menuTop = ref(0)
const selectedTag = ref<{ path: string; affix?: boolean } | null>(null)

const tags = computed(() => tagsViewStore.visitedViews)

const isActive = (path: string) => route.fullPath === path

const goTo = (tag: { path: string }) => {
  if (route.fullPath !== tag.path) {
    router.push(tag.path)
  }
}

const closeTag = (tag: { path: string; affix?: boolean }) => {
  if (tag.affix) return
  const isActiveTag = isActive(tag.path)
  const redirectPath = tagsViewStore.delView(tag.path)
  if (isActiveTag && redirectPath) {
    router.push(redirectPath)
  }
}

const openMenu = (tag: { path: string; affix?: boolean }, e: MouseEvent) => {
  selectedTag.value = tag
  menuLeft.value = e.clientX
  menuTop.value = e.clientY
  menuVisible.value = true
}

const closeMenu = () => {
  menuVisible.value = false
  selectedTag.value = null
}

const refreshSelected = () => {
  closeMenu()
  router.go(0)
}

const closeSelected = () => {
  closeMenu()
  if (selectedTag.value && !selectedTag.value.affix) {
    closeTag(selectedTag.value)
  }
}

const closeOthers = () => {
  closeMenu()
  if (selectedTag.value) {
    tagsViewStore.delOthersViews(selectedTag.value.path)
    if (!isActive(selectedTag.value.path)) {
      router.push(selectedTag.value.path)
    }
  }
}

const closeAll = () => {
  closeMenu()
  const path = tagsViewStore.delAllViews()
  router.push(path)
}

onMounted(() => {
  document.addEventListener('click', closeMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', closeMenu)
})
</script>

<style lang="scss" scoped>
.tags-view-container {
  position: relative;
  height: 38px;
  background: var(--bg-container);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
}

.tags-scroll {
  width: 100%;
  height: 100%;

  :deep(.el-scrollbar__wrap) {
    height: 100%;
    overflow-y: hidden;
  }
}

.tags-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 16px;
  height: 38px;
  white-space: nowrap;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 28px;
  padding: 0 10px;
  font-size: 12px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  background: var(--bg-page);
  color: var(--text-regular);
  cursor: pointer;
  transition: all var(--transition-fast);
  flex-shrink: 0;

  &:hover {
    color: var(--primary-color);
    border-color: var(--primary-border);
  }

  &.active {
    background: var(--primary-color);
    color: #fff;
    border-color: var(--primary-color);

    .tag-close:hover {
      background: rgba(255, 255, 255, 0.2);
    }
  }
}

.tag-text {
  line-height: 1;
}

.tag-close {
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  transition: background var(--transition-fast);

  &:hover {
    background: rgba(0, 0, 0, 0.1);
  }
}

.contextmenu {
  position: fixed;
  z-index: 3000;
  margin: 0;
  padding: 6px 0;
  background: var(--bg-container);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  list-style: none;
  min-width: 140px;

  li {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    font-size: 13px;
    color: var(--text-regular);
    cursor: pointer;
    transition: background var(--transition-fast);

    &:hover {
      background: var(--primary-bg);
      color: var(--primary-color);
    }
  }
}
</style>
