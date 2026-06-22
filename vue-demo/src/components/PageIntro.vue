<template>
  <div class="page-intro">
    <p>{{ text }}</p>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, required: true }
})
</script>
