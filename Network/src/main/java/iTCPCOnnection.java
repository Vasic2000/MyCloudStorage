public interface iTCPCOnnection {
    void onConnectionReady(TCP_Connection tcp_connection);
    void onReceive(TCP_Connection tcp_connection, byte[] buffer);
    void onDisconnect(TCP_Connection tcp_connection);
    void onException(TCP_Connection tcp_connection, Exception e);
}
