import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// The app is served by Spring under /adminapp/, so assets must resolve there.
// In dev (`npm run dev`), /admin/* API calls are proxied to the running backend.
export default defineConfig({
  base: "/adminapp/",
  plugins: [react()],
  build: {
    outDir: "dist",
    emptyOutDir: true,
  },
  server: {
    proxy: {
      "/admin": "http://localhost:8080",
    },
  },
});
