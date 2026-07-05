<template>
  <div class="dashboard-container">
    <h1>企业知识库系统</h1>

    <div class="upload-section">
      <h2>上传知识库文档</h2>
      <input type="file" @change="handleFileUpload" />
      <button @click="uploadDocument" :disabled="!selectedFile">上传</button>
      <p v-if="uploadStatus" class="status">{{ uploadStatus }}</p>
    </div>

    <div class="question-section">
      <h2>提问</h2>
      <input type="text" v-model="question" placeholder="请输入您的问题..." />
      <button @click="askQuestion" :disabled="!question">提问</button>
      <div v-if="answer" class="answer">
        <h3>回答：</h3>
        <p>{{ answer }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";

const selectedFile = ref(null);
const uploadStatus = ref("");
const question = ref("");
const answer = ref("");

const handleFileUpload = (event) => {
  selectedFile.value = event.target.files[0];
  uploadStatus.value = "";
};

const uploadDocument = async () => {
  if (!selectedFile.value) return;

  const formData = new FormData();
  formData.append("file", selectedFile.value);

  try {
    const response = await fetch("/api/kb/upload", {
      method: "POST",
      body: formData,
    });

    if (response.ok) {
      uploadStatus.value = "文档上传成功";
      selectedFile.value = null;
    } else {
      uploadStatus.value = "上传失败";
    }
  } catch (error) {
    console.error("Upload error:", error);
    uploadStatus.value = "网络错误";
  }
};

const askQuestion = async () => {
  if (!question.value) return;

  try {
    const response = await fetch("/api/kb/ask", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: `question=${encodeURIComponent(question.value)}`,
    });

    if (response.ok) {
      answer.value = await response.text();
    } else {
      answer.value = "获取回答失败";
    }
  } catch (error) {
    console.error("Question error:", error);
    answer.value = "网络错误";
  }
};
</script>

<style scoped>
.dashboard-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.upload-section,
.question-section {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 4px;
}

input[type="file"] {
  margin-bottom: 10px;
}

button {
  background: #4285f4;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
}

button:disabled {
  background: #cccccc;
}

.status {
  color: green;
  margin-top: 5px;
}

.answer {
  margin-top: 15px;
  padding: 10px;
  background: #f0f0f0;
  border-radius: 4px;
}
</style>
