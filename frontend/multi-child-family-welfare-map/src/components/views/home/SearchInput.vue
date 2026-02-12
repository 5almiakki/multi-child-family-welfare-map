<script setup>
import { ref } from 'vue';

const searchText = ref("");
const categories = ref([
  "금융기관", "문화시설", "체육시설", "유치원", "어린이집", "학원", "요식업등", "병의원", "한의원",
  "이미용업", "세탁, 목욕업", "자동차", "사진관", "유통업체", "안경", "여행업", "숙박업", "기타",
]);

const search = () => {

};
</script>

<template>
  <BForm>
    <BInputGroup class="shadow-sm">
      <BFormInput
        v-model="searchText"
        placeholder="검색어를 입력하세요."
      />
      <template #append>
        <BButton variant="success" @click="search">검색</BButton>
      </template>
    </BInputGroup>
    <div class="d-flex flex-nowrap overflow-auto pb-2 custom-scrollbar button-group">
      <BButton
        v-for="(item, index) in categories"
        :key="index"
        variant="light"
        pill
        size="sm"
        class="m-1 shadow-sm text-nowrap"
      >
        {{ item }}
      </BButton>
    </div>
  </BForm>
</template>

<style scoped>
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
