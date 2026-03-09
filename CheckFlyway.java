import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import java.lang.reflect.Method;

public class CheckFlyway {
    public static void main(String[] args) {
        FluentConfiguration config = Flyway.configure();
        for (Method m : config.getClass().getMethods()) {
            if (m.getName().toLowerCase().contains("order") || m.getName().toLowerCase().contains("missing")
                    || m.getName().toLowerCase().contains("ignore")) {
                System.out.println(m.getName());
            }
        }
    }
}
