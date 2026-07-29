/**
 * 연속 호출을 지정된 지연 시간만큼 묶어서 마지막 호출만 실행한다.
 * @param {Function} fn 디바운스할 함수
 * @param {number} delay 지연 시간(ms)
 * @returns {{ debounced: Function, cancel: Function }}
 */
export const useDebounceFn = (fn, delay) => {
  let timerId = null;

  const debounced = (...args) => {
    if (timerId) {
      clearTimeout(timerId);
    }
    timerId = setTimeout(() => {
      timerId = null;
      fn(...args);
    }, delay);
  };

  const cancel = () => {
    if (timerId) {
      clearTimeout(timerId);
      timerId = null;
    }
  };

  return { debounced, cancel };
};
