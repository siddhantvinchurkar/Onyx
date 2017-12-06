importScripts('cache-polyfill.js');

self.addEventListener('install', function(e) {
 e.waitUntil(
   caches.open('onyx').then(function(cache) {
     return cache.addAll([
       '/',
       '/index.html'
     ]);
   })
 );
});
