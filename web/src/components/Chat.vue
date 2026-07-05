<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-logo">📚 知识库系统</div>
      <nav class="sidebar-nav">
        <router-link
          to="/dashboard"
          :class="{ active: $route.path === '/dashboard' }"
          >📊 仪表盘</router-link
        >
        <router-link
          to="/knowledge"
          :class="{ active: $route.path === '/knowledge' }"
          >📁 知识库管理</router-link
        >
        <router-link to="/chat" :class="{ active: $route.path === '/chat' }"
          >💬 智能问答</router-link
        >
      </nav>
      <div class="sidebar-footer">
        <div class="user-info">{{ nickname }}</div>
        <button @click="logout">退出登录</button>
      </div>
    </aside>

    <main class="main-content chat-layout">
      <!-- 左侧：会话列表 -->
      <div class="chat-sidebar">
        <div class="chat-sidebar-header">
          <h3>对话历史</h3>
          <button
            class="btn-primary"
            style="padding: 6px 12px; font-size: 12px"
            @click="newChat"
          >
            新对话
          </button>
        </div>

        <div class="kb-selector">
          <label>选择知识库</label>
          <select v-model="selectedKbId" @change="loadHistory">
            <option :value="null">全部知识库</option>
            <option v-for="kb in knowledgeBases" :key="kb.id" :value="kb.id">
              {{ kb.name }}
            </option>
          </select>
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
          <div
            v-if="sessionList.length === 0"
            class="empty-state"
            style="padding: 20px"
          >
            <p style="font-size: 13px">暂无对话记录</p>
          </div>
        </div>
      </div>

      <!-- 右侧：聊天区域 -->
      <div class="chat-main">
        <div class="chat-messages" ref="messagesContainer">
          <div v-if="messages.length === 0" class="chat-welcome">
            <h2>🤖 智能问答助手</h2>
            <p>请选择知识库后开始提问，我将基于知识库内容为您解答</p>
          </div>

          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message', msg.role]"
          >
            <div class="message-avatar">
              {{ msg.role === "user" ? "👤" : "🤖" }}
            </div>
            <div class="message-content">
              <div
                class="message-text"
                v-html="renderMarkdown(msg.content)"
              ></div>
            </div>
          </div>

          <div v-if="streaming" class="message assistant">
            <div class="message-avatar">🤖</div>
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
            <textarea
              v-model="inputText"
              @keydown.enter.exact="sendMessage"
              placeholder="输入您的问题，按 Enter 发送..."
              rows="1"
              :disabled="streaming"
            ></textarea>
            <button
              class="btn-primary send-btn"
              @click="sendMessage"
              :disabled="!inputText.trim() || streaming"
            >
              发送
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from "vue";
import { useRouter } from "vue-router";

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
      // 按sessionId分组
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
    messages.value.push({
      role: "assistant",
      content: "请先选择一个知识库后再提问，这样我才能基于知识库内容为您解答。",
    });
    nextTick(() => scrollToBottom());
    return;
  }

  messages.value.push({ role: "user", content: question });
  inputText.value = "";
  streaming.value = true;
  streamingText.value = "";

  nextTick(() => scrollToBottom());

  try {
    // 尝试流式请求
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
                } else if (currentEvent === "done") {
                  // 流式完成，最终完整响应
                }
              }
            } else if (line.trim() === "") {
              currentEvent = "";
            }
          }
        }
      }

      messages.value.push({ role: "assistant", content: streamingText.value });
    } else {
      // 非流式回退
      const data = await response.json();
      if (data.code === 200) {
        currentSessionId.value = data.data.sessionId;
        messages.value.push({ role: "assistant", content: data.data.answer });
      } else {
        messages.value.push({
          role: "assistant",
          content: "抱歉，获取回答失败：" + (data.message || "未知错误"),
        });
      }
    }
  } catch (error) {
    // 回退到非流式
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
        messages.value.push({
          role: "assistant",
          content: "抱歉，获取回答失败",
        });
      }
    } catch (e) {
      messages.value.push({
        role: "assistant",
        content: "网络错误，请稍后重试",
      });
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
  // 简单的Markdown渲染
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
  height: calc(100vh - 0px);
}

.chat-sidebar {
  width: 260px;
  background: white;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.chat-sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
}

.chat-sidebar-header h3 {
  font-size: 15px;
}

.kb-selector {
  padding: 12px 16px;
  border-bottom: 1px solid #e0e0e0;
}

.kb-selector label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.kb-selector select {
  width: 100%;
  padding: 6px 8px;
  font-size: 13px;
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.session-item:hover {
  background: #f5f7fa;
}

.session-item.active {
  background: #e8f0fe;
  border-left: 3px solid #4285f4;
}

.session-title {
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.chat-welcome {
  text-align: center;
  padding: 80px 20px;
  color: #666;
}

.chat-welcome h2 {
  font-size: 24px;
  margin-bottom: 12px;
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

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #4285f4;
}

.message.assistant .message-avatar {
  background: #34a853;
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
  background: #4285f4;
  color: white;
  border-top-right-radius: 4px;
}

.message.assistant .message-text {
  background: white;
  border: 1px solid #e0e0e0;
  border-top-left-radius: 4px;
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
  color: #4285f4;
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
  border-top: 1px solid #e0e0e0;
}

.chat-input-wrapper {
  display: flex;
  gap: 10px;
  max-width: 800px;
  margin: 0 auto;
}

.chat-input-wrapper textarea {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  resize: none;
  min-height: 42px;
  max-height: 120px;
}

.send-btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
}
</style>
