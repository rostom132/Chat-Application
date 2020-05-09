import java.net.*;
import java.io.*;
import java.util.Scanner;
public class clientInfo{
  String NickName;
  String IP;
  int port;
  Scanner myObj;
  private void Init(){
    this.myObj = new Scanner(System.in);
  }
  public void getNickName(){
    if(myObj == null){
      Init();
    }
    System.out.println("Enter usename: ");
    this.NickName = this.myObj.nextLine();
  }
}
