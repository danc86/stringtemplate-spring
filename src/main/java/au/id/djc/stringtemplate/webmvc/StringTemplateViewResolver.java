package au.id.djc.stringtemplate.webmvc;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

import au.id.djc.stringtemplate.AttributeRenderer;

public class StringTemplateViewResolver extends AbstractTemplateViewResolver implements InitializingBean {
    
    private String rootTemplateName;
    private Charset charset;
    private StringTemplateErrorListener errorListener;
    private List<AttributeRenderer> attributeRenderers;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (errorListener == null) {
            throw new IllegalArgumentException("Property 'errorListener' is required");
        }
        if (attributeRenderers == null) {
            attributeRenderers = new ArrayList<AttributeRenderer>(
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), AttributeRenderer.class).values());
        }
    }
    
    public void setRootTemplateName(String rootTemplateName) {
        this.rootTemplateName = rootTemplateName;
    }
    
    public void setCharset(Charset charset) {
        this.charset = charset;
    }
    
    public void setErrorListener(StringTemplateErrorListener errorListener) {
        this.errorListener = errorListener;
    }
    
    /**
     * If this property is not set, the default behaviour is to use all
     * {@link AttributeRenderer} implementations in the current context.
     */
    public void setAttributeRenderers(List<AttributeRenderer> attributeRenderers) {
        this.attributeRenderers = attributeRenderers;
    }

    public StringTemplateViewResolver() {
        setViewClass(requiredViewClass());
    }

    @Override
    protected Class<?> requiredViewClass() {
        return StringTemplateView.class;
    }
    
    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        StringTemplateView view = (StringTemplateView) super.buildView(viewName);
        if (rootTemplateName != null) view.setRootTemplateName(rootTemplateName);
        if (charset != null) view.setCharset(charset);
        view.setErrorListener(errorListener);
        view.setAttributeRenderers(attributeRenderers);
        return view;
    }

}
