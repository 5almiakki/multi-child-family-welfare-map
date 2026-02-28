<script setup>
const props = defineProps({
  searchResultItem: Object
});
</script>

<template>
  <h5>{{ searchResultItem.title }}</h5>
  {{ searchResultItem.url }}
  <br>
  {{ searchResultItem.address }}
  <br>
  {{ searchResultItem.telNum }}
  <br>
  {{ searchResultItem.description }}
  <br>
  {{ searchResultItem.benefit }}
</template>
