/**
 * Created by zhanghaibiao on 2018/1/24.
 */
public class PythonGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PythonGenerator.class);

    private static final String CLASSPATH = "classpath";

    private static final String LOADER_CLASS = "classpath.resource.loader.class";

    /**
     * python模板代码生成
     *
     * @param file         文件绝对路径
     * @param params       参数
     * @param templateFile 模板文件名称
     */
    public static void generate(String file, List params, String templateFile) throws Exception {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, CLASSPATH);
        ve.setProperty(LOADER_CLASS, ClasspathResourceLoader.class.getName());
        ve.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");
        ve.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
        ve.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
        ve.init();
        Template template = ve.getTemplate(templateFile);
        VelocityContext ctx = new VelocityContext();
        if (params == null) return;
        String value = null;
        for (int i = 1; i <= params.size(); i++) {
            value = String.valueOf(params.get(i - 1));
            ctx.put(PythonParams.PARAM_PREFIX + i, Strings.isNullOrEmpty(value) ? "" : value);
        }
        try (PrintWriter writer = new PrintWriter(file)) {
            template.merge(ctx, writer);
            writer.flush();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new Exception(e);
        }
    }

}
