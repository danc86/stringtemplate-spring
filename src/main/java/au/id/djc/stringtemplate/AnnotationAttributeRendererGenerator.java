package au.id.djc.stringtemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

/**
 * This bean will automatically populate your {@link ApplicationContext} with an
 * {@link AttributeRenderer} implementation for each method on any (singleton)
 * beans which is annotated with {@link AttributeRendererMethod}.
 */
public class AnnotationAttributeRendererGenerator extends ApplicationObjectSupport {
    
    private static final Logger LOG = Logger.getLogger(AnnotationAttributeRendererGenerator.class.getName());
    
    private static final class MethodWrapper {
        
        private final String format;
        private final String beanName;
        private final Method method;
        
        public MethodWrapper(String format, String beanName, Method method) {
            this.format = format;
            this.beanName = beanName;
            this.method = method;
        }
        
        @Override
        public String toString() {
            return "MethodWrapper[" + beanName + "," + method.getName() + "]";
        }
        
    }
    
    private static final class MethodWrappingAttributeRenderer implements AttributeRenderer {
        
        private final ApplicationContext applicationContext;
        private final Class<?> targetClass;
        private final Map<String, MethodWrapper> methodsByFormat = new HashMap<String, MethodWrapper>();
        
        public MethodWrappingAttributeRenderer(ApplicationContext applicationContext,
                Class<?> targetClass, List<MethodWrapper> methodWrappers) {
            this.applicationContext = applicationContext;
            this.targetClass = targetClass;
            for (MethodWrapper methodWrapper : methodWrappers) {
                methodsByFormat.put(methodWrapper.format, methodWrapper);
            }
            LOG.info("Registering generated AttributeRenderer targeting " + targetClass.getName() + " with methods " + methodsByFormat);
        }
        
        @Override
        public Class<?> getTargetClass() {
            return targetClass;
        }
        
        @Override
        public String toString(Object o) {
            return toString(o, "");
        }
        
        @Override
        public String toString(Object o, String formatName) {
            MethodWrapper methodWrapper = methodsByFormat.get(formatName);
            if (methodWrapper == null) {
                if (!formatName.isEmpty()) {
                    return toString(o, "");
                } else {
                    return o.toString();
                }
            }
            Object bean = applicationContext.getBean(methodWrapper.beanName);
            try {
                return (String) methodWrapper.method.invoke(bean, o);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        
    }
    
    private final MultiValueMap<Class<?>, MethodWrapper> methodsByTargetClass = new LinkedMultiValueMap<Class<?>, MethodWrapper>();
    
    @Override
    protected void initApplicationContext() throws BeansException {
        String[] beanNames = getApplicationContext().getBeanNamesForType(Object.class, false, true);
        for (final String beanName : beanNames) {
            Object bean = getApplicationContext().getBean(beanName);
            ReflectionUtils.doWithMethods(bean.getClass(), new ReflectionUtils.MethodCallback() {
                @Override
                public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                    AttributeRendererMethod annotation = AnnotationUtils.findAnnotation(method, AttributeRendererMethod.class);
                    if (annotation != null) {
                        if (method.getParameterTypes().length != 1) {
                            throw new IllegalArgumentException("AttributeRenderer method does not take exactly one argument: "
                                    + method.getName() + " of bean " + beanName);
                        }
                        if (!String.class.isAssignableFrom(method.getReturnType())) {
                            throw new IllegalArgumentException("AttributeRenderer method does not return String: "
                                    + method.getName() + " of bean " + beanName);
                        }
                        methodsByTargetClass.add(method.getParameterTypes()[0],
                                new MethodWrapper(annotation.format(), beanName, method));
                    }
                }
            });
        }
        registerAttributeRendererBeans();
    }
    
    private void registerAttributeRendererBeans() {
        int i = 1;
        for (Entry<Class<?>, List<MethodWrapper>> entry : methodsByTargetClass.entrySet()) {
            MethodWrappingAttributeRenderer attributeRenderer =
                new MethodWrappingAttributeRenderer(getApplicationContext(), entry.getKey(), entry.getValue());
            ((ConfigurableApplicationContext) getApplicationContext()).getBeanFactory().registerSingleton(
                    String.format("generatedAttributeRenderer#%d-targeting-%s", i, entry.getKey().getName()),
                    attributeRenderer);
            i ++;
        } 
    }

}
