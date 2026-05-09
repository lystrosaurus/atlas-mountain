package io.github.lystrosaurus.atlasmountain.infra.redis;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DistributedLockAspect {

    private final DistributedLockService distributedLockService;
    private final DistributedLockKeyResolver keyResolver;

    public DistributedLockAspect(DistributedLockService distributedLockService, DistributedLockKeyResolver keyResolver) {
        this.distributedLockService = distributedLockService;
        this.keyResolver = keyResolver;
    }

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        StandardEvaluationContext context = new StandardEvaluationContext();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        String key = keyResolver.resolve(distributedLock.key(), context);
        return distributedLockService.execute(key, distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit(), () -> {
            try {
                return joinPoint.proceed();
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }
}
