import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNovelStore = defineStore('novel', () => {
  const currentNovel = ref(null)
  const segments = ref([])
  const isGenerating = ref(false)
  const streamText = ref('')

  function setCurrentNovel(novel) {
    if (!novel) {
      currentNovel.value = null
      segments.value = [{ id: 1, content: '' }]
      return
    }
    const sameNovel = currentNovel.value && currentNovel.value.id === novel.id
    const preferStream = sameNovel && streamText.value.length > (novel.content?.length || 0)

    currentNovel.value = novel
    const contentToUse = preferStream ? streamText.value : (novel.content || '')
    if (contentToUse) {
      splitIntoSegments(contentToUse)
      // 同步 currentNovel.content，避免 getFullContent 拿到的是旧 content
      currentNovel.value.content = contentToUse
    } else {
      segments.value = [{ id: 1, content: '' }]
    }
  }

  function splitIntoSegments(content) {
    const parts = content.split(/\n{2,}/).filter(Boolean)
    segments.value = parts.length
        ? parts.map((text, i) => ({ id: i + 1, content: text }))
        : [{ id: 1, content: '' }]
  }

  function updateSegment(id, content) {
    const seg = segments.value.find(s => s.id === id)
    if (seg) seg.content = content
  }

  function appendStreamText(text) {
    streamText.value += text
    // 同步追加到 segments 的最后一个段落，保证编辑页立即看到内容
    if (!segments.value || segments.value.length === 0) {
      segments.value = [{ id: 1, content: '' }]
    }
    const last = segments.value[segments.value.length - 1]
    last.content += text
  }

  function resetStream() {
    streamText.value = ''
    isGenerating.value = false
  }

  function getFullContent() {
    return segments.value.map(s => s.content).join('\n\n')
  }

  function finalizeStreamToNovel() {
    // 生成结束时调用：把 streamText 作为 content，并重新分段
    if (!streamText.value) return
    splitIntoSegments(streamText.value)
    if (currentNovel.value) {
      currentNovel.value.content = streamText.value
      currentNovel.value.wordCount = streamText.value.length
      currentNovel.value.status = 1
    }
  }

  function clearCurrent() {
    currentNovel.value = null
    segments.value = [{ id: 1, content: '' }]
    streamText.value = ''
    isGenerating.value = false
  }

  return {
    currentNovel,
    segments,
    isGenerating,
    streamText,
    setCurrentNovel,
    splitIntoSegments,
    updateSegment,
    appendStreamText,
    resetStream,
    getFullContent,
    finalizeStreamToNovel,
    clearCurrent
  }
})
