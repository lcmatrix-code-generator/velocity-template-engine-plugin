package top.lcmatrix.util.codegenerator.plugin.velocity;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import top.lcmatrix.util.codegenerator.common.plugin.AbstractTemplateEnginePlugin;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class VelocityPlugin extends AbstractTemplateEnginePlugin {

    public VelocityPlugin() {
        Velocity.init();
    }

    @Override
    public String apply(String s, Object model) {
        StringWriter sw = new StringWriter();
        VelocityContext velocityContext = model2Context(model);
        Velocity.evaluate(velocityContext, sw, "velocity plugin", s);
        return sw.toString();
    }

    @Override
    public byte[] apply(File templateFile, Object model) {
        StringWriter sw = new StringWriter();
        try {
            Velocity.evaluate(model2Context(model), sw, "velocity plugin", new FileReader(templateFile));
        } catch (FileNotFoundException e) {
            getLogger().error("template file not found");
            throw new RuntimeException("template file not found", e);
        }
        try {
            return sw.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            getLogger().error("apply template error", e);
            throw new RuntimeException(e);
        }
    }

    private VelocityContext model2Context(Object model){
        VelocityContext velocityContext = new VelocityContext();
        Class<?> aClass = model.getClass();
        Method[] methods = aClass.getMethods();
        Object[] invokeParams = new Object[0];
        for (Method method : methods) {
            String name = method.getName();
            int parameterCount = method.getParameterCount();
            if(name.startsWith("get") && parameterCount == 0){
                try {
                    velocityContext.put(StringUtils.uncapitalize(name.substring(3)),
                            method.invoke(model, invokeParams));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    getLogger().warn("model trans to context error", e);
                    throw new RuntimeException("model trans to context error", e);
                }
            }
        }
        return velocityContext;
    }

}
