import { defineStore } from "pinia";
import { ref } from "vue";
import { searchCompanies } from "@/services/companyService";

export const useSearchResultStore = defineStore('searchResult', () => {
  const searchResults = ref([]);
  const isLoading = ref(false);

  let abortController = null;

  /**
   * 이전 검색 요청을 취소하고 새 조건으로 검색한다.
   * @param {import('@/services/companyService').CompanySearchParams} params
   */
  const search = async params => {
    abortController?.abort();
    const controller = new AbortController();
    abortController = controller;

    isLoading.value = true;
    try {
      const results = await searchCompanies(params, controller.signal);
      if (controller === abortController) {
        searchResults.value = results;
      }
    } catch (error) {
      if (error.code !== "ERR_CANCELED") {
        console.error(error);
      }
    } finally {
      if (controller === abortController) {
        isLoading.value = false;
      }
    }
  };

  return { searchResults, isLoading, search };
});
