package bexmod.test;

import java.util.*;
import java.nio.file.*;
import java.io.*;
import java.net.*;

public class HW1Test extends GenericTest {

  void cleanup() {
    File a = new File("tmpRes/file1.txt"), b = new File("tmpRes/file2.txt"), c = new File("tmpRes/file3.txt"), d = new File("tmpRes/binary");
    a.delete();
    b.delete();
    c.delete();
    d.delete();

    File subdir = new File("tmpRes");
    subdir.delete();
  }

  void runTests(Set<String> tests) throws Exception {
    /* Check whether the test directory already exists. We don't want to accidentally overwrite anything. */

    Path path = Paths.get("tmpRes");
    if (Files.exists(path)) {
      System.err.println("A directory or file with the name 'test' already exists. Please delete or rename this before running the test suite, or run the tests in a different directory.");
      System.exit(1);
    }

    /* Create the test files */

    File subdir = new File("tmpRes");
    subdir.mkdir();

    PrintWriter p = new PrintWriter("tmpRes/file1.txt");
    p.println("Well done is better than well said.");
    p.close();

    p = new PrintWriter("tmpRes/file2.txt");
    p.println("No gains without pains.");
    p.close();

    p = new PrintWriter("tmpRes/file3.txt");
    p.println("Lost time is never found again.");
    p.close();

    FileOutputStream fos = new FileOutputStream("tmpRes/binary");
    for (int i=0; i<256; i++)
      fos.write(i);
    fos.close();

    /* Ask the user to confirm that the server is running */

    System.out.println("In another terminal window, please run 'java cis5550.webserver.Server 8000 "+subdir.getAbsolutePath()+"', and then hit Enter in this window to continue.");
    (new Scanner(System.in)).nextLine();
    System.out.printf("\n%-10s%-40sResult\n", "Test", "Description");
    System.out.println("--------------------------------------------------------");

    if (tests.contains("req")) {
      startTest("req", "Single request");
      Socket s = openSocket(8000);
      PrintWriter out = new PrintWriter(s.getOutputStream());
      out.print("GET /file1.txt HT");
      out.flush();
      Thread.sleep(200);
      assertNoResponseYet(s, "So far we haven't sent a full request, just 'GET /file1.txt HT', so the server should keep reading until it sees the double CRLF that indicates the end of the headers. However, it did send something back already; see below. Please double-check that you keep reading more data in a loop until you see the double CRLF!");
      out.print("TP/1.1\r\n");
      out.flush();
      Thread.sleep(200);
      assertNoResponseYet(s, "So far we haven't sent a full request, just 'GET /file1.txt HTTP/1.1<CRLF>', so the server should keep reading until it sees the double (!) CRLF that indicates the end of the headers. However, it did send something back already; see below. Please double-check that you keep reading more data in a loop until you see the double CRLF!");
      out.print("Hot: localhost\r\n\r\n");
      out.flush();
      readAndCheckResponse(s, "response");
      s.shutdownOutput();
      assertClosed(s, "The server was supposed to close the connection when the client closed its end, but it looks like it has not.", "The server seems to be sending more data after the end of the response! You may want to check your Content-Length header.");
      testSucceeded();
    }

    if (tests.contains("persist")) {
      startTest("persist", "Persistent connection");
      Socket s = openSocket(8000);
      PrintWriter out = new PrintWriter(s.getOutputStream());
      out.print("GET /file1.txt HTTP/1.1\r\nHost: localhost\r\n\r\n");
      out.flush();
      readAndCheckResponse(s, "first response (file1.txt)");
      out.print("GET /file2.txt HTTP/1.1\r\nHost: localhost\r\n\r\n");
      out.flush();
      readAndCheckResponse(s, "second response (file2.txt)");
      out.print("GET /file3.txt HTTP/1.1\r\nHost: localhost\r\n\r\n");
      out.flush();
      readAndCheckResponse(s, "third response (file3.txt)");
      s.shutdownOutput();
      assertClosed(s, "The server was supposed to close the connection when the client closed its end, but it looks like it has not.", "The server seems to be sending more data after the end of the response! You may want to check your Content-Length header.");
      testSucceeded();
    }

    if (tests.contains("err400")) {
      startTest("err400", "Error 400 (Bad Request)");
      Socket s = openSocket(8000);
      PrintWriter out = new PrintWriter(s.getOutputStream());
      out.print("NONSENSE\r\n\r\n");
      out.flush();
      Response r = readAndCheckResponse(s, "response");
      if (r.statusCode != 400)
        testFailed("The server was supposed to return status code 400, but it actually returned "+r.statusCode);
      s.shutdownOutput();
      assertClosed(s, "The server was supposed to close the connection when the client closed its end, but it looks like it has not.", "The server seems to be sending more data after the end of the response! You may want to check your Content-Length header.");
      testSucceeded();
    }

    if (tests.contains("err404")) {
      startTest("err404", "Error 404 (Not Found)");
      Socket s = openSocket(8000);
      PrintWriter out = new PrintWriter(s.getOutputStream());
      out.print("GET /some-file-that-does-not-exist.txt HTTP/1.1\r\nHost: localhost\r\n\r\n");
      out.flush();
      Response r = readAndCheckResponse(s, "response");
      if (r.statusCode != 404)
        testFailed("The server was supposed to return status code 404, but it actually returned "+r.statusCode);
      s.shutdownOutput();
      assertClosed(s, "The server was supposed to close the connection when the client closed its end, but it looks like it has not.", "The server seems to be sending more data after the end of the response! You may want to check your Content-Length header.");
      testSucceeded();
    }

    if (tests.contains("err405")) {
      startTest("err405", "Error 405 (Method Not Allowed)");
      Socket s = openSocket(8000);
      PrintWriter out = new PrintWriter(s.getOutputStream());
      out.print("POST /file1.txt HTTP/1.1\r\nHost: localhost\r\n\r\n");
      out.flush();
      Response r = readAndCheckResponse(s, "response");
      if (r.statusCode != 405)
        testFailed("The server was supposed to return status code 405, but it actually returned "+r.statusCode);
      s.shutdownOutput();
      assertClosed(s, "The server was supposed to close the connection when the client closed its end, but it looks like it has not.", "The server seems to be sending more data after the end of the response! You may want to check your Content-Length header.");
      testSucceeded();
    }

    if (tests.contains("err501")) {
      startTest("err501", "Error 501 (Not Implemented)");
      Socket s = openSocket(8000);
      PrintWriter out = new PrintWriter(s.getOutputStream());
      out.print("EAT /file1.txt HTTP/1.1\r\nHost: localhost\r\n\r\n");
      out.flush();
      Response r = readAndCheckResponse(s, "response");
      if (r.statusCode != 501)
        testFailed("The server was supposed to return status code 501, but it actually returned "+r.statusCode);
      s.shutdownOutput();
      assertClosed(s, "The server was supposed to close the connection when the client closed its end, but it looks like it has not.", "The server seems to be sending more data after the end of the response! You may want to check your Content-Length header.");
      testSucceeded();
    }

    if (tests.contains("err505")) {
      startTest("err505", "Error 505 (Version Not Supported)");
      Socket s = openSocket(8000);
      PrintWriter out = new PrintWriter(s.getOutputStream());
      out.print("GET /file1.txt HTTP/5.2\r\nHost: localhost\r\n\r\n");
      out.flush();
      Response r = readAndCheckResponse(s, "response");
      if (r.statusCode != 505)
        testFailed("The server was supposed to return status code 505, but it actually returned "+r.statusCode);
      s.shutdownOutput();
      assertClosed(s, "The server was supposed to close the connection when the client closed its end, but it looks like it has not.", "The server seems to be sending more data after the end of the response! You may want to check your Content-Length header.");
      testSucceeded();
    }

    if (tests.contains("withbody")) {
      startTest("withbody", "Multiple GET requests with bodies");
      Socket s = openSocket(8000);
      PrintWriter out = new PrintWriter(s.getOutputStream());
      out.print("GET /file1.txt HTTP/1.1\r\nContent-Length: 5\r\nHost: localhost\r\n\r\nHello");
      out.flush();
      readAndCheckResponse(s, "first response (file1.txt)");
      out.print("GET /file2.txt HTTP/1.1\r\nContent-Length: 14\r\nHost: localhost\r\n\r\nThis is a test");
      out.flush();
      readAndCheckResponse(s, "second response (file2.txt)");
      out.print("GET /file3.txt HTTP/1.1\r\nContent-Length: 0\r\nHost: localhost\r\n\r\n");
      out.flush();
      readAndCheckResponse(s, "third response (file3.txt)");
      s.shutdownOutput();
      assertClosed(s, "The server was supposed to close the connection when the client closed its end, but it looks like it has not.", "The server seems to be sending more data after the end of the response! You may want to check your Content-Length header.");
      testSucceeded();
    }

    if (tests.contains("connclose")) {
      startTest("connclose", "GET with body and no Content-Length");
      Socket s = openSocket(8000);
      PrintWriter out = new PrintWriter(s.getOutputStream());
      out.print("GET /file1.txt HTTP/1.1\r\nHost: localhost\r\n\r\nHello world! This is a test of a connection without Content-Length header!");
      out.flush();
      readAndCheckResponse(s, "response");
      s.shutdownOutput();
      assertClosed(s, "The server was supposed to close the connection when the client closed its end, but it looks like it has not.", "The server seems to be sending more data after the end of the response!");
      testSucceeded();
    }

    if (tests.contains("text")) {
      startTest("text", "Request for a text file");
      Socket s = openSocket(8000);
      PrintWriter out = new PrintWriter(s.getOutputStream());
      out.print("GET /file1.txt HTTP/1.1\r\nHost: localhost\r\n\r\n");
      out.flush();
      Response r = readAndCheckResponse(s, "response");
      s.shutdownOutput();
      assertClosed(s, "The server was supposed to close the connection when the client closed its end, but it looks like it has not.", "The server seems to be sending more data after the end of the response! You may want to check your Content-Length header.");
      if (r.statusCode != 200)
        testFailed("The server was supposed to return a 200 OK, but it actually returned at "+r.statusCode);
      if (r.body.length != 36)
        testFailed("The server was supposed to send 36 bytes, but it actually sent "+r.body.length+". Here is what we received:\n\n"+dump(r.body));
      if (!(new String(r.body)).equals("Well done is better than well said.\n"))
        testFailed("The server was supposed to send file1.txt, but we got something different. Here is what we received:\n\n"+dump(r.body));
      testSucceeded();
    }

    if (tests.contains("binary")) {
      startTest("binary", "Request for a binary file");
      Socket s = openSocket(8000);
      PrintWriter out = new PrintWriter(s.getOutputStream());
      out.print("GET /binary HTTP/1.1\r\nHost: localhost\r\n\r\n");
      out.flush();
      Response r = readAndCheckResponse(s, "response");
      s.shutdownOutput();
      assertClosed(s, "The server was supposed to close the connection when the client closed its end, but it looks like it has not.", "The server seems to be sending more data after the end of the response! You may want to check your Content-Length header.");
      if (r.statusCode != 200)
        testFailed("The server was supposed to return a 200 OK, but it actually returned at "+r.statusCode);
      if (r.body.length != 256)
        testFailed("The server was supposed to send 36 bytes, but it actually sent "+r.body.length+". Here is what we received:\n\n"+dump(r.body));
      for (int i=0; i<256; i++) {
        int theByte = (256+r.body[i])%256;
        if (i != theByte)
          testFailed("The server was supposed to send the file 'binary', but we got something different at position "+i+". Here is what we received:\n\n"+dump(r.body));
      }
      testSucceeded();
    }

    if (tests.contains("multi")) {
      startTest("multi", "Multiple requests in parallel");
      Socket s1 = openSocket(8000);
      PrintWriter out1 = new PrintWriter(s1.getOutputStream());
      out1.print("GET /file1.txt HTTP/1.1\r\n");
      out1.flush();
      Socket s2 = openSocket(8000);
      PrintWriter out2 = new PrintWriter(s2.getOutputStream());
      out2.print("GET /file2.txt HTTP/1.1\r\nHost: localhost\r\n\r\n");
      out2.flush();
      Response r2 = readAndCheckResponse(s2, "second response");
      if (r2.statusCode != 200)
        testFailed("The server was supposed to return a 200 OK in the second connection, but it actually returned at "+r2.statusCode);
      if (r2.body.length != 24)
        testFailed("The second request was supposed to return file2.txt, but we got something different. Here is what we received:\n\n"+dump(r2.body));
      s2.shutdownOutput();
      assertClosed(s2, "The server was supposed to close the second connection when the client closed its end, but it looks like it has not.", "The server seems to be sending more data after the end of the response! You may want to check your Content-Length header.");
      out1.print("Host: localhost\r\n\r\n");
      out1.flush();
      Response r1 = readAndCheckResponse(s1, "first response");
      s1.shutdownOutput();
      assertClosed(s1, "The server was supposed to close the first connection when the client closed its end, but it looks like it has not.", "The server seems to be sending more data after the end of the response! You may want to check your Content-Length header.");
      if (r1.statusCode != 200)
        testFailed("The server was supposed to return a 200 OK in the first connection, but it actually returned at "+r1.statusCode);
      if (r1.body.length != 36)
        testFailed("The first request was supposed to return file1.txt, but we got something different. Here is what we received:\n\n"+dump(r2.body));
      testSucceeded();
    }

    if (tests.contains("stress")) {
      startTest("stress", "Send 1,000 requests");
      for (int i=0; i<1000; i++) {
        Socket s = openSocket(8000);
        PrintWriter out = new PrintWriter(s.getOutputStream());
        out.print("GET /file1.txt HTTP/1.1\r\nHost: localhost\r\n\r\n");
        out.flush();
        Response r = readAndCheckResponse(s, "response");
        s.shutdownOutput();
        assertClosed(s, "The server was supposed to close the connection when the client closed its end, but it looks like it has not.", "The server seems to be sending more data after the end of the response! You may want to check your Content-Length header.");
        if (r.statusCode != 200)
          testFailed("The server was supposed to return a 200 OK, but it actually returned at "+r.statusCode);
        if (r.body.length != 36)
          testFailed("The server was supposed to send 36 bytes, but it actually sent "+r.body.length+". Here is what we received:\n\n"+dump(r.body));
        if (!(new String(r.body)).equals("Well done is better than well said.\n"))
          testFailed("The server was supposed to send file1.txt, but we got something different. Here is what we received:\n\n"+dump(r.body));
        s.close();
      }
      testSucceeded();
    }

    if (tests.contains("stress2")) {
      startTest("stress", "Send 1,000 requests");
      Socket s = openSocket(8000);
      PrintWriter out = new PrintWriter(s.getOutputStream());
      for (int i=0; i<1000; i++) {
        out.print("GET /file1.txt HTTP/1.1\r\nHost: localhost\r\n\r\n");
        out.flush();
        Response r = readAndCheckResponse(s, "response");
        if (r.statusCode != 200)
          testFailed("The server was supposed to return a 200 OK, but it actually returned at "+r.statusCode);
        if (r.body.length != 36)
          testFailed("The server was supposed to send 36 bytes, but it actually sent "+r.body.length+". Here is what we received:\n\n"+dump(r.body));
        if (!(new String(r.body)).equals("Well done is better than well said.\n"))
          testFailed("The server was supposed to send file1.txt, but we got something different. Here is what we received:\n\n"+dump(r.body));
      }
      s.close();
      testSucceeded();
    }

    if (tests.contains("ec-modif")) {
      startTest("ec-modif", "Test If-Modified-Since header");
      Socket s = openSocket(8000);
      PrintWriter out = new PrintWriter(s.getOutputStream());
      out.print("GET /file1.txt HTTP/1.1\r\nIf-Modified-Since: Mon, 13 Oct 2015 02:28:20 GMT\r\n\r\n");
      out.flush();
      Response r = readAndCheckResponse(s, "response");
      if (r.statusCode != 200)
        testFailed("The server was supposed to return a 200 Success, but it actually returned at "+r.statusCode);
      out.print("GET /file1.txt HTTP/1.1\r\nIf-Modified-Since: Wed, 21 Oct 2022 07:28:00 GMT\r\n\r\n");
      out.flush();
      r = readAndCheckResponse(s, "response");
      if (r.statusCode != 304)
        testFailed("The server was supposed to return a 304 Not Modified, but it actually returned at "+r.statusCode);
      s.shutdownOutput();
      assertClosed(s, "The server was supposed to close the connection when the client closed its end, but it looks like it has not.", "The server seems to be sending more data after the end of the response! You may want to check your Content-Length header.");
      testSucceeded();
    }


    System.out.println("--------------------------------------------------------\n");
    System.out.println("Looks like your solution passed all of the selected tests. Congratulations!");
    cleanup();
  }

	public static void main(String args[]) throws Exception {

    /* Make a set of enabled tests. If no command-line arguments were specified, run all tests. */

    Set<String> tests = new TreeSet<String>();
    if (args.length == 0) {
    	tests.add("req");
    	tests.add("persist");
    	tests.add("err400");
    	tests.add("err404");
        tests.add("err405");
    	tests.add("err501");
    	tests.add("err505");
    	tests.add("withbody");
    	tests.add("connclose");
    	tests.add("text");
//    	tests.add("binary");
    	tests.add("multi");
    	tests.add("stress");
        tests.add("ec-modif");
    } else {
    	for (int i=0; i<args.length; i++)
    		tests.add(args[i]);
    }

    HW1Test t = new HW1Test();
    t.runTests(tests);
  }
}