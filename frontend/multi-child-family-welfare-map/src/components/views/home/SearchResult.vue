<script setup>
import { useSearchResultStore } from '@/stores/searchResult';
import { computed } from 'vue';
import SearchResultItem from './SearchResultItem.vue';

const props = defineProps({
  modelValue: Boolean
});

const emit = defineEmits(['update:modelValue']);

const searchResultStore = useSearchResultStore();

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
    <div v-for="(searchResult, index) in searchResultStore.searchResults">
      <SearchResultItem
        :key="index"
        :search-result-item="searchResult"
      />
      <hr v-if="index < searchResultStore.searchResults.length - 1">
    </div>
  </BOffcanvas>
</template>
