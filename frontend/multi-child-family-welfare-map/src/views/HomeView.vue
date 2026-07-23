<script setup>
import SearchInput from "@/components/views/home/SearchInput.vue";
import SearchResult from "@/components/views/home/SearchResult.vue";
import { useSearchResultStore } from "@/stores/searchResult";
import { ref, onMounted, onUnmounted, watch } from "vue";

const searchResultStore = useSearchResultStore();

const { VITE_KAKAO_JAVASCRIPT_KEY } = import.meta.env;
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
    script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${VITE_KAKAO_JAVASCRIPT_KEY}&autoload=false&libraries=clusterer`;
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

let clusterer = null;

const createMap = () => {
  if (!window.kakao || !mapContainer.value) {
    return;
  }
  const options = {
    center: new window.kakao.maps.LatLng(33.450701, 126.570667),
    level: 3
  };
  mapInstance = new window.kakao.maps.Map(mapContainer.value, options);
  clusterer = new window.kakao.maps.MarkerClusterer({
    map: mapInstance,
    averageCenter: true,
    minLevel: 6
  });
};

const clearMarkers = () => {
  clusterer?.clear();
};

const renderMarkers = companies => {
  if (!mapInstance || !clusterer) {
    return;
  }
  clearMarkers();
  const markers = companies
    .filter(company => company.latitude != null && company.longitude != null)
    .map(company => new window.kakao.maps.Marker({
      position: new window.kakao.maps.LatLng(company.latitude, company.longitude)
    }));
  clusterer.addMarkers(markers);
};

watch(() => searchResultStore.searchResults, renderMarkers);

const isDrawerOpen = ref(false);

const handleSearch = ({ keyword, categories }) => {
  if (!mapInstance) {
    return;
  }
  const center = mapInstance.getCenter();
  isDrawerOpen.value = true;
  searchResultStore.search({
    name: keyword || undefined,
    categories: categories.length ? categories : undefined,
    latitude: center.getLat(),
    longitude: center.getLng()
  });
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
  clearMarkers();
  clusterer = null;
  if (mapInstance) {
    mapInstance = null;
  }
});
</script>

<template>
  <div class="map-wrapper">
    <div ref="mapContainer" class="map-container"></div>
    <SearchInput @on-search="handleSearch" />
    <SearchResult v-model="isDrawerOpen" />
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
</style>
