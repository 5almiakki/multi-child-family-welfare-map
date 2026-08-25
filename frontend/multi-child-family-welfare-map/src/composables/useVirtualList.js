import { ref, computed, onMounted, onUnmounted } from 'vue';

/**
 * 대용량 목록 렌더링 최적화를 위한 가상 스크롤 Composable
 * @param {import('vue').Ref<Array>} listRef 전체 데이터 리스트 Ref
 * @param {Object} options
 * @param {number} [options.itemHeight=120] 아이템 1개의 예상 평균 높이(px)
 * @param {number} [options.buffer=5] 뷰포트 위아래 여유 버퍼 아이템 수
 */
export const useVirtualList = (listRef, options = {}) => {
  const itemHeight = options.itemHeight || 120;
  const buffer = options.buffer || 5;

  const containerRef = ref(null);
  const scrollTop = ref(0);
  const containerHeight = ref(500);

  let resizeObserver = null;

  const onScroll = (e) => {
    scrollTop.value = e.target.scrollTop;
  };

  const totalCount = computed(() => listRef.value.length);
  const totalHeight = computed(() => totalCount.value * itemHeight);

  const startIndex = computed(() => {
    return Math.max(0, Math.floor(scrollTop.value / itemHeight) - buffer);
  });

  const endIndex = computed(() => {
    return Math.min(
      totalCount.value,
      Math.ceil((scrollTop.value + containerHeight.value) / itemHeight) + buffer
    );
  });

  const visibleItems = computed(() => {
    return listRef.value
      .slice(startIndex.value, endIndex.value)
      .map((item, i) => ({
        data: item,
        index: startIndex.value + i
      }));
  });

  const offsetY = computed(() => startIndex.value * itemHeight);

  onMounted(() => {
    if (containerRef.value) {
      containerHeight.value = containerRef.value.clientHeight || 500;
      resizeObserver = new ResizeObserver((entries) => {
        for (const entry of entries) {
          containerHeight.value = entry.contentRect.height;
        }
      });
      resizeObserver.observe(containerRef.value);
    }
  });

  onUnmounted(() => {
    resizeObserver?.disconnect();
  });

  return {
    containerRef,
    visibleItems,
    totalHeight,
    offsetY,
    onScroll
  };
};
