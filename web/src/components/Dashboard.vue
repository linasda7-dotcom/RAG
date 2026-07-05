<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-logo">📚 知识库系统</div>
      <nav class="sidebar-nav">
        <router-link
          to="/dashboard"
          :class="{ active: $route.path === '/dashboard' }"
        >
          📊 仪表盘
        </router-link>
        <router-link
          to="/knowledge"
          :class="{ active: $route.path === '/knowledge' }"
        >
          📁 知识库管理
        </router-link>
        <router-link to="/chat" :class="{ active: $route.path === '/chat' }">
          💬 智能问答
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <div class="user-info">{{ nickname }}</div>
        <button @click="logout">退出登录</button>
      </div>
    </aside>

    <main class="main-content">
      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">📁</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.kbCount }}</div>
            <div class="stat-label">知识库数量</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">📄</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.docCount }}</div>
            <div class="stat-label">文档数量</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">💬</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.chatCount }}</div>
            <div class="stat-label">问答次数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">👤</div>
          <div class="stat-info">
            <div class="stat-value">{{ username }}</div>
            <div class="stat-label">当前用户</div>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions card">
        <h3>快捷操作</h3>
        <div class="action-grid">
          <button class="action-btn" @click="$router.push('/knowledge')">
            <span class="action-icon">➕</span>
            <span>创建知识库</span>
          </button>
          <button class="action-btn" @click="$router.push('/chat')">
            <span class="action-icon">🤖</span>
            <span>开始问答</span>
          </button>
          <button class="action-btn" @click="loadStats">
            <span class="action-icon">🔄</span>
            <span>刷新数据</span>
          </button>
        </div>
      </div>

      <!-- 最近知识库 -->
      <div class="card" style="margin-top: 20px">
        <h3 style="margin-bottom: 16px">最近知识库</h3>
        <div v-if="recentKbs.length === 0" class="empty-state">
          <p>暂无知识库，快去创建一个吧！</p>
          <button class="btn-primary" @click="$router.push('/knowledge')">
            创建知识库
          </button>
        </div>
        <div v-else class="kb-grid">
          <div
            v-for="kb in recentKbs"
            :key="kb.id"
            class="kb-card"
            @click="$router.push('/chat')"
          >
            <div class="kb-name">{{ kb.name }}</div>
            <div class="kb-desc">{{ kb.description || "暂无描述" }}</div>
            <div class="kb-meta">{{ kb.docCount }} 篇文档</div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const username = ref(localStorage.getItem("username") || "");
const nickname = ref(localStorage.getItem("nickname") || username.value);

const stats = ref({
  kbCount: 0,
  docCount: 0,
  chatCount: 0,
});

interface KbItem {
  id: number;
  name: string;
  description: string;
  docCount: number;
}

const recentKbs = ref<KbItem[]>([]);

const loadStats = async () => {
  try {
    const userId = localStorage.getItem("userId");
    const headers: any = { "X-User-Id": userId || "" };

    const [kbRes, chatRes] = await Promise.all([
      fetch("/api/kb", { headers }),
      fetch("/api/chat/history", { headers }),
    ]);

    const kbData = await kbRes.json();
    if (kbData.code === 200) {
      recentKbs.value = kbData.data || [];
      stats.value.kbCount = kbData.data?.length || 0;
      stats.value.docCount =
        kbData.data?.reduce(
          (sum: number, kb: KbItem) => sum + (kb.docCount || 0),
          0,
        ) || 0;
    }

    const chatData = await chatRes.json();
    if (chatData.code === 200) {
      stats.value.chatCount = chatData.data?.length || 0;
    }
  } catch (error) {
    console.error("Failed to load stats:", error);
  }
};

const logout = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("userId");
  localStorage.removeItem("username");
  localStorage.removeItem("nickname");
  router.push("/login");
};

onMounted(() => {
  loadStats();
});
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  font-size: 32px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 13px;
  color: #666;
}

.quick-actions h3 {
  margin-bottom: 16px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: #f8f9fa;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  color: #333;
  transition: all 0.2s;
}

.action-btn:hover {
  border-color: #4285f4;
  background: #f0f4ff;
}

.action-icon {
  font-size: 20px;
}

.kb-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.kb-card {
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.kb-card:hover {
  border-color: #4285f4;
  box-shadow: 0 2px 8px rgba(66, 133, 244, 0.15);
}

.kb-name {
  font-weight: 600;
  margin-bottom: 4px;
}

.kb-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.kb-meta {
  font-size: 12px;
  color: #999;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .action-grid {
    grid-template-columns: 1fr;
  }
  .kb-grid {
    grid-template-columns: 1fr;
  }
}
</style>
