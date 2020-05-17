import java.io.*;
import java.net.*;
public class packet implements Serializable{
  String name;
  boolean online;
  String IP;
  public packet(String name, boolean online, String IP){
    this.name = name;
    this.online = online;
    this.IP = IP;
  }
}
