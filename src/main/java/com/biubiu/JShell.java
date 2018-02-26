/**
 * Java操作shell脚本
 * Created by zhanghaibiao on 2018/1/21.
 */
public class JShell {

    private static final Logger LOGGER = LoggerFactory.getLogger(JShell.class);

    private static final String OS_NAME = "os.name";

    private static final String WINDOWS = "windows";

    private static final String CHMOD_COMMAND = "/bin/chmod";

    private static final String CHMOD_COMMAND_ARGS = "755";

    private static final String SH_COMMAND = "sh";

    /**
     * 执行shell脚本
     *
     * @param spider         爬虫名称
     * @param dir            python项目根路径
     * @param shellDirectory shell脚本目录
     * @param shell          shell脚本
     */
    public static void executeShell(String spider, String dir, String shellDirectory, String shell) throws Exception {
        boolean window = System.getProperty(OS_NAME).toLowerCase().startsWith(WINDOWS);
        ProcessBuilder builder = new ProcessBuilder();
        if (!window) {
            builder.command(CHMOD_COMMAND, CHMOD_COMMAND_ARGS, shellDirectory.concat(shell));
            builder.command(SH_COMMAND, shell, spider, dir);
            builder.directory(new File(shellDirectory));
            try {
                Process process = builder.start();
                StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), (r) -> LOGGER.info(r));
                streamGobbler.run();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new Exception("exitCode");
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                throw new Exception(e);
            }
        }
    }

    /**
     * 文件复制
     *
     * @param directory 目录
     * @param shell     shell脚本
     */
    public static void copyFile(String directory, String shell) {
        File dest = new File(directory.concat(shell));
        if (dest.exists()) return;
        try {
            Files.copy(JShell.class.getClassLoader().getResourceAsStream(shell), dest.toPath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

}
