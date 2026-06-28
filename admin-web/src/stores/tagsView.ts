import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteLocationNormalized } from 'vue-router'

export interface TagItem {
  title: string
  path: string
  name: string
  affix?: boolean
}

export const useTagsViewStore = defineStore('tagsView', () => {
  const visitedViews = ref<TagItem[]>([])
  const cachedViews = ref<string[]>([])

  const addView = (route: RouteLocationNormalized) => {
    if (!route.name || !route.meta?.title) return
    const name = route.name as string
    
    const hasDashboard = visitedViews.value.some(v => v.name === 'Dashboard')
    if (!hasDashboard && name !== 'Dashboard') {
      visitedViews.value.push({
        title: '数据概览',
        path: '/dashboard',
        name: 'Dashboard',
        affix: true
      })
      if (!cachedViews.value.includes('Dashboard')) {
        cachedViews.value.push('Dashboard')
      }
    }
    
    const exists = visitedViews.value.some(v => v.name === name)
    const isAffix = name === 'Dashboard'
    if (!exists) {
      visitedViews.value.push({
        title: route.meta.title as string,
        path: route.fullPath || route.path,
        name,
        affix: isAffix
      })
    } else {
      const tag = visitedViews.value.find(v => v.name === name)
      if (tag) {
        tag.path = route.fullPath || route.path
        tag.title = route.meta.title as string
      }
    }
    if (!cachedViews.value.includes(name)) {
      cachedViews.value.push(name)
    }
  }

  const delView = (path: string) => {
    const index = visitedViews.value.findIndex(v => v.path === path)
    if (index > -1) {
      const view = visitedViews.value[index]
      if (view.affix) return null
      visitedViews.value.splice(index, 1)
      const cacheIndex = cachedViews.value.indexOf(view.name)
      if (cacheIndex > -1) {
        cachedViews.value.splice(cacheIndex, 1)
      }
      const lastView = visitedViews.value[visitedViews.value.length - 1]
      return lastView ? lastView.path : '/dashboard'
    }
    return null
  }

  const delOthersViews = (path: string) => {
    visitedViews.value = visitedViews.value.filter(v => v.affix || v.path === path)
    const current = visitedViews.value.find(v => v.path === path)
    cachedViews.value = current ? [current.name] : visitedViews.value.filter(v => v.affix).map(v => v.name)
  }

  const delAllViews = () => {
    visitedViews.value = visitedViews.value.filter(v => v.affix)
    cachedViews.value = visitedViews.value.filter(v => v.affix).map(v => v.name)
    return '/dashboard'
  }

  return {
    visitedViews,
    cachedViews,
    addView,
    delView,
    delOthersViews,
    delAllViews
  }
})
