<script setup>
import { useHighlight } from '@/composables/useHighlight';

const props = defineProps({
  text: { type: String, required: true },
  keyword: { type: String, default: '' }
});

const parts = useHighlight(() => props.text, () => props.keyword)
</script>

<template>
  <span>
    <template v-for="(part, index) in parts" :key="index">
      <mark v-if="part.matched">{{ part.text }}</mark>
      <template v-else>{{ part.text }}</template>
    </template>
  </span>
</template>

<style scoped>
mark {
  background-color: rgba(0, 0, 0, 0);
  color: orangered;
  padding: 0;
  font-weight: 600;
}
</style>
