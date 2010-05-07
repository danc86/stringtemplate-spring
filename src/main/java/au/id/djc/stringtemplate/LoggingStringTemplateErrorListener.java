package au.id.djc.stringtemplate;

import java.util.logging.Logger;

import org.antlr.stringtemplate.StringTemplateErrorListener;

public class LoggingStringTemplateErrorListener implements StringTemplateErrorListener {
    
    private static final Logger LOG = Logger.getLogger(LoggingStringTemplateErrorListener.class.getName());

    @Override
    public void error(String msg, Throwable e) {
        throw new RuntimeException(e);
    }

    @Override
    public void warning(String msg) {
        LOG.warning(msg);
    }

}
