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

const handleFileRemove = (file: UploadFile, uploadFileList: UploadFile[]) => {
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
