package br.com.jobinder.chatservice.infra.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    // TODO: JWT validation
    // For now, just extract userId from the query parameters
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // The URL is expected to be like: ws://host:port/chat?userId={userId}
        String query = request.getURI().getQuery();
        if (query != null && query.startsWith("userId=")) {
            String userId = query.substring(7);
            attributes.put("userId", userId); // Store userId for later use
            return true;
        }
        return false; // Reject the handshake if userId is not present
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}