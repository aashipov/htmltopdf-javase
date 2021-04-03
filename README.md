### HTML to PDF ###

https://github.com/aashipov/htmltopdf twin, implemented in JavaSE

Expects chromium executable at /usr/bin/chromium. Use chromium.executable system property to override

##### Docker, single instance #####

```docker pull aashipov/htmltopdf-javase:pure && docker run -d --rm --name=htmltopdf-javase -p 8080:8080 aashipov/htmltopdf-javase:pure```
