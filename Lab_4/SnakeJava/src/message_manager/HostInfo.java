package message_manager;

import java.net.InetAddress;

public class HostInfo
{
    private InetAddress ip;
    private int port;

    public HostInfo(InetAddress _ip, int _port)
    {
        ip = _ip;
        port = _port;
    }

    public InetAddress getIp()
    {
        return ip;
    }

    public int getPort()
    {
        return port;
    }

    @Override
    public int hashCode()
    {
        return (ip.toString() + port).hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == this)
            return true;
        if(o == null || o.getClass() != this.getClass())
            return false;

        HostInfo hi = (HostInfo)o;

        return hi.ip.equals(this.ip) && hi.port == this.port;
    }


    @Override
    public String toString()
    {
        return ip.toString() + ":" + port;
    }
}
