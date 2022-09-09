# Multithreaded Web Server written from scratch in Kotlin

Web Server runs on top of TCP and is able to process HTTP requests like GET and HEAD

Build and start the server by supplied port number as a program argument
Then you can open it in web browser at `localhost:port` for the main page

You can also interact with Web Server through `curl`.

This is how you can request home page (Assuming that server listens on port 8000):

```bash
curl localhost:8000/index.html
```