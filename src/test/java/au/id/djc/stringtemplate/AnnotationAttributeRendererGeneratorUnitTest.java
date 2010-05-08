package au.id.djc.stringtemplate;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;

public class AnnotationAttributeRendererGeneratorUnitTest {
    
    private StaticApplicationContext context;
    
    @Before
    public void setUp() {
        context = new StaticApplicationContext();
        context.registerSingleton("dummyBean", Object.class);
    }
    
    @Test
    public void shouldRegisterNothingForNoAnnotatedBeans() {
        context.registerSingleton("generator", AnnotationAttributeRendererGenerator.class);
        context.refresh();
        assertThat(context.getBeansOfType(AttributeRenderer.class).size(), equalTo(0));
    }
    
    public static class Annotated {
        @AttributeRendererMethod
        public String render(String s) { return s; }
    }
    
    @Test
    public void shouldRegisterGeneratedBean() {
        context.registerSingleton("annotated", Annotated.class);
        context.registerSingleton("generator", AnnotationAttributeRendererGenerator.class);
        context.refresh();
        assertThat(context.getBeansOfType(AttributeRenderer.class).size(), equalTo(1));
    }

}
