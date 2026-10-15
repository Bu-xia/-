<template>
  <router-view />
</template>

<style>
* { box-sizing: border-box; }
html, body, #app { height: 100%; margin: 0; }
body {
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
  background: #f1f5f9;
  -webkit-font-smoothing: antialiased;
}
.el-card { border-radius: 12px; border: none; box-shadow: 0 1px 3px rgba(15,23,42,.08); }
.el-button--primary { --el-button-bg-color: #6366f1; --el-button-border-color: #6366f1; --el-button-hover-bg-color: #4f46e5; --el-button-hover-border-color: #4f46e5; }
</style>
