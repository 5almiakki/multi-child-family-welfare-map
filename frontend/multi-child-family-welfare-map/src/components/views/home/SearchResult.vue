<script setup>
import { computed } from 'vue';

const props = defineProps({
  modelValue: Boolean,
  searchData: Array
});

const emit = defineEmits(['update:modelValue']);

const isOpen = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});
</script>

<template>
  <BOffcanvas
    id="search-result"
    class="shadow-lg search-result"
    title="검색 결과"
    placement="bottom"
    v-model="isOpen"
    no-backdrop
  >
    <div class="result-list">
    </div>
  </BOffcanvas>
</template>
