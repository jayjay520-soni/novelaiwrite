<template>
  <div class="page-container">
    <div class="home-grid">
      <div class="form-panel card-panel">
        <h2 class="panel-title">
          <el-icon><MagicStick /></el-icon>
          AI 小说生成
        </h2>

        <el-form :model="form" label-position="top" @submit.prevent="handleGenerate">
          <el-form-item label="作品标题">
            <el-input v-model="form.title" placeholder="给你的小说起个名字" />
          </el-form-item>

          <el-form-item label="小说类型">
            <el-select v-model="form.genre" placeholder="选择类型" style="width: 100%">
              <el-option v-for="g in genres" :key="g" :label="g" :value="g" />
            </el-select>
          </el-form-item>

          <el-form-item label="目标字数">
            <el-slider v-model="form.wordCount" :min="500" :max="10000" :step="500" show-input />
          </el-form-item>

          <el-form-item label="创作提示词">
            <el-input
                v-model="form.prompt"
                type="textarea"
                :rows="6"
                placeholder="描述你的故事创意，例如：一个普通大学生意外获得穿越时空的能力，在唐宋元明清五个朝代展开冒险..."
            />
          </el-form-item>

          <div class="form-actions">
            <el-button type="primary" size="large" :loading="generating" @click="handleGenerate">
              <el-icon><Promotion /></el-icon>
              开始生成
            </el-button>
            <el-button size="large" @click="resetForm">重置</el-button>
          </div>
        </el-form>
      </div>

      <div class="preview-panel card-panel">
        <div class="preview-header">
          <h2 class="panel-title">
            <el-icon><View /></el-icon>
            实时预览
          </h2>
          <el-button v-if="streamText" text type="primary" @click="goToEditor">
            进入编辑 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <div class="preview-content" ref="previewRef">
          <p v-if="!streamText && !generating" class="placeholder">
            生成的内容将在这里实时显示...
          </p>
          <p v-else class="stream-text">{{ streamText }}<span v-if="generating" class="cursor">|</span></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useNovelStore } from '@/stores/novel'
import { createNovel } from '@/api/novel'
import { createStreamSession } from '@/api/llm'
import { SseClient, buildStreamUrl } from '@/utils/sse'

const router = useRouter()
const userStore = useUserStore()
const novelStore = useNovelStore()

const genres = ['玄幻', '都市', '科幻', '言情', '悬疑', '历史', '武侠', '奇幻']
const generating = ref(false)
const streamText = ref('')
const previewRef = ref(null)
const currentNovelId = ref(null)
let sseClient = null

const form = ref({
  title: '',
  genre: '玄幻',
  wordCount: 2000,
  prompt: ''
})

function resetForm() {
  form.value = { title: '', genre: '玄幻', wordCount: 2000, prompt: '' }
  streamText.value = ''
}

onMounted(() => {
  const tplStr = sessionStorage.getItem('novelTemplate')
  if (tplStr) {
    try {
      const tpl = JSON.parse(tplStr)
      form.value.title = tpl.title
      form.value.genre = tpl.genre
      form.value.prompt = tpl.prompt
      sessionStorage.removeItem('novelTemplate')
    } catch { /* ignore */ }
  }
})

async function handleGenerate() {
  if (!form.value.prompt.trim()) {
    ElMessage.warning('请输入创作提示词')
    return
  }
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入作品标题')
    return
  }

  generating.value = true
  streamText.value = ''
  sseClient?.close()

  try {
    const novelRes = await createNovel({
      title: form.value.title,
      genre: form.value.genre,
      summary: form.value.prompt
    })
    currentNovelId.value = novelRes.data.id
    novelStore.setCurrentNovel(novelRes.data)

    const sessionRes = await createStreamSession({
      prompt: form.value.prompt,
      genre: form.value.genre,
      wordCount: form.value.wordCount,
      novelId: currentNovelId.value
    })

    const clientId = sessionRes.data
    const url = buildStreamUrl(clientId, {
      prompt: form.value.prompt,
      genre: form.value.genre,
      wordCount: form.value.wordCount,
      novelId: currentNovelId.value
    }, userStore.token)

    sseClient = new SseClient(url, {
      onMessage: (text) => {
        streamText.value += text
        novelStore.appendStreamText(text)
        nextTick(() => {
          if (previewRef.value) {
            previewRef.value.scrollTop = previewRef.value.scrollHeight
          }
        })
      },
      onDone: () => {
        generating.value = false
        novelStore.isGenerating = false
        // 生成结束：把流式内容规范化为段落，并写回 currentNovel.content
        novelStore.finalizeStreamToNovel()
        ElMessage.success('生成完成')
      },
      onError: (msg) => {
        generating.value = false
        // 即使出错，也把已生成的内容同步到 store，避免丢失
        novelStore.finalizeStreamToNovel()
        ElMessage.error(msg || '生成失败')
      }
    })

    novelStore.isGenerating = true
    novelStore.resetStream()
    sseClient.connect()
  } catch {
    generating.value = false
  }
}

function goToEditor() {
  router.push(`/editor/${currentNovelId.value}`)
}
</script>

<style scoped lang="scss">
.home-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  align-items: start;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  margin-bottom: 20px;
  color: #303133;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .panel-title { margin-bottom: 0; }
}

.preview-content {
  min-height: 400px;
  max-height: 600px;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  line-height: 1.8;
  font-size: 15px;
  white-space: pre-wrap;
}

.placeholder {
  color: #c0c4cc;
  text-align: center;
  padding-top: 120px;
}

.cursor {
  animation: blink 1s infinite;
  color: #409eff;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

@media (max-width: 768px) {
  .home-grid {
    grid-template-columns: 1fr;
  }

  .preview-content {
    min-height: 300px;
    max-height: 400px;
  }
}
</style>
