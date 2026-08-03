<template>
  <div class="page-container">
    <h2 class="page-title">模板广场</h2>
    <p class="page-desc">选择预设模板，快速开始 AI 小说创作</p>

    <div class="template-grid">
      <div
        v-for="tpl in templates"
        :key="tpl.id"
        class="template-card card-panel"
        @click="useTemplate(tpl)"
      >
        <div class="template-icon" :style="{ background: tpl.color }">
          <el-icon :size="28" color="#fff"><component :is="tpl.icon" /></el-icon>
        </div>
        <h3>{{ tpl.title }}</h3>
        <el-tag size="small">{{ tpl.genre }}</el-tag>
        <p class="template-desc">{{ tpl.description }}</p>
        <el-button type="primary" text>
          使用模板 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const templates = [
  {
    id: 1,
    title: '玄幻修仙',
    genre: '玄幻',
    icon: 'Sunrise',
    color: 'linear-gradient(135deg, #667eea, #764ba2)',
    description: '少年偶得仙缘，踏上修仙之路，历经磨难终成大道。',
    prompt: '一个普通少年在山中采药时意外发现上古仙人洞府，获得修仙传承。请以此展开一段修仙成长故事，包含入门、历练、突破等经典情节。'
  },
  {
    id: 2,
    title: '都市逆袭',
    genre: '都市',
    icon: 'OfficeBuilding',
    color: 'linear-gradient(135deg, #f093fb, #f5576c)',
    description: '底层青年凭借机遇与智慧，在都市中逆袭崛起。',
    prompt: '一个刚毕业的普通大学生，在大城市打拼遭遇挫折，意外获得特殊能力/机遇，开始逆袭人生。'
  },
  {
    id: 3,
    title: '星际探险',
    genre: '科幻',
    icon: 'Ship',
    color: 'linear-gradient(135deg, #4facfe, #00f2fe)',
    description: '人类舰队深入未知星域，探索宇宙奥秘与外星文明。',
    prompt: '2150年，人类首次派出深空探索舰队前往比邻星系，途中遭遇未知信号和神秘现象。'
  },
  {
    id: 4,
    title: '古风言情',
    genre: '言情',
    icon: 'Cherry',
    color: 'linear-gradient(135deg, #fa709a, #fee140)',
    description: '才子佳人，跨越身份与命运的爱情故事。',
    prompt: '一位书香门第的小姐与一位寒门书生在江南春日相遇，展开一段曲折动人的爱情故事。'
  },
  {
    id: 5,
    title: '悬疑推理',
    genre: '悬疑',
    icon: 'Search',
    color: 'linear-gradient(135deg, #a18cd1, #fbc2eb)',
    description: '离奇案件层层递进，真相扑朔迷离。',
    prompt: '一座封闭的古堡中发生离奇命案，在场的每个人都有嫌疑，请构建一个充满反转的悬疑故事。'
  },
  {
    id: 6,
    title: '历史架空',
    genre: '历史',
    icon: 'Reading',
    color: 'linear-gradient(135deg, #ffecd2, #fcb69f)',
    description: '穿越/架空历史背景，改写王朝命运。',
    prompt: '一位现代历史学者意外穿越到明朝末年，凭借历史知识试图改变王朝覆灭的命运。'
  }
]

function useTemplate(tpl) {
  sessionStorage.setItem('novelTemplate', JSON.stringify(tpl))
  ElMessage.success(`已选择「${tpl.title}」模板`)
  router.push('/home')
}
</script>

<style scoped lang="scss">
.page-title {
  font-size: 22px;
  margin-bottom: 4px;
}

.page-desc {
  color: #909399;
  margin-bottom: 24px;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.template-card {
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-4px);
  }

  h3 {
    font-size: 16px;
    margin: 12px 0 8px;
  }
}

.template-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.template-desc {
  font-size: 13px;
  color: #909399;
  margin: 12px 0;
  line-height: 1.6;
  min-height: 42px;
}
</style>
