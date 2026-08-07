<script setup>
import { ref, watch } from 'vue';
import { useDebounceFn } from '@/composables/useDebounceFn';
import { COMPANY_CATEGORIES } from '@/constants/category';
import { SEARCH_DEBOUNCE_DELAY_MS } from '@/constants/search';
import { autocompleteCompanyNames } from '@/services/companyService';

const emit = defineEmits([
  "on-search"
]);

const keyword = ref("");
const categories = ref(COMPANY_CATEGORIES);
const selectedCategories = ref([]);

const suggestions = ref([]);
const isSuggestionsVisible = ref(false);
let autocompleteAbortController = null;

const fetchSuggestions = async (value) => {
  const trimmed = value.trim();
  if (!trimmed) {
    suggestions.value = [];
    isSuggestionsVisible.value = false;
    return;
  }

  autocompleteAbortController?.abort();
  autocompleteAbortController = new AbortController();

  try {
    const results = await autocompleteCompanyNames(
      trimmed,
      10,
      autocompleteAbortController.signal
    );
    suggestions.value = results;
    isSuggestionsVisible.value = results.length > 0;
  } catch (error) {
    if (error.code !== 'ERR_CANCELED') {
      console.error('검색어 추천 요청 실패:', error);
    }
  }
};

const { debounced: debouncedFetchSuggestions, cancel: cancelDebouncedFetch } =
  useDebounceFn(fetchSuggestions, SEARCH_DEBOUNCE_DELAY_MS);

watch(keyword, (value) => {
  cancelDebouncedFetch();
  debouncedFetchSuggestions(value);
});

const closeSuggestions = () => {
  isSuggestionsVisible.value = false;
};

const selectSuggestion = (name) => {
  keyword.value = name;
  closeSuggestions();
  cancelDebouncedFetch();
  autocompleteAbortController?.abort();
  emit("on-search", {
    keyword: name,
    categories: selectedCategories.value
  });
};

const emitSearch = () => {
  closeSuggestions();
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
      <div class="autocomplete-wrapper">
        <BInputGroup class="shadow-sm">
          <BFormInput
            v-model="keyword"
            placeholder="검색어를 입력하세요."
            autocomplete="off"
            @keyup.enter="searchImmediately"
            @blur="closeSuggestions"
            @focus="isSuggestionsVisible = suggestions.length > 0"
          />
          <template #append>
            <BButton variant="success" @click="searchImmediately">검색</BButton>
          </template>
        </BInputGroup>

        <ul v-if="isSuggestionsVisible" class="suggestions-list shadow">
          <li
            v-for="(name, index) in suggestions"
            :key="index"
            class="suggestions-item"
            @mousedown.prevent="selectSuggestion(name)"
          >
            <HighlightText :text="name" :keyword="keyword" />
          </li>
        </ul>
      </div>

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

.autocomplete-wrapper {
  position: relative;
}

.suggestions-list {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin: 4px 0 0;
  padding: 4px 0;
  list-style: none;
  background-color: #fff;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  max-height: 260px;
  overflow-y: auto;
  z-index: 1100;
}

.suggestions-item {
  padding: 8px 14px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.suggestions-item:hover {
  background-color: #f0f9f4;
  color: #198754;
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
