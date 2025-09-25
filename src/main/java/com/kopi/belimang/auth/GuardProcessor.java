package com.kopi.belimang.auth;


import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GuardProcessor implements BeanPostProcessor, BeanFactoryAware {
    // Initially we don't need BeanFactory, but we are registering beans to a singleton object. Which is problematic:
    // to register a bean, we need the singleton object already registered as a bean first. How can we ensure that?
    // The answer is by implementing `BeanFactoryAware`.
    private BeanFactory beanFactory;
    private BeanRegistry beanRegistry;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Bean can be wrapped in a proxy. This function unwraps it to the original bean
        Class<?> targetClass = AopUtils.getTargetClass(bean);

        if ( AnnotationUtils.findAnnotation(targetClass, Controller.class) != null ) {
            RequestMapping controllerLevelAnnotation = AnnotatedElementUtils.findMergedAnnotation(targetClass, RequestMapping.class);
            String[] controllerUrls = Optional.ofNullable(controllerLevelAnnotation)
                    .map(v -> v.value())
                    .orElse(new String[]{""});

            ReflectionUtils.doWithMethods(targetClass, method -> {
                // Parse through the annotations
                Guard annotation = AnnotationUtils.findAnnotation(method, Guard.class);
                if ( annotation != null ) {
                    RequestMapping methodLevelAnnotation = AnnotatedElementUtils.findMergedAnnotation(targetClass, RequestMapping.class);
                    String[] methodUrls = Optional.ofNullable(methodLevelAnnotation)
                            .map(v -> v.value())
                            .orElse(new String[]{""});

                    for (String controllerUrl : controllerUrls) {
                        for (String methodUrl : methodUrls) {
                            String url = "" + controllerUrl + methodUrl;
                            if ( !url.isEmpty() ) {
                                getBeanRegistry().put(url, annotation);
                            }
                        }
                    }
                }
            });
        }

        return bean;
    }

    // BeanRegistry retrieval is done "manually" like so, instead of e.g. using `@Autowired` annotation. In simple
    // terms getting BeanRegistry is done lazily. This method works because the processor no longer require in
    // compile-time that BeanRegistry already instantiated (which spring does not guarantee). Lazy loading is the way.
    private BeanRegistry getBeanRegistry() {
        if ( beanRegistry == null ) {
            beanRegistry = beanFactory.getBean(BeanRegistry.class);
        }

        return beanRegistry;
    }

    // Need to implement this by `BeanFactoryAware`
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
