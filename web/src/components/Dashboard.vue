<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-logo">
        <el-icon :size="24"><Reading /></el-icon>
        <span>知识库系统</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#1a1a2e"
        text-color="#e0e0e0"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/knowledge">
          <el-icon><FolderOpened /></el-icon>
          <span>知识库管理</span>
        </el-menu-item>
        <el-menu-item index="/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>智能问答</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-dropdown trigger="click" style="width: 100%">
          <div class="user-info">
            <el-avatar :size="28" icon="UserFilled" />
            <span>{{ nickname }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="logout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </aside>

    <main class="main-content">
      <!-- 统计卡片 -->
      <el-row :gutter="16" style="margin-bottom: 24px">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-inner">
              <el-icon :size="40" color="#409EFF"><FolderOpened /></el-icon>
              <div class="stat-info">
                <div class="stat-value">{{ stats.kbCount }}</div>
                <div class="stat-label">知识库数量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-inner">
              <el-icon :size="40" color="#67C23A"><Document /></el-icon>
              <div class="stat-info">
                <div class="stat-value">{{ stats.docCount }}</div>
                <div class="stat-label">文档数量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-inner">
              <el-icon :size="40" color="#E6A23C"><ChatDotRound /></el-icon>
              <div class="stat-info">
                <div class="stat-value">{{ stats.chatCount }}</div>
                <div class="stat-label">问答次数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-inner">
              <el-icon :size="40" color="#F56C6C"><User /></el-icon>
              <div class="stat-info">
                <div class="stat-value" style="font-size: 18px">
                  {{ username }}
                </div>
                <div class="stat-label">当前用户</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 快捷操作 -->
      <el-card shadow="hover" style="margin-bottom: 24px">
        <template #header>
          <span style="font-weight: 600">快捷操作</span>
        </template>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-button
              size="large"
              style="width: 100%"
              @click="$router.push('/knowledge')"
            >
              <el-icon><FolderAdd /></el-icon>创建知识库
            </el-button>
          </el-col>
          <el-col :span="8">
            <el-button
              type="primary"
              size="large"
              style="width: 100%"
              @click="$router.push('/chat')"
            >
              <el-icon><ChatDotRound /></el-icon>开始问答
            </el-button>
          </el-col>
          <el-col :span="8">
            <el-button size="large" style="width: 100%" @click="loadStats">
              <el-icon><Refresh /></el-icon>刷新数据
            </el-button>
          </el-col>
        </el-row>
      </el-card>

      <!-- 最近知识库 -->
      <el-card shadow="hover">
        <template #header>
          <span style="font-weight: 600">最近知识库</span>
        </template>
        <el-empty
          v-if="recentKbs.length === 0"
          description="暂无知识库，快去创建一个吧！"
        >
          <el-button type="primary" @click="$router.push('/knowledge')"
            >创建知识库</el-button
          >
        </el-empty>
        <el-row v-else :gutter="12">
          <el-col :span="8" v-for="kb in recentKbs" :key="kb.id">
            <el-card
              shadow="hover"
              class="kb-card"
              @click="$router.push('/chat')"
            >
              <div class="kb-name">{{ kb.name }}</div>
              <div class="kb-desc">{{ kb.description || "暂无描述" }}</div>
              <el-text type="info" size="small"
                >{{ kb.docCount }} 篇文档</el-text
              >
            </el-card>
          </el-col>
        </el-row>
      </el-card>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  Reading,
  DataAnalysis,
  FolderOpened,
  ChatDotRound,
  SwitchButton,
  User,
  Document,
  FolderAdd,
  Refresh,
} from "@element-plus/icons-vue";

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
.stat-card .stat-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.kb-card {
  cursor: pointer;
  transition: all 0.2s;
}

.kb-card:hover {
  border-color: #409eff;
}

.kb-name {
  font-weight: 600;
  margin-bottom: 4px;
  color: #303133;
}

.kb-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .el-col {
    max-width: 100% !important;
    flex: 0 0 100% !important;
  }
}
</style>
