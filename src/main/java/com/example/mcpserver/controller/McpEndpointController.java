package com.example.mcpserver.controller;

import com.example.mcpserver.model.*;
import com.example.mcpserver.service.McpProtocolService;
import com.example.mcpserver.service.SessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Main MCP endpoint controller implementing Streamable HTTP transport.
 * 
 * This controller handles both POST and GET requests to the MCP endpoint
 * according to the MCP Streamable HTTP transport specification.
 */
@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = "*")
public class McpEndpointController {
    
    private static final Logger logger = LoggerFactory.getLogger(McpEndpointController.class);
    private static final String MCP_SESSION_HEADER = "Mcp-Session-Id";
    private static final String LAST_EVENT_ID_HEADER = "Last-Event-ID";
    
    private final McpProtocolService protocolService;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;
    private final AtomicLong eventIdCounter = new AtomicLong(0);
    
    // Store active SSE emitters for each session
    private final Map<String, SseEmitter> sessionEmitters = new ConcurrentHashMap<>();
    
    @Autowired
    public McpEndpointController(McpProtocolService protocolService, 
                               SessionService sessionService,
                               ObjectMapper objectMapper) {
        this.protocolService = protocolService;
        this.sessionService = sessionService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Handles POST requests to the MCP endpoint.
     * 
     * This endpoint accepts JSON-RPC messages and returns appropriate responses.
     * For streaming responses, clients should use a separate GET request.
     */
    @PostMapping
    public ResponseEntity<?> handlePost(@RequestBody Object body,
                                       @RequestHeader(value = "Accept", defaultValue = "") String accept,
                                       @RequestHeader(value = MCP_SESSION_HEADER, required = false) String sessionId,
                                       HttpServletRequest request) {
        
        logger.info("Received POST request to MCP endpoint");
        logger.info("Accept header: {}", accept);
        logger.info("Session ID: {}", sessionId);
        logger.info("Request body type: {}", body.getClass().getSimpleName());
        
        // Validate Origin header for security
        if (!validateOrigin(request)) {
            logger.warn("Invalid Origin header from: {}", request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Get or create session
        McpSession session = getOrCreateSession(sessionId);
        
        try {
            // Parse the request body
            logger.debug("Received request body: {}", objectMapper.writeValueAsString(body));
            List<JsonRpcMessage> messages = parseMessages(body);
            
            // Check if all messages are notifications/responses
            boolean allNotificationsOrResponses = messages.stream()
                .allMatch(msg -> msg.isNotification() || msg instanceof JsonRpcResponse);
            
            if (allNotificationsOrResponses) {
                // Process notifications/responses and return 202 Accepted
                processNotificationsAndResponses(messages, session);
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .header(MCP_SESSION_HEADER, session.getSessionId())
                    .build();
            }
            
            // For requests, return a direct JSON response
            // If client wants streaming, they should use GET endpoint
                return createJsonResponse(messages, session);
            
        } catch (Exception e) {
            logger.error("Error processing POST request: {}", e.getMessage(), e);
            JsonRpcResponse errorResponse = createErrorResponse(null, -32700, "Parse error: " + e.getMessage());
            return ResponseEntity.badRequest()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(errorResponse);
        }
    }
    
    /**
     * Handles GET requests to the MCP endpoint.
     * 
     * This endpoint creates an SSE stream for server-to-client communication.
     */
    @GetMapping
    public ResponseEntity<SseEmitter> handleGet(@RequestHeader(value = "Accept", defaultValue = "") String accept,
                                              @RequestHeader(value = MCP_SESSION_HEADER, required = false) String sessionId,
                                              @RequestHeader(value = LAST_EVENT_ID_HEADER, required = false) String lastEventId,
                                              HttpServletRequest request) {
        
        logger.info("Received GET request to MCP endpoint");
        logger.info("Accept header: {}", accept);
        logger.info("Session ID: {}", sessionId);
        logger.info("Last Event ID: {}", lastEventId);
        
        // Validate Origin header for security
        if (!validateOrigin(request)) {
            logger.warn("Invalid Origin header from: {}", request.getRemoteAddr());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Origin header");
        }
        
        // Check if client accepts SSE
        if (!accept.contains("text/event-stream")) {
            logger.warn("Client does not accept SSE: {}", accept);
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Client must accept text/event-stream");
        }
        
        try {
            // Get session
            McpSession session = getOrCreateSession(sessionId);
            logger.info("Using session: {}", session.getSessionId());
            
            // Create SSE stream
            SseEmitter emitter = createSseEmitter(session.getSessionId(), lastEventId);
            
            // Store the emitter for this session
            sessionEmitters.put(session.getSessionId(), emitter);
            
            logger.info("Created SSE emitter for session: {}", session.getSessionId());
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/event-stream")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header(HttpHeaders.CONNECTION, "keep-alive")
                .header(MCP_SESSION_HEADER, session.getSessionId())
                .body(emitter);
                
        } catch (Exception e) {
            logger.error("Error creating SSE stream: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create SSE stream");
        }
    }
    
    /**
     * Handles DELETE requests to terminate a session.
     */
    @DeleteMapping
    public ResponseEntity<?> handleDelete(@RequestHeader(value = MCP_SESSION_HEADER, required = false) String sessionId,
                                         HttpServletRequest request) {
        
        // Validate Origin header for security
        if (!validateOrigin(request)) {
            logger.warn("Invalid Origin header from: {}", request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (sessionId != null && sessionService.isValidSessionId(sessionId)) {
            // Terminate the session
            sessionService.terminateSession(sessionId);
            
            // Close any active SSE emitter
            SseEmitter emitter = sessionEmitters.remove(sessionId);
            if (emitter != null) {
                emitter.complete();
            }
            
            logger.info("Session terminated: {}", sessionId);
            return ResponseEntity.ok().build();
        }
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
    
    /**
     * Validates the Origin header to prevent DNS rebinding attacks.
     */
    private boolean validateOrigin(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        String host = request.getHeader("Host");
        
        // Allow requests without Origin header (e.g., from localhost)
        if (origin == null) {
            return true;
        }
        
        // For localhost development, allow localhost origins
        if (host != null && host.contains("localhost")) {
            return origin.contains("localhost") || origin.contains("127.0.0.1");
        }
        
        // In production, implement proper origin validation
        // For now, allow all origins (should be restricted in production)
        return true;
    }
    
    /**
     * Gets or creates a session based on the session ID.
     */
    private McpSession getOrCreateSession(String sessionId) {
        if (sessionId != null && sessionService.isValidSessionId(sessionId)) {
            return sessionService.getSession(sessionId)
                .orElseGet(() -> {
                    logger.info("Creating new session for existing ID: {}", sessionId);
                    return sessionService.createSession();
                });
        } else {
            logger.info("Creating new session");
            return sessionService.createSession();
        }
    }
    
    /**
     * Parses the request body into JSON-RPC messages.
     */
    private List<JsonRpcMessage> parseMessages(Object body) throws JsonProcessingException {
        if (body instanceof List) {
            List<JsonRpcMessage> messages = new ArrayList<>();
            for (Object item : (List<?>) body) {
                messages.addAll(parseMessages(item));
            }
            return messages;
        } else {
            // Convert to string first, then parse to ensure proper type detection
            String jsonString = objectMapper.writeValueAsString(body);
            logger.debug("Parsing JSON string: {}", jsonString);
            
            // Parse as a generic Map first to determine the message type
            Map<String, Object> jsonMap = objectMapper.readValue(jsonString, Map.class);
            
            // Check if it has an 'id' field to determine if it's a request/response or notification
            boolean hasId = jsonMap.containsKey("id") && jsonMap.get("id") != null;
            boolean hasMethod = jsonMap.containsKey("method");
            boolean hasResult = jsonMap.containsKey("result");
            boolean hasError = jsonMap.containsKey("error");
            
            try {
                if (hasMethod && hasId) {
                    // It's a request
                    JsonRpcRequest request = objectMapper.readValue(jsonString, JsonRpcRequest.class);
                    return List.of(request);
                } else if (hasMethod && !hasId) {
                    // It's a notification
                    JsonRpcNotification notification = objectMapper.readValue(jsonString, JsonRpcNotification.class);
                    return List.of(notification);
                } else if ((hasResult || hasError) && hasId) {
                    // It's a response
                    JsonRpcResponse response = objectMapper.readValue(jsonString, JsonRpcResponse.class);
                    return List.of(response);
                } else {
                    throw new JsonProcessingException("Unable to determine JSON-RPC message type") {};
                }
            } catch (Exception e) {
                logger.error("Failed to parse JSON-RPC message: {}", e.getMessage());
                throw new JsonProcessingException("Failed to parse JSON-RPC message: " + e.getMessage()) {};
            }
        }
    }
    
    /**
     * Processes notifications and responses.
     */
    private void processNotificationsAndResponses(List<JsonRpcMessage> messages, McpSession session) {
        for (JsonRpcMessage message : messages) {
            if (message instanceof JsonRpcNotification) {
                protocolService.processNotification((JsonRpcNotification) message, session);
            }
            // Responses are typically not processed on the server side
        }
    }
    

    
    /**
     * Creates a JSON response for requests.
     */
    private ResponseEntity<?> createJsonResponse(List<JsonRpcMessage> messages, McpSession session) {
        if (messages.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse(null, -32600, "Invalid Request"));
        }
        
        // Process all requests and return appropriate response
        if (messages.size() == 1) {
            // Single message - return as single response
            JsonRpcMessage message = messages.get(0);
                        if (message instanceof JsonRpcRequest) {
                JsonRpcRequest request = (JsonRpcRequest) message;
                JsonRpcResponse response = protocolService.processRequest(request, session);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(MCP_SESSION_HEADER, session.getSessionId())
                    .body(response);
            } else {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse(null, -32600, "Invalid Request"));
            }
        } else {
            // Multiple messages - return as array of responses
            List<JsonRpcResponse> responses = new ArrayList<>();
                    for (JsonRpcMessage message : messages) {
                        if (message instanceof JsonRpcRequest) {
                    JsonRpcRequest request = (JsonRpcRequest) message;
                    JsonRpcResponse response = protocolService.processRequest(request, session);
                    responses.add(response);
                        } else if (message instanceof JsonRpcNotification) {
                    // Process notification but don't include in response
                            protocolService.processNotification((JsonRpcNotification) message, session);
                        }
                    }
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(MCP_SESSION_HEADER, session.getSessionId())
                .body(responses);
        }
    }
    
    /**
     * Creates an SSE emitter with heartbeat and tools list.
     */
    private SseEmitter createSseEmitter(String sessionId, String lastEventId) {
        SseEmitter emitter = new SseEmitter(30000L); // 30 second timeout
        
        emitter.onCompletion(() -> {
            logger.debug("SSE stream completed for session: {}", sessionId);
            sessionEmitters.remove(sessionId);
        });
        
        emitter.onTimeout(() -> {
            logger.debug("SSE stream timeout for session: {}", sessionId);
            sessionEmitters.remove(sessionId);
        });
        
        emitter.onError((ex) -> {
            logger.error("SSE stream error for session {}: {}", sessionId, ex.getMessage());
            sessionEmitters.remove(sessionId);
        });
        
        // Send initial heartbeat to establish connection
        try {
            SseEmitter.SseEventBuilder heartbeat = SseEmitter.event()
                .id("heartbeat")
                .data("connected")
                .name("heartbeat");
            emitter.send(heartbeat);
            logger.debug("Sent heartbeat to session: {}", sessionId);
            
            // Don't send tools list through SSE - let Cursor request it directly
            logger.debug("SSE connection established for session: {}", sessionId);
            
        } catch (IOException e) {
            logger.error("Error sending initial SSE messages: {}", e.getMessage());
        }
        
        // TODO: Handle resumability with lastEventId
        if (lastEventId != null) {
            logger.debug("Resuming SSE stream from event ID: {} for session: {}", lastEventId, sessionId);
        }
        
        return emitter;
    }
    
    /**
     * Sends a message through the SSE stream.
     */
    private void sendSseMessage(SseEmitter emitter, JsonRpcMessage message, String sessionId) {
        try {
            String eventId = String.valueOf(eventIdCounter.incrementAndGet());
            String data = objectMapper.writeValueAsString(message);
            
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                .id(eventId)
                .data(data);
            
            emitter.send(event);
            logger.debug("Sent SSE message with ID {} to session {}", eventId, sessionId);
            
        } catch (IOException e) {
            logger.error("Error sending SSE message: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Creates an error response.
     */
    private JsonRpcResponse createErrorResponse(Object id, int code, String message) {
        JsonRpcResponse.JsonRpcError error = new JsonRpcResponse.JsonRpcError(code, message);
        return new JsonRpcResponse(id, error);
    }
} 