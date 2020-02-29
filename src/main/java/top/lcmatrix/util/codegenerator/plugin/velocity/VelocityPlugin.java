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
    public byte[] apply(String s, Object model) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream);
        VelocityContext velocityContext = model2Context(model);
        for (Object key : velocityContext.getKeys()) {
            System.out.println(key + "=" + velocityContext.get(key.toString()));
        }
        Velocity.evaluate(velocityContext, outputStreamWriter, "velocity plugin", s);
        try {
            outputStreamWriter.flush();
        } catch (IOException e) {
            getLogger().error("apply template error", e);
            return null;
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public byte[] apply(File templateFile, Object model) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream);
        try {
            Velocity.evaluate(model2Context(model), outputStreamWriter, "velocity plugin", new FileReader(templateFile));
        } catch (FileNotFoundException e) {
            getLogger().error("template file not found");
            throw new RuntimeException("template file not found", e);
        }
        try {
            outputStreamWriter.flush();
        } catch (IOException e) {
            getLogger().error("apply template error", e);
            return null;
        }
        return byteArrayOutputStream.toByteArray();
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
