### HTML to PDF ###

https://github.com/aashipov/htmltopdf twin, implemented in JavaSE (jetty)

##### Docker, single instance #####

```docker pull aashipov/htmltopdf-javase:jetty && docker run -d --rm --name=htmltopdf-javase -p 8080:8080 aashipov/htmltopdf-javase:jetty```

##### Docker, three instance behind HAProxy #####

```cd test``` ```cd farm``` ```bash farm-refresh```
