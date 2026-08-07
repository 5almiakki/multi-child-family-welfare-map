import { computed, toValue } from 'vue'

function escapeRegExp(str) {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

/**
 * 텍스트에서 키워드를 하이라이트한다.
 * @param {Function} text 텍스트
 * @param {Function} keyword 키워드
 * @returns {{ text: string, matched: boolean}[]} 하이라이트된 텍스트
 */
export function useHighlight(text, keyword) {
  return computed(() => {
    const rawText = toValue(text);
    const kw = toValue(keyword)?.trim();

    if (!kw) {
      return [{ text: rawText, matched: false }];
    }

    const escaped = escapeRegExp(kw);
    const regex = new RegExp(`(${escaped})`, 'gi');
    const parts = rawText.split(regex);

    return parts
      .filter(part => part !== '')
      .map(part => ({
        text: part,
        matched: part.toLowerCase() === kw.toLowerCase()
      }));
  });
};
