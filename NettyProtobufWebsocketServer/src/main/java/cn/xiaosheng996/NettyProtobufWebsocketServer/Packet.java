package cn.xiaosheng996.NettyProtobufWebsocketServer;


public class Packet{
  public static final byte HEAD_TCP = -128;
  public static final byte HEAD_UDP = 0;
  public static final byte HEAD_NEED_ACK = 64;
  public static final byte HEAD_ACK = 44;
  public static final byte HEAD_PROTOCOL_MASK = 3;
  public static final byte PROTOCOL_PROTOBUF = 0;
  public static final byte PROTOCOL_JSON = 1;
  private final byte head;
  private final short sid;
  private final int cmd;
  private final byte[] bytes;
  
  public Packet(byte head, int cmd, byte[] bytes) {
    this(head, (short)0, cmd, bytes);
  }

  public Packet(byte head, short sid, int cmd, byte[] bytes){
    this.head = head;
    this.sid = sid;
    this.cmd = cmd;
    this.bytes = bytes;
  }

  public byte getHead() {
    return this.head;
  }

  public int getCmd() {
    return this.cmd;
  }

  public byte[] getBytes() {
    return this.bytes;
  }

  public short getSid() {
    return this.sid;
  }
}
