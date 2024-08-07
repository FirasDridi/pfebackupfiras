//package org.example.interceptors;
//
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.example.annotations.Payant;
//import org.example.services.KafkaProducerService;
//import org.example.services.LogService;
//import org.example.services.RestClientService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.lang.reflect.Method;
//import java.lang.reflect.Parameter;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//@Aspect
//@Component
//public class PayantInterceptor {
//
//    private static final Logger logger = LoggerFactory.getLogger(PayantInterceptor.class);
//
//    @Autowired
//    private RestClientService restClientService;
//
//    @Autowired
//    private KafkaProducerService kafkaProducerService;
//
//    @Autowired
//    private LogService logService;
//
//    @Before("@annotation(payant)")
//    public void beforePayantMethodCall(JoinPoint joinPoint, Payant payant) throws NoSuchMethodException {
//        Long userId = extractValueFromJoinPoint(joinPoint, Long.class, "userId");
//        Long groupId = extractValueFromJoinPoint(joinPoint, Long.class, "groupId");
//        UUID serviceId = extractValueFromJoinPoint(joinPoint, UUID.class, "serviceId");
//
//        String logMessage = String.format("PayantInterceptor - Method called by user for client: %d and service: %s",
//                groupId, serviceId);
//        logService.addLog(logMessage);
//
//        try {
//            if (!restClientService.hasValidToken(groupId)) {
//                logMessage = "PayantInterceptor - Payment required for client ID: " + groupId;
//                logService.addLog(logMessage);
//                throw new AccessDeniedException("Payment required to access this resource.");
//            }
//
//            if (!restClientService.isServiceAssociatedWithGroup(groupId, serviceId.toString())) {
//                logMessage = "PayantInterceptor - Access denied for client ID: " + groupId + " to service ID: " + serviceId;
//                logService.addLog(logMessage);
//                throw new AccessDeniedException("Client does not have access to this service.");
//            }
//
//            if (!restClientService.isUserInGroup(userId, groupId)) {
//                logMessage = "PayantInterceptor - User: " + userId + " does not belong to client: " + groupId;
//                logService.addLog(logMessage);
//                throw new AccessDeniedException("User does not belong to this client.");
//            }
//
//            Map<String, Object> accessDetails = new HashMap<>();
//            accessDetails.put("userId", userId);
//            accessDetails.put("clientId", groupId);
//            accessDetails.put("serviceId", serviceId);
//            accessDetails.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//
//            kafkaProducerService.sendAccessLog(accessDetails);
//            logMessage = String.format("PayantInterceptor - Access log sent to Kafka: %s", accessDetails);
//            logService.addLog(logMessage);
//            logger.debug("PayantInterceptor - Access log sent to Kafka: {}", accessDetails);
//
//        } catch (Exception e) {
//            logService.addLog("PayantInterceptor - Exception: " + e.getMessage());
//            throw e;
//        }
//    }
//
//    private <T> T extractValueFromJoinPoint(JoinPoint joinPoint, Class<T> valueType, String parameterName) throws NoSuchMethodException {
//        Method method = getMethodFromJoinPoint(joinPoint);
//        Parameter[] parameters = method.getParameters();
//        Object[] args = joinPoint.getArgs();
//
//        for (int i = 0; i < parameters.length; i++) {
//            if (parameters[i].getName().equals(parameterName) && valueType.isInstance(args[i])) {
//                return valueType.cast(args[i]);
//            }
//        }
//        throw new IllegalArgumentException("Parameter with name " + parameterName + " not found in method " + method.getName());
//    }
//
//    private Method getMethodFromJoinPoint(JoinPoint joinPoint) throws NoSuchMethodException {
//        String methodName = joinPoint.getSignature().getName();
//        Class<?>[] parameterTypes = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getParameterTypes();
//        return joinPoint.getTarget().getClass().getMethod(methodName, parameterTypes);
//    }
//
//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
//        logger.error("PayantInterceptor - Access denied: {}", ex.getMessage());
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
//    }
//
//    public static class AccessDeniedException extends RuntimeException {
//        public AccessDeniedException(String message) {
//            super(message);
//        }
//    }
//}
