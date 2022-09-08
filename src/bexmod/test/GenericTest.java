package bexmod.test;

import java.util.*;
import java.nio.file.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;

class GenericTest {
  class Response {
    int statusCode;
    HashMap<String,String> headers;
    byte body[], response[];

    Response() {
      statusCode = 0;
      headers = new HashMap<String,String>();
      body = null;
      response = null;
    }

    String body() {
      return new String(body, StandardCharsets.UTF_8);
    }
  };

  void cleanup() {
  }

  void startTest(String testCode, String testName) {
    System.out.printf("%-9s %-40s", testCode, testName);
  }

  void testSucceeded() {
    System.out.println("[ OK ]");
  }

  void testFailed(String explanation) {
    System.out.println("[FAIL]\n\n"+explanation);
    cleanup();
    System.exit(1);
  }

  String randomAlphaNum(int minLen, int maxLen) {
    Random x = new Random();
    int len = minLen+x.nextInt(maxLen-minLen+1);
    String theVal = "";
    for (int i=0; i<len; i++)
      theVal = theVal + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".charAt(x.nextInt(62));
    return theVal;
  }

  boolean isPrint(int b)
  {
    if (Character.isLetter(b) || Character.isDigit(b))
      return true;
    if ((b == ' ') || (b == '/') || (b == '-') || (b == ':') || (b == '<') || (b == '>') || (b == '(') || (b == ')') || (b == '!') || (b == ',') || (b == '+') || (b == '%') || (b == '\"'))
      return true;

    return false;
  }

  String dump(byte[] data)
  {
    String result = "------------------------------------------------------------------------\n";
    for (int i=0; i<data.length; i+=16) {
      result += String.format("%04x  ", i);
      for (int j=0; j<16; j++) {
        if ((i+j)<data.length)
          result += String.format("%02x ", data[i+j]);
        else
          result += "   ";
      }
      result += "|";
      for (int j=0; j<16; j++) {
        if ((i+j)<data.length)
          result += isPrint(data[i+j]) ? String.format("%c", data[i+j]) : ".";
        else
          result += " ";
      }
      result += "|\n";
    }
    return result + "------------------------------------------------------------------------\n";
  }

  void assertClosed(Socket s, String explanationIfTimeout, String explanationIfMoreData) {
    InputStream in = null;
    try {
      in = s.getInputStream();
      s.setSoTimeout(1000);
    } catch (Exception e) {
      testFailed("An unknown problem occurred when reading: "+e);
    }
    int b = 0;
    try {
      b = in.read();
    } catch (SocketTimeoutException ste) {
      testFailed(explanationIfTimeout);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    if (b != -1) {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      buffer.write(b);
      try {
        while (true) {
          b = in.read();
          if (b == -1)
            break;
          buffer.write(b);
        }
      } catch (Exception e) {};
      testFailed(explanationIfMoreData+"\n\nHere is the extra data we got:\n\n"+dump(buffer.toByteArray()));
    }
  }

  void assertNoResponseYet(Socket s, String explanationIfResponse) {
    InputStream in = null;
    try {
      in = s.getInputStream();
      s.setSoTimeout(200);
    } catch (Exception e) {
      testFailed("An unknown problem occurred when reading: "+e);
    }
    int b = 0;
    boolean gotSTE = false;
    try {
      b = in.read();
    } catch (SocketTimeoutException ste) {
      gotSTE = true;
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    if (!gotSTE) {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      buffer.write(b);
      try {
        while (true) {
          b = in.read();
          if (b == -1)
            break;
          buffer.write(b);
        }
      } catch (Exception e) {};
      testFailed(explanationIfResponse+"\n\nHere is the data we got:\n\n"+dump(buffer.toByteArray()));
    }
  }

  Response readAndCheckResponse(Socket s, String which) {
    return readAndCheckResponse(s, which, true);
  }

  Response readAndCheckResponse(Socket s, String which, boolean expectContentLength) {
    Response r = new Response();
    InputStream in = null;
    try {
      in = s.getInputStream();
      s.setSoTimeout(1000);
    } catch (Exception e) {
      testFailed("An unknown problem occurred when reading: "+e);
    }
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    ByteArrayOutputStream entire = new ByteArrayOutputStream();
    int matchPtr = 0, numBytesRead = 0;
    while (matchPtr < 4) {
      int b = 0;
      try {
        b = in.read();
        if (b>=0)
          numBytesRead ++;
      } catch (SocketTimeoutException ste) {
        if (numBytesRead == 0)
          testFailed("A timeout occurred before any (!) bytes were received from the server.");
        testFailed("A timeout occurred before the entire "+which+" could be read. Here is what we received so far:\n\n"+dump(buffer.toByteArray())+"\nCheck whether you are sending two (!) CRLFs at the end of the headers (not just one, and not just a CR), and whether you need to flush the OutputStream.");
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      if (b<0) {
        if (numBytesRead == 0)
          testFailed("The server closed the connection before any (!) bytes of the "+which+" were received.");
        testFailed("The server closed the connection before the entire "+which+" could be read. Here is what we received so far:\n\n"+dump(buffer.toByteArray())+"\nCheck whether you are sending two (!) CRLFs at the end of the headers (not just one, and not just a CR), and whether you need to flush the OutputStream.");
      }

      buffer.write(b);
      entire.write(b);
      if ((((matchPtr==0) || (matchPtr==2)) && (b=='\r')) || (((matchPtr==1) || (matchPtr==3)) && (b=='\n')))
        matchPtr ++;
      else
        matchPtr = 0;
    }

    try {
      BufferedReader hdr = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer.toByteArray())));
      String statusLine = hdr.readLine();
      String[] p = statusLine.split(" ");
      if (p.length < 3) 
        testFailed("The status line we got was '"+statusLine+"'. It is supposed to have 3 fields, but it looks like it has only "+p.length+". Here is what we received:\n\n"+dump(buffer.toByteArray()));

      if (!p[0].equals("HTTP/1.1"))
        testFailed("The status line we got was '"+statusLine+"'. The protocol was '"+p[0]+"', but it was supposed to be HTTP/1.1. Here is what we received:\n\n"+dump(buffer.toByteArray()));
      if (p[1].length() != 3)
        testFailed("The status line we got was '"+statusLine+"'. The status code, '"+p[1]+"', was supposed to have three digits, but it doesn't. Here is what we received:\n\n"+dump(buffer.toByteArray()));

      try {
        r.statusCode = Integer.parseInt(p[1]);
      } catch (NumberFormatException nfe) {
        testFailed("The status line we got was '"+statusLine+"'. The status code, '"+p[1]+"', was supposed to be numeric, but it isn't. Here is what we received:\n\n"+dump(buffer.toByteArray()));
      }

      while (true) {
        String l = hdr.readLine();
        if (l.equals(""))
          break;

        String[] p2 = l.split(":", 2);
        if (p2.length == 2) {
          r.headers.put(p2[0].toLowerCase().trim(), p2[1].trim());
        } else {
          testFailed("We got a header line without a colon: '"+l+". Here is what we received:\n\n"+dump(buffer.toByteArray()));
        }
      }

    } catch (IOException ioe) {
      testFailed("Unknown exception while parsing the headers: "+ioe);
      ioe.printStackTrace();
    }

    ByteArrayOutputStream body = new ByteArrayOutputStream();
    String cl = r.headers.get("content-length");
    int bodyBytesReceived = 0;
    for (int i=0; (cl == null) || (i<Integer.valueOf(cl).intValue()); i++) {
      int b = 0;
      try {
        b = in.read();
      } catch (SocketTimeoutException ste) {
        if (expectContentLength)
          testFailed("A timeout occurred before the entire message body could be read. We got "+i+" bytes, but expected "+cl+". "
                    +"Here is what we received so far:\n\n"+dump(buffer.toByteArray())+"\nCheck whether you sent a Content-Length header "
                    +((cl==null) ? "(we did not see one in the response)" : "(the value we saw was: '"+cl+"')")
                    +", and, if so, whether the value in that header is correct. An off-by-one error can easily cause this test to fail. ");
        else
          testFailed("A timeout occurred before the entire message body could be read. We got "+i+" bytes so far, which are:\n\n"+dump(buffer.toByteArray())+"\n"
                    +"Check whether you closed the connection after handling the request.");
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      if (b<0) {
        if (cl != null)
          testFailed("The server closed the connection before the entire message body could be read. Here is what we received so far:\n\n"+dump(buffer.toByteArray())+"\nCheck whether you sent a Content-Length header (the value we saw was: '"+cl+"'), and, if so, whether the value in that header is correct. An off-by-one error can easily cause this test to fail.");
        else
          break;
      }

      body.write(b);
      entire.write(b);
    }

    r.body = body.toByteArray();
    r.response = entire.toByteArray();
    return r;
  }

  Socket openSocket(int port) {
    Socket s = null;
    try {
      s = new Socket("localhost", port);
    } catch (ConnectException ce) {
      testFailed("Cannot connect to port "+port+" on this machine. Are you sure the server is running and has port 8000 open? You can use 'ps' and 'netstat' to check.");
    } catch (Exception e) {
      testFailed("Unknown problem: "+e);
      e.printStackTrace();
    }

    return s;
  }
}