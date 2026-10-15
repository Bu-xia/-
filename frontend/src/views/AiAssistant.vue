<template>
  <el-card shadow="never" class="chat-card">
    <template #header>
      <div class="chat-head">
        <el-icon><ChatDotRound /></el-icon> <span>AI 助教（通义千问）</span>
        <div class="spacer"></div>
        <span class="status"><span class="dot"></span>在线</span>
        <el-tag size="small" effect="plain">上下文 {{ messages.length }} 条</el-tag>
        <el-button link type="danger" size="small" @click="clearContext">清空对话</el-button>
      </div>
    </template>
    <div class="messages" ref="msgBox">
      <div v-if="messages.length === 0" class="welcome">
        <div class="welcome-icon">🤖</div>
        <p>你好，我是 AI 助教，可以帮你解答课程、学习规划等问题。</p>
      </div>
      <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
        <div class="avatar">{{ m.role === 'user' ? '我' : 'AI' }}</div>
        <div class="bubble">{{ m.content }}</div>
      </div>
      <div v-if="streaming" class="msg assistant"><div class="avatar">AI</div><div class="bubble"><span class="cursor"></span></div></div>
    </div>
    <div class="input-row">
      <el-input v-model="input" type="textarea" :rows="2" resize="none" placeholder="输入你的学习问题…" @keydown.enter.exact.prevent="send" />
      <el-button type="primary" :loading="streaming" @click="send">
        <el-icon v-if="!streaming"><Promotion /></el-icon> 发送
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
import { nextTick, ref } from 'vue';
import { ElMessage } from 'element-plus';

const messages = ref([]);
const input = ref('');
const streaming = ref(false);
const msgBox = ref();

function clearContext() {
  if (streaming.value) return;
  messages.value = [];
  ElMessage.success('已开始新对话');
}

async function scroll() {
  await nextTick();
  if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight;
}

async function send() {
  // 发送消息并流式接收 AI 回复
  const text = input.value.trim();
  if (!text || streaming.value) return;
  messages.value.push({ role: 'user', content: text });
  input.value = '';
  streaming.value = true;
  scroll();

  const history = messages.value.map((m) => ({ role: m.role, content: m.content }));
  const token = localStorage.getItem('sp_token');
  const aiMsg = { role: 'assistant', content: '' };
  messages.value.push(aiMsg);

  try {
    // 用 fetch 读取 SSE 流，逐段拼接到 AI 气泡里实现打字机效果
    const resp = await fetch('/api/ai/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
      body: JSON.stringify({ messages: history })
    });
    if (!resp.ok || !resp.body) {
      const err = await resp.json().catch(() => ({ message: '请求失败' }));
      aiMsg.content = err.message || '请求失败';
      return;
    }
    const reader = resp.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split('\n');
      buffer = lines.pop();
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5).trim();
          if (data && data !== '[DONE]') {
            try {
              const j = JSON.parse(data);
              if (j.content) aiMsg.content += j.content;
            } catch (e) { /* 忽略非 JSON 行 */ }
          }
        }
      }
      scroll();
    }
    if (!aiMsg.content) aiMsg.content = '（无回复）';
  } catch (e) {
    aiMsg.content = '网络错误，请稍后再试。';
  } finally {
    streaming.value = false;
    scroll();
  }
}
</script>

<style scoped>
.chat-card { height: calc(100vh - 120px); display: flex; flex-direction: column; }
.chat-head { display: flex; align-items: center; gap: 10px; font-weight: 600; }
.spacer { flex: 1; }
.status { font-size: 12px; color: #22c55e; display: flex; align-items: center; gap: 5px; font-weight: 400; }
.dot { width: 7px; height: 7px; border-radius: 50%; background: #22c55e; }
.messages { flex: 1; overflow-y: auto; padding: 12px 4px; display: flex; flex-direction: column; gap: 14px; }
.welcome { text-align: center; color: #94a3b8; padding: 40px 20px; }
.welcome-icon { font-size: 40px; }
.msg { display: flex; gap: 10px; }
.msg.user { flex-direction: row-reverse; }
.avatar { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 13px; flex-shrink: 0; }
.msg.assistant .avatar { background: linear-gradient(135deg,#6366f1,#8b5cf6); }
.msg.user .avatar { background: #334155; }
.bubble { max-width: 74%; padding: 10px 14px; border-radius: 10px; white-space: pre-wrap; line-height: 1.65; font-size: 14px; }
.msg.user .bubble { background: #6366f1; color: #fff; border-top-right-radius: 2px; }
.msg.assistant .bubble { background: #eef2ff; color: #1e293b; border-top-left-radius: 2px; }
.cursor { display: inline-block; width: 8px; height: 16px; background: #6366f1; animation: blink 1s infinite; vertical-align: middle; }
@keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: 0; } }
.input-row { display: flex; gap: 10px; margin-top: 12px; align-items: flex-end; }
</style>
