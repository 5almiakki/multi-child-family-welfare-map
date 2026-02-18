<script setup>
import SearchInput from "@/components/views/home/SearchInput.vue";
import { ref, onMounted, onUnmounted } from "vue";

const { VITE_KAKAO_JAVASCRIPT_KEY } = import.meta.env
const mapContainer = ref(null);
let mapInstance = null;
let scriptLoaded = false;

const loadKakaoMapScript = () => {
  return new Promise((resolve, reject) => {
    if (scriptLoaded) {
      resolve();
      return;
    }
    const script = document.createElement("script");
    script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${VITE_KAKAO_JAVASCRIPT_KEY}&autoload=false`;
    script.onload = () => {
      scriptLoaded = true;
      resolve();
    };
    script.onerror = () => {
      reject(new Error("Kakao Map script failed to load"));
    };
    document.head.appendChild(script);
  });
};

const createMap = () => {
  if (!window.kakao || !mapContainer.value) {
    return;
  }
  const options = {
    center: new window.kakao.maps.LatLng(33.450701, 126.570667),
    level: 3
  };
  mapInstance = new window.kakao.maps.Map(mapContainer.value, options);
};

onMounted(() => {
  loadKakaoMapScript()
    .then(() => {
      window.kakao.maps.load(() => {
        createMap();
      });
    })
    .catch(error => {
      console.log(error);
    });
});

onUnmounted(() => {
  if (mapInstance) {
    mapInstance = null;
  }
});
</script>

<template>
  <div class="map-wrapper">
    <div ref="mapContainer" class="map-container"></div>
    <div class="search-overlay">
      <SearchInput />
    </div>
  </div>
</template>

<style scoped>
.map-wrapper {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.map-container {
  width: 100%;
  height: 100%;
}

.search-overlay {
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  z-index: 1000;
}
</style>
