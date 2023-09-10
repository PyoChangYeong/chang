const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
    // 백엔드 서버의 주소로 변경할 수 있음
    const backendUrl = 'http://localhost:8080';

    // /api로 시작하는 요청을 프록시하지 않음
    app.use(
        '/api',
        createProxyMiddleware({
            target: backendUrl,
            changeOrigin: true,
            // 프론트엔드에서 /api로 시작하는 요청을 프록시하지 않음
            pathRewrite: {
                '^/api': '/',
            },
        })
    );

    // 나머지 모든 요청을 프록시
    app.use(
        createProxyMiddleware({
            target: backendUrl,
            changeOrigin: true,
        })
    );
};

