<script setup>
defineProps({
  searchResultItem: {
    type: Object,
    required: true
  }
});
</script>

<template>
  <div class="result-item">
    <div class="item-name mb-1">{{ searchResultItem.name }}</div>
    <div class="item-info text-muted mb-1">
      <span v-if="searchResultItem.tel" class="me-2">{{ searchResultItem.tel }}</span>
      <span v-if="searchResultItem.sourceAddress">{{ searchResultItem.sourceAddress }}</span>
    </div>
    <div class="item-benefit text-success">
      {{ searchResultItem.benefit }}
    </div>
  </div>
</template>

<style scoped>
.result-item {
  height: 95px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  overflow: hidden;
}

.item-name {
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-info {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-benefit {
  font-size: 0.85rem;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
