<script setup>
import { useSearchResultStore } from '@/stores/searchResult';
import { ref, watch } from 'vue';
import SearchResultItem from './SearchResultItem.vue';

const props = defineProps({
  modelValue: Boolean
});

const emit = defineEmits(['update:modelValue']);

const searchResultStore = useSearchResultStore();

const SHEET_HEIGHT = {
  CLOSED: 0,
  LOW: 10,
  MEDIUM: 45,
  HIGH: 85
};
const sheetHeightPercent = ref(SHEET_HEIGHT.CLOSED);
const isDragging = ref(false);
const hasSearched = ref(false);

let startY = 0;
let startHeight = 0;

watch(
  () => props.modelValue,
  (open) => {
    if (!open) {
      sheetHeightPercent.value = hasSearched.value ? SHEET_HEIGHT.LOW : SHEET_HEIGHT.CLOSED;
      return;
    }
    hasSearched.value = true;
    sheetHeightPercent.value = SHEET_HEIGHT.MEDIUM;
  },
  { immediate: true }
);

const onPointerDown = (e) => {
  isDragging.value = true;
  startY = e.clientY;
  startHeight = sheetHeightPercent.value;
  window.addEventListener('pointermove', onPointerMove);
  window.addEventListener('pointerup', onPointerUp);
};

const onPointerMove = (e) => {
  if (!isDragging.value) {
    return;
  }
  const deltaY = startY - e.clientY;
  const deltaPercent = (deltaY / window.innerHeight) * 100;
  sheetHeightPercent.value = Math.min(
      Math.max(startHeight + deltaPercent, SHEET_HEIGHT.LOW),
      SHEET_HEIGHT.HIGH);
};

const onPointerUp = () => {
  if (!isDragging.value) return;
  isDragging.value = false;
  window.removeEventListener('pointermove', onPointerMove);
  window.removeEventListener('pointerup', onPointerUp);
  const current = sheetHeightPercent.value;
  if (current < 30) {
    sheetHeightPercent.value = hasSearched.value ? SHEET_HEIGHT.LOW : SHEET_HEIGHT.CLOSED;
    emit('update:modelValue', false);
  } else if (current < 65) {
    sheetHeightPercent.value = SHEET_HEIGHT.MEDIUM;
    emit('update:modelValue', true);
  } else {
    sheetHeightPercent.value = SHEET_HEIGHT.HIGH;
    emit('update:modelValue', true);
  }
};
</script>

<template>
  <div
    class="bottom-sheet shadow-lg"
    :class="{ dragging: isDragging }"
    :style="{ height: `${sheetHeightPercent}vh` }"
  >
    <div class="sheet-handle-bar" @pointerdown="onPointerDown">
      <div class="handle-pill"></div>
      <div class="d-flex justify-content-between align-items-center w-100 px-3 py-1">
        <span class="sheet-title">
          검색 결과
          <BBadge variant="info">
            {{ searchResultStore.searchResults.length }}
          </BBadge>
        </span>
      </div>
    </div>
    <div class="sheet-content">
      <div v-if="searchResultStore.searchResults.length === 0" class="text-center py-4 text-muted">
        검색 결과가 없습니다.
      </div>
      <div v-for="(searchResult, index) in searchResultStore.searchResults" :key="index">
        <SearchResultItem :search-result-item="searchResult" />
        <hr v-if="index < searchResultStore.searchResults.length - 1" class="my-4" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.bottom-sheet {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  border-top-left-radius: 16px;
  border-top-right-radius: 16px;
  z-index: 1050;
  display: flex;
  flex-direction: column;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.15);
  transition: height 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  touch-action: none;
}
.bottom-sheet.dragging {
  transition: none;
}
.sheet-handle-bar {
  padding: 8px 0 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: grab;
  user-select: none;
  background-color: #ffffff;
  border-top-left-radius: 16px;
  border-top-right-radius: 16px;
}
.sheet-handle-bar:active {
  cursor: grabbing;
}
.handle-pill {
  width: 36px;
  height: 4px;
  background-color: #ced4da;
  border-radius: 2px;
  margin-bottom: 6px;
}
.sheet-title {
  font-weight: 600;
}
.sheet-content {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px 20px;
  -webkit-overflow-scrolling: touch;
}
</style>
