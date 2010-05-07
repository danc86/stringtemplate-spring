package au.id.djc.stringtemplate.webmvc;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.springframework.web.servlet.view.AbstractTemplateView;

import au.id.djc.stringtemplate.AttributeRenderer;

public class StringTemplateView extends AbstractTemplateView {

    private String rootTemplateName = "root";
    private Charset charset = Charset.defaultCharset();
    private StringTemplateErrorListener errorListener;
    private List<AttributeRenderer> attributeRenderers;
    
    public void setRootTemplateName(String rootTemplateName) {
        this.rootTemplateName = rootTemplateName;
    }
    
    public void setCharset(Charset charset) {
        this.charset = charset;
    }
    
    public void setErrorListener(StringTemplateErrorListener errorListener) {
        this.errorListener = errorListener;
    }
    
    public void setAttributeRenderers(List<AttributeRenderer> attributeRenderers) {
        this.attributeRenderers = attributeRenderers;
    }

    @Override
    public boolean checkResource(Locale locale) throws Exception {
        return getClass().getResourceAsStream(getUrl()) != null;
    }

    @Override
    protected void renderMergedTemplateModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        InputStream groupStream = getClass().getResourceAsStream(getUrl());
        StringTemplateGroup templateGroup = new StringTemplateGroup(new InputStreamReader(groupStream, charset),
                DefaultTemplateLexer.class, errorListener);
        for (AttributeRenderer attributeRenderer: attributeRenderers) {
            templateGroup.registerRenderer(attributeRenderer.getTargetClass(), attributeRenderer);
        }
        StringTemplate template = templateGroup.getInstanceOf(rootTemplateName);
        template.setAttributes(model);
        PrintWriter writer = response.getWriter();
        writer.print(template);
    }

}
