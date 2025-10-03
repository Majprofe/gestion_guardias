<template>
    <header>
    <Header />
  </header>
  <main>
    <router-view />
  </main>
</template>

<script setup>
import Header from "@/components/header.vue";
import Footer from "@/components/footer.vue";
</script>

<style>
  html,body{
    margin: 0px;
    padding: 0px;
    box-sizing: border-box;
    font-family: 'Poppins', sans-serif;
    background-color: #faf6fa;
  }
  h1 {
    color: #2c3e50;
    font-weight: 600;
    font-size: 1.5em;
    text-align: center;
    margin-bottom: 10px;
}
</style>
