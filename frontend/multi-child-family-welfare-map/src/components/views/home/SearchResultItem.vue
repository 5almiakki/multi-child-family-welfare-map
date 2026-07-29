<script setup>
const props = defineProps({
  searchResultItem: Object
});
</script>

<template>
  <h5>{{ searchResultItem.name }}</h5>
  {{ searchResultItem.homepageUrl }}
  <br>
  {{ searchResultItem.normalizedAddress }}
  <br>
  {{ searchResultItem.tel }}
  <br>
  {{ searchResultItem.description }}
  <br>
  {{ searchResultItem.benefit }}
</template>
