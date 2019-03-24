package smartbell.restapi;

import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class BellExecutorService {
    public final Executor io;

    public BellExecutorService() {
        io = Executors.newCachedThreadPool();
    }

}
