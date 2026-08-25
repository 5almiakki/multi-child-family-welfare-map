<script setup>
const props = defineProps({
  searchResultItem: Object
});
</script>

<template>
  <h5>{{ searchResultItem.name }}</h5>
  <p v-if="searchResultItem.tel || searchResultItem.sourceAddress">
    <span v-if="searchResultItem.tel">{{ searchResultItem.tel }}<br></span>
    {{ searchResultItem.sourceAddress }}
  </p>
  <p>{{ searchResultItem.benefit }}</p>
</template>
