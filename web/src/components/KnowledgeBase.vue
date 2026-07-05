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

    <main class="main-content">
      <div class="page-header">
        <h2>知识库管理</h2>
        <button class="btn-primary" @click="showCreateModal = true">
          + 新建知识库
        </button>
      </div>

      <!-- 知识库列表 -->
      <div v-if="knowledgeBases.length === 0" class="empty-state card">
        <p>暂无知识库</p>
        <button class="btn-primary" @click="showCreateModal = true">
          创建第一个知识库
        </button>
      </div>

      <div v-else class="kb-list">
        <div v-for="kb in knowledgeBases" :key="kb.id" class="kb-item card">
          <div class="kb-info">
            <h3>{{ kb.name }}</h3>
            <p class="kb-desc">{{ kb.description || "暂无描述" }}</p>
            <div class="kb-meta">
              <span>📄 {{ kb.docCount }} 篇文档</span>
              <span>🕐 {{ formatDate(kb.createdAt) }}</span>
            </div>
          </div>
          <div class="kb-actions">
            <button class="btn-outline" @click="viewDocuments(kb.id)">
              查看文档
            </button>
            <button class="btn-primary" @click="openUploadModal(kb.id)">
              上传文档
            </button>
            <button class="btn-danger" @click="deleteKb(kb.id)">删除</button>
          </div>
        </div>
      </div>

      <!-- 文档列表弹窗 -->
      <div
        v-if="showDocModal"
        class="modal-overlay"
        @click.self="showDocModal = false"
      >
        <div class="modal" style="max-width: 700px">
          <h3>文档列表</h3>
          <div v-if="documents.length === 0" class="empty-state">
            <p>暂无文档</p>
          </div>
          <table v-else>
            <thead>
              <tr>
                <th>文件名</th>
                <th>类型</th>
                <th>大小</th>
                <th>分块数</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="doc in documents" :key="doc.id">
                <td>{{ doc.fileName }}</td>
                <td>{{ doc.fileType }}</td>
                <td>{{ formatSize(doc.fileSize) }}</td>
                <td>{{ doc.chunkCount }}</td>
                <td>
                  <span :class="['badge', 'badge-' + doc.status]">{{
                    statusText(doc.status)
                  }}</span>
                </td>
                <td>
                  <button
                    class="btn-danger"
                    style="padding: 4px 8px; font-size: 12px"
                    @click="deleteDoc(doc.id)"
                  >
                    删除
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="modal-actions">
            <button class="btn-outline" @click="showDocModal = false">
              关闭
            </button>
          </div>
        </div>
      </div>

      <!-- 创建知识库弹窗 -->
      <div
        v-if="showCreateModal"
        class="modal-overlay"
        @click.self="showCreateModal = false"
      >
        <div class="modal">
          <h3>新建知识库</h3>
          <div class="form-group">
            <label>知识库名称</label>
            <input v-model="newKbName" placeholder="请输入知识库名称" />
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea
              v-model="newKbDesc"
              placeholder="请输入知识库描述（可选）"
              rows="3"
            ></textarea>
          </div>
          <div class="modal-actions">
            <button class="btn-outline" @click="showCreateModal = false">
              取消
            </button>
            <button class="btn-primary" @click="createKb">创建</button>
          </div>
        </div>
      </div>

      <!-- 上传文档弹窗 -->
      <div
        v-if="showUploadModal"
        class="modal-overlay"
        @click.self="showUploadModal = false"
      >
        <div class="modal">
          <h3>上传文档</h3>
          <div class="upload-area">
            <input
              type="file"
              ref="fileInput"
              @change="handleFileSelect"
              accept=".txt,.md,.markdown"
            />
            <p class="upload-hint">支持 .txt, .md 格式文件</p>
          </div>
          <div v-if="selectedFile" class="selected-file">
            已选择: {{ selectedFile.name }}
          </div>
          <div class="modal-actions">
            <button class="btn-outline" @click="showUploadModal = false">
              取消
            </button>
            <button
              class="btn-primary"
              @click="uploadDocument"
              :disabled="!selectedFile || uploading"
            >
              {{ uploading ? "上传中..." : "上传" }}
            </button>
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
const nickname = ref(localStorage.getItem("nickname") || "");

const knowledgeBases = ref<any[]>([]);
const documents = ref<any[]>([]);
const showCreateModal = ref(false);
const showDocModal = ref(false);
const showUploadModal = ref(false);
const newKbName = ref("");
const newKbDesc = ref("");
const selectedFile = ref<File | null>(null);
const uploading = ref(false);
const currentKbId = ref<number | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);

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
  if (!newKbName.value.trim()) return;
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
      loadKnowledgeBases();
    }
  } catch (error) {
    console.error("Failed to create knowledge base:", error);
  }
};

const deleteKb = async (id: number) => {
  if (!confirm("确定要删除此知识库吗？")) return;
  try {
    await fetch(`/api/kb/${id}`, { method: "DELETE", headers: getHeaders() });
    loadKnowledgeBases();
  } catch (error) {
    console.error("Failed to delete knowledge base:", error);
  }
};

const viewDocuments = async (kbId: number) => {
  currentKbId.value = kbId;
  try {
    const res = await fetch(`/api/document/list/${kbId}`);
    const data = await res.json();
    if (data.code === 200) {
      documents.value = data.data || [];
    }
    showDocModal.value = true;
  } catch (error) {
    console.error("Failed to load documents:", error);
  }
};

const openUploadModal = (kbId: number) => {
  currentKbId.value = kbId;
  selectedFile.value = null;
  showUploadModal.value = true;
};

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    selectedFile.value = target.files[0];
  }
};

const uploadDocument = async () => {
  if (!selectedFile.value || !currentKbId.value) return;
  uploading.value = true;
  try {
    const formData = new FormData();
    formData.append("file", selectedFile.value);
    formData.append("kbId", String(currentKbId.value));

    const res = await fetch("/api/document/upload", {
      method: "POST",
      headers: getHeaders(),
      body: formData,
    });
    const data = await res.json();
    if (data.code === 200) {
      showUploadModal.value = false;
      loadKnowledgeBases();
    }
  } catch (error) {
    console.error("Failed to upload document:", error);
  } finally {
    uploading.value = false;
  }
};

const deleteDoc = async (id: number) => {
  if (!confirm("确定要删除此文档吗？")) return;
  try {
    await fetch(`/api/document/${id}`, { method: "DELETE" });
    if (currentKbId.value) viewDocuments(currentKbId.value);
    loadKnowledgeBases();
  } catch (error) {
    console.error("Failed to delete document:", error);
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

.kb-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.kb-info h3 {
  font-size: 16px;
  margin-bottom: 4px;
}

.kb-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.kb-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
}

.kb-actions {
  display: flex;
  gap: 8px;
}

.upload-area {
  padding: 20px;
  border: 2px dashed #e0e0e0;
  border-radius: 8px;
  text-align: center;
  margin-bottom: 12px;
}

.upload-hint {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.selected-file {
  font-size: 14px;
  color: #34a853;
  margin-bottom: 12px;
}
</style>
