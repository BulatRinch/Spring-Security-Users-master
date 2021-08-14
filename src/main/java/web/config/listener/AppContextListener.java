package web.config.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Старт!");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Отключение!");
        System.out.println("Удаление контекста");
        System.out.println("Вызов MySQL AbandonedConnectionCleanupThread checkedShutdown");
        // ... Сначала закройте все фоновые задачи, которые могут использовать БД ...
        // ... Затем закройте все пулы соединений с БД ...

        // Теперь отмените регистрацию драйверов JDBC в ClassLoader этого контекста:
        // Получаем ClassLoader веб-приложения
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        // Перебираем все драйверы
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cl) {
                // Этот драйвер был зарегистрирован загрузчиком ClassLoader, поэтому отмените его регистрацию:
                try {
                    System.out.println("Отмена регистрации драйвера JDBC " + driver);
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException ex) {
                    System.out.println("Ошибка отмены регистрации драйвера JDBC " + driver);
                    ex.printStackTrace();
                }
            } else {
                // драйвер не был зарегистрирован загрузчиком ClassLoader и может использоваться в другом месте
                System.out.println("Зарегистрировать драйвер JDBC " + driver + " т.к. он не принадлежит ClassLoader");
            }
        }
    }
}
