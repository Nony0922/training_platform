<template>
  <div class="stat-cards">
    <div
      v-for="item in items"
      :key="item.label"
      class="stat-card"
      :class="item.tone ? `stat-card--${item.tone}` : ''"
    >
      <span v-if="item.icon" class="stat-card-icon">{{ item.icon }}</span>
      <div class="stat-card-body">
        <span class="stat-card-value">{{ item.value }}</span>
        <span class="stat-card-label">{{ item.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  items: { type: Array, default: () => [] }
})
</script>
