<script setup>
import { ref, watch } from 'vue';
import { useDebounceFn } from '@/composables/useDebounceFn';
import { COMPANY_CATEGORIES } from '@/constants/category';
import { SEARCH_DEBOUNCE_DELAY_MS } from '@/constants/search';

const emit = defineEmits([
  "on-search"
]);

const keyword = ref("");
const categories = ref(COMPANY_CATEGORIES);
const selectedCategories = ref([]);

const emitSearch = () => {
  emit("on-search", {
    keyword: keyword.value.trim(),
    categories: selectedCategories.value
  });
};

const { debounced: debouncedSearch, cancel: cancelDebouncedSearch } =
  useDebounceFn(emitSearch, SEARCH_DEBOUNCE_DELAY_MS);

const searchImmediately = () => {
  cancelDebouncedSearch();
  emitSearch();
};

watch(selectedCategories, searchImmediately, { deep: true });
</script>

<template>
  <div class="search-overlay">
    <BForm @submit.prevent>
      <BInputGroup class="shadow-sm">
        <BFormInput
          v-model="keyword"
          placeholder="검색어를 입력하세요."
          @keyup.enter="searchImmediately"
        />
        <template #append>
          <BButton variant="success" @click="searchImmediately">검색</BButton>
        </template>
      </BInputGroup>
      <div class="d-flex flex-nowrap overflow-auto pb-2 custom-scrollbar button-group">
        <BFormCheckbox
          v-for="(category, index) in categories"
          :key="index"
          v-model="selectedCategories"
          :value="category"
          button
          button-variant="light"
          size="sm"
          class="m-1 shadow-sm text-nowrap"
        >
          {{ category }}
        </BFormCheckbox>
      </div>
    </BForm>
  </div>
</template>

<style scoped>
.search-overlay {
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  z-index: 1000;
}

.custom-scrollbar::-webkit-scrollbar {
  display: none;
}

.custom-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.button-group {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 10px;
}
</style>
