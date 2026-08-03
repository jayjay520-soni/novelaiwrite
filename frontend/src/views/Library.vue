<template>
  <div class="page-container">
    <div class="library-header">
      <h2>我的作品库</h2>
      <el-button type="primary" @click="$router.push('/home')">
        <el-icon><Plus /></el-icon> 新建作品
      </el-button>
    </div>

    <div v-loading="loading" class="novel-grid">
      <div v-for="novel in novels" :key="novel.id" class="novel-card card-panel" @click="openNovel(novel)">
        <div class="novel-card-header">
          <h3>{{ novel.title }}</h3>
          <el-tag size="small" :type="statusType(novel.status)">{{ statusLabel(novel.status) }}</el-tag>
        </div>
        <p class="novel-genre">{{ novel.genre || '未分类' }}</p>
        <p class="novel-summary">{{ novel.summary || '暂无简介' }}</p>
        <div class="novel-meta">
          <span>{{ novel.wordCount || 0 }} 字</span>
          <span>{{ formatDate(novel.updateTime) }}</span>
        </div>
        <div class="novel-actions" @click.stop>
          <el-button text type="primary" @click="openNovel(novel)">编辑</el-button>
          <el-button text type="danger" @click="handleDelete(novel)">删除</el-button>
        </div>
      </div>

      <el-empty v-if="!loading && novels.length === 0" description="暂无作品，去创作吧！" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNovelList, deleteNovel } from '@/api/novel'
import { useNovelStore } from '@/stores/novel'

const router = useRouter()
const novelStore = useNovelStore()
const novels = ref([])
const loading = ref(false)

const statusMap = { 0: ['草稿', 'info'], 1: ['已完成', 'success'], 2: ['生成中', 'warning'] }
const statusLabel = (s) => statusMap[s]?.[0] || '草稿'
const statusType = (s) => statusMap[s]?.[1] || 'info'

onMounted(loadNovels)

async function loadNovels() {
  loading.value = true
  try {
    const res = await getNovelList()
    novels.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openNovel(novel) {
  novelStore.setCurrentNovel(novel)
  router.push(`/editor/${novel.id}`)
}

async function handleDelete(novel) {
  await ElMessageBox.confirm(`确定删除「${novel.title}」？`, '提示', { type: 'warning' })
  await deleteNovel(novel.id)
  ElMessage.success('删除成功')
  loadNovels()
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
</script>

<style scoped lang="scss">
.library-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 { font-size: 22px; }
}

.novel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.novel-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }
}

.novel-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;

  h3 {
    font-size: 16px;
    flex: 1;
    margin-right: 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.novel-genre {
  font-size: 13px;
  color: #409eff;
  margin-bottom: 8px;
}

.novel-summary {
  font-size: 13px;
  color: #909399;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12px;
  min-height: 36px;
}

.novel-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #c0c4cc;
  margin-bottom: 8px;
}

.novel-actions {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  border-top: 1px solid #ebeef5;
  padding-top: 8px;
}
</style>
