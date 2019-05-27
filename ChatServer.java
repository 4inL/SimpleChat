// My Github Address : https://github.com/4inL/SimpleChat
// Writer : 21400523 LEE MYEONG SEOK
// Date   : 2019-05-27

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;i
	private String[] badwords = {"Fuck", "fuck"};		
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		try{
			int bad = 0;
			Object obj = hm.get(id);
			PrintWriter pw = (PrintWriter)obj;
			String line = null;
			while((line = br.readLine()) != null){
				for(int i = 0; i < badwords.length; i++) {	  		//////
					bad = 0;
					if( line.contains(badwords[i]) ) {
						pw.println("YOU CANNOT SAY IT !!!");
						pw.flush();
						bad = bad+1;
						break;
					}
				}	//check bad words

				if(line.equals("/quit"))
					break;
				if(bad != 0){
					continue;
				}else if(line.equals("/userlist")) {
					send_userlist(id);
				}else if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}else
					broadcastexcept(id + " : " + line, pw);
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				pw.println(msg);
				pw.flush();
			}
		}
	} // broadcast
	public void broadcastexcept(String msg, PrintWriter hostpw){ 		//////
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				if(pw.equals(hostpw)) {
				} else{
					pw.println(msg);
					pw.flush();
				}
			}
		}
	} // broadcast except himself
	public void send_userlist(String id) {
		Object obj = hm.get(id);
		if(obj != null){
		  PrintWriter pw = (PrintWriter)obj;
			synchronized(hm){
				Collection collection = hm.keySet();
				Iterator iter = collection.iterator();
				while(iter.hasNext()){
					pw.println(iter.next());
					pw.flush();
				}
			}
		}
	}	//print userlist
}
