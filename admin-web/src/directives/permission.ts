import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    checkPermission(el, binding)
  },
  updated(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    checkPermission(el, binding)
  }
}

function checkPermission(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
  const { value } = binding
  const userStore = useUserStore()

  if (value && value.length > 0) {
    const hasPerm = Array.isArray(value)
      ? value.some(perm => userStore.hasPermission(perm))
      : userStore.hasPermission(value)

    if (!hasPerm) {
      el.parentNode?.removeChild(el)
    }
  }
}

export default permission
