package num_sequence;

import java.util.concurrent.atomic.AtomicLong;

public class NumSequenceGenerator
{
    private AtomicLong counter = new AtomicLong(0);

    public long getNextNum()
    {
        return counter.incrementAndGet();
    }
}