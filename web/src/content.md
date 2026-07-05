`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\web\src\App.vue`n```
<script setup lang="ts"></script>

<template>
  <router-view />
</template>

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\web\src\components\Chat.vue`n```
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

    <main class="main-content chat-layout">
      <!-- 左侧：会话列表 -->
      <div class="chat-sidebar">
        <div class="chat-sidebar-header">
          <h3>对话历史</h3>
          <el-button type="primary" size="small" @click="newChat">
            <el-icon><Plus /></el-icon>新对话
          </el-button>
        </div>

        <div class="kb-selector">
          <el-select
            v-model="selectedKbId"
            placeholder="选择知识库"
            clearable
            style="width: 100%"
            @change="loadHistory"
          >
            <el-option
              v-for="kb in knowledgeBases"
              :key="kb.id"
              :label="kb.name"
              :value="kb.id"
            />
          </el-select>
        </div>

        <div class="session-list">
          <div
            v-for="session in sessionList"
            :key="session.sessionId"
            :class="[
              'session-item',
              { active: currentSessionId === session.sessionId },
            ]"
            @click="switchSession(session.sessionId)"
          >
            <div class="session-title">{{ session.firstQuestion }}</div>
            <div class="session-time">{{ formatDate(session.createdAt) }}</div>
          </div>
          <el-empty
            v-if="sessionList.length === 0"
            description="暂无对话记录"
            :image-size="60"
          />
        </div>
      </div>

      <!-- 右侧：聊天区域 -->
      <div class="chat-main">
        <div class="chat-messages" ref="messagesContainer">
          <div v-if="messages.length === 0 && !streaming" class="chat-welcome">
            <el-icon :size="64" color="#409EFF"><ChatDotRound /></el-icon>
            <h2>智能问答助手</h2>
            <p>请选择知识库后开始提问，我将基于知识库内容为您解答</p>
          </div>

          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message', msg.role]"
          >
            <el-avatar
              v-if="msg.role === 'user'"
              :size="36"
              icon="UserFilled"
              style="background: #409eff"
            />
            <el-avatar v-else :size="36" style="background: #67c23a">
              <el-icon :size="20"><Monitor /></el-icon>
            </el-avatar>
            <div class="message-content">
              <div
                class="message-text"
                v-html="renderMarkdown(msg.content)"
              ></div>
            </div>
          </div>

          <div v-if="streaming" class="message assistant">
            <el-avatar :size="36" style="background: #67c23a">
              <el-icon :size="20"><Monitor /></el-icon>
            </el-avatar>
            <div class="message-content">
              <div
                class="message-text"
                v-html="renderMarkdown(streamingText)"
              ></div>
              <span class="typing-indicator">▊</span>
            </div>
          </div>
        </div>

        <div class="chat-input-area">
          <div class="chat-input-wrapper">
            <el-input
              v-model="inputText"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 4 }"
              placeholder="输入您的问题，按 Enter 发送..."
              :disabled="streaming"
              resize="none"
              @keydown.enter.exact.prevent="sendMessage"
            />
            <el-button
              type="primary"
              :icon="Promotion"
              :disabled="!inputText.trim() || streaming"
              @click="sendMessage"
            >
              发送
            </el-button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  Reading,
  DataAnalysis,
  FolderOpened,
  ChatDotRound,
  SwitchButton,
  Plus,
  Monitor,
  Promotion,
} from "@element-plus/icons-vue";

const router = useRouter();
const nickname = ref(localStorage.getItem("nickname") || "");

interface Message {
  role: "user" | "assistant";
  content: string;
}

interface SessionInfo {
  sessionId: string;
  firstQuestion: string;
  createdAt: string;
}

const knowledgeBases = ref<any[]>([]);
const selectedKbId = ref<number | null>(null);
const messages = ref<Message[]>([]);
const inputText = ref("");
const streaming = ref(false);
const streamingText = ref("");
const currentSessionId = ref<string>("");
const sessionList = ref<SessionInfo[]>([]);
const messagesContainer = ref<HTMLElement | null>(null);

const getHeaders = () => ({
  "Content-Type": "application/json",
  "X-User-Id": localStorage.getItem("userId") || "",
});

const loadKnowledgeBases = async () => {
  try {
    const res = await fetch("/api/kb", { headers: getHeaders() });
    const data = await res.json();
    if (data.code === 200) {
      knowledgeBases.value = data.data || [];
    }
  } catch (error) {
    console.error("Failed to load knowledge bases:", error);
  }
};

const loadHistory = async () => {
  try {
    let url = "/api/chat/history";
    if (selectedKbId.value) {
      url = `/api/chat/history/kb/${selectedKbId.value}`;
    }
    const res = await fetch(url, { headers: getHeaders() });
    const data = await res.json();
    if (data.code === 200) {
      const histories = data.data || [];
      const sessionMap = new Map<string, SessionInfo>();
      histories.forEach((h: any) => {
        if (!sessionMap.has(h.sessionId)) {
          sessionMap.set(h.sessionId, {
            sessionId: h.sessionId,
            firstQuestion:
              h.question.substring(0, 30) +
              (h.question.length > 30 ? "..." : ""),
            createdAt: h.createdAt,
          });
        }
      });
      sessionList.value = Array.from(sessionMap.values()).reverse();
    }
  } catch (error) {
    console.error("Failed to load history:", error);
  }
};

const switchSession = async (sessionId: string) => {
  currentSessionId.value = sessionId;
  try {
    const res = await fetch(`/api/chat/history?sessionId=${sessionId}`, {
      headers: getHeaders(),
    });
    const data = await res.json();
    if (data.code === 200) {
      messages.value = (data.data || [])
        .map((h: any) => [
          { role: "user" as const, content: h.question },
          { role: "assistant" as const, content: h.answer },
        ])
        .flat();
      nextTick(() => scrollToBottom());
    }
  } catch (error) {
    console.error("Failed to load session messages:", error);
  }
};

const newChat = () => {
  currentSessionId.value = "";
  messages.value = [];
};

const sendMessage = async () => {
  const question = inputText.value.trim();
  if (!question || streaming.value) return;

  if (!selectedKbId.value) {
    ElMessage.warning("请先选择一个知识库后再提问");
    return;
  }

  messages.value.push({ role: "user", content: question });
  inputText.value = "";
  streaming.value = true;
  streamingText.value = "";

  nextTick(() => scrollToBottom());

  try {
    const response = await fetch("/api/chat/stream", {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({
        question,
        kbId: selectedKbId.value,
        sessionId: currentSessionId.value || undefined,
      }),
    });

    if (response.headers.get("content-type")?.includes("text/event-stream")) {
      const reader = response.body?.getReader();
      const decoder = new TextDecoder();

      if (reader) {
        let buffer = "";
        let currentEvent = "";
        let messageAdded = false;
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split("\n");
          buffer = lines.pop() || "";

          for (const line of lines) {
            if (line.startsWith("event:")) {
              currentEvent = line.substring(6).trim();
            } else if (line.startsWith("data:")) {
              const data = line.substring(5).trim();
              if (data) {
                if (currentEvent === "token") {
                  streamingText.value += data;
                  nextTick(() => scrollToBottom());
                } else if (currentEvent === "done" && !messageAdded) {
                  messages.value.push({
                    role: "assistant",
                    content: streamingText.value,
                  });
                  currentSessionId.value = data;
                  messageAdded = true;
                }
              }
            } else if (line.trim() === "") {
              currentEvent = "";
            }
          }
        }
      }
    } else {
      const data = await response.json();
      if (data.code === 200) {
        currentSessionId.value = data.data.sessionId;
        messages.value.push({ role: "assistant", content: data.data.answer });
      } else {
        ElMessage.error(data.message || "获取回答失败");
      }
    }
  } catch (error) {
    try {
      const res = await fetch("/api/chat/ask", {
        method: "POST",
        headers: getHeaders(),
        body: JSON.stringify({
          question,
          kbId: selectedKbId.value,
          sessionId: currentSessionId.value || undefined,
        }),
      });
      const data = await res.json();
      if (data.code === 200) {
        currentSessionId.value = data.data.sessionId;
        messages.value.push({ role: "assistant", content: data.data.answer });
      } else {
        ElMessage.error("获取回答失败");
      }
    } catch (e) {
      ElMessage.error("网络错误，请稍后重试");
    }
  } finally {
    streaming.value = false;
    streamingText.value = "";
    nextTick(() => scrollToBottom());
    loadHistory();
  }
};

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
};

const renderMarkdown = (text: string) => {
  if (!text) return "";
  return text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
    .replace(/\*(.*?)\*/g, "<em>$1</em>")
    .replace(/`([^`]+)`/g, "<code>$1</code>")
    .replace(/\n/g, "<br>");
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return "";
  return new Date(dateStr).toLocaleString("zh-CN", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};

const logout = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("userId");
  localStorage.removeItem("username");
  localStorage.removeItem("nickname");
  router.push("/login");
};

onMounted(() => {
  loadKnowledgeBases();
  loadHistory();
});
</script>

<style scoped>
.chat-layout {
  display: flex;
  gap: 0;
  padding: 0 !important;
  height: 100vh;
}

.chat-sidebar {
  width: 260px;
  background: white;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.chat-sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.chat-sidebar-header h3 {
  font-size: 15px;
  color: #303133;
}

.kb-selector {
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f2f6fc;
  transition: background 0.2s;
}

.session-item:hover {
  background: #f5f7fa;
}

.session-item.active {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.session-title {
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #303133;
}

.session-time {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.chat-welcome {
  text-align: center;
  padding: 80px 20px;
  color: #909399;
}

.chat-welcome h2 {
  font-size: 24px;
  margin: 16px 0 12px;
  color: #303133;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  max-width: 800px;
}

.message.user {
  flex-direction: row-reverse;
  margin-left: auto;
}

.message-content {
  max-width: 70%;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.message.user .message-text {
  background: #409eff;
  color: white;
  border-top-right-radius: 4px;
}

.message.assistant .message-text {
  background: white;
  border: 1px solid #e4e7ed;
  border-top-left-radius: 4px;
  color: #303133;
}

.message-text :deep(code) {
  background: rgba(0, 0, 0, 0.06);
  padding: 2px 4px;
  border-radius: 3px;
  font-size: 13px;
}

.message.user .message-text :deep(code) {
  background: rgba(255, 255, 255, 0.2);
}

.typing-indicator {
  animation: blink 1s infinite;
  color: #409eff;
}

@keyframes blink {
  0%,
  50% {
    opacity: 1;
  }
  51%,
  100% {
    opacity: 0;
  }
}

.chat-input-area {
  padding: 16px 20px;
  background: white;
  border-top: 1px solid #e4e7ed;
}

.chat-input-wrapper {
  display: flex;
  gap: 10px;
  max-width: 800px;
  margin: 0 auto;
  align-items: flex-end;
}

.chat-input-wrapper :deep(.el-textarea) {
  flex: 1;
}
</style>

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\web\src\components\Dashboard.vue`n```
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

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\web\src\components\HelloWorld.vue`n```
<script setup lang="ts">
import { ref } from 'vue'
import viteLogo from '../assets/vite.svg'
import heroImg from '../assets/hero.png'
import vueLogo from '../assets/vue.svg'

const count = ref(0)
</script>

<template>
  <section id="center">
    <div class="hero">
      <img :src="heroImg" class="base" width="170" height="179" alt="" />
      <img :src="vueLogo" class="framework" alt="Vue logo" />
      <img :src="viteLogo" class="vite" alt="Vite logo" />
    </div>
    <div>
      <h1>Get started</h1>
      <p>Edit <code>src/App.vue</code> and save to test <code>HMR</code></p>
    </div>
    <button type="button" class="counter" @click="count++">
      Count is {{ count }}
    </button>
  </section>

  <div class="ticks"></div>

  <section id="next-steps">
    <div id="docs">
      <svg class="icon" role="presentation" aria-hidden="true">
        <use href="/icons.svg#documentation-icon"></use>
      </svg>
      <h2>Documentation</h2>
      <p>Your questions, answered</p>
      <ul>
        <li>
          <a href="https://vite.dev/" target="_blank">
            <img class="logo" :src="viteLogo" alt="" />
            Explore Vite
          </a>
        </li>
        <li>
          <a href="https://vuejs.org/" target="_blank">
            <img class="button-icon" :src="vueLogo" alt="" />
            Learn more
          </a>
        </li>
      </ul>
    </div>
    <div id="social">
      <svg class="icon" role="presentation" aria-hidden="true">
        <use href="/icons.svg#social-icon"></use>
      </svg>
      <h2>Connect with us</h2>
      <p>Join the Vite community</p>
      <ul>
        <li>
          <a href="https://github.com/vitejs/vite" target="_blank">
            <svg class="button-icon" role="presentation" aria-hidden="true">
              <use href="/icons.svg#github-icon"></use>
            </svg>
            GitHub
          </a>
        </li>
        <li>
          <a href="https://chat.vite.dev/" target="_blank">
            <svg class="button-icon" role="presentation" aria-hidden="true">
              <use href="/icons.svg#discord-icon"></use>
            </svg>
            Discord
          </a>
        </li>
        <li>
          <a href="https://x.com/vite_js" target="_blank">
            <svg class="button-icon" role="presentation" aria-hidden="true">
              <use href="/icons.svg#x-icon"></use>
            </svg>
            X.com
          </a>
        </li>
        <li>
          <a href="https://bsky.app/profile/vite.dev" target="_blank">
            <svg class="button-icon" role="presentation" aria-hidden="true">
              <use href="/icons.svg#bluesky-icon"></use>
            </svg>
            Bluesky
          </a>
        </li>
      </ul>
    </div>
  </section>

  <div class="ticks"></div>
  <section id="spacer"></section>
</template>

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\web\src\components\KnowledgeBase.vue`n```
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
      <div class="page-header">
        <h2>知识库管理</h2>
        <el-button type="primary" @click="showCreateModal = true">
          <el-icon><Plus /></el-icon>新建知识库
        </el-button>
      </div>

      <!-- 知识库列表 -->
      <el-empty v-if="knowledgeBases.length === 0" description="暂无知识库">
        <el-button type="primary" @click="showCreateModal = true"
          >创建第一个知识库</el-button
        >
      </el-empty>

      <div v-else class="kb-list">
        <el-card
          v-for="kb in knowledgeBases"
          :key="kb.id"
          shadow="hover"
          class="kb-item"
        >
          <div class="kb-info">
            <h3>{{ kb.name }}</h3>
            <p class="kb-desc">{{ kb.description || "暂无描述" }}</p>
            <div class="kb-meta">
              <el-text type="info" size="small">
                <el-icon><Document /></el-icon> {{ kb.docCount }} 篇文档
              </el-text>
              <el-text type="info" size="small">
                <el-icon><Clock /></el-icon> {{ formatDate(kb.createdAt) }}
              </el-text>
            </div>
          </div>
          <div class="kb-actions">
            <el-button @click="viewDocuments(kb.id)">
              <el-icon><View /></el-icon>查看文档
            </el-button>
            <el-button type="primary" @click="openUploadModal(kb.id)">
              <el-icon><Upload /></el-icon>上传文档
            </el-button>
            <el-button type="danger" @click="handleDeleteKb(kb.id)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 文档列表弹窗 -->
      <el-dialog v-model="showDocModal" title="文档列表" width="700px">
        <el-empty v-if="documents.length === 0" description="暂无文档" />
        <el-table v-else :data="documents" stripe>
          <el-table-column prop="fileName" label="文件名" />
          <el-table-column prop="fileType" label="类型" width="80" />
          <el-table-column label="大小" width="100">
            <template #default="{ row }">{{
              formatSize(row.fileSize)
            }}</template>
          </el-table-column>
          <el-table-column prop="chunkCount" label="分块数" width="80" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag
                :type="
                  row.status === 'completed'
                    ? 'success'
                    : row.status === 'processing'
                      ? 'warning'
                      : 'danger'
                "
                size="small"
              >
                {{ statusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button
                type="danger"
                size="small"
                text
                @click="handleDeleteDoc(row.id)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>

      <!-- 创建知识库弹窗 -->
      <el-dialog v-model="showCreateModal" title="新建知识库" width="480px">
        <el-form label-position="top">
          <el-form-item label="知识库名称">
            <el-input v-model="newKbName" placeholder="请输入知识库名称" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input
              type="textarea"
              v-model="newKbDesc"
              placeholder="请输入知识库描述（可选）"
              :rows="3"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateModal = false">取消</el-button>
          <el-button type="primary" @click="createKb">创建</el-button>
        </template>
      </el-dialog>

      <!-- 上传文档弹窗 -->
      <el-dialog
        v-model="showUploadModal"
        title="上传文档"
        width="560px"
        class="upload-dialog"
      >
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="10"
          multiple
          drag
          accept=".txt,.md,.markdown,.pdf,.doc,.docx"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :file-list="fileList"
          class="upload-dragger"
        >
          <div class="upload-inner">
            <el-icon class="upload-icon"><UploadFilled /></el-icon>
            <div class="upload-text">将文件拖到此处，或<em>点击上传</em></div>
            <div class="upload-hint">
              支持 .txt、.md、.pdf、.doc、.docx 格式，最多 10 个文件
            </div>
          </div>
        </el-upload>
        <template #footer>
          <el-button @click="showUploadModal = false">取消</el-button>
          <el-button
            type="primary"
            :loading="uploading"
            :disabled="fileList.length === 0"
            @click="uploadDocument"
          >
            {{ uploading ? "上传中..." : `上传 ${fileList.length} 个文件` }}
          </el-button>
        </template>
      </el-dialog>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Reading,
  DataAnalysis,
  FolderOpened,
  ChatDotRound,
  SwitchButton,
  Plus,
  Document,
  Clock,
  View,
  Upload,
  UploadFilled,
  Delete,
} from "@element-plus/icons-vue";
import type { UploadFile, UploadUserFile } from "element-plus";

const router = useRouter();
const nickname = ref(localStorage.getItem("nickname") || "");

const knowledgeBases = ref<any[]>([]);
const documents = ref<any[]>([]);
const showCreateModal = ref(false);
const showDocModal = ref(false);
const showUploadModal = ref(false);
const newKbName = ref("");
const newKbDesc = ref("");
const selectedFile = ref<File | null>(null);
const fileList = ref<UploadUserFile[]>([]);
const uploading = ref(false);
const currentKbId = ref<number | null>(null);
const uploadRef = ref();

const getHeaders = () => ({
  "X-User-Id": localStorage.getItem("userId") || "",
});

const loadKnowledgeBases = async () => {
  try {
    const res = await fetch("/api/kb", { headers: getHeaders() });
    const data = await res.json();
    if (data.code === 200) {
      knowledgeBases.value = data.data || [];
    }
  } catch (error) {
    console.error("Failed to load knowledge bases:", error);
  }
};

const createKb = async () => {
  if (!newKbName.value.trim()) {
    ElMessage.warning("请输入知识库名称");
    return;
  }
  try {
    const res = await fetch("/api/kb", {
      method: "POST",
      headers: { "Content-Type": "application/json", ...getHeaders() },
      body: JSON.stringify({
        name: newKbName.value,
        description: newKbDesc.value,
      }),
    });
    const data = await res.json();
    if (data.code === 200) {
      showCreateModal.value = false;
      newKbName.value = "";
      newKbDesc.value = "";
      ElMessage.success("创建成功");
      loadKnowledgeBases();
    } else {
      ElMessage.error(data.message || "创建失败");
    }
  } catch (error) {
    ElMessage.error("创建失败");
  }
};

const handleDeleteKb = async (id: number) => {
  try {
    await ElMessageBox.confirm("确定要删除此知识库吗？", "提示", {
      type: "warning",
    });
    await fetch(`/api/kb/${id}`, { method: "DELETE", headers: getHeaders() });
    ElMessage.success("删除成功");
    loadKnowledgeBases();
  } catch {
    // 用户取消
  }
};

const viewDocuments = async (kbId: number) => {
  currentKbId.value = kbId;
  try {
    const res = await fetch(`/api/document/list/${kbId}`, {
      headers: getHeaders(),
    });
    const data = await res.json();
    if (data.code === 200) {
      documents.value = data.data || [];
    } else {
      ElMessage.error(data.message || "加载文档列表失败");
    }
    showDocModal.value = true;
  } catch (error) {
    console.error("Failed to load documents:", error);
    ElMessage.error("加载文档列表失败");
    showDocModal.value = true;
  }
};

const openUploadModal = (kbId: number) => {
  currentKbId.value = kbId;
  selectedFile.value = null;
  fileList.value = [];
  showUploadModal.value = true;
  nextTick(() => {
    uploadRef.value?.clearFiles();
  });
};

const handleFileChange = (file: UploadFile, uploadFileList: UploadFile[]) => {
  fileList.value = uploadFileList;
  if (file.raw && !selectedFile.value) {
    selectedFile.value = file.raw;
  }
};

const handleFileRemove = (_file: UploadFile, uploadFileList: UploadFile[]) => {
  fileList.value = uploadFileList;
  selectedFile.value =
    uploadFileList.length > 0 ? uploadFileList[0].raw || null : null;
};

const uploadDocument = async () => {
  if (fileList.value.length === 0 || !currentKbId.value) return;
  uploading.value = true;
  try {
    if (fileList.value.length === 1) {
      // 单文件上传
      const raw = fileList.value[0].raw;
      if (!raw) {
        ElMessage.error("文件数据异常");
        return;
      }
      const formData = new FormData();
      formData.append("file", raw);
      formData.append("kbId", String(currentKbId.value));

      const res = await fetch("/api/document/upload", {
        method: "POST",
        headers: getHeaders(),
        body: formData,
      });
      const data = await res.json();
      if (data.code === 200) {
        showUploadModal.value = false;
        ElMessage.success("上传成功");
        loadKnowledgeBases();
      } else {
        ElMessage.error(data.message || "上传失败");
      }
    } else {
      // 多文件批量上传
      const formData = new FormData();
      let hasFile = false;
      fileList.value.forEach((f) => {
        if (f.raw) {
          formData.append("files", f.raw);
          hasFile = true;
        }
      });
      if (!hasFile) {
        ElMessage.error("文件数据异常");
        return;
      }
      formData.append("kbId", String(currentKbId.value));

      const res = await fetch("/api/document/batch-upload", {
        method: "POST",
        headers: getHeaders(),
        body: formData,
      });
      const data = await res.json();
      if (data.code === 200) {
        showUploadModal.value = false;
        const count = (data.data || []).length;
        ElMessage.success(`成功上传 ${count} 个文件`);
        loadKnowledgeBases();
      } else {
        ElMessage.error(data.message || "上传失败");
      }
    }
  } catch (error) {
    ElMessage.error("上传失败");
  } finally {
    uploading.value = false;
  }
};

const handleDeleteDoc = async (id: number) => {
  try {
    await ElMessageBox.confirm("确定要删除此文档吗？", "提示", {
      type: "warning",
    });
    await fetch(`/api/document/${id}`, {
      method: "DELETE",
      headers: getHeaders(),
    });
    ElMessage.success("删除成功");
    if (currentKbId.value) viewDocuments(currentKbId.value);
    loadKnowledgeBases();
  } catch {
    // 用户取消
  }
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return "";
  return new Date(dateStr).toLocaleString("zh-CN");
};

const formatSize = (bytes: number) => {
  if (!bytes) return "0 B";
  const units = ["B", "KB", "MB", "GB"];
  let i = 0;
  let size = bytes;
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024;
    i++;
  }
  return size.toFixed(1) + " " + units[i];
};

const statusText = (status: string) => {
  const map: Record<string, string> = {
    processing: "处理中",
    completed: "已完成",
    failed: "失败",
  };
  return map[status] || status;
};

const logout = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("userId");
  localStorage.removeItem("username");
  localStorage.removeItem("nickname");
  router.push("/login");
};

onMounted(() => {
  loadKnowledgeBases();
});
</script>

<style scoped>
.kb-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.kb-item :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.kb-info h3 {
  font-size: 16px;
  margin-bottom: 4px;
  color: #303133;
}

.kb-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.kb-meta {
  display: flex;
  gap: 16px;
}

.kb-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* 上传弹窗美化 */
.upload-dragger :deep(.el-upload-dragger) {
  padding: 30px 20px;
  border-radius: 12px;
  border: 2px dashed #d9ecff;
  background: #f5f9ff;
  transition: all 0.3s;
}

.upload-dragger :deep(.el-upload-dragger:hover) {
  border-color: #409eff;
  background: #ecf5ff;
}

.upload-dragger :deep(.el-upload-dragger.is-dragover) {
  border-color: #409eff;
  background: #d9ecff;
}

.upload-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 4px;
}

.upload-text {
  font-size: 14px;
  color: #606266;
}

.upload-text em {
  color: #409eff;
  font-style: normal;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.upload-dialog :deep(.el-dialog__body) {
  padding-top: 16px;
  padding-bottom: 8px;
}
</style>

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\web\src\components\Login.vue`n```
<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <el-icon :size="48" color="#409EFF"><Reading /></el-icon>
        <h1>企业知识库智能问答系统</h1>
        <p>基于大模型的知识管理与智能问答平台</p>
      </div>

      <el-tabs v-model="mode" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form @submit.prevent="handleSubmit" label-position="top">
            <el-form-item label="用户名">
              <el-input
                v-model="username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item label="密码">
              <el-input
                type="password"
                v-model="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleSubmit"
              style="width: 100%"
            >
              {{ loading ? "处理中..." : "登 录" }}
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form @submit.prevent="handleSubmit" label-position="top">
            <el-form-item label="用户名">
              <el-input
                v-model="username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item label="密码">
              <el-input
                type="password"
                v-model="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input
                v-model="nickname"
                placeholder="请输入昵称（可选）"
                :prefix-icon="UserFilled"
                size="large"
              />
            </el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleSubmit"
              style="width: 100%"
            >
              {{ loading ? "处理中..." : "注 册" }}
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <el-alert
        v-if="errorMsg"
        :title="errorMsg"
        type="error"
        show-icon
        :closable="false"
        style="margin-top: 12px"
      />

      <div class="login-footer">
        <el-text type="info" size="small">默认账号: admin / admin123</el-text>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { User, Lock, UserFilled, Reading } from "@element-plus/icons-vue";

const router = useRouter();
const mode = ref<"login" | "register">("login");
const username = ref("");
const password = ref("");
const nickname = ref("");
const loading = ref(false);
const errorMsg = ref("");

const handleSubmit = async () => {
  loading.value = true;
  errorMsg.value = "";

  try {
    const url =
      mode.value === "login" ? "/api/auth/login" : "/api/auth/register";
    const body: any = { username: username.value, password: password.value };
    if (mode.value === "register" && nickname.value) {
      body.nickname = nickname.value;
    }

    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    const result = await response.json();

    if (result.code === 200) {
      localStorage.setItem("token", result.data.token);
      localStorage.setItem("userId", String(result.data.userId));
      localStorage.setItem("username", result.data.username);
      localStorage.setItem("nickname", result.data.nickname);
      router.push("/dashboard");
    } else {
      errorMsg.value = result.message || "操作失败";
    }
  } catch (error) {
    errorMsg.value = "网络错误，请稍后重试";
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0f172a;
  position: relative;
  overflow: hidden;
}

.login-page::before {
  content: "";
  position: absolute;
  top: -40%;
  left: -20%;
  width: 70%;
  height: 70%;
  background: radial-gradient(
    circle,
    rgba(64, 158, 255, 0.15) 0%,
    transparent 70%
  );
  pointer-events: none;
}

.login-page::after {
  content: "";
  position: absolute;
  bottom: -30%;
  right: -10%;
  width: 60%;
  height: 60%;
  background: radial-gradient(
    circle,
    rgba(103, 194, 58, 0.1) 0%,
    transparent 70%
  );
  pointer-events: none;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 40px;
  width: 420px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-header h1 {
  font-size: 22px;
  margin: 12px 0 8px;
  color: #303133;
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
}
</style>

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\web\src\main.ts`n```
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './assets/main.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus)
app.use(router)
app.mount('#app')

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\web\src\router\index.ts`n```
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../components/Login.vue')
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('../components/Dashboard.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/knowledge',
      name: 'Knowledge',
      component: () => import('../components/KnowledgeBase.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/chat',
      name: 'Chat',
      component: () => import('../components/Chat.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router

```
`n`n---`n## C:\Users\admin\Desktop\基于大模型的企业知识库智能问答系统\web\src\style.css`n```
:root {
  --text: #6b6375;
  --text-h: #08060d;
  --bg: #fff;
  --border: #e5e4e7;
  --code-bg: #f4f3ec;
  --accent: #aa3bff;
  --accent-bg: rgba(170, 59, 255, 0.1);
  --accent-border: rgba(170, 59, 255, 0.5);
  --social-bg: rgba(244, 243, 236, 0.5);
  --shadow:
    rgba(0, 0, 0, 0.1) 0 10px 15px -3px, rgba(0, 0, 0, 0.05) 0 4px 6px -2px;

  --sans: system-ui, 'Segoe UI', Roboto, sans-serif;
  --heading: system-ui, 'Segoe UI', Roboto, sans-serif;
  --mono: ui-monospace, Consolas, monospace;

  font: 18px/145% var(--sans);
  letter-spacing: 0.18px;
  color-scheme: light dark;
  color: var(--text);
  background: var(--bg);
  font-synthesis: none;
  text-rendering: optimizeLegibility;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;

  @media (max-width: 1024px) {
    font-size: 16px;
  }
}

@media (prefers-color-scheme: dark) {
  :root {
    --text: #9ca3af;
    --text-h: #f3f4f6;
    --bg: #16171d;
    --border: #2e303a;
    --code-bg: #1f2028;
    --accent: #c084fc;
    --accent-bg: rgba(192, 132, 252, 0.15);
    --accent-border: rgba(192, 132, 252, 0.5);
    --social-bg: rgba(47, 48, 58, 0.5);
    --shadow:
      rgba(0, 0, 0, 0.4) 0 10px 15px -3px, rgba(0, 0, 0, 0.25) 0 4px 6px -2px;
  }

  #social .button-icon {
    filter: invert(1) brightness(2);
  }
}

body {
  margin: 0;
}

h1,
h2 {
  font-family: var(--heading);
  font-weight: 500;
  color: var(--text-h);
}

h1 {
  font-size: 56px;
  letter-spacing: -1.68px;
  margin: 32px 0;
  @media (max-width: 1024px) {
    font-size: 36px;
    margin: 20px 0;
  }
}
h2 {
  font-size: 24px;
  line-height: 118%;
  letter-spacing: -0.24px;
  margin: 0 0 8px;
  @media (max-width: 1024px) {
    font-size: 20px;
  }
}
p {
  margin: 0;
}

code,
.counter {
  font-family: var(--mono);
  display: inline-flex;
  border-radius: 4px;
  color: var(--text-h);
}

code {
  font-size: 15px;
  line-height: 135%;
  padding: 4px 8px;
  background: var(--code-bg);
}

.counter {
  font-size: 16px;
  padding: 5px 10px;
  border-radius: 5px;
  color: var(--accent);
  background: var(--accent-bg);
  border: 2px solid transparent;
  transition: border-color 0.3s;
  margin-bottom: 24px;

  &:hover {
    border-color: var(--accent-border);
  }
  &:focus-visible {
    outline: 2px solid var(--accent);
    outline-offset: 2px;
  }
}

.hero {
  position: relative;

  .base,
  .framework,
  .vite {
    inset-inline: 0;
    margin: 0 auto;
  }

  .base {
    width: 170px;
    position: relative;
    z-index: 0;
  }

  .framework,
  .vite {
    position: absolute;
  }

  .framework {
    z-index: 1;
    top: 34px;
    height: 28px;
    transform: perspective(2000px) rotateZ(300deg) rotateX(44deg) rotateY(39deg)
      scale(1.4);
  }

  .vite {
    z-index: 0;
    top: 107px;
    height: 26px;
    width: auto;
    transform: perspective(2000px) rotateZ(300deg) rotateX(40deg) rotateY(39deg)
      scale(0.8);
  }
}

#app {
  width: 1126px;
  max-width: 100%;
  margin: 0 auto;
  text-align: center;
  border-inline: 1px solid var(--border);
  min-height: 100svh;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

#center {
  display: flex;
  flex-direction: column;
  gap: 25px;
  place-content: center;
  place-items: center;
  flex-grow: 1;

  @media (max-width: 1024px) {
    padding: 32px 20px 24px;
    gap: 18px;
  }
}

#next-steps {
  display: flex;
  border-top: 1px solid var(--border);
  text-align: left;

  & > div {
    flex: 1 1 0;
    padding: 32px;
    @media (max-width: 1024px) {
      padding: 24px 20px;
    }
  }

  .icon {
    margin-bottom: 16px;
    width: 22px;
    height: 22px;
  }

  @media (max-width: 1024px) {
    flex-direction: column;
    text-align: center;
  }
}

#docs {
  border-right: 1px solid var(--border);

  @media (max-width: 1024px) {
    border-right: none;
    border-bottom: 1px solid var(--border);
  }
}

#next-steps ul {
  list-style: none;
  padding: 0;
  display: flex;
  gap: 8px;
  margin: 32px 0 0;

  .logo {
    height: 18px;
  }

  a {
    color: var(--text-h);
    font-size: 16px;
    border-radius: 6px;
    background: var(--social-bg);
    display: flex;
    padding: 6px 12px;
    align-items: center;
    gap: 8px;
    text-decoration: none;
    transition: box-shadow 0.3s;

    &:hover {
      box-shadow: var(--shadow);
    }
    .button-icon {
      height: 18px;
      width: 18px;
    }
  }

  @media (max-width: 1024px) {
    margin-top: 20px;
    flex-wrap: wrap;
    justify-content: center;

    li {
      flex: 1 1 calc(50% - 8px);
    }

    a {
      width: 100%;
      justify-content: center;
      box-sizing: border-box;
    }
  }
}

#spacer {
  height: 88px;
  border-top: 1px solid var(--border);
  @media (max-width: 1024px) {
    height: 48px;
  }
}

.ticks {
  position: relative;
  width: 100%;

  &::before,
  &::after {
    content: '';
    position: absolute;
    top: -4.5px;
    border: 5px solid transparent;
  }

  &::before {
    left: 0;
    border-left-color: var(--border);
  }
  &::after {
    right: 0;
    border-right-color: var(--border);
  }
}

```
