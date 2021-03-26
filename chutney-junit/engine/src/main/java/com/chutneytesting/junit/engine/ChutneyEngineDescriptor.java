package com.chutneytesting.junit.engine;

import static java.util.Optional.ofNullable;

import com.chutneytesting.junit.api.AfterAll;
import com.chutneytesting.junit.api.BeforeAll;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

public class ChutneyEngineDescriptor extends EngineDescriptor implements Node<ChutneyEngineExecutionContext> {

    private final Optional<Object> chutneyClass;

    public ChutneyEngineDescriptor(UniqueId uniqueId, String displayName, Object chutneyClass) {
        super(uniqueId, displayName);
        this.chutneyClass = ofNullable(chutneyClass);
    }

    @Override
    public ChutneyEngineExecutionContext before(ChutneyEngineExecutionContext context) throws Exception {
        invokeHook(BeforeAll.class);
        return context;
    }

    @Override
    public void after(ChutneyEngineExecutionContext context) throws Exception {
        invokeHook(AfterAll.class);
    }

    private void invokeHook(Class<? extends Annotation> annotationType) throws ReflectiveOperationException {
        if (!this.children.isEmpty() && chutneyClass.isPresent()) {
            Object chutneyClassInstance = chutneyClass.get();
            Optional<Method> annotatedMethod = AnnotationSupport
                .findAnnotatedMethods(chutneyClassInstance.getClass(), annotationType, HierarchyTraversalMode.TOP_DOWN)
                .stream().findFirst();

            if (annotatedMethod.isPresent()) {
                Method method = annotatedMethod.get();
                method.invoke(chutneyClassInstance);
            }
        }
    }
}
