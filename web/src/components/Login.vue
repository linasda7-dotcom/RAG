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
