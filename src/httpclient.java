


import java.io.*;
import java.net.Socket;
import java.util.Base64;


public class httpclient {
    final int byteIndexHtml = 362;
    final int byteBigTxt = 6488394;
    final int byteProtectedHtml = 470;

    public Socket my_socket;
    public PrintWriter send;
    public BufferedReader receive;

    StringBuffer stringBuffer;
    BufferedReader stdIn;

    public void connect(String ip, int port) throws IOException {

        my_socket = new Socket(ip, port);
        System.out.println("Just connected to " + my_socket.getRemoteSocketAddress());
        send = new PrintWriter(my_socket.getOutputStream(), true);
        receive = new BufferedReader(new InputStreamReader(my_socket.getInputStream()));
        stringBuffer = new StringBuffer();
        stdIn = new BufferedReader(new InputStreamReader(System.in));

    }
    public  int indexOfString(String name,char searched){
        int x = -1;
        for (int i = 0; i< name.length(); i++){
            if(name.charAt(i)==searched){
                x= i;
                break;
            }
        }
        return x;
    }
    public String convertFilename(String fileName){

        if(fileName.equals("/")){
            fileName = "index2.html";
        }
        else {
            System.out.println("Received File Name "+fileName);
            int position = indexOfString(fileName,'.');
            fileName = fileName.substring(1,position)+"2."+fileName.substring(position+1,position+5);
        }
        return fileName;
    }
    public String convertFilenameTXT(String fileName){
        System.out.println("Received File Name "+fileName);
        int position = indexOfString(fileName,'.');
        fileName = fileName.substring(1,position)+"2."+fileName.substring(position+1,position+4);
        return fileName;
    }
    public String convertFilenameTXT(String fileName,String range){
        System.out.println("Received File Name "+fileName);
        int position = indexOfString(fileName,'.');
        fileName = fileName.substring(1,position)+range.split("-")[1]+"."+fileName.substring(position+1,position+4);
        return fileName;
    }
    public String readResponseMessage(String filename)throws IOException{
        String response ="";
        BufferedReader brTest = new BufferedReader(new FileReader(filename));
        response = response.concat(brTest.readLine());
        return response;
    }
    public String readResponseMessageTXT(String filename)throws IOException{
        String response ="";
        String response2 ="";
        BufferedReader brTest = new BufferedReader(new FileReader(filename));
        while ((response = brTest.readLine())!=null){
            response2 = response2.concat(response);
        }

        return response2;
    }
    public String message(String msg) throws IOException {
        String response ="";
        System.out.println(msg);
        send.write(msg);
        send.flush();
        String messageFileName = "";


        if(msg.contains("GET")){
            String[] temp= msg.split("\n|\r| ");
            if(msg.contains("Authorization:")){
                for (int i = 0; i < temp.length; i++) {
                    if (temp[i].equals("GET")) {
                        messageFileName = temp[i+1];
                    }
                }
            }
            else{
                messageFileName = temp[1];
                System.out.println(messageFileName);
            }


            if (temp[1].contains("txt")){
                messageFileName=convertFilenameTXT(messageFileName,temp[7]);
            }else {
                messageFileName=convertFilename(messageFileName);
            }

            System.out.println("Coverted: "+messageFileName);

            OutputStream web = new FileOutputStream(messageFileName);
            InputStream rec = my_socket.getInputStream();

            int number_of_bytes = 0;
            if(messageFileName.contains("index")){
                number_of_bytes = byteIndexHtml;
            }
            else if(messageFileName.contains("big")){
                number_of_bytes = byteBigTxt;
            }
            if(messageFileName.contains("protected")){
                number_of_bytes = byteProtectedHtml;
            }
            byte[] bytes = new byte[number_of_bytes];
            int count;
            while ((count=rec.read(bytes))>0){
                web.write(bytes,0,count);
                if ( rec.available()==0){
                    break;
                }
            }

            if (temp[1].contains("txt")){
                response=readResponseMessage(messageFileName);
            }else {
                response=readResponseMessage(messageFileName);
            }

            //web.close();
            //rec.close();


        }
        if(msg.contains("HEAD")){
            String[] temp= msg.split("\n|\r| ");
            messageFileName = temp[1];
            System.out.println(messageFileName);
            messageFileName=convertFilenameTXT(messageFileName);
            System.out.println("Coverted: "+messageFileName);

            OutputStream web = new FileOutputStream(messageFileName);
            InputStream rec = my_socket.getInputStream();

            int number_of_bytes = 0;
            if(messageFileName.contains("index")){
                number_of_bytes = byteIndexHtml;
            }
            else if(messageFileName.contains("big")){
                number_of_bytes = byteBigTxt;
            }
            if(messageFileName.contains("protected")){
                number_of_bytes = byteProtectedHtml;
            }
            byte[] bytes = new byte[number_of_bytes];
            int count;

            while ((count=rec.read(bytes))>0){

                web.write(bytes,0,count);

                if ( rec.available()==0){
                    break;
                }


            }
            response=readResponseMessageTXT(messageFileName);
            //TODO
        }
        return response;
    }
    public void stopConnection() throws IOException{
        receive.close();
        send.close();
        my_socket.close();
    }
    public String extractHiddenEntity(String filename) throws IOException{
        String content = "";

        BufferedReader in = new BufferedReader(new FileReader(filename));
        String temp;
        while ((temp = in.readLine()) != null) {
            content = content.concat(temp);
        }
        in.close();

        int start = content.lastIndexOf("<a href=")+9;
        int end;
        if (filename.contains("protected")){
            end= start+7;
        }else {
            end= content.indexOf(".html")+5;
            System.out.println(end);
        }



        System.out.println(start+" "+end);


        return  content.substring(start,end);

    }
    public static void main(final String[] args) throws IOException {
        httpclient me = new httpclient();
        me.connect("127.0.0.1",8000);
        String currentResponse;
        if(me.my_socket.isConnected()){
            System.out.println("Success");
            try {
                /*Part A*/
                currentResponse=me.message("GET / HTTP/1.1\r\n\r\n");
                System.out.println(currentResponse);
            }catch (final Exception e) {
                System.out.println(e.getCause()+" "+e.getMessage()+ " "+e.getLocalizedMessage());
                //System.out.println(my_socket.getOutputStream());
                System.out.println("Exception");
                me.stopConnection();
            }

            String hiddenEntity =me.extractHiddenEntity("index2.html");
            System.out.println("Found Entity from index.html: "+hiddenEntity);

            try {
                /*Part B*/
                String encodeBytes = Base64.getEncoder().encodeToString(("bilkentstu" + ":" + "cs421s2021").getBytes());
                currentResponse=me.message("GET /"+hiddenEntity+" HTTP/1.1\r\n\r\n");
                System.out.println(currentResponse);
                System.out.println(currentResponse);
                currentResponse=me.message("GET /"+hiddenEntity+ " HTTP/1.1\r\n\r\n"+"Authorization: Basic "+encodeBytes+"\r\n\r\n");

                hiddenEntity = me.extractHiddenEntity(me.convertFilename("/"+hiddenEntity));
                System.out.println(hiddenEntity);
                System.out.println("Found Entity : "+hiddenEntity);
                /*PartC*/

                currentResponse=me.message("HEAD /"+hiddenEntity+" HTTP/1.1\r\n\r\n");

                System.out.println(currentResponse);
                long []test_variables = new long[5];
                test_variables[0] =10;
                test_variables[1] =100;
                test_variables[2] =1000;
                test_variables[3] =10000;
                test_variables[4] =15000;
                for (int i = 0; i < test_variables.length; i++) {
                    long start = System.nanoTime();
                    currentResponse=me.message("GET /"+hiddenEntity+ " HTTP/1.1\r\n\r\n"+"Range: bytes=0-"+test_variables[i]+"\r\n\r\n");
                    System.out.println(currentResponse);
                    long finish = System.nanoTime();
                    long timeElapsed = finish - start;
                    test_variables[i]=timeElapsed;
                }
                for (int i = 0; i < test_variables.length; i++) {
                    System.out.println(test_variables[i]);
                }
                long start = System.nanoTime();
                currentResponse=me.message("GET /"+hiddenEntity+ " HTTP/1.1\r\n\r\n"+"Range: bytes=0-"+me.byteBigTxt+"\r\n\r\n");
                System.out.println(currentResponse);
                long finish = System.nanoTime();
                long timeElapsed = finish - start;
                System.out.println(timeElapsed);
                //currentResponse=me.message("Authorization: Basic "+encodeBytes+"\r\n\r\n");
                //System.out.println(currentResponse);hi


            }catch (final Exception e) {
                System.out.println(e.getCause()+" "+e.getMessage()+ " "+e.getLocalizedMessage());
                System.out.println("Exception");
            }

            me.message("EXIT HTTP/1.1\r\n\r\n");
        }

        me.stopConnection();
        System.out.println("Connection closed");
    }
}