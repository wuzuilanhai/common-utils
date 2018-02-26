/**
 * 多数据源操作实现类
 * Created by zhanghaibiao on 2017/11/16.
 */
@Slf4j
public class MultiDataSourceOperator {

    private static Map<String, DruidDataSource> dataSources = Maps.newConcurrentMap();

    private static DruidDataSource addDataSource(String url, String user, String password, String driver) {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(url);
        druidDataSource.setUsername(user);
        druidDataSource.setPassword(password);
        druidDataSource.setDriverClassName(driver);
        druidDataSource.setMaxActive(25);
        druidDataSource.setInitialSize(5);
        druidDataSource.setMaxWait(3000);
        druidDataSource.setRemoveAbandoned(true);
        druidDataSource.setRemoveAbandonedTimeout(1800);
        return druidDataSource;
    }

    public static DruidDataSource getOrCacheDataSource(String url, String user, String password, String driver) {
        return Optional.ofNullable(dataSources.get(url)).orElseGet(() -> {
            DruidDataSource druidDataSource = addDataSource(url, user, password, driver);
            dataSources.put(url, druidDataSource);
            log.info("容器初始化缓存多数据源 {}", druidDataSource);
            return druidDataSource;
        });
    }

    public static Connection getConnection(String url) {
        DruidDataSource dataSource = dataSources.get(url);
        Connection connection = null;
        try {
            connection = dataSource != null ? dataSource.getConnection() : null;
        } catch (SQLException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    public static void removeConnection(String url) {
        dataSources.remove(url);
    }

    public static Connection cacheConnection(String url, String user, String password, String driver) {
        Connection connection = null;
        try {
            connection = getOrCacheDataSource(url, user, password, driver).getConnection();
        } catch (SQLException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

}
