<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h1>📚 企业知识库智能问答系统</h1>
        <p>基于大模型的知识管理与智能问答平台</p>
      </div>

      <div class="login-tabs">
        <button :class="{ active: mode === 'login' }" @click="mode = 'login'">
          登录
        </button>
        <button
          :class="{ active: mode === 'register' }"
          @click="mode = 'register'"
        >
          注册
        </button>
      </div>

      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>用户名</label>
          <input
            type="text"
            v-model="username"
            placeholder="请输入用户名"
            required
          />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input
            type="password"
            v-model="password"
            placeholder="请输入密码"
            required
          />
        </div>
        <div v-if="mode === 'register'" class="form-group">
          <label>昵称</label>
          <input
            type="text"
            v-model="nickname"
            placeholder="请输入昵称（可选）"
          />
        </div>
        <button type="submit" class="btn-primary login-btn" :disabled="loading">
          {{ loading ? "处理中..." : mode === "login" ? "登 录" : "注 册" }}
        </button>
      </form>

      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

      <div class="login-footer">
        <p>默认账号: admin / admin123</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";

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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-header h1 {
  font-size: 22px;
  margin-bottom: 8px;
  color: #333;
}

.login-header p {
  color: #666;
  font-size: 14px;
}

.login-tabs {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 2px solid #eee;
}

.login-tabs button {
  flex: 1;
  padding: 10px;
  background: none;
  color: #666;
  font-size: 15px;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  border-radius: 0;
}

.login-tabs button.active {
  color: #4285f4;
  border-bottom-color: #4285f4;
  font-weight: 600;
}

.login-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  margin-top: 8px;
}

.error-msg {
  color: #ea4335;
  text-align: center;
  margin-top: 12px;
  font-size: 14px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #999;
  font-size: 12px;
}
</style>
