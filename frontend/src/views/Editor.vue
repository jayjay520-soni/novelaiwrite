<template>
  <div class="page-container">
    <div class="editor-header card-panel">
      <div class="title-area">
        <el-input v-model="title" placeholder="作品标题" class="title-input" />
        <el-tag :type="statusType">{{ statusLabel }}</el-tag>
      </div>
      <div class="toolbar">
        <el-button @click="copyFullText">
          <el-icon><CopyDocument /></el-icon> 复制全文
        </el-button>
        <el-dropdown @command="handleExport">
          <el-button>
            <el-icon><Download /></el-icon> 导出
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="txt">导出 TXT</el-dropdown-item>
              <el-dropdown-item command="md">导出 Markdown</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="primary" :loading="saving" @click="handleSave">
          <el-icon><Check /></el-icon> 保存
        </el-button>
      </div>
    </div>

    <div class="editor-body">
      <div class="segments-panel card-panel">
        <div class="segments-header">
          <h3>段落编辑</h3>
          <el-button text type="primary" @click="addSegment">+ 新增段落</el-button>
        </div>

        <div v-for="seg in segments" :key="seg.id" class="segment-item">
          <div class="segment-toolbar">
            <span class="segment-label">段落 {{ seg.id }}</span>
            <el-button text size="small" @click="copySegment(seg.content)">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
            <el-button text size="small" type="danger" @click="removeSegment(seg.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <el-input
              v-model="seg.content"
              type="textarea"
              :rows="6"
              placeholder="段落内容..."
              @input="onSegmentChange"
          />
        </div>
      </div>

      <div class="preview-panel card-panel">
        <h3>预览</h3>
        <div class="preview-text">
          <div v-for="seg in segments" :key="'p-' + seg.id" class="preview-paragraph">
            {{ seg.content }}
          </div>
          <p v-if="!hasContent" class="empty-tip">暂无内容，请在左侧编辑或从 AI 创作页生成</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useNovelStore } from '@/stores/novel'
import { getNovelDetail, updateNovel, createNovel } from '@/api/novel'
import { exportAsTxt, exportAsMarkdown, copyToClipboard } from '@/utils/export'

const route = useRoute()
const novelStore = useNovelStore()

const title = ref('未命名作品')
const status = ref(0)
const saving = ref(false)
const novelId = ref(null)

const segments = computed(() => novelStore.segments)
const hasContent = computed(() => segments.value.some(s => s.content.trim()))

const statusMap = { 0: ['草稿', 'info'], 1: ['已完成', 'success'], 2: ['生成中', 'warning'] }
const statusLabel = computed(() => statusMap[status.value]?.[0] || '草稿')
const statusType = computed(() => statusMap[status.value]?.[1] || 'info')

onMounted(async () => {
  try {
    const id = route.params.id
    if (id) {
      await loadNovel(id)
    } else if (novelStore.currentNovel) {
      applyNovel(novelStore.currentNovel)
    }
  } catch (e) {
    // 任何初始化异常都不能让整个组件崩溃导致导航卡死
    console.error('Editor 初始化失败:', e)
  }
})

watch(() => route.params.id, (id) => {
  if (id) {
    loadNovel(id).catch(e => console.error('切换 novel 失败:', e))
  }
})

async function loadNovel(id) {
  try {
    const res = await getNovelDetail(id, { skipErrorTip: true })
    applyNovel(res.data)
  } catch (e) {
    // 降级：如果 store 中已经有同 id 的 novel（刚生成完跳转过来的场景），
    // 即使后端暂时异常，也直接使用 store 中的内容，避免白屏/报错
    const existing = novelStore.currentNovel
    if (existing && String(existing.id) === String(id)) {
      applyNovel(existing)
      return
    }
    // 确实无法加载，再抛出让拦截器提示
    ElMessage.error(e?.responseData?.message || e?.message || '加载作品失败，请稍后重试')
    throw e
  }
}

function applyNovel(novel) {
  novelId.value = novel.id
  title.value = novel.title
  status.value = novel.status
  novelStore.setCurrentNovel(novel)
}

function onSegmentChange() {
  // reactive via v-model
}

function addSegment() {
  const maxId = Math.max(...segments.value.map(s => s.id), 0)
  novelStore.segments.push({ id: maxId + 1, content: '' })
}

function removeSegment(id) {
  if (segments.value.length <= 1) {
    ElMessage.warning('至少保留一个段落')
    return
  }
  novelStore.segments = segments.value.filter(s => s.id !== id)
}

async function handleSave() {
  saving.value = true
  try {
    const content = novelStore.getFullContent()
    const data = { title: title.value, content, status: 1 }

    if (novelId.value) {
      await updateNovel(novelId.value, data)
    } else {
      const res = await createNovel({ title: title.value, genre: '通用' })
      novelId.value = res.data.id
      await updateNovel(novelId.value, data)
    }
    status.value = 1
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

async function copyFullText() {
  await copyToClipboard(novelStore.getFullContent())
  ElMessage.success('已复制全文')
}

async function copySegment(text) {
  await copyToClipboard(text)
  ElMessage.success('已复制段落')
}

function handleExport(format) {
  const content = novelStore.getFullContent()
  const filename = title.value.replace(/[\\/:*?"<>|]/g, '_')
  if (format === 'md') {
    exportAsMarkdown(title.value, content, filename)
  } else {
    exportAsTxt(content, filename)
  }
  ElMessage.success('导出成功')
}
</script>

<style scoped lang="scss">
.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.title-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-input {
  width: 300px;
  font-size: 18px;

  :deep(.el-input__inner) {
    font-weight: 600;
  }
}

.toolbar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.editor-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.segments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 { font-size: 16px; }
}

.segment-item {
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
}

.segment-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.segment-label {
  flex: 1;
  font-size: 13px;
  color: #909399;
}

.preview-panel h3 {
  font-size: 16px;
  margin-bottom: 16px;
}

.preview-text {
  min-height: 500px;
  line-height: 1.8;
  font-size: 15px;
}

.preview-paragraph {
  margin-bottom: 16px;
  text-indent: 2em;
}

.empty-tip {
  color: #c0c4cc;
  text-align: center;
  padding-top: 100px;
}

@media (max-width: 768px) {
  .editor-body {
    grid-template-columns: 1fr;
  }

  .title-input { width: 100%; }
}
</style>
