import { defineStore } from "pinia";

export const useSearchResultStore = defineStore('searchResult', () => {
  const searchResult = ref([
    {
      title: "다가학원",
      url: "http://example.com",
      address: "부산광역시 부산진구 개금1동 xxx-xxx",
      telNum: "051-000-0000",
      description: "다가학원은 다가학원이다.",
      benefit: "올해 입학금 면제(셋째자녀)"
    },
    {
      title: "다가학원",
      url: "http://example.com",
      address: "부산광역시 부산진구 개금1동 xxx-xxx",
      telNum: "051-000-0000",
      description: "다가학원은 다가학원이다.",
      benefit: "올해 입학금 면제(셋째자녀)"
    }
  ]);

  return { searchResult };
});
