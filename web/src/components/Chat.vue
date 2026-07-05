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
